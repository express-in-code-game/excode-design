ARG BASE_IMAGE
FROM ${BASE_IMAGE}

COPY  nginx.default.conf /etc/nginx/conf.d/default.conf

ONBUILD ARG dir_runner_to
ONBUILD ARG jar_name
ONBUILD COPY --from=builder ${dir_runner_to}/resources/public /usr/share/nginx/html/
ONBUILD ENTRYPOINT ["nginx", "-g", "daemon off;"]