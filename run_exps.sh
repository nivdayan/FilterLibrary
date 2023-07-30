
#! /bin/bash

javac filters/*.java bitmap_implementations/*.java infiniFilter_experiments/*.java  

java infiniFilter_experiments.Experiment1 16 12 24 exp1
java infiniFilter_experiments.Experiment1 8 12 24 exp2
gnuplot figure13_script.gnuplot  
mv output.png figure13.png 

java infiniFilter_experiments.Experiment2 16 10 24 exp3
java infiniFilter_experiments.Experiment2 8 10 24 exp4
gnuplot figure14_script.gnuplot 
mv output.png figure14.png 

java infiniFilter_experiments.Experiment3 8 10 24 exp5
gnuplot figure15_script.gnuplot 
mv output.png figure15.png 

java infiniFilter_experiments.Experiment4 8 10 24 exp6
gnuplot figure16_script.gnuplot 
mv output.png figure16.png 