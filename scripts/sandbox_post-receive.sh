#!/bin/sh
# stop all servers
cd /Users/hematournament/git/sandbox1/scripts
./viewer.sh stop 9000
./admin.sh stop 8079
#cd /Users/hematournament/git/sandbox2/scripts
./viewer.sh stop 9001
./admin.sh stop 8080
 checkout the files
git --work-tree=/Users/hematournament/git/sandbox1 --git-dir=/Users/hematournament/git/htm.git checkout -f
git --work-tree=/Users/hematournament/git/sandbox2 --git-dir=/Users/hematournament/git/htm.git checkout -f
# start the servers
cd /Users/hematournament/git/sandbox1/scripts
./viewer.sh start 9000
./admin.sh start 8079
cd /Users/hematournament/git/sandbox2/scripts
./viewer.sh start 9001
./admin.sh start 8080
