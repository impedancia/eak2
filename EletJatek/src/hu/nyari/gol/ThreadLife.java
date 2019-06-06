package hu.nyari.gol;

import java.util.ArrayList;
import java.util.List;

/** Game of Life. */
public class ThreadLife extends Life {


    private static int n;
    private static int m;
    private static int iterations;
    private static int range_length;
    private static int range_mod;
    private List<Integer[]> ranges;

    public ThreadLife(int n, int m, boolean torus) {
        super(n, m, torus);
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

        while( count > 0 ){
            int f =0;
            int t =0;
            for(int i=0; i<ranges.size();i++) {
                Integer[] range = ranges.get(i);
                f = range[0];
                t = range[1];
                System.out.println("f: "+f+"t: "+t);
                range_step(f, t);
            }

            boolean[][] tmp = from; from = to; to = tmp;  // swap from and to
            --count;
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
            n = Integer.parseInt(args[0]);
            m = Integer.parseInt(args[1]);
            iterations = Integer.parseInt(args[2]);
            degree_of_parallelism = Integer.parseInt(args[5]);
            ThreadLife life = new ThreadLife(n, m,false);
            life.fromFile(args[3]);//.animate(iterations,1,100,0,0,n,m);
            System.out.println("Computation lasted "+ life.measure(iterations) +" milliseconds.");
            life.toFile(args[4]);
        }
    }

}