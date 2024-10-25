package contest_24.dataClasses;

public class Position {
    int x;
    int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "|" + y + ")";
    }

    @Override
    public int hashCode() {
        return x * 1234 + y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position other) {
            return this.x == other.x && this.y == other.y;
        }
        return false;
    }
}
