package hu.nyari.threadtest;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(20000);
        (new Thread()).start();
    }
}
