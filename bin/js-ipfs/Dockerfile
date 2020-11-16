FROM ipfs/js-ipfs

RUN jsipfs config Pubsub.Enabled --bool false

ENTRYPOINT ["jsipfs"]

CMD ["daemon", "--migrate=true"]