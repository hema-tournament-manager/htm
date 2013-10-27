#!/bin/bash
admin_port="8079"
url="http://localhost:$admin_port/api/v1/status/all"
json_file="htm.json"
if [ -x $json_file ]; then
	rm $json_file
fi

curl -o $json_file -O $url

