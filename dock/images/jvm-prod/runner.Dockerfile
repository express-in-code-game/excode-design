ARG BASE_IMAGE
FROM ${BASE_IMAGE}

ONBUILD ARG dir_runner_to
ONBUILD ARG jar_name
ONBUILD COPY --from=builder ${dir_runner_to}/target/${jar_name} ${jar_name}
ONBUILD ENV jar_name_env=${jar_name}
ONBUILD ENTRYPOINT [ "sh", "-c", "java -jar ${jar_name_env}"]