#!/bin/bash
fileName=$1
num_to_start=$2
repNum=$3
lines=1
echo "these are the Port Nums" > ./$fileName
echo "Bash version ${BASH_VERSION}..."
for (( c=0; c<$num_to_start; c++ ))
do
	java DynamoNode $fileName &
done
((++num_to_start))
while [[ $lines -lt $num_to_start ]]; do
	lines=$(wc -l < ./$fileName)
	sleep 1
done
java Client $fileName $repNum
echo "done $lines"
