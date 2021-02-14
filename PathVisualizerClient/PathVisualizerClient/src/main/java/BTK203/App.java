package BTK203;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        ClientSocketHelper helper = new ClientSocketHelper(3695);
        
        Timer timer = new Timer();
        TimerTask updateTask = new TimerTask() {
            public void run() {
                helper.update();
            }
        };
        timer.scheduleAtFixedRate(updateTask, 0, 62);
    }
}
