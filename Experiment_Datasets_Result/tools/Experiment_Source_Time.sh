#! /bin/bash

# This script is used to record both time and space of the studied algorithms in the experiment:
# Note that due to debt in the behavior of PrefixSpan and Lapin algorithm the result can be biased for these two implementations
# Since the spmf library includes another version for prefixspan we experiment here with PrefixSpan_AGP
# the present version take the time and space requirement from the output of each algorithm
# usage bash Experiment_Source_Time path time_out nbr_of_repetition

path=$1
limit=$2
repeat=$3
LC_NUMERIC="en_US.UTF-8"
for ds in $path/*
do																											
	echo  "		$ds"
	size=$(wc -l < $ds)
	for ms in {2,4,8,10,20,50,100,500,1000}
	do
		ms_abs=$(awk -- 'BEGIN{printf "%.3f\n", ARGV[1]/ARGV[2]}' "$size" $ms)
		ms_abs=$(echo $ms_abs | awk '{printf("%d\n",$0+=$0<0?0:0.9)}')
		ms_rel=$(awk -- 'BEGIN{printf "%.3f\n", 1/ARGV[1]}' "$ms")
	echo "		$ms_rel"
	for prg in {"WASMA","PrefixSpan_AGP","CM-SPADE","CM-SPAM","Fast","LAPIN"}	
	do
		if [[ $prg = "WASMA" ]] 
		then
			echo  "			WASMA-wsc =========="
			for i in `seq 1 $repeat`
			do
				timeout $limit java  $prg $ms_abs $ds r  false true > res1
				rv=$?
				if [[ $rv -ne 0 ]]
				then 
					echo "			Error: $rv"
					break
				else 
					echo -ne "			$(cat res1 | awk '/time/  {printf("%0.2f", $3/1000)}')"
					echo "		$(cat res1 | awk  '/mory/ {print $3}')"
					rm -f r
				fi
			done
			echo  "			WASMA-ssc =========="
			for i in `seq 1 $repeat`
			do
				timeout $limit java  $prg $ms_abs $ds r  false false > res2
				rv=$?
				if [[ $rv -ne 0 ]]
				then 
					echo "			Error: $rv"
					break
				else
					echo -ne "			$(cat res2 | awk '/time/ {printf("%.2f", $3/1000)}')"
					echo "		$(cat res2 | awk  '/mory/ {print $3}')"
					rm -f r
				fi
			done
		else
			echo  "			$prg =========="
			for i in `seq 1 $repeat`
			do
				if [ $prg = "Fast" ]
				then
					timeout $limit java -jar spmf.jar run $prg $ds r  $ms_rel 100% > res3
					rv=$?
					if [[ $rv -ne 0 ]]
					then 
						echo "			Error: $rv"
						break
					else 
						echo -ne "			$(cat res3 | awk '/time/ {printf("%.2f", $3/1000)}')"
					fi	
				else 
					start_time=$(date +%s.%N)	
					timeout $limit java -jar spmf.jar run $prg $ds r  $ms_rel  > res3
					rv=$?
					end_time=$(date +%s.%N)
					if [[ $rv -ne 0 ]]
					then 
						echo "			Error: $rv"
						break	
					else	
					echo -ne "			$(cat res3 | awk '/time/ {printf("%.2f", $4/1000)}')"
					fi
				fi		
					mem=`printf	"%.2f"	$(cat res3 | awk '/mory/ {print $5}')`
					echo "		$mem"
					rm -f r
			done
		fi
		done
	done
done
