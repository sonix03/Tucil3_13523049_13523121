import java.util.List;
import javax.swing.*;

public class AnimationManager {
    private final List<Board> steps;
    private final BoardGUI panel;
    private int index = 0;
    private final Timer timer;
    private boolean stopped = false;
    private Runnable onFinish = null;

    public AnimationManager(List<Board> steps, BoardGUI panel) {
        this.steps = steps;
        this.panel = panel;
        this.timer = new Timer(500, e -> nextStep());
    }

    public AnimationManager(List<Board> steps, BoardGUI panel, Runnable onFinish) {
        this(steps, panel);
        this.onFinish = onFinish;
    }

    public void start() {
        index = 0;
        stopped = false;
        timer.start();
    }

    public void stop() {
        stopped = true;
        timer.stop();
    }

    private void nextStep() {
        if (stopped || index >= steps.size()) {
            timer.stop();
            if (onFinish != null) onFinish.run(); 
            return;
        }

        panel.setBoard(steps.get(index++));
    }
}
