package e9qzf1;

import java.util.Queue;
import java.util.concurrent.*;

public class NBodyParallel2 extends NBody
{
    static ExecutorService executorService;
    static CompletionService<Body> completionService;
    static Queue<Integer> workQueue = new LinkedBlockingQueue<>();
    static Body[] from;
    static Body[] to;
    static CyclicBarrier barrier;

    static void update( int i, double dt ){
        Vector acceleration = acceleration(i, from);
        Vector newVelocity = from[i].velocity.euler( acceleration, dt );
        Vector newPosition = from[i].position.euler( newVelocity, dt);
        to[i] =  new Body( from[i].mass, newPosition, newVelocity );
    }
    static Body[] simulate_par( Body[] bodies, double dt, int steps ){


        executorService = Executors.newFixedThreadPool(bodies.length);
        barrier = new CyclicBarrier(bodies.length);
        for(int i = 0 ; i<= bodies.length ; i++){
            Runnable task = new Runnable() {
                @Override
                public void run() {
                        int workitem = workQueue.poll();
                        update(workitem,dt);
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            };
            executorService.submit(task);
        }


//        completionService<
        from = new Body[bodies.length];
        to = new Body[bodies.length];

        java.util.Arrays.setAll(from, i -> bodies[i]);
        for( int count=0; count<steps; ++count ){
            for( int i=0; i<to.length; ++i ){
                workQueue.add(i);
            }
            // swap from and to
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

        bodies = simulate_par(bodies, 0.0001, steps);

        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }
}
