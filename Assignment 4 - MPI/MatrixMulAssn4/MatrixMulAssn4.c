#include<stdio.h>
#include<mpi.h>


#define rowsA 5 
#define columnsA 32 
#define rowsB 32 
#define columnsB 5 
#define masterToSlaveTag 1 
#define slaveToMasterTag 2 

void setMatrixA();
void setMatrixB();
void printMatrices(); 

int rank; 
int size; 
int i, j, k; 
double mat_a[rowsA][columnsA]; 
double mat_b[rowsB][columnsB]; 
double resultMatrix[rowsA][columnsB]; 
double startTime; 
double endTime; 
int lowBound; 
int upperBound; 
int portion; 
MPI_Status status; 
MPI_Request request; 

void setMatrixA() {
    int x = 2;
    for (i = 0; i < rowsA; i++) {
        for (j = 0; j < columnsA; j++) {
            mat_a[i][j] = x;
            x += 2;
        }
    }
}

void setMatrixB() {

    int y = 1;
    for (i = 0; i < rowsB; i++) {
        for (j = 0; j < columnsB; j++) {
            mat_b[i][j] = y;
            y += 2;
        }
    }
}


void printMatrices()
{

    printf("The Matrix A is :\n\n");
    for (i = 0; i < rowsA; i++) {
        printf("\n");
        for (j = 0; j < columnsA; j++) {
            printf("%4.1f  ", mat_a[i][j]);
        }
        printf("\n");
    }
    printf("\n\n");
    printf("The Matrix B is :\n");

    for (i = 0; i < rowsB; i++) {
        printf("\n");
        for (j = 0; j < columnsB; j++)
            printf("%7.1f  ", mat_b[i][j]);
    }
    printf("\n\n");
    printf("The Result of Matrix AxB is :\n");
    for (i = 0; i < rowsA; i++) {
        printf("\n");
        for (j = 0; j < columnsB; j++)
            printf("%10.1f  ", resultMatrix[i][j]);
    }
    printf("\n\n");
}

int main(int argc, char* argv[])
{

    MPI_Init(&argc, &argv); 
    MPI_Comm_rank(MPI_COMM_WORLD, &rank); 
    MPI_Comm_size(MPI_COMM_WORLD, &size); 

    /* master initializes work*/
    if (rank == 0) {
        setMatrixA();
        setMatrixB();
        startTime = MPI_Wtime();
        for (i = 1; i < size; i++) {//for each slave other than master
            portion = (rowsA / (size - 1)); // calculate portion without master
            lowBound = (i - 1) * portion;
            if (((i + 1) == size) && ((rowsA % (size - 1)) != 0)) {//if rows of [A] cannot be equally divided among slaves
                upperBound = rowsA; //last slave gets all the remaining rows
            }
            else {
                upperBound = lowBound + portion; //rows of [A] are equally divisable among slaves
            }
            //send the low bound first without blocking, to the intended slave
            MPI_Isend(&lowBound, 1, MPI_INT, i, masterToSlaveTag, MPI_COMM_WORLD, &request);
            //next send the upper bound without blocking, to the intended slave
            MPI_Isend(&upperBound, 1, MPI_INT, i, masterToSlaveTag + 1, MPI_COMM_WORLD, &request);
            //finally send the allocated row portion of [A] without blocking, to the intended slave
            MPI_Isend(&mat_a[lowBound][0], (upperBound - lowBound) * columnsA, MPI_DOUBLE, i, masterToSlaveTag + 2, MPI_COMM_WORLD, &request);
        }
    }
  
    MPI_Bcast(&mat_b, rowsB * columnsB, MPI_DOUBLE, 0, MPI_COMM_WORLD);

    /* work done by slaves*/
    if (rank > 0) {
        //receive low bound from the master
        MPI_Recv(&lowBound, 1, MPI_INT, 0, masterToSlaveTag, MPI_COMM_WORLD, &status);
        //next receive upper bound from the master
        MPI_Recv(&upperBound, 1, MPI_INT, 0, masterToSlaveTag + 1, MPI_COMM_WORLD, &status);
        //finally receive row portion of [A] to be processed from the master
        MPI_Recv(&mat_a[lowBound][0], (upperBound - lowBound) * columnsA, MPI_DOUBLE, 0, masterToSlaveTag + 2, MPI_COMM_WORLD, &status);
        for (i = lowBound; i < upperBound; i++) {//iterate through a given set of rows of [A]
            for (j = 0; j < columnsB; j++) {
                for (k = 0; k < rowsB; k++) {
                    resultMatrix[i][j] += (mat_a[i][k] * mat_b[k][j]);
                }
            }
        }
        //send back the low bound first without blocking, to the master
        MPI_Isend(&lowBound, 1, MPI_INT, 0, slaveToMasterTag, MPI_COMM_WORLD, &request);
        //send the upper bound next without blocking, to the master
        MPI_Isend(&upperBound, 1, MPI_INT, 0, slaveToMasterTag + 1, MPI_COMM_WORLD, &request);
        //finally send the processed portion of data without blocking, to the master
        MPI_Isend(&resultMatrix[lowBound][0], (upperBound - lowBound) * columnsB, MPI_DOUBLE, 0, slaveToMasterTag + 2, MPI_COMM_WORLD, &request);
    }

    /* master gathers processed work*/
    if (rank == 0) {
        for (i = 1; i < size; i++) {// untill all slaves have handed back the processed data
            //receive low bound from a slave
            MPI_Recv(&lowBound, 1, MPI_INT, i, slaveToMasterTag, MPI_COMM_WORLD, &status);
            //receive upper bound from a slave
            MPI_Recv(&upperBound, 1, MPI_INT, i, slaveToMasterTag + 1, MPI_COMM_WORLD, &status);
            //receive processed data from a slave
            MPI_Recv(&resultMatrix[lowBound][0], (upperBound - lowBound) * columnsB, MPI_DOUBLE, i, slaveToMasterTag + 2, MPI_COMM_WORLD, &status);
        }
        endTime = MPI_Wtime();
        printf("\nRunning Time = %f\n\n", endTime - startTime);
        printMatrices();
    }
    MPI_Finalize(); 
    return 0;
}



