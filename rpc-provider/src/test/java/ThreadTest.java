import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Valar Morghulis
 * @Date 2023/10/6
 */
public class ThreadTest {
    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new MyThread(), 1, 5, TimeUnit.SECONDS);

    }

    public static class MyThread extends TimerTask {

        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            System.out.println("11111");
        }
    }
}
