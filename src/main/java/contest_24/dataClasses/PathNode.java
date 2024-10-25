package contest_24.dataClasses;

public class PathNode {
    Position position;
    PathNode prev;
    PathNode next;

    PathNode(int x, int y) {
        this.position = new Position(x, y);
    }

    public Direction direction(){
        if (next == null) {
            return Direction.X;
        }
        else if (position.x == next.position.x) {
            return position.y < next.position.y ? Direction.S : Direction.W;
        }
        else {
            return position.x < next.position.x ? Direction.D : Direction.A;
        }
    }

    @Override
    public String toString() {
        return position + (next == null ? "" : ">" + next);
    }
}
