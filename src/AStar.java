public class AStar implements Solver {
    int heuristic;

    public AStar(int heuristic) {
        this.heuristic = heuristic;
    }

    public void solve(Board start) {
        System.out.println("[A* belum diimplementasi lengkap, heuristic " + heuristic + "]");
    }
}