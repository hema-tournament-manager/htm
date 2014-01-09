#!/bin/sh
VAR=$(sbt compile | egrep "Reformatted [0-9]+ Scala source")
if [ ! -z "$VAR" ]; then
	echo "You are trying to commit unformatted Scala source, shame on you!"
	exit 1
fi
