#!/bin/bash
admin_port="8079"
json_file="htm.json"
save_dir="/tmp/htm_json"
remote_user="root"
remote_host="www.ghfs.se"
remote_path="/var/www/ghfs.se/html/api/v1"
remote_name="htm.json"
default_host="192.168.2.2"

ret=0

if [ $1 ]; then
	url="$1"
else
	url="http://$default_host:$admin_port/api/v1/status/all"
fi

if [ ! -d "$save_dir" ]; then
	mkdir $save_dir
	ret=$?
	if [ $ret -gt 0 ]; then
		echo "Something went wrong when creating $save_dir"
		exit 1
	fi
fi

pushd $save_dir
ret=0
if [ ! -d ".git" ]; then
	curl -o $json_file -O $url
	git init &&  git add $json_file && git commit -asm "Initial Commit"
	ret=$?
else
	curl -o $json_file -O $url
	git commit -asm "Saved for export"
	ret=$?
fi

if [ $ret -gt 0 ]; then
	echo "Something went wrong when we tried to save $json_file to $save_dir"
	exit 1
fi

ret=0
scp $json_file $remote_user@$remote_host:$remote_path/$remote_name
ret=$?

if [ $ret -gt 0 ]; then
	echo "Something went wrong when trying to publish $json_file"
	exit 1
else
	echo "Update complete!"
fi

pushd
