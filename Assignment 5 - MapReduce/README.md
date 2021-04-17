## Assignment 5 - MapReduce

Commands :

hdfs namenode -format
start-dfs
start-yarn
jps
hadoop fs -mkdir /input_dir
hadoop fs -put C:/input_file /input_dir
hadoop fs -ls /input_dir/
hadoop fs -cat /input_dir/input_file
hadoop jar MapReduceAssignmentDhananjay.jar WordCountAssignment.WordCount /input_dir /output_dir
hadoop fs -cat /output_dir/*
