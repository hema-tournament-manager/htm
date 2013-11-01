#!/bin/bash
remote_host="192.168.2.2"
remote_path="/home/git/htm"
db="htm_admin.h2.db"

if [ ! -d /tmp/htm_db ]; then
	mkdir -p /tmp/htm_db
fi

pushd /tmp/htm_db
ret=0
scp root@$remote_host:$remote_path/$db $db
if [ ! -d ".git" ]; then
	git init &&  git add $db && git commit -asm "Initial Commit"
	ret=$?
else
	git commit -asm "Saved for export"
	ret=$?
fi
popd
