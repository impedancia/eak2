package hu.nyari.gol;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class ReadInput implements Runnable
{
    private static volatile boolean running = true;
    Scanner user_input = new Scanner(System.in);
    CountDownLatch[] _startLatch;
    public ReadInput(CountDownLatch[] startlatch){
        _startLatch = startlatch;
    }
    public void run()
    {
        while(running)
        {
            if (user_input.hasNext()){
                synchronized (System.out)
                {
                    String command = user_input.nextLine();
                    switch (command) {
                        case "stop":
                            //_startLatch.set( new CountDownLatch(1));
                            _startLatch[0] = new CountDownLatch(1);
                            break;
                        case "start":
                            //_startLatch.get().countDown();
                            _startLatch[0].countDown();
                            break;
                        case "halt":
                            break;
                    }
                    System.out.println("User command: " + command);                }
            }

        }
    }

    public void kill()
    {
        running = false;
        System.out.println("Thread Killed.");
    }
}