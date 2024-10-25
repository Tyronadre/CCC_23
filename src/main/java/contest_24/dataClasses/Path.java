package contest_24.dataClasses;

import java.util.ArrayList;
import java.util.List;

public class Path {
    public PathNode start;
    public PathNode end;

    public Path() {

    }

    public static Path createPath(Lawn lawn) {
        Position pos = new Position(0, 0);
        var p = new Path();

        //We can only zigzag if the lawn has an even width.
        //We know the border is always without trees, so we can make the lawn smaller by 1

        boolean goRight = true;
        boolean goDown = true;
        if (lawn.width % 2 != 0) {
            //go straight down until we hit the end of the lawn
            p.append(createStraight(pos, new Position(0, lawn.height - 1)));
            goRight = true;
            goDown = false;
        } else {
            // Go straight right until we hit the end of the lawn
            p.append(createStraight(pos, new Position(lawn.width - 1, 0)));
            goRight = true;
            goDown = false;

        }
        // we have an even lawn and are either in the top right corner or in the bottom left corner.
        do {
            System.out.println(pos);
            // we have a tree in this row, so we zigzag

            if (lawn.treeInRow(pos.y)) {
                // we are in the top right corner
                if (pos.x == lawn.width - 1) {
                    p.append(createZigZag(pos, new Position(0, lawn.height - 1), lawn));
                    pos = new Position(0, lawn.height - 1);
                    goRight = false;
                    goDown = false;
                } else {
                    p.append(createZigZag(pos, new Position(lawn.width - 1, 0), lawn));
                    pos = new Position(lawn.width - 1, 0);
                    goRight = true;
                    goDown = true;
                }
            } else {
                // we have no tree in this row, so we go straight

                if (goRight) {
                    pos.y++;
                    p.append(createStraight(pos, new Position(lawn.width - 1, pos.y)));
                    pos = new Position(lawn.width - 1, pos.y);
                    goRight = false;
                } else {

                    p.append(createStraight(pos, new Position(lawn.width % 2 != 0 ? 1:0, pos.y)));
                    pos = new Position(0, pos.y);
                    goRight = true;
                }
                if (goDown) pos.y++;
                else pos.y--;
            }
            if (pos.equals(new Position(0, 0))) {
                break;
            }
        } while (true);

        /*
        int x = 0;
        int y = 0;
        var p = new Path();
        boolean rowDirection = true; // true = right, false = left
        for (int yLawn = 0; yLawn < lawn.height; yLawn++) {
            boolean treeInRow = lawn.treeInRow(yLawn);

            if (treeInRow) {
                //we know the tree is in this row, and the row has uneven width
                //we do a simple zig zag approach, and skip the x where the tree is

                var startPos = new Position(x, yLawn);
                var endPos = new Position(lawn.width - x - 1, lawn.height - 1);
                var zigZag = Path.createZigZag(startPos, endPos, lawn);
                System.out.println("ZIGZAG: " + zigZag);
                p.append(zigZag);
                x = lawn.width - x - 1;
                y = lawn.height - 1;
                yLawn++;
                continue;
            }

            for (int xLawn = 0; xLawn < lawn.width; xLawn++) {
                p.append(x, y);

                if (xLawn == lawn.width - 1)
                    break;
                if (rowDirection) {
                    x++;
                } else {
                    x--;
                }

            }
            rowDirection = !rowDirection;
            y++;
        }
         */

        return p;
    }

    private static Path createStraight(Position start, Position end) {
        var p = new Path();
        Direction dir = null;
        if (start.x == end.x) {
            dir = start.y < end.y ? Direction.S : Direction.W;
        } else {
            dir = start.x < end.x ? Direction.D : Direction.A;
        }
        do {
            p.append(start.x, start.y);
            if (start.equals(end)) {
                break;
            }
            switch (dir) {
                case W -> start.y--;
                case A -> start.x--;
                case S -> start.y++;
                case D -> start.x++;
            }
        } while (true);
        return p;
    }

    private static Path createZigZag(Position startPos, Position endPos, Lawn lawn) {
        System.out.println("ZIGZAG: start: " + startPos + " end: " + endPos);
        if (startPos.x - endPos.x % 2 == 0) {
            throw new IllegalArgumentException("ZigZag must have even width");
        }
        boolean right = startPos.x < endPos.x;

        var p = new Path();
        boolean down = true;
        do {
            p.append(startPos.x, startPos.y);

            if (down) startPos.y++;
            else startPos.y--;

            //check if this position is a tree. if it is we dont go here, but we go right
            if (lawn.isTree(startPos.x, startPos.y)) {
                //revert y
                if (down) startPos.y--;
                else startPos.y++;
                startPos.x++;
            } else {
                down = !down;
                p.append(startPos.x, startPos.y);
            }


            if (startPos.equals(endPos)) {
                break;
            }
            if (right) startPos.x++;
            else startPos.x--;

        } while (true);
        return p;
    }

    public static Path createPath(String path) {
        int x = 0;
        int y = 0;
        var p = new Path();
        p.append(x, y);
        for (int c = 0; c < path.length(); c++) {
            switch (path.charAt(c)) {
                case 'W' -> y--;
                case 'A' -> x--;
                case 'S' -> y++;
                case 'D' -> x++;
            }
            p.append(x, y);
        }
        return p;
    }

    private void append(Path zigZag) {
        if (start == null) {
            start = zigZag.start;
        } else {
            end.next = zigZag.start;
        }
        end = zigZag.end;
    }

    public void append(int x, int y) {
        if (start == null) {
            start = new PathNode(x, y);
            end = start;
        } else {
            end.next = new PathNode(x, y);
            end = end.next;
        }
    }

    public int minX() {
        PathNode current = start;
        int min = 0;
        while (current != null) {
            min = Math.min(min, current.position.x);
            current = current.next;
        }
        return min;
    }

    public int minY() {
        PathNode current = start;
        int min = 0;
        while (current != null) {
            min = Math.min(min, current.position.y);
            current = current.next;
        }
        return min;
    }

    public int maxX() {
        PathNode current = start;
        int max = 0;
        while (current != null) {
            max = Math.max(max, current.position.x);
            current = current.next;
        }
        return max;
    }

    public int maxY() {
        PathNode current = start;
        int max = 0;
        while (current != null) {
            max = Math.max(max, current.position.y);
            current = current.next;
        }
        return max;
    }

    @Override
    public String toString() {
        return start.toString();
    }

    public int getWidth() {
        return maxX() - minX() + 1;
    }

    public int getHeight() {
        return maxY() - minY() + 1;
    }

    public void shift(int x, int y) {
        PathNode current = start;
        while (current != null) {
            current.position.x += x;
            current.position.y += y;
            current = current.next;
        }
    }

    /**
     * @return true, if no pos is visited twice
     */
    public boolean validate() {
        List<Position> visited = new ArrayList<>();
        PathNode current = start;
        while (current != null) {
            if (visited.contains(current.position)) {
                return false;
            }
            visited.add(current.position);
            current = current.next;
        }
        return true;
    }

    public String getPathString() {
        StringBuilder sb = new StringBuilder();
        PathNode current = start;
        while (current != null) {
            sb.append(current.direction());
            current = current.next;
        }
        return sb.toString();
    }
}
