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

    protected ExecutorService executorService = Executors.newFixedThreadPool(2);
    protected Map<Integer, List<Future<?>>> tasks = new HashMap<Integer, List<Future<?>>>();
    protected AtomicInteger step = new AtomicInteger(0);

    @Override
    public void sort(long[] array) {
        if (array == null) throw new IllegalArgumentException("Argument must not be null.");
        step.set(0);
        sort(array, 0, array.length - 1);
        executorService.shutdown();
    }

    protected void sort(long[] arr, int lo, int hi) {
        assert arr != null && 0 <= lo && hi < arr.length && -1 <= hi - lo;
        int currstep = step.get() + 1;
        System.out.println("step: " + currstep);
        step.set(currstep);
        int prevstep = currstep - 1;
        if (lo < hi) {  // there are at least two elements to sort
            final int pivot = split(arr, lo, hi);
            assert lo <= pivot && pivot <= hi;


            List<Future<?>> prevtasks = new ArrayList<>();
            if (tasks.containsKey(prevstep))
                prevtasks = tasks.get(prevstep);
            for(Future<?> pt : prevtasks)
            {
                try {
                    pt.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            List<Future<?>> currtasks = new ArrayList<>();

            Thread t1 = new Thread(() -> sort(arr, lo, pivot - 1));
            Thread t2 = new Thread(() -> sort(arr, pivot + 1, hi));
            Future<?> task1 = executorService.submit(t1);
            Future<?> task2 = executorService.submit(t2);
            currtasks.add(task1);
            currtasks.add(task2);
            tasks.put(currstep, currtasks);
        }

    }
}
