package contest_25;

import util.Framework;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.Timer;
import java.awt.event.*;

public class T5Neu {
    public static void main(String[] args) {
        final int level = 5;
        for (int i = 2; i <= 2; i++) {

            var lines = Framework.readFile(level, i);
            Framework.writeOutput(level, i, solve(lines));
        }
    }

    private static String solve(List<String> lines) {
        var res = new ArrayList<String>();
        for (int i = 101; i < 122; ) {
            System.out.println("i: " + i);
            var line1 = lines.get(i++).split(" ");
            var posStation = line1[0].split(",");
            var stationX = Integer.parseInt(posStation[0]);
            var stationY = Integer.parseInt(posStation[1]);

            String line2 = lines.get(i++);
            var posAsteroid = line2.split(",");
            var asteroidX = Integer.parseInt(posAsteroid[0]);
            var asteroidY = Integer.parseInt(posAsteroid[1]);

            var minX = Math.min(stationX - 3, -3);
            var maxX = Math.max(stationX + 3, 3);
            var minY = Math.min(stationY - 3, -3);
            var maxY = Math.max(stationY + 3, 3);
            var graph = new Graph(minX, minY, maxX, maxY);


            for (int x = asteroidX - 2; x <= asteroidX + 2; x++) {
                for (int y = asteroidY - 2; y <= asteroidY + 2; y++) {
                    graph.removeNode(x, y);
                }
            }

            var startNode = graph.getNode(0, 0);
            var endNode = graph.getNode(stationX, stationY);

            new AStarVisualizer(graph, startNode, endNode);
//            var path = Pathfinding.findPath(graph, startNode, endNode);
//            GraphVisualizer.show(graph,path);
//            System.out.println(path.stream().map(it -> "(" + it.x() + "," + it.y() + ")").collect(Collectors.joining("->")));
//
//            var tPath = transformPath(path);
//
//            res.add(tPath);
//            System.out.println(tPath);

        }


        return String.join("\n", res);
    }


    /*
    We have a List of 2D Nodes.
    From this path we want to create two lists with the following meaning:
    Each lists represent the pace of an object in x and y direction respectively.
    The object can have the following paces:
    0 (doenst move for one time unit)
    5 (moves one space in 5 time units)
    4 (moves one space in 4 time units)
    and so on up to 1 (moves 1 in 1 time unit)
    The object can only accelerate and decelerate by 1 unit at a time. if the object is stationary (pace 0) the next pace will be 5 (slow moving), then 4 etc. up to 1.
    If it moves in the other direction we need to use negative paces so after 0 comes -5, then -4 up to -1.
    Pay attention that the points of the given list are not points in time, but just points in space that the object will
    visit in this order. So if we have the nodes (0,0), (0,1) and then (1,1) the x list will be
    0 5 0
    and the y list will be
    0 0 0 0 0 0 5 0
    because the y needs to 'wait' until the x movement is finished. ofc. the ship can also move diagonaly, if that is
    possible with the given points.
     */

    public static String transformPath(List<Node> path) {
        var xList = new ArrayList<Integer>();
        var yList = new ArrayList<Integer>();
        xList.add(0);
        yList.add(0);

        // sammel wie lange wir in die selbe richtung gehen

        int index = 0;
        var node = path.get(index++);
        var nextNode = path.get(index);
        while (index < path.size()) {


            var isX = nextNode.x() - node.x();
            var isY = nextNode.y() - node.y();

            if (isX != 0) {
                var negative = isX < 0 ? -1 : 1;
                var size = 1;
                while (true) {
                    node = nextNode;
                    if (++index >= path.size()) break;
                    nextNode = path.get(index);
                    if (nextNode.x() == node.x()) break;
                    else size++;
                }

                buildLists((size) * negative, xList, yList);
            }

            if (isY != 0) {
                var negative = isY < 0 ? -1 : 1;
                var size = 1;
                while (true) {
                    node = nextNode;
                    if (++index >= path.size()) break;
                    nextNode = path.get(index);
                    if (nextNode.y() == node.y()) break;
                    else size++;
                }
                buildLists((size) * negative , yList, xList);
            }
        }

        xList.add(0);
        yList.add(0);

        return String.join(" ", xList.stream().map(String::valueOf).toList()) +
            "\n" +
            String.join(" ", yList.stream().map(String::valueOf).toList()) +
            "\n";
    }

