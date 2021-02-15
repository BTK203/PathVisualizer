package BTK203;

/**
 * Hello world!
 */
public final class App {
    private static final PathVisualizerManager manager = new PathVisualizerManager();

    private App() {
    }

    /**
     * Starts PathVisualizer
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {        
        System.out.println("Starting...");
        manager.start();
    }

    /**
     * Returns the program's GUI
     */
    public static PathVisualizerManager getManager() {
        return manager;
    }
}
