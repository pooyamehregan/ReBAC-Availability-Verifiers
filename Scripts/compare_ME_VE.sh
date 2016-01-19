o > sat4jresults
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
COUNTER=200
while [ $COUNTER -lt 2001 ]; do
        java -server -Xms40g -Xmx60g -jar Policy2QBF_ME_VE.jar 20 s 1 3 3 $COUNTER 1 0.1 5 5 1 0.2 1 3600 2>&1 sat4jresults
        let COUNTER=COUNTER+200
done
