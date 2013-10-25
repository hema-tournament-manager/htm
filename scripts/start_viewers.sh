#!/bin/bash
script_name=$(basename $0)
if [ ! -d "$PWD/../play" ]; then
	echo "You must be in the scripts dir when executing: $script_name"
	exit 1
fi

project="htm-viewer-play"

if [ $1 ]; then
	viewers[0]="$1"
else
	viewers[0]="9001"
	viewers[1]="9002"
	viewers[2]="9003"
fi

function viewer_status {
	local _ret=0
	local _pid=
	_pid=$(ps -ef|grep sbt-launch|grep $1|awk '{ print $2 }')
	if [ "x$_pid" != "x" ]; then
		echo "Viewer: $1 is running on pid: $_pid"
		_ret=0
	else
		echo "Viewer: $1 is running not running"
		_ret=1
	fi

	if [ $_ret -gt 0 ]; then
		local _pid=
		_pid=$(screen -list|grep $1|cut -d"." -f1)
		if [ "x$_pid" != "x" ]; then
			echo "Screen $1 is already running on pid: $_pid"
			echo "Slaying $_pid"
			kill $_pid
			if [ $? == 0 ]; then
				start_viewer $1
			fi
		else
			start_viewer $1
		fi
	fi
}

function start_viewer {
	echo "Starting: $1"
	pushd ../play >/dev/null
	screen -dmS viewer$1 sbt "project $project" "run $1"
	popd >/dev/null
}

for viewer in "${viewers[@]}"; do
	viewer_status $viewer
done
