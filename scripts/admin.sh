#!/bin/bash
script_name=$(basename $0)
if [ ! -d "$PWD/../scripts" ]; then
	echo "You must be in the scripts dir when executing: $script_name"
	exit 1
fi

project="admin"
if [ $2 ]; then
	admins[0]="$2"
else
	admins[0]="8079"
fi


function usage {
	echo "Usage: $script_name <command> [port]"
	echo " command:      start|stop|restart"
	echo " port:         Port you want the admin to listen on."
}

function stop_admin {
	for admin in "${admins[@]}"; do
		local _pid=
		_pid=$(screen -list|grep admin$admin|cut -d"." -f1)
		if [ "x$_pid" != "x" ]; then
			echo "Screen $admin is running on pid: $_pid"
			echo "Slaying $_pid"
			kill $_pid
		fi
	done
}

function start_admin {
	for admin in "${admins[@]}"; do
		local _ret=0
		local _pid=
		_pid=$(ps -ef|grep sbt-launch|grep $admin|awk '{ print $2 }')
		if [ "x$_pid" != "x" ]; then
			echo "Admin: $1 is running on pid: $_pid"
			_ret=0
		else
			echo "Admin: $1 is running not running"
			_ret=1
		fi

		if [ $_ret -gt 0 ]; then
			local _pid=
			_pid=$(screen -list|grep $admin|cut -d"." -f1)
			if [ "x$_pid" != "x" ]; then
				echo "Screen $admin is already running on pid: $_pid"
				echo "Slaying $_pid"
				kill $_pid
				if [ $? == 0 ]; then
					_start_admin $admin
				fi
			else
				_start_admin $admin
			fi
		fi
	done
}

function _start_admin {
	echo "Starting: $1"
	pushd ../>/dev/null
	screen -dmS admin$1 sbt "project $project" "set port in container.Configuration := $1" "container:start" "~"
	popd >/dev/null
}

function restart_admin {
	stop_admin
	sleep 2
	start_admin
}

if [ $1 ]; then
	if [ "$1" == "stop" ]; then
		stop_admin
		exit
	elif [ "$1" == "restart" ]; then
		restart_admin
		exit
	elif [ "$1" == "start" ]; then
		start_admin
	else
		usage
	fi
else
	usage
fi
