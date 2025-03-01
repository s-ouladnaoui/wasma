#! /bin/bash
limit=900s
LC_NUMERIC="en_US.UTF-8"
#for ds in {"../datasets/kosarak25k.txt","../datasets/BMS1_spmf.txt","../datasets/BIBLE.txt","../datasets/e_shop.txt","../datasets/SIGN.txt","../datasets/MSNBC.txt","../datasets/LEVIATHAN.txt","../datasets/LastWords.txt","../datasets/online_retail_II_best_products.txt"}
for ds in {"../datasets/kosarak25k.txt","../datasets/BMS1_spmf.txt","../datasets/BIBLE.txt"}
do
echo  "		$ds"
	size=$(wc -l < $ds)
	for ms in {1000,100,10} 
	do
		ms_abs=$(awk -- 'BEGIN{printf "%.3f\n", ARGV[1]/ARGV[2]}' "$size" $ms)
			ms_abs=$(echo $ms_abs | awk '{printf("%d\n",$0+=$0<0?0:0.9)}')
			ms_rel=$(awk -- 'BEGIN{printf "%.3f\n", 1/ARGV[1]}' "$ms")
	echo "		$ms_rel"
	for prg in {"CM-SPAM","Fast"}
	do
			if [ $prg =	 "WASMA" ] 
			then
				echo  "			WASMA-wsc"
				for i in {1..2} 
				do
				timeout $limit java  $prg $ms_abs $ds r  false true > res1
				if [[ $? -eq 124 ]]
				then 
					echo "			timeout"
					break
				elif [[ $? -eq 137 ]]
				then
					echo " 			out of memory"
					break
				fi
				echo -ne "			$(cat res1 | awk '/time/ {print $3}')"
				echo "		$(cat res1 | awk  '/mory/ {print $3}')"
				done
				echo  "			WASMA-ssc"
				rm -f r
				for i in {1..2}
				do
				timeout $limit java  $prg $ms_abs $ds r  false false > res2
				if [[ $? -eq 124 ]]
				then 
					echo "			timeout"
					break
				elif [[ $? -eq 137 ]]
				then
					echo "			out of memory"
					break
				fi
				echo -ne "			$(cat res2 | awk '/time/ {print $3}')"
				echo "		$(cat res2 | awk  '/mory/ {print $3}')"
				rm -f r
				done
			else
				echo  "			$prg"
				for i in {1..2}
				do
				if [ $prg = "Fast" ]
				then 	
				timeout $limit java -jar ../old_sources/spmf.jar run $prg $ds r  $ms_rel 100% > res3
				else timeout $limit java -jar ../old_sources/spmf.jar run $prg $ds r  $ms_rel  > res3
				fi
				if [[ $? -eq 124 ]]
				then 
					echo "			timeout"
					break
				elif [[ $? -eq 137 ]]
				then
					echo "			out of memory"
					break
				fi
				if [ $prg = "Fast" ]
				then 
				echo -ne "			$(cat res3 | awk '/time/ {print 1000*$3}')	"
				else echo -ne "			$(cat res3 | awk '/time/ {print $4}')	"
				fi
				echo "		$(cat res3 | awk '/mory/ {print $5}')"
				rm -f r
				done
			fi
		done
	done
done
