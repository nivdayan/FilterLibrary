set datafile separator ","
set logscale x
set key autotitle columnhead
set output 'output.png' 
set term pngcairo size 2000, 400
set xlabel "# entries"
set size 1.5,1.5 
set multiplot layout 1,4 rowsfirst 

cd 'exp3'

set size 0.25,0.9 
set origin 0.0,0
set logscale y
#set label 1 'b' at graph 0.92,0.9 font ',8'
set ylabel "false positive rate"
plot 'false_positive_rate.csv' using 1:2 with lines, 'false_positive_rate.csv' using 1:3 with lines, 'false_positive_rate.csv' using 1:4 with lines, 'false_positive_rate.csv' using 1:5 with lines
unset logscale y

set size 0.25,0.9 
set origin 0.25,0
#set label 1 'a' at graph 0.92,0.9 font ',8'
set ylabel "avg. query latency (us)"
set key left top 
plot 'read_speed.csv' using 1:($2/1000) with lines, 'read_speed.csv' using 1:($3/1000) with lines, 'read_speed.csv' using 1:($4/1000) with lines, 'read_speed.csv' using 1:($5/1000) with lines

cd '../exp4'

set size 0.25,0.9 
set origin 0.5,0
set logscale y
#set label 1 'b' at graph 0.92,0.9 font ',8'
set ylabel "false positive rate"
plot 'false_positive_rate.csv' using 1:2 with lines, 'false_positive_rate.csv' using 1:3 with lines, 'false_positive_rate.csv' using 1:4 with lines, 'false_positive_rate.csv' using 1:5 with lines
unset logscale y

set size 0.25,0.9 
set origin 0.75,0
#set label 1 'a' at graph 0.92,0.9 font ',8'
set ylabel "avg. query latency (us)"
set key left top 
plot 'read_speed.csv' using 1:($2/1000) with lines, 'read_speed.csv' using 1:($3/1000) with lines, 'read_speed.csv' using 1:($4/1000) with lines, 'read_speed.csv' using 1:($5/1000) with lines



unset multiplot

