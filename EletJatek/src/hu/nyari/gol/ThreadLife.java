package hu.nyari.gol;

import net.jcip.annotations.GuardedBy;

import java.io.IOException;
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
    final private static AtomicInteger iteration_number = new AtomicInteger();
    private static Timer timer = new Timer();
    private List<Integer[]> ranges;
    private ExecutorService executorService;
    private CountDownLatch startLatch;
    private AtomicBoolean running;

    public static Object consoleLock = new Object();
    public ThreadLife(int n, int m, boolean torus) {


        super(n, m, torus);
        running = new AtomicBoolean(true);
    }

    private static int degree_of_parallelism;

    /** Compute new state for only one cell */
    protected void one_step(int i, int j){
               // System.out.println(("i: "+i+"j: "+j));
                switch( living9(i,j) ){
                    case  3: to[i][j] = true; break;
                    case  4: to[i][j] = from[i][j]; break;
                    default: to[i][j] = false;
                }
    }

    protected void range_step(int f, int t){
        for(int k = f; k<=t; k++)
        {
            int i = k / m;
            int j = k % m;
            one_step(i,j);
        }
    }
    private void calculate_ranges(){
        int number_of_cells = n*m;
        range_length = number_of_cells / degree_of_parallelism;
        range_mod = number_of_cells % degree_of_parallelism;

        ranges = new ArrayList<Integer[]>();
        for(int i=0; i<degree_of_parallelism;i++) {
            Integer[] range =new Integer[2];
            range[0] = i * range_length;
            range[1] = (i+1) * range_length-1;
            ranges.add(range);
        }
        if (range_mod > 0) {
            Integer[] range = new Integer[2];
            range[0] = number_of_cells - 1 - range_mod;
            range[1] = number_of_cells - 1;
            ranges.add(range);
        }
    }

    public void iterate( int count ){
        calculate_ranges();
        int max = count;
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                synchronized (consoleLock) {
                    int it = iteration_number.get();
                    System.out.println("iteration count: " + it);
                }
            }
        },1000,1000);
        startLatch = new CountDownLatch(0);
        Runnable input = new Runnable()
        {
            Scanner user_input = new Scanner(System.in);
            CountDownLatch[] _startLatch;
            List<Integer> cmd = new ArrayList<>();
            public void run()
            {
                while(running.get())
                {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int kk=65;
                    try {
                        kk = System.in.read();
                        cmd.add(kk);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    synchronized (consoleLock) {
                                String command = user_input.nextLine();
                                command = ((char) kk) + command;
                                switch (command) {
                                    case "stop":
                                        //_startLatch.set( new CountDownLatch(1));
                                        _startLatch[0] = new CountDownLatch(1);
                                        break;
                                    case "start":
                                        //_startLatch.get().countDown();
                                        _startLatch[0].countDown();
                                        break;
                                    case "halt":
                                        break;
                                }
                                System.out.println("User command: " + command);
                            }

                }
            }
        };
        Thread console = new Thread(input);
        console.start();
        executorService = Executors.newFixedThreadPool(degree_of_parallelism);
        while( count > 0 ){
            long startTime = System.currentTimeMillis();
            CountDownLatch latch = new CountDownLatch(degree_of_parallelism);
          //  barrier.reset();
            iteration_number.set(max - count);
            for(int i=0; i<ranges.size();i++) {
                final int param = i;
                Runnable range_calculation =
                        new Runnable(){
                            public void run(){
                                //long startTime = System.currentTimeMillis();
                                try {
                                    startLatch.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
//                                System.out.println("Thread: " +param);
                                int f =0;
                                int t =0;
                                Integer[] range = ranges.get(param);
                                f = range[0];
                                t = range[1];
//                                System.out.println("Thread range: " +f+"-"+t);
                                range_step(f, t);
                                latch.countDown();
                                /*try {
                                    barrier.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (BrokenBarrierException e) {
                                    e.printStackTrace();
                                }*/
                                //                               System.out.println("Thread: " +param+"finished");
                                //long endTime = System.currentTimeMillis();
                                //System.out.println("Thread time : " + (endTime-startTime) );
//
                            }
                        };
                //range_calculation.run();
                executorService.submit(range_calculation);

                long endTime = System.currentTimeMillis();
                //System.out.println("Starting threads time : " + (endTime-startTime) );
            }
            /*try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }*/
            try {
                latch.await();

 //               System.out.println("After await");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean[][] tmp = from; from = to; to = tmp;  // swap from and to
            --count;
        }
        timer.cancel();
        timer.purge();
        executorService.shutdown();
        running.set(false);
        try {
            console.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
   //         Thread.sleep(10000);
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