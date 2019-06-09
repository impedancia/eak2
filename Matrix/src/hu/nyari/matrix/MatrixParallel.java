package hu.nyari.matrix;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MatrixParallel extends Matrix {

    protected ExecutorService executorService;
    protected CountDownLatch latch;
    protected CountDownLatch startLatch;
    protected int degree_of_parallelism;
    protected AtomicBoolean running = new AtomicBoolean(true);
    protected Queue<Integer> workQueue;
    public MatrixParallel(int M, int N, int degree_of_parallelism) {
        super(M, N);
        this.degree_of_parallelism = degree_of_parallelism;
    }

    public MatrixParallel(double[][] data, int degree_of_parallelism) {
        super(data);
        this.degree_of_parallelism = degree_of_parallelism;
    }

    public MatrixParallel(Matrix A, int degree_of_parallelism) {
        super(A);
        this.degree_of_parallelism = degree_of_parallelism;
    }


    public MatrixParallel times(MatrixParallel B) {
        Matrix A = this;
        if (A.N != B.M) throw new RuntimeException("Illegal matrix dimensions.");
        MatrixParallel C = new MatrixParallel(A.M, B.N,degree_of_parallelism);

        executorService = Executors.newFixedThreadPool(degree_of_parallelism);
        startLatch = new CountDownLatch(1);
        latch = new CountDownLatch(C.N);
        workQueue = new ConcurrentLinkedQueue<>();
        for(int i = 0; i<= C.N; i++)
        {
            workQueue.add(i);
        }
        for(int i =0; i <= degree_of_parallelism ; i++){
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while(running.get())
                    {
                        int rownum = workQueue.poll();
                        times_par(B,rownum, C);
                        latch.countDown();
                    }

                }
            };
            executorService.submit(worker);
        }
        startLatch.countDown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        return C;
    }

    public void times_par(MatrixParallel B, int i, MatrixParallel C) {
        MatrixParallel A = this;
//        for (int i = 0; i < C.M; i++)
            for (int k = 0; k < A.N; k++)
                for (int j = 0; j < C.N; j++)
                    C.data[i][j] += (A.data[i][k] * B.data[k][j]);
    }
}
