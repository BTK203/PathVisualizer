package BTK203;

import java.util.Scanner;
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
        ServerSocketHelper helper = new ServerSocketHelper(3695);
        Point2D position = new Point2D(0, 0, 0);
        
        Timer timer = new Timer();
        TimerTask updateTask = new TimerTask() {
            public void run() {
                helper.update();
            }
        };
        timer.scheduleAtFixedRate(updateTask, 0, 10);

        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("You can:");
            System.out.println("1: Change Position");
            System.out.println("2: Transfer a path");
            System.out.println("3: Exit");
            System.out.println("What would you like to do? ");
            String action = scanner.nextLine();
            
            if(action.equals("1")) {
                System.out.println("Enter new position in the format \"[x],[y],[heading]\"");
                String newPosition = scanner.nextLine();
                position = Point2D.fromString(newPosition);
                helper.setRobotPosition(position);
                System.out.println("position updated to " + helper.getRobotPosition().toString());
                continue;
            }

            if(action.equals("2")) {
                System.out.println("transfer path");
                continue;
            }

            if(action.equals("3")) {
                System.out.println("exit");
                break;
            }
        }

        timer.cancel();
        scanner.close();
        return;
    }
}
