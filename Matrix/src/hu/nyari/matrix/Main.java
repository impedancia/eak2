package hu.nyari.matrix;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static String outdir;
    public static void main(String[] args) throws Exception {
        if (args.length < 3)
            throw new Exception();

        String file1 = args[0];
        String file2 = args[1];
        outdir = args[2];

        //generate(5000,5000);

        //times_seq(file1, file2);
        times_par(file1,file2,10);

    }
    private static void times_par(String file1, String file2, int dog) throws IOException {
        MatrixParallel m1 = new MatrixParallel(processFile(file1),dog);
        MatrixParallel m2 = new MatrixParallel(processFile(file2),dog);
        long startTime = System.currentTimeMillis();
        MatrixParallel t = m1.times(m2);
        long endTime = System.currentTimeMillis();
        System.out.println("par multiplication time: "+ (endTime-startTime)+" ms");
        writeFile(outdir +"out_par"+m1.data.length+"x"+m1.data[0].length+".txt", t);
    }
    private static void times_seq(String file1, String file2) throws IOException {
        Matrix m1 = processFile(file1);
        Matrix m2 = processFile(file2);
        long startTime = System.currentTimeMillis();
        Matrix t = m1.times(m2);
        long endTime = System.currentTimeMillis();
        System.out.println("seq multiplication time: "+ (endTime-startTime)+" ms");
        writeFile(outdir +"out_seq"+m1.data.length+"x"+m1.data[0].length+".txt", t);
    }
    private static Matrix processFile(String filepath) throws IOException {
        Matrix retval;
        List<Double> row = new ArrayList<>();
        List<List<Double>> m = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            //List<Long> list = new ArrayList<Long>();
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\\s");
                for(String s : split)
                {
                    row.add(Double.valueOf(s));
                }
                m.add(row);
                row = new ArrayList<>();
            }
        }
        retval = new Matrix(m.size(),m.get(0).size());
        for(int i=0;i<=m.size()-1;i++){
            List<Double> r = m.get(i);
            for(int j= 0; j<=r.size()-1;j++){
                retval.data[i][j] = r.get(j);
            }
        }
        return retval;
    }

    private static void writeFile(String outfilename, Matrix m) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outfilename)))
        {
            for(int i=0; i<= m.data.length-1; i++) {
                for (int j = 0; j <= m.data[i].length - 1; j++) {
                    bw.write(Double.toString(m.data[i][j]));
                    if (j < m.data[i].length) bw.write(" ");
                }
                bw.write("\r\n");
            }
        }
    }
    private static void generate(int n, int m) throws IOException {
        Matrix matrix = new Matrix(n,m);
        matrix.fillWithRandom();
        writeFile(outdir +"out_seq"+matrix.data.length+"x"+matrix.data[0].length+"_rnd.txt",matrix);
    }
}
