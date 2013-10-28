#!/bin/bash
script_name=$(basename $0)
if [ ! -d "$PWD/../play" ]; then
	echo "You must be in the scripts dir when executing: $script_name"
	exit 1
fi

project="htm-viewer-play"
if [ $2 ]; then
	viewers[0]="$1"
else
	viewers[0]="9001"
	viewers[1]="9002"
	viewers[2]="9003"
fi


function usage {
	echo "Usage: $script_name <command> [port]"
	echo " command:      start|stop|restart"
	echo " port:         Port you want the viewer to listen on."
}

function stop_viewer {
	for viewer in "${viewers[@]}"; do
		local _pid=
		_pid=$(screen -list|grep viewer$viewer|cut -d"." -f1)
		if [ "x$_pid" != "x" ]; then
			echo "Screen $viewer is running on pid: $_pid"
			echo "Slaying $_pid"
			kill $_pid
		fi
	done
}

function start_viewer {
	for viewer in "${viewers[@]}"; do
		local _ret=0
		local _pid=
		_pid=$(ps -ef|grep sbt-launch|grep $viewer|awk '{ print $2 }')
		if [ "x$_pid" != "x" ]; then
			echo "Viewer: $1 is running on pid: $_pid"
			_ret=0
		else
			echo "Viewer: $1 is running not running"
			_ret=1
		fi

		if [ $_ret -gt 0 ]; then
			local _pid=
			_pid=$(screen -list|grep $viewer|cut -d"." -f1)
			if [ "x$_pid" != "x" ]; then
				echo "Screen $viewer is already running on pid: $_pid"
				echo "Slaying $_pid"
				kill $_pid
				if [ $? == 0 ]; then
					_start_viewer $viewer
				fi
			else
				_start_viewer $viewer
			fi
		fi
	done
}

function _start_viewer {
	echo "Starting: $1"
	pushd ../play >/dev/null
	screen -dmS viewer$1 sbt "project $project" "run $1"
	popd >/dev/null
}

function restart_viewer {
	stop_viewer
	sleep 2
	start_viewer
}

if [ $1 ]; then
	if [ "$1" == "stop" ]; then
		stop_viewer
		exit
	elif [ "$1" == "restart" ]; then
		restart_viewer
		exit
	elif [ "$1" == "start" ]; then
		start_viewer
	else
		usage
	fi
else
	usage
fi
