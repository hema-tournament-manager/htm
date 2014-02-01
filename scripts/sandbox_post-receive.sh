#!/bin/sh
LOGFILE="/tmp/sandbox.log"
if [ -e $LOGFILE ]; then
	rm $LOGFILE
fi

# stop all servers
echo "Stop all the servers" >> $LOGFILE
cd /Users/hematournament/git/sandbox1/scripts
./viewer.sh stop 9000 >> $LOGFILE
./admin.sh stop 8079 >> $LOGFILE
cd /Users/hematournament/git/sandbox2/scripts
./viewer.sh stop 9001 >> $LOGFILE
./admin.sh stop 8080 >> $LOGFILE

# checkout the files
echo "Checkout new code" >> $LOGFILE
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

#Send the log to team@htm-project.org
cat $LOGFILE | mailx -s "Sandbox update" team@htm-project.org