    public static void buildLists(int n, List<Integer> xList, List<Integer> yList) {

        if (n == 0) {
            xList.add(0);
            yList.add(0);
            return;
        }

        var distance = Math.abs(n);
        var direction = n < 0 ? -1 : 1;
        var pace = 5;
        while (pace > 0) {
            if (6 - pace == distance) {
                xList.add(pace * direction);
                pace++;
                if (pace == 6) pace = 0;
                distance--;
                continue;
            }
            // if we can accelerate we need to have at least 2 more move spots
            if (6 - pace < distance - 1) {
                xList.add(pace * direction);
                if (pace != 1) pace--;
                distance--;
                continue;
            }

            xList.add(pace * direction);
            distance--;
        }

        // yList erhält 'sum' viele 0en
        for (int i = 0; i < xList.stream().mapToInt(Math::abs).sum(); i++) {
            yList.add(0);
        }
    }


}


class Pathfinding {

    private static Map<Node, Integer> gScore;
    private static Map<Node, Integer> fScore;

    public static List<Node> findPath(Graph graph, Node start, Node goal) {
        // A* open/closed sets
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> fScore.getOrDefault(n, Integer.MAX_VALUE)));
        Set<Node> closed = new HashSet<>();

        Map<Node, Node> cameFrom = new HashMap<>();
        gScore = new HashMap<>();
        fScore = new HashMap<>();

        gScore.put(start, 0);
        fScore.put(start, heuristic(start, goal));

        open.add(start);

        while (!open.isEmpty()) {
            Node current = open.poll();

            if (current.equals(goal)) {
                return reconstructPath(cameFrom, current);
            }

            closed.add(current);

            for (Node neighbor : getNeighbors(graph, current)) {
                if (closed.contains(neighbor))
                    continue;

                int cost = 1;

                // prüfen, ob Richtungswechsel
                if (isTurn(current, neighbor, cameFrom)) {
                    cost += 100;
                }

                int tentativeG = gScore.get(current) + cost;

                if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    fScore.put(neighbor, tentativeG + heuristic(neighbor, goal));

                    if (!open.contains(neighbor))
                        open.add(neighbor);
                }
            }
        }

        return List.of(); // no path found
    }

    // Manhattan distance for grids
    private static int heuristic(Node a, Node b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    // prüft, ob die Bewegung eine neue Richtung einführt
    private static boolean isTurn(Node current, Node neighbor, Map<Node, Node> cameFrom) {

        Node prev = cameFrom.get(current);
        if (prev == null)
            return false; // Startknoten → keine Richtung vorhanden

        int dx1 = current.x() - prev.x();
        int dy1 = current.y() - prev.y();

        int dx2 = neighbor.x() - current.x();
        int dy2 = neighbor.y() - current.y();

        return !(dx1 == dx2 && dy1 == dy2);
    }

    // 4-Nachbarn
    private static List<Node> getNeighbors(Graph graph, Node node) {
        List<Node> neighbors = new ArrayList<>();

        int[][] dirs = {
            {1, 0}, {-1, 0},
            {0, 1}, {0, -1}
        };

        for (int[] d : dirs) {
            int nx = node.x() + d[0];
            int ny = node.y() + d[1];

            graph.nodes.stream()
                .filter(n -> n.x() == nx && n.y() == ny)
                .findFirst()
                .ifPresent(neighbors::add);
        }

        return neighbors;
    }

    private static List<Node> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        List<Node> path = new ArrayList<>();
        path.add(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }
}


 class AStarVisualizer {

    // ------- Konfiguration -------
    private static final int CELL_SIZE = 48;
    private static final int GRID_W = 20;
    private static final int GRID_H = 14;

    private final Graph graph;
    private final JFrame frame;
    private final GridPanel gridPanel;
    private final ControlPanel controlPanel;
    private final AStarStepper stepper;
    private Timer autoTimer;

    public AStarVisualizer(Graph graph, Node start, Node goal) {
        this.graph = graph;
        this.stepper = new AStarStepper(graph, start, goal);

        frame = new JFrame("A* Visualizer (turn-penalty)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gridPanel = new GridPanel();
        controlPanel = new ControlPanel();

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ---------- Grid drawing ----------
    private class GridPanel extends JPanel {
        GridPanel() {
            setPreferredSize(new Dimension(GRID_W * CELL_SIZE, GRID_H * CELL_SIZE));
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    // Optional: klick zum setzen von start/goal (nicht implementiert automatisch)
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // draw cells
            for (Node n : graph.nodes) {
                int x = n.x();
                int y = n.y();
                int px = x * CELL_SIZE;
                int py = y * CELL_SIZE;

                // background color depending on state
                if (stepper.isOnPath(n)) {
                    g.setColor(new Color(102, 178, 255)); // blau path
                } else if (stepper.isCurrent(n)) {
                    g.setColor(new Color(255, 165, 0)); // orange current
                } else if (stepper.isClosed(n)) {
                    g.setColor(new Color(220, 220, 220)); // light gray closed
                } else if (stepper.isOpen(n)) {
                    g.setColor(new Color(180, 255, 180)); // green open
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(px, py, CELL_SIZE, CELL_SIZE);

                // cell border
                g.setColor(Color.GRAY);
                g.drawRect(px, py, CELL_SIZE, CELL_SIZE);

                // draw start/goal markers
                if (n.equals(stepper.start)) {
                    g.setColor(Color.MAGENTA);
                    g.fillOval(px + 6, py + 6, CELL_SIZE - 12, CELL_SIZE - 12);
                } else if (n.equals(stepper.goal)) {
                    g.setColor(Color.RED.darker());
                    g.fillOval(px + 6, py + 6, CELL_SIZE - 12, CELL_SIZE - 12);
                }

                // draw g / f text
                Integer gVal = stepper.gScore.get(n);
                Integer fVal = stepper.fScore.get(n);

                String left = (gVal == null) ? "-" : Integer.toString(gVal);
                String right = (fVal == null) ? "-" : Integer.toString(fVal);

                g.setColor(Color.BLACK);
                FontMetrics fm = g.getFontMetrics();
                String text = left + " / " + right;
                int tw = fm.stringWidth(text);
                g.drawString(text, px + (CELL_SIZE - tw) / 2, py + CELL_SIZE / 2 + fm.getAscent() / 2);
            }
        }
    }

    // ---------- Controls ----------
    private class ControlPanel extends JPanel {
        JButton stepBtn = new JButton("Step");
        JButton runBtn = new JButton("Run");
        JButton stopBtn = new JButton("Stop");
        JButton resetBtn = new JButton("Reset");
        JLabel info = new JLabel("Ready");

        ControlPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(stepBtn);
            add(runBtn);
            add(stopBtn);
            add(resetBtn);
            add(info);

            stepBtn.addActionListener(e -> {
                boolean finished = stepper.step();
                gridPanel.repaint();
                info.setText(stepper.statusMessage());
                if (finished) {
                    info.setText("Finished: " + stepper.statusMessage());
                }
            });

            runBtn.addActionListener(e -> {
                if (autoTimer != null && autoTimer.isRunning()) return;
                autoTimer = new Timer(120, evt -> {
                    boolean finished = stepper.step();
                    gridPanel.repaint();
                    info.setText(stepper.statusMessage());
                    if (finished) {
                        autoTimer.stop();
                        info.setText("Finished: " + stepper.statusMessage());
                    }
                });
                autoTimer.start();
            });

            stopBtn.addActionListener(e -> {
                if (autoTimer != null) autoTimer.stop();
                info.setText("Stopped");
            });

            resetBtn.addActionListener(e -> {
                if (autoTimer != null) autoTimer.stop();
                stepper.reset();
                gridPanel.repaint();
                info.setText("Reset");
            });
        }
    }

    // ---------- A* Stepper ----------
    private static class AStarStepper {

        private final Graph graph;
        private final Node start;
        private final Node goal;

        // algorithm state
        private final PriorityQueue<Node> openQueue;
        private final Set<Node> openSet;
        private final Set<Node> closedSet;
        private final Map<Node, Node> cameFrom;
        private Map<Node, Integer> gScore = Map.of();
        private Map<Node, Integer> fScore = Map.of();

        private Node current;
        private boolean finished = false;

        private static final int TURN_PENALTY = 5;

        AStarStepper(Graph graph, Node start, Node goal) {
            this.graph = graph;
            this.start = start;
            this.goal = goal;
            this.openSet = new HashSet<>();
            this.closedSet = new HashSet<>();
            this.cameFrom = new HashMap<>();
            this.gScore = new HashMap<>();
            this.fScore = new HashMap<>();

            Comparator<Node> comp = Comparator.comparingInt(n -> fScore.getOrDefault(n, Integer.MAX_VALUE));
            this.openQueue = new PriorityQueue<>(comp);

            init();
        }

        void init() {
            openQueue.clear();
            openSet.clear();
            closedSet.clear();
            cameFrom.clear();
            gScore.clear();
            fScore.clear();
            current = null;
            finished = false;

            gScore.put(start, 0);
            fScore.put(start, heuristic(start, goal));
            openQueue.add(start);
            openSet.add(start);
        }

        void reset() {
            init();
        }

        // A single expansion step. Returns true if finished (found path or no path)
        boolean step() {
            if (finished) return true;

            // pop next from queue (skip stale/closed elements)
            Node node = null;
            while (!openQueue.isEmpty()) {
                Node n = openQueue.poll();
                if (closedSet.contains(n)) continue;
                // ensure it's still in openSet
                if (!openSet.contains(n)) continue;
                node = n;
                break;
            }

            if (node == null) {
                // no path
                finished = true;
                current = null;
                return true;
            }

            current = node;
            // remove from open set, add to closed
            openSet.remove(current);
            closedSet.add(current);

            // if goal reached -> finished
            if (current.equals(goal)) {
                finished = true;
                return true;
            }

            // expand neighbors (4-directional)
            for (Node neighbor : getNeighbors(graph, current)) {
                if (closedSet.contains(neighbor)) continue;

                // compute tentative g with turn penalty
                int moveCost = 1; // base move cost
                if (isTurn(current, neighbor)) moveCost += TURN_PENALTY;

                int tentativeG = gScore.getOrDefault(current, Integer.MAX_VALUE) + moveCost;

                if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    fScore.put(neighbor, tentativeG + heuristic(neighbor, goal));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                        openQueue.add(neighbor);
                    } else {
                        // re-add so priority queue ordering will consider new f (duplicates allowed)
                        openQueue.add(neighbor);
                    }
                }
            }

            return false;
        }

        // Helpers for visualization
        boolean isOpen(Node n) {
            return openSet.contains(n);
        }

        boolean isClosed(Node n) {
            return closedSet.contains(n);
        }

        boolean isCurrent(Node n) {
            return n.equals(current);
        }

        boolean isOnPath(Node n) {
            if (!finished) return false;
            Node cur = goal;
            while (cur != null && cameFrom.containsKey(cur)) {
                if (cur.equals(n)) return true;
                cur = cameFrom.get(cur);
            }
            return start.equals(n);
        }

        String statusMessage() {
            if (finished) {
                if (cameFrom.containsKey(goal) || start.equals(goal)) return "Path found";
                else return "No path";
            } else {
                return "Open: " + openSet.size() + " Closed: " + closedSet.size();
            }
        }

        // expose maps for drawing
        Map<Node, Integer> gScore() { return gScore; }
        Map<Node, Integer> fScore() { return fScore; }

        // utility access for GridPanel
        final Map<Node, Integer> gScorePublic = gScore;
        final Map<Node, Integer> fScorePublic = fScore;

        // determine if move from current->neighbor is a turn compared to how we entered current
        private boolean isTurn(Node current, Node neighbor) {
            Node prev = cameFrom.get(current);
            if (prev == null) return false;
            int dx1 = current.x() - prev.x();
            int dy1 = current.y() - prev.y();
            int dx2 = neighbor.x() - current.x();
            int dy2 = neighbor.y() - current.y();
            return !(dx1 == dx2 && dy1 == dy2);
        }

        // Manhattan heuristic
        private int heuristic(Node a, Node b) {
            return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
        }

        // 4-neighbors lookup in graph
        private List<Node> getNeighbors(Graph graph, Node node) {
            List<Node> neighbors = new ArrayList<>();
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = node.x() + d[0];
                int ny = node.y() + d[1];
                graph.nodes.stream()
                    .filter(n -> n.x() == nx && n.y() == ny)
                    .findFirst()
                    .ifPresent(neighbors::add);
            }
            return neighbors;
        }
    }

    // ---------- Accessors for painting ----------
    private boolean isOnPath(Node n) { return stepper.isOnPath(n); }
    private boolean isCurrent(Node n) { return stepper.isCurrent(n); }
    private boolean isClosed(Node n) { return stepper.isClosed(n); }
    private boolean isOpen(Node n) { return stepper.isOpen(n); }

    // ---------- main ----------
    public static void main(String[] args) {
        // build grid graph 0..GRID_W-1, 0..GRID_H-1
        Graph g = new Graph(0,0, GRID_W, GRID_H);

        Node start = new Node(1, 1);
        Node goal  = new Node(17, 10);

        // ensure start/goal exist in graph
        final boolean okStart = g.nodes.stream().anyMatch(n -> n.x()==start.x() && n.y()==start.y());
        final boolean okGoal = g.nodes.stream().anyMatch(n -> n.x()==goal.x() && n.y()==goal.y());
        if (!okStart || !okGoal) {
            System.err.println("Start or goal outside grid");
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new AStarVisualizer(g, start, goal));
    }
}



