public class Move {
    public char piece;
    public char direction;
    public Board board;

    public Move(char piece, char direction, Board board) {
        this.piece = piece;
        this.direction = direction;
        this.board = board;
    }
}
