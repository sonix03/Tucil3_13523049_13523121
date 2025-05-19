import java.util.List;
import javax.swing.*;

public class AnimationManager {
    private final List<Board> steps;
    private final BoardGUI panel;
    private final JSlider speedSlider;
    private SwingWorker<Void, Board> worker;
    private boolean stopped = false;
    private Runnable onFinish;

    public AnimationManager(List<Board> steps, BoardGUI panel, JSlider speedSlider) {
        this(steps, panel, speedSlider, null);
    }

    public AnimationManager(List<Board> steps, BoardGUI panel, JSlider speedSlider, Runnable onFinish) {
        this.steps = steps;
        this.panel = panel;
        this.speedSlider = speedSlider;
        this.onFinish = onFinish;
    }

    public void start() {
        stopped = false;
        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < steps.size(); i++) {
                    if (stopped) break;

                    // Kirim board ke publish
                    publish(steps.get(i));

                    // Dapatkan delay dari slider
                    int delay = speedSlider.getValue();
                    Thread.sleep(delay);
                }
                return null;
            }

            @Override
            protected void process(List<Board> chunks) {
                Board latest = chunks.get(chunks.size() - 1);
                panel.setBoard(latest);
            }

            @Override
            protected void done() {
                if (onFinish != null) onFinish.run();
            }
        };
        worker.execute();
    }

    public void stop() {
        stopped = true;
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
    }

    public List<Board> getSteps() {
        return steps;
    }
}
