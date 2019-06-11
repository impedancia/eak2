package e9qzf1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.*;

public class NBodyParallel2 extends NBody
{
    static ExecutorService executorService;
//    static CompletionService<Body> completionService;
    static Queue<Integer> workQueue = new LinkedBlockingQueue<>();
    static Body[] from;
    static Body[] to;
 //   static CyclicBarrier barrier;
    static CountDownLatch latch;

    static void update( int i, double dt ){
        Vector acceleration = acceleration(i, from);
        Vector newVelocity = from[i].velocity.euler( acceleration, dt );
        Vector newPosition = from[i].position.euler( newVelocity, dt);
        to[i] =  new Body( from[i].mass, newPosition, newVelocity );
    }
    static Body[] simulate_par( Body[] bodies, double dt, int steps ){
        executorService = Executors.newFixedThreadPool(bodies.length);
//        completionService<
        from = new Body[bodies.length];
        to = new Body[bodies.length];
//        barrier = new CyclicBarrier(bodies.length);
        java.util.Arrays.setAll(from, i -> bodies[i]);
        for( int count=0; count<steps; ++count ){
            for( int i=0; i<to.length; ++i ){
                workQueue.add(i);
            }

            latch = new CountDownLatch(bodies.length);
            for(int i = 0 ; i<= bodies.length ; i++){
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        int workitem = workQueue.poll();
                        update(workitem,dt);
                        latch.countDown();
                    }
                };
                executorService.submit(task);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            barrier.reset();
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
        int degree_of_parallelism = Integer.parseInt(args[2]);
        Body[] bodies = randomSystem(size);
//        bodies = simulate(bodies,0.0001,3);   // to warm up
        long startTime = System.currentTimeMillis();

        bodies = simulate_par(bodies, 0.0001, steps);

        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        try {
            writeFile("c:\\temp\\bodies_par.dat",bodies);
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
