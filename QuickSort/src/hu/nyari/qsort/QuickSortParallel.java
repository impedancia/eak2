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
    public void sort_par(long[] array){
          sort(array,0,array.length-1);
    }

    @Override
    protected void sort( long[] arr, int lo, int hi ){
        assert arr != null && 0 <= lo && hi < arr.length && -1 <= hi - lo;
        if( lo < hi ){  // there are at least two elements to sort
            final int pivot = split( arr, lo, hi );
            assert lo <= pivot && pivot <= hi;
            Thread t1 = new Thread(() ->sort( arr, lo,      pivot-1 ));
            Thread t2 = new Thread(() ->sort( arr, pivot+1, hi      ));
            t1.start();
            t2.start();
            try {
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
