#! /bin/bash
limit=1800s
for ds in {"kosarak25k.txt","BMS1_spmf.txt","LEVIATHAN.txt","SIGN.txt","FIFA.txt","BIBLE.txt","r10k_100"} 
do
size=$(wc -l < $ds)
echo  "	$ds"
for ms in {10,100,1000} 
do
ms_abs=$(awk -- 'BEGIN{printf "%.3f\n", ARGV[1]/ARGV[2]}' "$size" $ms)
			ms_abs=$(echo $ms_abs | awk '{printf("%d\n",$0+=$0<0?0:0.9)}')
			ms_rel=$(awk -- 'BEGIN{printf "%.3f\n", 1/ARGV[1]}' "$ms")
echo "		$ms_rel"
for prg in {"WASMA","PrefixSpan","CM-SPADE","LAPIN"} 
do
echo  "			$prg"
			if [ $prg =	 "WASMA" ] 
			then
				#/usr/bin/time -f 'Elapsed time: %es\nMemory usage: %M KB\nCPU usage: %P' timeout 300s java  $prg $ms_abs $ds r  false true
				for i in {1..10} 
				do
				timeout $limit java  $prg $ms_abs $ds r  false true > res1
				if [[ $? -eq 124 ]]
				then 
					break
				fi
				echo -ne "			$(cat res1 | awk '/time/ {print $3}')"
				echo "		$(cat res1 | awk  '/mory/ {print $3}')"
				done
				echo "			==================="
				for i in {1..10}
				do
				timeout $limit java  $prg $ms_abs $ds r  false false > res2
				if [[ $? -eq 124 ]]
				then 
					break
				fi
				#/usr/bin/time -f 'Elapsed time: %es\nMemory usage: %M KB\nCPU usage: %P' timeout 300s  java  $prg $ms_abs $ds r  false false
				echo -ne "			$(cat res2 | awk '/time/ {print $3}')"
				echo "		$(cat res2 | awk  '/mory/ {print $3}')"
				done
			else
				#/usr/bin/time -f 'Elapsed time: %es\nMemory usage: %M KB\nCPU usage: %P' timeout 300s  java -jar spmf.jar run $prg $ds r  $ms_rel
				for i in {1..10}
				do
				timeout $limit java -jar spmf.jar run $prg $ds r  $ms_rel > res3
				if [[ $? -eq 124 ]]
				then 
					break
				fi
				echo -ne "			$(cat res3 | awk '/time/ {print $4}')	"
				echo "		$(cat res3 | awk '/mory/ {print $5}')"
				done
			fi
		done
	done
done
