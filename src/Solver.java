import java.util.List;

public interface Solver {
    void solve(Board start);

    public List<Board> solveAndReturnPath(Board start);
}