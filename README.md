

This filters library contains the InfiniFilter paper from SIGMOD 2023 as well as some baselines against which it can be compared.  

## Installation
Compile the library by running the following command from the root directory of the repository. The java development kit version 11 or above should be installed. 
```console
javac filters/*.java bitmap_implementations/*.java infiniFilter_experiments/*.java  
```

## Reproducing the Results
To reproduce the results of the SIGMOD 2023 paper, run the script run_exps.sh from the root directory of the repository. This script requires Gnuplot to be installed to generate the figures. The script will reproduce Figures 13 to 16 in the paper and name them accordingly. 

```console
./run_exps.sh   
```