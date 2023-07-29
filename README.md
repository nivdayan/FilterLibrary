

This filters library contains the InfiniFilter paper from SIGMOD 2023 as well as some baselines against which it can be compared.  

## Installation
Compile the library by running the following command from the root directory of the repository. The java development kit version 11 or above should be installed. 
```console
javac filters/*.java bitmap_implementations/*.java infiniFilter_experiments/*.java  
```

## Reproducing the Results

To reproduce the experimental results in the paper, run the following commands from the root directory of the repository. Each command will generate a folder with five csv files: false_positive_rate.txt, memory.txt, read_speed.txt, writes_speed.txt, and all.txt. The last file contains all the results in one place. Each of these files shows how the given metric changed during the experiment with data growth. The details of each experiment are given in the paper.   

For Figure 13, Parts (A) to (D), run: 
```console
java infiniFilter_experiments.Experiment1 16 12 31    
```

For Figure 13, Parts (E) to (H), run: 
```console
java infiniFilter_experiments.Experiment1 8 10 31    
```

For Figure 14, Parts (A) to (B), run: 
```console
java infiniFilter_experiments.Experiment2 16 10 31   
```

For Figure 14, Parts (C) to (D), run: 
```console
java infiniFilter_experiments.Experiment2 8 10 31  
```

For Figure 15, run: 
```console
java infiniFilter_experiments.Experiment3 8 10 31
```

To reproduce Figure 16, run: 
```console
java infiniFilter_experiments.Experiment4 16 10 31
```

