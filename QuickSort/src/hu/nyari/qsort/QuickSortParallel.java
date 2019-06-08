package hu.nyari.qsort;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class QuickSortParallel extends  QuickSort {

    protected ExecutorService executorService = Executors.newFixedThreadPool(2);
    protected Map<Integer,Future<?>[]> tasks = new HashMap<Integer, Future<?>[]>();
    protected AtomicInteger step =new AtomicInteger(0);

    public void sort( long[] array ){
        if( array == null ) throw new IllegalArgumentException("Argument must not be null.");
        step.set(0);
        sort(array,0,array.length-1);
    }
    protected void sort( long[] arr, int lo, int hi ){
        assert arr != null && 0 <= lo && hi < arr.length && -1 <= hi - lo;
        int prevstep = step.get();
        int currstep = prevstep+1;
        step.set(currstep);
        if( lo < hi ){  // there are at least two elements to sort
            final int pivot = split( arr, lo, hi );
            assert lo <= pivot && pivot <= hi;
            if (!tasks.containsKey(step.get()-1)) {
                Thread t1 = new Thread(() -> sort(arr, lo, pivot - 1));
                Thread t2 = new Thread(() -> sort(arr, pivot + 1, hi));
                Future<?> task1 = executorService.submit(t1);
                Future<?> task2 = executorService.submit(t2);
                tasks.put(currstep,new Future<?>[]{task1, task2});
            }
            else {
                Future<?>[] prevtasks = tasks.get(prevstep);
                try {
                    prevtasks[0].wait();
                    prevtasks[1].wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // még ide azt szeretném megírni, hogy egymástól függetlenül indulhasson el az új lépés két oldala
                Thread t1 = new Thread(() -> sort(arr, lo, pivot - 1));
                Thread t2 = new Thread(() -> sort(arr, pivot + 1, hi));
                Future<?> task1 = executorService.submit(t1);
                Future<?> task2 = executorService.submit(t2);
                tasks.put(currstep,new Future<?>[]{task1, task2});

            }

        }
    }
}
