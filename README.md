

This filters library contains the InfiniFilter paper from SIGMOD 2023 as well as some baselines against which it can be compared.  

## Installation
To compile the library, compile the library using the following command using the java development kit version 11 or above. 
```console
javac filters/*.java bitmap_implementations/*.java infiniFilter_experiments/*.java  
```

# Reproducing the Results
To reproduce the results from the paper, run the following commands. 
```console
java infiniFilter_experiments.Experiment1 16 12 31    # Reproduce Figure 13, Parts (A) to (D)

java infiniFilter_experiments.Experiment1 8 10 31    # Reproduce Figure 13, Parts (E) to (H)

java infiniFilter_experiments.Experiment2 16 10 31    # Reproduce Figure 14, Parts (A) and (B)

java infiniFilter_experiments.Experiment2 8 10 31    # Reproduce Figure 14, Parts (C) and (D)

java infiniFilter_experiments.Experiment3 8 10 31    # Reproduce Figure 15

java infiniFilter_experiments.Experiment4 16 10 31    # Reproduce Figure 16
```


