package hu.nyari.qsort;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ThreadWaiter implements Runnable {

    Future<?> _task;
    Runnable _continueWith;
    public ThreadWaiter(Future<?> task, Runnable continueWith){
        _task = task;
        _continueWith = continueWith;
    }

    @Override
    public void run() {
        try {
            _task.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            _continueWith.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
