## Assignment 4 - MPI 

In this assignment, you are supposed to calculate the product of two matrices A (of size N*32) and B
(of size 32*N), which should be an N*N matrix. Specifically, you are supposed to:
• Design a parallel scheme for computing matrix multiplication, including how to:
o Separate the task into divisions and let each process finish one division
o Transfer data between processes
o Form the output matrix using the result of each process.
• Implement the parallel mechanism with any type of communications in MPI (e.g., Blocking
communication (MPI_Send/MPI_Recv), or Collective communication).
• Observe the running time of your programs; change some of the parameters to see how it is
associated with N and communication type (and number of processes, if available).
