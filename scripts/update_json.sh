#!/bin/bash
admin_port="8079"
json_file="htm.json"
if [ $1 ]; then
	url="$1"
else
	url="http://localhost:$admin_port/api/v1/status/all"
fi

if [ -x $json_file ]; then
	rm $json_file
fi

curl -o $json_file -O $url

