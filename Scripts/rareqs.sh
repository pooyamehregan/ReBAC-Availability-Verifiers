#!/bin/bash

COMMAND='./run.sh ../qcirs/qcir'
COUNTER=0
cd ~/experiment/rareqs-nn-beta/
while [ $COUNTER -lt $1 ]; do
	{ \time -f "%e" $COMMAND$COUNTER; } 2>> ../rareqsresults
	let COUNTER=COUNTER+1
done
