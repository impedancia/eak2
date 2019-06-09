package hu.nyari.qsort;
import hu.nyari.netrol.QuickSorter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws Exception {
        //test_parallelism();
       // generate();
       // Thread.sleep(10000);
        if (args.length < 2) throw new Exception();

        String infile = args[0];
        String outfile = args[1];
        //int degree_of_par = args[2];
     //   long[] array = processFile(infile);
     //   qsort_seq(infile, outfile, array);
        //qsort_parallel_net(infile,outfile,array);
        long[] array2 = processFile(infile);
        qsort_parallel(infile,outfile,array2);
    }

    private static void qsort_seq(String infile, String outfile, long[] array) throws IOException {
        long startTime = System.currentTimeMillis();


        QuickSort qs = new QuickSort();
        qs.sort(array);


        long endTime = System.currentTimeMillis();
        System.out.println("seq sorting time: "+ (endTime-startTime)+" ms");
        writeFile(outfile+"out_seq"+array.length+".txt", array);
    }
    private static void qsort_parallel_net(String infile, String outfile, long[] array) throws IOException {
        long startTime = System.currentTimeMillis();


        QuickSorter qs = new QuickSorter(array,0,array.length-1);
        qs.run_par();

        long endTime = System.currentTimeMillis();
        System.out.println("par sorting time: "+ (endTime-startTime)+" ms");
        writeFile(outfile+"out_net"+array.length+".txt", array);
    }
    private static void qsort_parallel(String infile, String outfile, long[] array) throws IOException {
        long startTime = System.currentTimeMillis();


        QuickSortParallel qs = new QuickSortParallel(2);
        qs.sort_par(array);

        long endTime = System.currentTimeMillis();
        System.out.println("par sorting time: "+ (endTime-startTime)+" ms");
        writeFile(outfile+"out_par"+array.length+".txt", array);
    }
    private static long[] processFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            List<Long> list = new ArrayList<Long>();
            while ((line = br.readLine()) != null) {
                list.add(Long.valueOf(line));
            }
            long[] longs = new long[list.size()];
            for(int i=0;i<list.size();i++){
                longs[i] = list.get(i).longValue();
            }
            return longs;
        }
    }

    private static void writeFile(String path, long[] array) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for(long l : array)
                bw.write(l+"\r\n");
        }

    }
    private static void generate() throws IOException {
        long[] array = new long[] {54L,26L,93L,17L,77L,31L,44L,55L,20L};
        /*Supplier<Long> generator = () -> {

        };*/
        Random rnd = new Random();
        FileWriter writer = new FileWriter("output.txt");
        rnd.longs(10000000).forEach(x -> {
            try {
                writer.write(Long.toString(x)+"\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private static void test_parallelism() {
        long a = System.nanoTime();
        IntStream.range(0, Integer.MAX_VALUE).sum();
        long b = System.nanoTime();
        IntStream.range(0, Integer.MAX_VALUE).parallel().sum();
        long c = System.nanoTime();
        System.out.println(b - a);
        System.out.println(c - b);
        System.out.println(b - a > c - b ? "OK" : "?");

    }
}
