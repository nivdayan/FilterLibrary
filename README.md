# Compilation Instructions

The code can run on Java version 11 and above.

cd FilterLibrary  
javac filters/*.java bitmap_implementations/*.java infiniFilter_experiments/*.java  

# Running Instructions

java filters.Client  # this will run the tests to make sure the code is working correctly  


java infiniFilter_experiments.Experiment1 16 10 15    # compare many different baselines with 16 bits per entry, starting with 1K entries, and multiplying the data size up to a data size of 32K entries. 


