(ns mult.impl.editor
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [goog.object]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   #_["fs" :as fs]
   #_["path" :as path]
   [cognitect.transit :as t]

   [mult.protocols :as p]
   [mult.impl.channels :as channels]
   [mult.impl.paredit :as paredit]
   [cljs.nodejs :as node]))

(def fs (node/require "fs"))
(def path (node/require "path"))

(def ^:const NS_DECL_LINE_RANGE 100)

(declare vscode)
(try
  (def vscode (node/require "vscode"))
  (catch js/Error e))

(defn show-information-message
  [vscode msg]
  (.. vscode.window (showInformationMessage msg)))

(defn register-command
  [vscode context id callback]
  (let [disposable (.. vscode.commands
                       (registerCommand
                        id
                        (fn [& args]
                          (callback id args))))]
    (.. context.subscriptions (push disposable))))

(defn register-commands
  [commands vscode context callback]
  (let [ids commands]
    (doseq [id ids]
      (register-command vscode context id callback))))

(defn tabapp-html
  [vscode context panel script-path html-path script-replace]
  (def panel panel)
  (let [script-uri (as-> nil o
                     (.join path context.extensionPath script-path)
                     (vscode.Uri.file o)
                     (.asWebviewUri panel.webview o)
                     (.toString o))
        html (as-> nil o
               (.join path context.extensionPath html-path)
               (.readFileSync fs o)
               (.toString o)
               (string/replace o script-replace script-uri))]
    html))

(defn read-workspace-file
  [filepath callback]
  (let []
    (as-> nil o
      (.join path vscode.workspace.rootPath filepath)
      (vscode.Uri.file o)
      (.readFile vscode.workspace.fs o)
      (.then o callback))))

(defn make-panel
  [vscode context id handlers]
  (let [panel (vscode.window.createWebviewPanel
               id
               "mult tab"
               vscode.ViewColumn.Two
               #js {:enableScripts true
                    :retainContextWhenHidden true})]
    (.onDidDispose panel (fn []
                           ((:on-dispose handlers) id)))
    (.onDidReceiveMessage  panel.webview (fn [msg]
                                           (let [data (read-string msg)]
                                             ((:on-message handlers) id data))))
    (.onDidChangeViewState panel (fn [panel]
                                   ((:on-state-change handlers) {:active? panel.active})))

    panel))


; https://stackoverflow.com/a/41029103/10589291

(defn js-lookup
  [obj]
  (-> (fn [result key]
        (let [v (goog.object/get obj key)]
          (if (= "function" (goog/typeOf v))
            result
            (assoc result key v))))
      (reduce {} (.getKeys goog/object obj))))

(defn js-lookup-nested
  [obj]
  (if (goog.isObject obj)
    (-> (fn [result key]
          (let [v (goog.object/get obj key)]
            (if (= "function" (goog/typeOf v))
              result
              (assoc result key (js-lookup-nested v)))))
        (reduce {} (.getKeys goog/object obj)))
    obj))

(comment

  vscode.workspace.rootPath

  vscode.workspace.workspaceFile
  vscode.workspace.workspaceFolders

  (as-> nil o
    (.join path vscode.workspace.rootPath ".vscode")
    (vscode.Uri.file o)
    (.readDirectory vscode.workspace.fs o)
    (.then o (fn [d] (println d))))

  (as-> nil o
    (.join path vscode.workspace.rootPath ".vscode/mult.edn")
    (vscode.Uri.file o)
    (.readFile vscode.workspace.fs o)
    (.then o (fn [d] (println d))))

  ; https://code.visualstudio.com/api/references/vscode-api#TextDocument
  ; https://code.visualstudio.com/api/references/vscode-api#Selection
  ; https://code.visualstudio.com/api/references/vscode-api#Range
  
  (if  vscode.window.activeTextEditor
    vscode.window.activeTextEditor.document.uri
    :no-active-editor)

  (println vscode.window.activeTextEditor.selection)

  (js-lookup-nested vscode.window.activeTextEditor.selection)

  (do
    (def start vscode.window.activeTextEditor.selection.start)
    (def end vscode.window.activeTextEditor.selection.end)
    (def range (vscode.Range. start end))
    (def text (.getText vscode.window.activeTextEditor.document range)))
  
  ;;
  )

