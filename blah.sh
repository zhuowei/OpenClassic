#!/bin/bash
for (( var1=1; 1<<var1<16; var1++ ))
do
	echo $var1
done;
