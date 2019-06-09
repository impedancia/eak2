package hu.nyari.qsort;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class QuickSortParallel extends  QuickSort {

    protected ExecutorService executorService;
    protected Map<Integer, List<Future<?>>> tasks = new HashMap<Integer, List<Future<?>>>();
    protected CountDownLatch latch;
    int degree_of_parallelism;

    public QuickSortParallel(int degree_of_parallelism){
        this.degree_of_parallelism = degree_of_parallelism;
        this.executorService = Executors.newFixedThreadPool(degree_of_parallelism*2);
    }

    public void sort_par(long[] array){
        //create units of work
        //ez max kétszer annyi lesz, mint a degree of parallelism
        List<int[]> unitsofwork = new ArrayList<>();

        if (degree_of_parallelism == 0)
            sort(array);
        else
            create_units_of_work(array, 0, array.length-1, degree_of_parallelism, unitsofwork);

        latch = new CountDownLatch(unitsofwork.size());
        for(int[] work : unitsofwork){
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    sort(array,work[0],work[1]);
                    latch.countDown();
                }
            };
            executorService.submit(worker);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    private void create_units_of_work(long[] array,int from, int to, int degree_of_parallelism_, List<int[]> units) {
        if (degree_of_parallelism_ == 0) {
            if (from != to)
                units.add(new int[]{from, to});
        }
        else {
            int pivot = split(array, from, to);
            // left
            if (pivot > 0) {
                create_units_of_work(array, from, pivot - 1, degree_of_parallelism_ - 1, units);
            }
            //right
            if (pivot < array.length - 1) {
                create_units_of_work(array, pivot + 1, to, degree_of_parallelism_ - 1, units);
            }
        }
    }

}
