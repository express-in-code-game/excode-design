# FROM ipfs/go-ipfs:v0.7.0@sha256:d5bc45bd354e3fef2161adb0daa7dc390316589cfdcc6b0edc1ad13ac66626fd
# that's 0.8.0 + 2months
FROM ipfs/go-ipfs:master-latest@sha256:f19d17f1572e3c1797b8b8dd59951b33aa1cf41a3543e0e69f34d2af08b10ec6 

COPY container_daemon /usr/local/bin/start_ipfs

RUN chmod 0755 /usr/local/bin/start_ipfs

ENTRYPOINT ["/sbin/tini", "--", "/usr/local/bin/start_ipfs"]

# Execute the daemon subcommand by default
CMD ["daemon", "--writable", "--enable-pubsub-experiment", "--migrate=true"]