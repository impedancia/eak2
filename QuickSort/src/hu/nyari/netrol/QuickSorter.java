package hu.nyari.netrol;

import java.io.Console;
import java.util.Random;

public class QuickSorter implements Runnable{
    long[] data;
    int start,end;
    public QuickSorter(long[] data, int start, int end) {
        this.data=data;
        this.start=start;
        this.end=end;
    }
    public void run(){
        quickSort(this.data,this.start,this.end);
    }
    public void run_par()
    {
        int s=partition(data,0,this.end);
        Thread t1=new Thread(new QuickSorter(data,0,s-1));
        Thread t2=new Thread(new QuickSorter(data,s+1,data.length-1));
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        }catch(InterruptedException e){
            System.out.println(e);
        }

    }
    static void swap(long[] data, int i, int j){
        long tmp=data[i];
        data[i]=data[j];
        data[j]=tmp;
    }
    static int partition(long[] data, int start, int end) {
        if(start==end)
            return start;
        long pivot=data[end];
        int s=start-1;
        for(int i=start;i<end;i++)
            if(data[i]<=pivot)
                swap(data,++s,i);
        swap(data,++s,end);
        return s;
    }
    static void quickSort(long[] data, int start, int end) {
        if (end<=start)
            return;
        int s=partition(data,start,end);
        quickSort(data,start,s-1);
        quickSort(data,s+1,end);
    }
    static long[] randomList(int n,int k) {
        Random random=new Random();
        long[] data=new long[n];
        for(int i=0;i<data.length;i++)
            data[i]=random.nextLong();
        return data;
    }
    public static void main(String[] args) {
        long[] data={3,1,2,8,5,6};
        quickSort(data,0,data.length-1);
        for(long i:data)
            System.console().format("%d ",i);
        System.console().format("\n");
        int n=10000000;
        data=randomList(n,1000000);
        int s=partition(data,0,n-1);
        Thread t1=new Thread(new QuickSorter(data,0,s-1));
        Thread t2=new Thread(new QuickSorter(data,s+1,data.length-1));
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        }catch(InterruptedException e){
            System.out.println(e);
        }
    }
}