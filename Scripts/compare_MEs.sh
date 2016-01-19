#!/bin/bash

#echo > sat4jresults
#{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults 
#echo 'RAReQS (AVG, STD):'
#echo  'GhostQ (AVG, STD):'

#COMMAND='{ java -Xss4g -Xmx16g -jar Policy2QBF.jar s 1000 1 0.1 10 1 0.1 10 0; } 2> sat4jresults'

echo > sat4J_2.dat
echo > sat4J_3.dat
echo > logs2
echo > logs3
echo > nohup.out
#-XX:-UseGCOverheadLimit
#-Xss64g
COUNTER=0
while [ $COUNTER -lt 50 ]; do
        java -server -Xms40g -Xmx60g -jar Policy2QBF.jar 1 s 1 3 3 7000 4 10 3 3 4 0.05 1 2>&1 sat4jresults
        let COUNTER=COUNTER+1
done
