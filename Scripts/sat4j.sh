#!/bin/bash

#echo > sat4jresults
#{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults 
#echo 'RAReQS (AVG, STD):'
#echo  'GhostQ (AVG, STD):'

#COMMAND='{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults'

echo > sat4J_1.dat
echo > sat4J_2.dat
echo > logs
echo > logs2
echo > nohup.out
COUNTER=100
while [ $COUNTER -lt 1100 ]; do
        java -server -Xss24g -Xms24g -Xmx24g -jar Policy2QBF.jar 50 s 1 3 3 $COUNTER 1 10 5 5 1 0.05 1 2>&1 sat4jresults
        let COUNTER=COUNTER+100
done
