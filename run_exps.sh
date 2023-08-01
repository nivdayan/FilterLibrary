
#! /bin/bash

javac filters/*.java bitmap_implementations/*.java infiniFilter_experiments/*.java  

rm figure13.png figure14.png figure15.png figure16.png
rm -rf exp*

max_data_size_power="${1:-24}"

java infiniFilter_experiments.Experiment1 16 12 $max_data_size_power exp1
java infiniFilter_experiments.Experiment1 8 12 $max_data_size_power exp2
gnuplot figure13_script.gnuplot  
mv output.png figure13.png 

java infiniFilter_experiments.Experiment2 16 10 $max_data_size_power exp3
java infiniFilter_experiments.Experiment2 8 10 $max_data_size_power exp4
gnuplot figure14_script.gnuplot 
mv output.png figure14.png 

java infiniFilter_experiments.Experiment3 8 10 $max_data_size_power exp5
gnuplot figure15_script.gnuplot 
mv output.png figure15.png 

java infiniFilter_experiments.Experiment4 8 10 $max_data_size_power exp6
gnuplot figure16_script.gnuplot 
mv output.png figure16.png 