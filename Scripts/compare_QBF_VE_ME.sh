#!/bin/bash

#echo > sat4jresults
#{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults 
#echo 'RAReQS (AVG, STD):'
#echo  'GhostQ (AVG, STD):'

#COMMAND='{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults'

echo > sat4J_1.dat
echo > sat4J_3.dat
echo > logs
echo > logs3
echo > nohup.out
echo > rareqsresults
echo > ghostqresults
REPEAT=$1
COUNTER=$2
UPTO=$3
ulimit -s unlimited
while [ $COUNTER -lt $UPTO ]; do
        java -server -Xss24g -Xms24g -Xmx24g -jar Policy2QBF_QBF_VE_ME.jar $REPEAT s 1 3 3 $COUNTER 1 0.1 5 5 1 0.2 1 3600 2>&1 sat4jresults
        echo -e $COUNTER"\n" >> rareqsresults
        ./rareqs.sh $REPEAT
        echo -e $COUNTER"\n" >> ghostqresults
        ./ghostq.sh $REPEAT
        let COUNTER=COUNTER+100
done
