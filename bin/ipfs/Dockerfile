FROM ipfs/go-ipfs:v0.7.0@sha256:d5bc45bd354e3fef2161adb0daa7dc390316589cfdcc6b0edc1ad13ac66626fd

COPY container_daemon /usr/local/bin/start_ipfs

RUN chmod 0755 /usr/local/bin/start_ipfs

ENTRYPOINT ["/sbin/tini", "--", "/usr/local/bin/start_ipfs"]

# Execute the daemon subcommand by default
CMD ["daemon", "--migrate=true"]