(defn parse-ns
  "Safely tries to read the first form from the source text.
   Returns ns name or nil"
  [filename text log]
  (try
    (when (re-matches #".+\.clj(s|c)?" filename)
      (let [fform (read-string text)]
        (when (= (first fform) 'ns)
          (second fform))))
    (catch js/Error ex (log "; parse-ns error " {:filename filename
                                                 :err ex}))))

(defn active-ns
  [text-editor log]
  (when text-editor
    (let [range (vscode.Range.
                 (vscode.Position. 0 0)
                 (vscode.Position. NS_DECL_LINE_RANGE 0))
          text (.getText text-editor.document range)
          filepath text-editor.document.fileName
          ns-sym (parse-ns filepath text log)
          data {:filepath filepath
                :ns-sym ns-sym}]
      data
      #_(prn text-editor.document.languageId))))

(defn editor
  [channels ctx]
  (let [pid [:proc-editor (random-uuid)]
        {:keys [main| log| editor|m ops| cmd|]} channels
        {context :editor-context} ctx
        proc| (chan 1)
        editor|t (tap editor|m (chan 10))
        editor|i (channels/editor|i)
        main|i (channels/main|i)
        ops|i (channels/ops|i)
        cmd|i (channels/cmd|i)
        log|i (channels/log|i)
        log (fn [& args] (put! log| (apply p/-vl-log log|i args)))
        release #(do
                   (untap editor|m  editor|t)
                   (close! editor|t)
                   (close! proc|)
                   (put! main| (p/-vl-proc-stopped main|i pid)))
        lookup (atom {:context context
                      :vscode vscode})]
    (do
      (put! main| (p/-vl-proc-started main|i pid proc|))
      (.onDidChangeActiveTextEditor vscode.window (fn [text-editor]
                                                    (let [data (active-ns text-editor log)]
                                                      (when data
                                                        (put! ops| (p/-vl-texteditor-changed ops|i data)))))))

    (go (loop []
          (try
            (if-let [[v port] (alts! [editor|t proc|])]
              (condp = port
                proc| (release)
                editor|t (let [op (p/-op editor|i v)]
                           (cond)
                           (recur))))
            (catch js/Error e (do (log "; proc-editor error, will exit" e)))
            (finally
              (release))))
        (println "; proc-editor go-block exits"))
    (reify
      p/Release
      (-release [_] (release))
      p/Editor
      (-show-info-msg [_ msg] (show-information-message vscode msg))
      (-register-commands [_ commands]
        (let [cmd-fn (fn [id args]
                       (put! cmd| (p/-vl-cmd cmd|i id args)))]
          (register-commands commands vscode context cmd-fn)))
      (-create-tab [_ id]
        (let [id (or id (random-uuid))
              panel (make-panel vscode context id
                                {:on-message
                                 (fn [id data] (put! ops| (assoc data :tab/id id)))
                                 :on-dispose (fn [id] (put! ops| (p/-vl-tab-disposed ops|i id)))
                                 :on-state-change (fn [data] (prn data))
                                 })
              html (tabapp-html vscode context panel
                                "resources/out/tabapp.js"
                                "resources/index.html"
                                "/out/tabapp.js")
              lookup {:id id}]
          (set! panel.webview.html html)
          (reify
            p/Send
            (-send [_ v] (.postMessage (.-webview panel) (pr-str v)))
            p/Release
            (-release [_] (println "release for tab not implemented"))
            p/Active
            (-active? [_] panel.active)
            cljs.core/ILookup
            (-lookup [_ k] (-lookup _ k nil))
            (-lookup [_ k not-found] (-lookup lookup k not-found)))))
      (-read-workspace-file [_ filepath]
        (let [c| (chan 1)]
          (read-workspace-file filepath (fn [file] (put! c| (.toString file)) (close! c|)))
          c|))
      (-join-workspace-path [_ subpath]
        (let [extpath (. context -extensionPath)]
          (.join path extpath subpath)))
      (-active-ns [_] (active-ns vscode.window.activeTextEditor log))
      (-selection [_]
        (when  vscode.window.activeTextEditor
          (let [start vscode.window.activeTextEditor.selection.start
                end vscode.window.activeTextEditor.selection.end
                range (vscode.Range. start end)
                text (.getText vscode.window.activeTextEditor.document range)]
            text)))
      ILookup
      (-lookup [_ k] (-lookup _ k nil))
      (-lookup [_ k not-found] (-lookup @lookup k not-found)))))


(comment

  (read-string (str (ex-info "err" {:a 1})))
  

  (defn roundtrip [x]
    (let [w (t/writer :json)
          r (t/reader :json)]
      (t/read r (t/write w x))))

  (defn test-roundtrip []
    (let [list1 [:red :green :blue]
          list2 [:apple :pear :grape]
          data  {(t/integer 1) list1
                 (t/integer 2) list2}
          data' (roundtrip data)]
      (assert (= data data'))))

  ;;
  )