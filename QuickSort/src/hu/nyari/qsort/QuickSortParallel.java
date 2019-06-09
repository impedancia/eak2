package hu.nyari.qsort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class QuickSortParallel extends  QuickSort {
    int dog =2;
    int count =0;

    public QuickSortParallel(int degree_of_parallelism) throws Exception {
        dog = degree_of_parallelism;
        if (dog % 2 != 0) throw new Exception();
        executorService = Executors.newFixedThreadPool(dog);
    }

    protected ExecutorService executorService;
    public void sort_par(long[] array){
        List<Thread> threads = mySort(array, 0, array.length-1);

        try
        {
            for (Thread t : threads)
            {
                t.start();
               //executorService.submit(t);
            }
            for (Thread t : threads)
            {
                t.join();
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally {
            executorService.shutdown();
        }

    }

    public List<Thread> mySort(long[] array, int from, int to)
    {
        List<Thread> threads = new ArrayList<Thread>();
        //Thread t1 = new Thread(new Worker(array, from, split - 1));
        //Thread t2 = new Thread(new Worker(array, split + 1, to - 1));
        if( from < to ) {
            count = count + 2;
            int split = split(array, from, to);
            int from1 = from;
            int to1 = split-1;

            int from2 = split == 0 ? 0 : split -1;
            int to2 = to-1;
            System.out.println("t1 pivot: "+split+"from: "+from1+"to: "+to1);
            System.out.println("t2 pivot: "+split+"from: "+from2+"to: "+to2);
            if (count < dog) {
                mySort(array, from1, to1);
                mySort(array, from2, to2);
            }
        }
        System.out.print("array:");
        for(long l : array)
        {
            System.out.print(" "+l);
        }
        System.out.println();
/*
        if(count < dog ) {
            threads.addAll(mySort(array, from, split));
            threads.addAll(mySort(array, split , to));
        }else {
            threads.add(t1);
            threads.add(t2);
        }
*/
        return threads;
    }

    private class Worker implements Runnable
    {
        private final long[] arr;
        private final int from, to;

        public Worker(long[] arr, int from, int to)
        {

            this.arr = arr;
            this.from = from;
            this.to = to;
            //System.out.println("from: "+from+"to: "+to);
        }

        @Override
        public void run()
        {
            sort(arr, from, to);
        }
    }
}