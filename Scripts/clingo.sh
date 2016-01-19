#!/bin/bash

COMMAND='./clingo ../asps/asp'
COMMAND1='./gringo ../asps/asp'
COMMAND2='./clasp'
COUNTER=1
cd ~/experiment/clingo-4.5.0-x86_64-linux
while [ $COUNTER -le $1 ]; do
        { \time -f "%e" $COMMAND$COUNTER; } 2>> ../clingoresults
	#{ \time -f "%U" $COMMAND$COUNTER; } 2>> ../clingoresults
        #{ \time -f "%e" $COMMAND1$COUNTER; } 2>> ../gringoresults | { \time -f "%e" $COMMAND2; } 2>> ../claspresults
        #{ \time -f "%U" $COMMAND1$COUNTER; } 2>> ../gringoresults | { \time -f "%U" $COMMAND2; } 2>> ../claspresults
	let COUNTER=COUNTER+1
done
