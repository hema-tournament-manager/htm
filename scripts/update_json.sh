#!/bin/bash
admin_port="8079"
json_file="htm.json"
save_dir="/tmp/htm_json"
if [ $1 ]; then
	url="$1"
else
	url="http://localhost:$admin_port/api/v1/status/all"
fi

if [ ! -d "$save_dir" ]; then
	mkdir $save_dir
fi

pushd $save_dir
if [ ! -d ".git" ]; then
	curl -o $json_file -O $url
	git init
	git add $json_file
	git commit -asm "Initial Commit"
else
	curl -o $json_file -O $url
	git commit -asm "Saved for export"
fi


pushd
