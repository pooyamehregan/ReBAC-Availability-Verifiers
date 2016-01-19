#!/bin/bash

#echo > sat4jresults
#{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults 
#echo 'RAReQS (AVG, STD):'
#echo  'GhostQ (AVG, STD):'

#COMMAND='{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults'

#echo > sat4J_3.dat
#echo > logs3
echo > nohup.out
echo > clingoresults
echo > gringoresults
echo > claspresults
REPEAT=$1
TYPE=$2
COUNTER=$3
UPTO=$4

ulimit -s unlimited
while [ $COUNTER -lt $UPTO ]; do
        java -server -Xss30g -Xms40g -Xmx60g -jar Policy2QBF_ASP.jar $REPEAT $TYPE 1 3 3 $COUNTER 4 0.01 5 5 4 0.5 5 3600 2>&1 sat4jresults
        echo -e $COUNTER"\n" >> clingoresults
	echo -e $COUNTER"\n" >> gringoresults
	echo -e $COUNTER"\n" >> claspresults
        ./clingo.sh $REPEAT
        let COUNTER=COUNTER+10000
done

