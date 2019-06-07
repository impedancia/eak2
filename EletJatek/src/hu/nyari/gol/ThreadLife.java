package hu.nyari.gol;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Game of Life. */
public class ThreadLife extends Life {


    private static int n;
    private static int m;
    private static int iterations;
    private static int range_length;
    private static int range_mod;
    private static int range_count;
    final private static AtomicInteger iteration_number = new AtomicInteger();
    private static Timer timer = new Timer();
    private List<Integer[]> ranges;
    private ExecutorService executorService;
    private ConcurrentLinkedQueue<Integer[]> rangesQueue;
    private CountDownLatch latch;
    private List<CancellableRunnable> runnables = new ArrayList<>(degree_of_parallelism);
    private Map<CancellableRunnable, Future<?>> tasks = new HashMap<>();
    private CyclicBarrier barrier;

    public ThreadLife(int n, int m, boolean torus) {
        super(n, m, torus);
    }

    private static int degree_of_parallelism;

    /** Compute new state for only one cell */
    protected void one_step(int i, int j){
        switch( living9(i,j) ){
            case  3: to[i][j] = true; break;
            case  4: to[i][j] = from[i][j]; break;
            default: to[i][j] = false;
        }
    }

   /* protected void range_step(int f, int t){
        for(int k = f; k<=t; k++)
        {
            int i = k / m;
            int j = k % m;
            one_step(i,j);
        }
    }*/
   protected void range_step(int f, int k){
       int ii = k / m;
       for( int i=ii; i<ii+1; ++i ){
           for( int j=0; j<to[0].length; ++j ){
               switch( living9(i,j) ){
                   case  3: to[i][j] = true; break;
                   case  4: to[i][j] = from[i][j]; break;
                   default: to[i][j] = false;
               }
           }
       }
   }
    private void calculate_ranges(int len){
        int number_of_cells = n*m;
        if (len == 0) {
            range_length = number_of_cells / degree_of_parallelism;
            range_count = number_of_cells / range_length;
            range_mod = number_of_cells % range_length;
        }
        else {
            range_length = len;
            range_mod = number_of_cells % len;
            range_count = number_of_cells / len;

        }

        ranges = new ArrayList<Integer[]>();
        for(int i=0; i<range_count;i++) {
            Integer[] range =new Integer[2];
            range[0] = i * range_length;
            range[1] = (i+1) * range_length-1;
            ranges.add(range);
        }
        if (range_mod > 0) {
            Integer[] range = new Integer[2];
            range[0] = number_of_cells - range_mod;
            range[1] = number_of_cells - 1;
            ranges.add(range);
            range_count++;
        }
    }

    public void iterate( int count ){
        calculate_ranges(m);
        int max = count;
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                int it = iteration_number.get();
                System.out.println("iteration count: " + it);
//                System.out.println("latch: "+latch.getCount());
            }
        },1000,1000);

        executorService = Executors.newFixedThreadPool(degree_of_parallelism);
       // barrier = new CyclicBarrier(degree_of_parallelism);

        rangesQueue = new ConcurrentLinkedQueue<Integer[]>();
        for(int i = 0; i<= count; i++){
            rangesQueue.addAll(ranges);
        }
     //   rangesQueue.element();
        for(int i = 0; i<= degree_of_parallelism; i++) {
            CancellableRunnable range_calculation_infinite =
                    new CancellableRunnable() {
                        private volatile boolean running = true;
                        public void cancel() {
                            running = false;
                        }

                        public void run() {
                            int f = 0;
                            int t = 0;

                            while (running) {
                                Integer[] range = rangesQueue.poll();
                                f = range[0];
                                t = range[1];
                                range_step(f, t);
                                latch.countDown();
//                                System.out.println("latch: "+latch.getCount());
//                                barrier.
                            }
                        }
                    };
            //range_calculation.run();
            runnables.add(range_calculation_infinite);
            //Future<?> future = executorService.submit(range_calculation_infinite);
            //tasks.put(range_calculation_infinite,future);
        }

        //final CyclicBarrier barrier = new CyclicBarrier(degree_of_parallelism);
        while( count > 0 ) {
            long startTime = System.currentTimeMillis();
            for (Integer[] range :ranges) {
                int f = 0;
                int t = 0;
                f = range[0];
                t = range[1];
                range_step(f, t);
            }

         //   latch = new CountDownLatch(range_count);

            iteration_number.set(max - count);


          /*  try {
                latch.await();

                //               System.out.println("After await");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/
            boolean[][] tmp = from;
            from = to;
            to = tmp;  // swap from and to
            --count;

        }
        //  running.set(false);
/*        for(CancellableRunnable r : runnables){
            r.cancel();
            Future<?> f = tasks.get(r);
            f.cancel(true);
        }*/
        timer.cancel();
        timer.purge();
        executorService.shutdown();
    }

    public static void main(String[] args ) throws Exception {
        if( args.length < 6 ){
            new Life(20,50,false).blinker(0,0).animate(100,1,100,0,0,20,50);
            new Life(50,50,false).acorn(25,25).animate(200,1,100,0,0,50,50);
            new Life(500,500,false).inf10(5,20).animate(100,1,100,0,0,20,50);
            new Life(500,500,false).gosperGliderGun(5,5).animate(200,1,100,0,0,50,80);
            new Life(300,300,false).acorn(150,150).animate(6000,100,0,140,100,20,70);
        } else {
            //ez azért van, hogy legyen időm rácsatlakoztatni a profilert
    //        Thread.sleep(10000);
            n = Integer.parseInt(args[0]);
            m = Integer.parseInt(args[1]);
            iterations = Integer.parseInt(args[2]);
            degree_of_parallelism = Integer.parseInt(args[5]);
            if (degree_of_parallelism > n*m) throw new InvalidParameterException("Több a szál, mint a feldolgozandó elem.");
            ThreadLife life = new ThreadLife(n, m,false);
            life.fromFile(args[3]);//.animate(iterations,1,100,0,0,n,m);
            System.out.println("Computation lasted "+ life.measure(iterations) +" milliseconds.");
            life.toFile(args[4]);
        }
    }

}