class GraphVisualizer extends JPanel {

    private final Graph graph;
    private final int cellSize;
    private List<Node> path;

    public GraphVisualizer(Graph graph, int cellSize) {
        this.graph = graph;
        this.cellSize = cellSize;
        this.path = List.of();
        setPreferredSize(computeSize(graph, cellSize));
    }

    private static Dimension computeSize(Graph graph, int cellSize) {
        int maxX = graph.nodes.stream().mapToInt(Node::x).max().orElse(0);
        int maxY = graph.nodes.stream().mapToInt(Node::y).max().orElse(0);
        return new Dimension((maxX + 1) * cellSize, (maxY + 1) * cellSize);
    }

    // Create window with the visualizer
    public static void show(Graph graph, List<Node> path) {
        JFrame f = new JFrame("Graph Visualizer");
        GraphVisualizer vis = new GraphVisualizer(graph, 10);
        vis.setPath(path);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new JScrollPane(vis));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public void setPath(List<Node> path) {
        this.path = path;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var minX = Math.abs(graph.getMinX());
        var minY = Math.abs(graph.getMinY());

        // Draw nodes
        for (Node n : graph.nodes) {
            int px = (n.x() + minX)* cellSize;
            int py = (n.y() + minY) * cellSize;

            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(px, py, cellSize, cellSize);

            g.setColor(Color.GRAY);
            g.drawRect(px, py, cellSize, cellSize);
        }

        // Draw path
        if (path != null) {
            g.setColor(Color.RED);
            for (Node n : path) {
                int px = (n.x() + minX) * cellSize;
                int py = (n.y() + minY) * cellSize;
                g.fillRect(px, py, cellSize, cellSize);
            }
        }
    }
}


class Graph {
    public List<Node> nodes = new ArrayList<>();

    public Graph(int minX, int minY, int maxX, int maxY) {
        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                nodes.add(new Node(i, j));
            }
        }
    }

    public void removeNode(int x, int y) {
        nodes.removeIf(it -> it.x() == x && it.y() == y);
    }

    public Node getNode(int x, int y) {
        return nodes.stream().filter(e -> e.x() == x && e.y() == y).findFirst().orElse(null);
    }

    public int getMinX() {
        return nodes.stream().mapToInt(Node::x).min().orElse(0);
    }
    public int getMinY() {
        return nodes.stream().mapToInt(Node::y).min().orElse(0);
    }
}


record Node(int x, int y) implements Comparable<Node> {

    @Override
    public int compareTo(Node o) {
        return Integer.compare(x, o.x);
    }
}
