package e9qzf1;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NBodyParallel extends NBody {

    static ExecutorService executorService;
    static CompletionService<Body> completionService;
    static Queue<Integer> workQueue = new ConcurrentLinkedQueue<>();
    static Body[] from;
    static Body[] to;

    static AtomicBoolean running;
    static CountDownLatch latch;
    static CountDownLatch startLatch;
  //  static CyclicBarrier barrier;


    static Body update( int i, double dt ){
        Vector acceleration = acceleration(i, from);
        Vector newVelocity = from[i].velocity.euler( acceleration, dt );
        Vector newPosition = from[i].position.euler( newVelocity, dt);
        return new Body( from[i].mass, newPosition, newVelocity );
    }

    static Body[] simulate_par( Body[] bodies, double dt, int steps, int degree_of_parallelism ) throws InterruptedException {
        from = new Body[bodies.length];
        to = new Body[bodies.length];
        latch = new CountDownLatch(bodies.length);
        startLatch = new CountDownLatch(1);
    //    barrier = new CyclicBarrier(degree_of_parallelism);
        executorService = Executors.newFixedThreadPool(degree_of_parallelism);
        //completionService = new ExecutorCompletionService<Body>(executorService);
        running = new AtomicBoolean(true);
//        running.set(true);

        for(int i = 0; i<= degree_of_parallelism;i++){
            Runnable task = new Runnable() {
                //AtomicBoolean running;
                @Override
                public void run() {
                    while( running.get()) {
                    /*    try {
                            startLatch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        int workitem = workQueue.poll();
                        to[workitem] = update(workitem, dt);
                        latch.countDown();
                    }
                }
            };
            executorService.submit(task);

        }
        java.util.Arrays.setAll(from, i -> bodies[i]);

//        startLatch.countDown();

        for( int count=0; count<steps; ++count ){
      //      startLatch = new CountDownLatch(1);
            for( int i=0; i<to.length; ++i ){ workQueue.add(i);}
            latch.await();
            if (count < steps) {


                latch = new CountDownLatch(bodies.length);
            }
//            startLatch.countDown();

            Body[] tmp = to; to = from; from = tmp;
        }

        executorService.shutdown();
        return to;
    }




    public static void main( String[] args )  {
        int size = Integer.parseInt(args[0]);
        int steps = Integer.parseInt(args[1]);
        int degree_of_parallelism = Integer.parseInt(args[2]);
        Body[] bodies = randomSystem(size);
//        bodies = simulate(bodies,0.0001,3);   // to warm up
        long startTime = System.currentTimeMillis();
        try {
            bodies = simulate_par(bodies, 0.0001, steps, degree_of_parallelism);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        try {
            writeFile("c:\\temp\\bodies_par1.dat",bodies);
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
