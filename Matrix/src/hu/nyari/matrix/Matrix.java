package hu.nyari.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Matrix {
	protected final int M;
    protected final int N;
    public final double[][] data;

    public Matrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new double[M][N];
    }

    public Matrix(double[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new double[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                    this.data[i][j] = data[i][j];
    }

    public Matrix(Matrix A) { this(A.data); }

    public Matrix fillWithRandom() {
        for (int i = 0; i < this.M; i++)
            for (int j = 0; j < this.N; j++)
                this.data[i][j] = Math.random();
        return this;
    }

    public Matrix times(Matrix B) {
        Matrix A = this;
        if (A.N != B.M) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(A.M, B.N);
        for (int i = 0; i < C.M; i++)
           for (int k = 0; k < A.N; k++)
               for (int j = 0; j < C.N; j++)
                    C.data[i][j] += (A.data[i][k] * B.data[k][j]);
        return C;
    }
}
