package e9qzf1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class NBodyParallel3 extends NBody{

    private static List<FutureTask<Body>> tasks;
    private static ExecutorService executorService;

    static Body[] simulate_par(Body[] bodies, double dt, int steps ){

        Body[] from = new Body[bodies.length], to = new Body[bodies.length];
        java.util.Arrays.setAll(from, i -> bodies[i]);

        executorService = Executors.newFixedThreadPool(bodies.length);
        tasks = new ArrayList<>();

        for( int count=0; count<steps; ++count ){

            for( int i=0; i<to.length; ++i ){
                int i_ = i;
                Body[] from_ = from;
                FutureTask<Body> task = new FutureTask<>(() -> update(i_, from_,dt));

                tasks.add(task);
                executorService.execute(task);
            }

            for( int i=0; i<to.length; ++i ){
                FutureTask<Body> f = tasks.get(i);
                try {
                    to[i] = f.get();
                } catch (InterruptedException | ExecutionException e) {
                    f.cancel(true);
                } finally {
                    if (f.isDone()) {
                        f.cancel(true);
                    }
                }
            }

            // swap from and to
            Body[] tmp = to; to = from; from = tmp;
        }

        executorService.shutdown();

        return to;
    }
    public static void main( String[] args )  {
      /*  try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        int size = Integer.parseInt(args[0]);
        int steps = Integer.parseInt(args[1]);
//        int degree_of_parallelism = Integer.parseInt(args[2]);
        Body[] bodies = randomSystem(size);
//        bodies = simulate(bodies,0.0001,3);   // to warm up
        Body[] out_seq;
        long startTime = System.currentTimeMillis();

        out_seq = simulate(bodies, 0.0001, steps);

        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        try {
            writeFile("c:\\temp\\bodies_seq.dat",out_seq);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFile(String path, Body[] array) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for(Body l : array)
                bw.write(l.toString()+"\r\n");
        }

    }
}
