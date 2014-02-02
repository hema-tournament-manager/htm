#!/bin/sh
DEBUG=0
if [ "$1" == "debug" ]; then
	DEBUG=1
fi

LOGFILE="/tmp/sandbox.log"
if [ -e $LOGFILE ]; then
	rm $LOGFILE
fi

STASH="http://jira.htm-project.org:9001/projects/HTM/repos/htm/commits"
# Example: http://jira.htm-project.org:9001/projects/HTM/repos/htm/commits/de99eaff67612d1cf4040eb5c3f9e3ad6d69120f?to=5750854e4081b2d6c8378ca1b8adb98c861201c2
PRE_COMMIT=
GIT_STATS=
GIT_LOG=

CURRENT_COMMIT=$(git rev-parse HEAD)
if [ -f /tmp/htm-prev-commit ]; then
	PRE_COMMIT=$(cat /tmp/htm-prev-commit)
	GIT_STATS=$(git diff --pretty --stat ${PRE_COMMIT}..${CURRENT_COMMIT})
	GIT_LOG=$(git log --pretty=oneline --abbrev-commit ${PRE_COMMIT}..${CURRENT_COMMIT})
	STASH="${STASH}/${PRE_COMMIT}?to=${CURRENT_COMMIT}"
else
	STASH="${STASH}/${CURRENT_COMMIT}"
fi

if [ ${DEBUG} -eq 0 ]; then
	# stop all servers
	echo "Stop all the servers" >> $LOGFILE
	cd /Users/hematournament/git/sandbox1/scripts
	./viewer.sh stop 9000 >> $LOGFILE
	./admin.sh stop 8079 >> $LOGFILE
	#cd /Users/hematournament/git/sandbox2/scripts
	./viewer.sh stop 9001 >> $LOGFILE
	./admin.sh stop 8080 >> $LOGFILE

	# checkout the files
	echo "Checkout new code (rev: ${CURRENT_COMMIT})" >> $LOGFILE
	git --work-tree=/Users/hematournament/git/sandbox1 --git-dir=/Users/hematournament/git/htm.git checkout -f >> $LOGFILE
	git --work-tree=/Users/hematournament/git/sandbox2 --git-dir=/Users/hematournament/git/htm.git checkout -f >> $LOGFILE

	# start the servers
	echo "Start all the sandbox servers" >> $LOGFILE
	cd /Users/hematournament/git/sandbox1/scripts
	./viewer.sh start 9000 >> $LOGFILE
	./admin.sh start 8079 >> $LOGFILE
	cd /Users/hematournament/git/sandbox2/scripts
	./viewer.sh start 9001 >> $LOGFILE
	./admin.sh start 8080 >> $LOGFILE
fi

echo "=================== GIT stats for the Nerds ==================" >> ${LOGFILE}
echo ${STASH} >> ${LOGFILE}
if [ "x${PRE_COMMIT}" != "x" ]; then
	echo "${PRE_COMMIT}" >> {$LOGFILE}
fi
if [ "x${GIT_LOG}" != "x" ]; then
	echo "${GIT_LOG}" >> {$LOGFILE}
fi
if [ "x${GIT_STATS}" != "x" ]; then
	echo "${GIT_STATS}" >> {$LOGFILE}
fi

#Send the log to team@htm-project.org
if [ ${DEBUG} -gt 0 ]; then
	cat $LOGFILE | mailx -s "Sandbox update" mattias@htm-project.org
else
	cat $LOGFILE | mailx -s "Sandbox update" team@htm-project.org
fi
echo ${CURRENT_COMMIT} > /tmp/htm-prev-commit
