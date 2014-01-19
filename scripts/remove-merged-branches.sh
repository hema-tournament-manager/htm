#!/bin/sh
echo Removing merged branches:
git branch --merged master | grep -v 'master$' | xargs git branch -d
echo Done
