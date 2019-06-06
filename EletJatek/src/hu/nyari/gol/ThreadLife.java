package hu.nyari.gol;

/** Game of Life. */
public class ThreadLife extends Life {


    public ThreadLife(int n, int m, boolean torus) {
        super(n, m, torus);
    }

    public static void main(String[] args ) throws Exception {
        if( args.length < 6 ){
            new Life(20,50,false).blinker(0,0).animate(100,1,100,0,0,20,50);
            new Life(50,50,false).acorn(25,25).animate(200,1,100,0,0,50,50);
            new Life(500,500,false).inf10(5,20).animate(100,1,100,0,0,20,50);
            new Life(500,500,false).gosperGliderGun(5,5).animate(200,1,100,0,0,50,80);
            new Life(300,300,false).acorn(150,150).animate(6000,100,0,140,100,20,70);
        } else {
            int n = Integer.parseInt(args[0]);
            int m = Integer.parseInt(args[1]);
            int iterations = Integer.parseInt(args[2]);
            int degree_of_parallelism = Integer.parseInt(args[5]);
            Life life = new Life(n,m,false);
            life.fromFile(args[3]);
            System.out.println("Computation lasted "+ life.measure(iterations) +" milliseconds.");
            life.toFile(args[4]);
        }
    }

}