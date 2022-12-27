
scp -P 15824 /Users/nivdayan/Dropbox/eclipse-workspace-java2/QuotientFilter/src/testing_project/* temp_pedro@4.tcp.eu.ngrok.io:/home/temp_pedro/InfiniFilter/testing_project
ssh -p 15824 temp_pedro@4.tcp.eu.ngrok.io
cd InfiniFilter
javac testing_project/TesterClient.java
java testing_project/TesterClient 




cd FilterLibrary/src/ 
javac filters/*.java bitmap_implementations/*.java
java filters.Client 


ssh -p 8022 niv@preciousss.itu.dk


scp -P 8022 /Users/nivdayan/Dropbox/eclipse-workspace-java2/QuotientFilter/src/testing_project/* niv@preciousss.itu.dk:/home/temp_pedro/InfiniFilter/testing_project



