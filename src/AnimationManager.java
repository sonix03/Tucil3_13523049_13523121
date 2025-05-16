import java.util.List;
import javax.swing.*;

public class AnimationManager {
    private final List<Board> steps;
    private final BoardGUI panel;
    private int index = 0;
    private final Timer timer;

    public AnimationManager(List<Board> steps, BoardGUI panel) {
        this.steps = steps;
        this.panel = panel;
        this.timer = new Timer(500, e -> nextStep());
    }

    public void start() {
        index = 0;
        timer.start();
    }

    private void nextStep() {
        if (index >= steps.size()) {
            timer.stop();
            return;
        }
        panel.setBoard(steps.get(index++));
    }
}
