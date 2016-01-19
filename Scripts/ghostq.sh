#!/bin/bash

COMMAND='./solver-cegar.sh ../qcirs/qcir'
COUNTER=0
cd ~/experiment/ghostq-qcir
while [ $COUNTER -lt $1 ]; do
        { \time -f "%e"  $COMMAND$COUNTER; } 2>> ../ghostqresults
        let COUNTER=COUNTER+1
done
