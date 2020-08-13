# contributing

## git workflow

- fork
- commit to fork's master as you please
- once done (you are still in your fork's master)
  - git remote add source https://github.com/clojuretools/mult
  - git fetch source
  - git merge --no-ff [-X theirs] source/master, resolve conflicts
    - that's the part where thinking is required, may use -X theirs
    - it's important to merge before rebase to squash the merge commit as well
      - alternatively, rebase, merge, rebase again
      - the goal is: to have in the end one clean commit to the source repository
  - git rebase -i source/master , squash commits into 1
    - all commits, including the merge one, are squashed into one, commit date becomes now
  - git commit --amend --no-edit --date=now 
    - sets the date to now (otherwise it's the date of the picked commit)
  - if during this process, another commit happened and there are conflicts, do merge, rebase steps again
  - git push -f
    - this will replace your master branch history with the result of rebase
    - if you want to keep your raw commit history, checkout a branch for that first 
  - once done, create pull request from fork/master to source/master

#### additional notes

- to rebase -i comfortably, using vscode instead of terminal, add to ~/.gitconfig
  ```bash
  [diff]
      tool = default-difftool
  [difftool "default-difftool"]
      cmd = code --wait --diff $LOCAL $REMOTE
  [core]
    editor = code --wait
  ```