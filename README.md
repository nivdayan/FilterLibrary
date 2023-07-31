

This filters library contains the InfiniFilter paper from SIGMOD 2023 as well as some baselines against which it can be compared. InfiniFilter is a probabalistic data structure that answers set-membership queries (similarly to a Bloom filter or a Quotient Filter). The core innovation is that InfiniFilter is able to dynamically expand while maintaining good guarantees over performance and the false positive rate. The full paper can be accessed [here](https://nivdayan.github.io/infinifilter.pdf). 

## Compilation
Compile the library by running the following command from the root directory of the repository. 
```console
javac filters/*.java bitmap_implementations/*.java infiniFilter_experiments/*.java  
```

## Experimental Reproduction
To reproduce the results of the SIGMOD 2023 paper, run the script run_exps.sh from the root directory of the repository. The script will reproduce Figures 13 to 16 in the paper and name them accordingly: figure13.png to figure.16.png. The raw data used to generate these figures is stored in a group of directories called exp1 to exp6. Running the script again will delete results of older runs and regenerate them. 

```console
./run_exps.sh   
```

## Dependencies
Running the experiments requires having the Java Development Kit version 11 or above installed. Plotting the figures to illustrate the experimental results requires having Gnuplot installed. Both dependencies can be obtained using the following commands on Ubuntu or equivalents on other systems.  

```console
sudo apt install default-jdk
sudo apt-get install gnuplot
```
