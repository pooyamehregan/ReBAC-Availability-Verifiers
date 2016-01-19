#!/bin/bash

#echo > sat4jresults
#{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults 
#echo 'RAReQS (AVG, STD):'
#echo  'GhostQ (AVG, STD):'

#COMMAND='{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults'

echo > sat4J_3.dat
echo > logs3
echo > nohup.out
echo > clingoresults
REPEAT=$1
TYPE=$2
COUNTER=$3
UPTO=$4

ulimit -s unlimited
while [ $COUNTER -lt $UPTO ]; do
        java -server -Xss24g -Xms24g -Xmx24g -jar Policy2QBF_ASP_ME.jar $REPEAT $TYPE 2 2 2 $COUNTER 2 0.1 5 5 2 0.1 2 3600 2>&1 sat4jresults
        echo -e $COUNTER"\n" >> clingoresults
        ./clingo.sh $REPEAT
        let COUNTER=COUNTER+100
done

