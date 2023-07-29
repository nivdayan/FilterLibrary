

This filters library contains the InfiniFilter paper from SIGMOD 2023 as well as some baselines against which it can be compared.  

## Installation
Compile the library using the following command. The java development kit version 11 or above should be installed. 
```console
javac filters/*.java bitmap_implementations/*.java infiniFilter_experiments/*.java  
```

## Reproducing the Results

To reproduce Figure 13, Parts (A) to (D), run the following: 
```console
java infiniFilter_experiments.Experiment1 16 12 31    
```

To reproduce Figure 13, Parts (E) to (H), run the following: 
```console
java infiniFilter_experiments.Experiment1 8 10 31    
```

To reproduce Figure 14, Parts (A) to (B), run the following: 
```console
java infiniFilter_experiments.Experiment2 16 10 31   
```

To reproduce Figure 14, Parts (C) to (D), run the following: 
```console
java infiniFilter_experiments.Experiment2 8 10 31  
```

To reproduce Figure 15, run the following: 
```console
java infiniFilter_experiments.Experiment3 8 10 31
```

To reproduce Figure 16, run the following: 
```console
java infiniFilter_experiments.Experiment4 16 10 31
```




