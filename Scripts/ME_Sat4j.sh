#!/bin/bash

#echo > sat4jresults
#{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults 
#echo 'RAReQS (AVG, STD):'
#echo  'GhostQ (AVG, STD):'

#COMMAND='{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults'

echo > sat4J_1.dat
echo > sat4J_2.dat
echo > sat4J_3.dat
echo > logs
echo > logs2
echo > logs3
echo > nohup.out
#-XX:-UseGCOverheadLimit
#-Xss64g
COUNTER=10000
while [ $COUNTER -lt 100001 ]; do
        java -server -Xms40g -Xmx60g -jar Policy2QBF_ME.jar 20 s 1 3 3 $COUNTER 4 200 5 5 4 0.2 1 900 2>&1 sat4jresults
        let COUNTER=COUNTER+10000
done
