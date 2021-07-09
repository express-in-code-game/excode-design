#!/bin/sh

echo "hello"
# jsipfs init
# jsipfs config Pubsub.Enabled --bool true
jsipfs config Addresses.API "/ip4/0.0.0.0/tcp/5002"
jsipfs config Addresses.Gateway "/ip4/0.0.0.0/tcp/9090"
jsipfs config Swarm.EnableAutoRelay --bool false
jsipfs daemon --migrate=true
