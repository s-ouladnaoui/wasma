#! /bin/bash
cd src
for ms in {10,100,1000} do
	for prg in {"WASMA","PrefixSpan","CM-SPADE"} do
		for ds in {"../kosarak25k.txt","../BIBLE.txt","../MSNBC.txt"} do
			size=$(wc -l < $ds)
			ms_abs=$(awk -- 'BEGIN{printf "%.3f\n", ARGV[1]/ARGV[2]}' "$size" $ms)
			ms_abs=$(echo $ms_abs | awk '{printf("%d\n",$0+=$0<0?0:0.9)}')
			echo "Absolute Support= $ms_abs" 
			ms_rel=$(awk -- 'BEGIN{printf "%.3f\n", 1/ARGV[1]}' "$ms")
			echo "Relative Support: = $ms_rel" 	
			if [ $prg =	 "WASMA" ] then
				echo ========= $prg wsc on $ds  $ms_abs ==========================
				java -Xmx14g $prg $ms_abs $ds "r_$ds_1"  false true
				echo ========= $ $prg ssc on $ds  $ms_abs ========================
				java -Xmx14g $prg $ms_abs $ds "r_$ds_1f"  false false
			else
				echo ========= $prg on $ds  with $ms_rel =========================
				java -Xmx14g -jar ../spmf.jar run "$prg" "$ds" r  $ms_rel	
			fi
		done
	done
done