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

    protected ExecutorService executorService = Executors.newFixedThreadPool(4);
    protected Map<Integer, List<Future<?>>> tasks = new HashMap<Integer, List<Future<?>>>();
    protected AtomicInteger step =new AtomicInteger(0);

    @Override
    public void sort( long[] array ){
        if( array == null ) throw new IllegalArgumentException("Argument must not be null.");
        step.set(0);
        sort(array,0,array.length-1, 0);
        executorService.shutdown();
    }

    protected void sort( long[] arr, int lo, int hi, int step_ ){
        assert arr != null && 0 <= lo && hi < arr.length && -1 <= hi - lo;
        int prevstep =step_-1;
        int currstep = step_;
        System.out.println("step: "+ step);
        step.set(currstep);
        if( lo < hi ){  // there are at least two elements to sort
            final int pivot = split( arr, lo, hi );
            assert lo <= pivot && pivot <= hi;
            if (!tasks.containsKey(prevstep)) {
                List<Future<?>> currtasks = new ArrayList<>();

                Thread t1 = new Thread(() -> sort(arr, lo, pivot - 1, step_+1));
                Thread t2 = new Thread(() -> sort(arr, pivot + 1, hi, step_+1));
                Future<?> task1 = executorService.submit(t1);
                Future<?> task2 = executorService.submit(t2);
                currtasks.add(task1);
                currtasks.add(task2);
                tasks.put(currstep,currtasks);
            }
            else {

                List<Future<?>> prevtasks = tasks.get(prevstep);
                tasks.put(currstep,new ArrayList<>());

                ThreadWaiter wait1 = new ThreadWaiter(prevtasks.get(0),() -> {
                    Thread t1 = new Thread(() -> sort(arr, lo, pivot - 1));
                    Future<?> task = executorService.submit(t1);
                    List<Future<?>> currtasks = tasks.get(currstep);
                    currtasks.add(task);
                });

                ThreadWaiter wait2 = new ThreadWaiter(prevtasks.get(1),() -> {
                    Thread t2 = new Thread(() -> sort(arr, pivot + 1, hi));
                    Future<?> task = executorService.submit(t2);
                    List<Future<?>> currtasks = tasks.get(currstep);
                    currtasks.add(task);
                });
                executorService.submit(wait1);
                executorService.submit(wait2);
            }

        }
    }
}
