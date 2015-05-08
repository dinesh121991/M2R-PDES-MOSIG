
/**
 *
 * Simple parallel sort
 *
 * Divide array into chunks of equal size
 * and sort in parallel
 * Gather results on processor 0 and merge results
 *
 * Sascha Hunold <sascha.hunold@imag.fr>
 * 19/11/2010
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <unistd.h>

#include <mpi.h>


int compar(const void *a, const void *b) {
	return *(int *) a - *(int *) b;
}

int main(int argc, char *argv[]) {

	int rank;
	int p;
	int c;

	int *array;
	int nsize = -1;

	int *chunk;
	int chunk_size;
	int i;

	int seed = 1;
	int verbose = 0;

	MPI_Init(&argc, &argv);

	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &p);


    while ((c = getopt(argc, argv, "n:s:v")) != -1) {
        switch (c) {
        case 'n':
            nsize = atoi(optarg);
            break;
        case 's':
        	seed = atoi(optarg);
        	break;
        case 'v':
            verbose=1;
            break;
        }
    }


    if( nsize < 1 ) {

    	if( rank == 0 ) {
    		fprintf(stderr, "usage: mpirun -np <p> sort -n <size> [-s <seed> -v]\n");
    	}

	} else {
		int *displ;
		int *snd_cnt;
		int snd_size;

		double start_time;
		double end_time;

		snd_size = nsize / p;
		displ = (int*)malloc(p * sizeof(int));
		snd_cnt = (int*)malloc(p * sizeof(int));
		for(i=0; i<p; i++) {
			displ[i] = i * snd_size;
			snd_cnt[i] = snd_size;
			if( i == p-1 ) {
				snd_cnt[i] += nsize%p;
			}
		}

		if( rank == 0 ) {

		    srandom(seed);

			array = (int*)malloc(nsize * sizeof(int));

			for(i=0; i<nsize; i++) {
				array[i] = random() % (2*nsize);
			}

		}

		chunk_size = snd_cnt[rank];
		chunk = (int*)malloc(chunk_size * sizeof(int));


		start_time = MPI_Wtime();

		MPI_Scatterv(array, snd_cnt, displ, MPI_INT, chunk, chunk_size, MPI_INT, 0, MPI_COMM_WORLD );

		qsort(chunk, chunk_size, sizeof(int), compar);

		MPI_Gatherv(chunk, chunk_size, MPI_INT, array, snd_cnt, displ, MPI_INT, 0, MPI_COMM_WORLD );


		// merge results
		if( rank == 0 ) {
			int *result;
			int *position;
			int j;


			position = (int*)malloc(p*sizeof(int));
			for(i=0; i<p; i++) {
				position[i] = 0;
			}

			result=(int*)malloc(nsize*sizeof(int));

			for(i=0; i<nsize; i++) {
				int minidx = -1;
				int min = INT_MAX;


				for(j=0; j<p; j++) {
					if( position[j] < snd_cnt[j] ) {
						if( array[ displ[j] + position[j] ] < min ) {
							min = array[ displ[j] + position[j] ];
							minidx = j;
						}
					}
				}

				result[i] = array[ displ[minidx] + position[minidx] ];
				position[minidx]++;

			}

			end_time = MPI_Wtime();

			printf("n=%d\n", nsize);
			printf("p=%d\n", p);
			printf("time [s]=%g\n", (end_time-start_time));


			if( verbose ) {
				for(i=0; i<nsize; i++) {
					printf("%d ", result[i]);
				}
				printf("\n");
			}


			free(result);
			free(position);
			free(array);
		}

		free(chunk);
		free(displ);
		free(snd_cnt);
	}


	MPI_Finalize();

}
