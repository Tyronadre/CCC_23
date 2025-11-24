package graphLib.gui;

import graphLib.alg.AStar;
import graphLib.alg.Algorithm;
import graphLib.base.Node;
import graphLib.graph2D.GraphGrid2D;
import graphLib.gui.event.AlgorithmChangedEvent;
import graphLib.gui.event.EventBus;
import graphLib.gui.event.Subscribe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GraphGrid2DGui extends GraphGUI {
    public static final int SIZE = 50;

    GraphGrid2D graph;
    BaseGUI<GraphGrid2D.Point> gui;
    Algorithm<GraphGrid2D.Point, ?> currentAlgorithm;

    // cap number of interesting nodes to render in fast path to avoid lag
    private static final int MAX_INTERESTING_RENDER = Integer.MAX_VALUE;

    int smallestX;
    int smallestY;
    int biggestX;
    int biggestY;
    double scale = 1;

    private static boolean DEBUG = false;

    // Pan offsets in device pixels
    double translateX = 0;
    double translateY = 0;

    // For dragging
    int lastMouseX;
    int lastMouseY;
    boolean dragging = false;

    Map<Node<GraphGrid2D.Point>, Rectangle> rectangleList = new HashMap<>();

    // Spatial index: gridX -> (gridY -> list of nodes)
    Map<Integer, Map<Integer, List<Node<GraphGrid2D.Point>>>> gridIndex = new HashMap<>();

    // QuadTree renderer helper
    private final QuadTreeRenderer quadTreeRenderer;

    public GraphGrid2DGui(GraphGrid2D graph) {
        this.gui = new BaseGUI<>(this, graph);
        this.graph = graph;
        EventBus.instance.register(this);

        calculateBaseLayout();

        // initialize quad renderer after layout/index is ready
        this.quadTreeRenderer = new QuadTreeRenderer(gridIndex, SIZE);

        // mouse listeners for pan and zoom
        MouseAdapter ma = getMouseAdapter();
        this.addMouseListener(ma);
        this.addMouseMotionListener(ma);

        this.addMouseWheelListener(e -> {
            // zoom centered on mouse position
            double oldScale = scale;
            int wheel = e.getWheelRotation(); // positive == down (zoom out)
            double factor = Math.pow(1.1, -wheel);
            double newScale = scale * factor;
            // clamp
            newScale = Math.max(0.01, Math.min(10.0, newScale));
            factor = newScale / oldScale;
            // screen coordinates of mouse
            double sx = e.getX();
            double sy = e.getY();
            // adjust translate so the point under the cursor remains fixed
            translateX = factor * translateX + (1 - factor) * sx;
            translateY = factor * translateY + (1 - factor) * sy;

            scale = newScale;
            repaint();
        });

        // make focusable and add key listener for debug toggle
        this.setFocusable(true);
        this.addKeyListener(getKeyAdapter());
    }

    private MouseAdapter getMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragging = true;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!dragging) return;
                int mx = e.getX();
                int my = e.getY();
                int dx = mx - lastMouseX;
                int dy = my - lastMouseY;
                // translate in device pixels
                translateX += dx;
                translateY += dy;
                lastMouseX = mx;
                lastMouseY = my;
                repaint();
            }
        };
    }

    @Override
    public KeyAdapter getKeyAdapter() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'd' || e.getKeyChar() == 'D') {
                    DEBUG = !DEBUG;
                    System.out.println("DEBUG: " + DEBUG);
                    repaint();
                }
            }
        };
    }

    public JPanel getCustomControls() {
        var panel = new JPanel();

        var xSLabel = new JLabel("XStart:");
        var xSText = new JTextField("0", 5);
        var ySLabel = new JLabel("YStart:");
        var ySText = new JTextField("0", 5);
        var xELabel = new JLabel("XEnd:");
        var xEText = new JTextField("0", 5);
        var yELabel = new JLabel("YEnd:");
        var yEText = new JTextField("0", 5);

        var aStar = new JButton("A*");
        aStar.addActionListener(e -> gui.setAlgorithm(new AStar<>(graph, graph.getOrNullNode(Integer.parseInt(xSText.getText()), Integer.parseInt(ySText.getText())), graph.getOrNullNode(Integer.parseInt(xEText.getText()), Integer.parseInt(yEText.getText())))));

        panel.add(Box.createVerticalStrut(10));

        panel.add(xSLabel);
        panel.add(xSText);
        panel.add(ySLabel);
        panel.add(ySText);
        panel.add(xELabel);
        panel.add(xEText);
        panel.add(yELabel);
        panel.add(yEText);

        panel.add(aStar);


        return panel;
    }

    @SuppressWarnings({"unused", "unchecked"})
    @Subscribe
    private void onAlgorithmChangedEvent(AlgorithmChangedEvent event) {
        currentAlgorithm = (Algorithm<GraphGrid2D.Point, ?>) event.getAlgorithm();
        repaint();
    }

    private void calculateBaseLayout() {
        var smallestX = Integer.MAX_VALUE;
        var smallestY = Integer.MAX_VALUE;
        var biggestX = Integer.MIN_VALUE;
        var biggestY = Integer.MIN_VALUE;

        for (var node : graph.getAllNode()) {
            var p = node.getData();
            if (p.x() < smallestX) smallestX = p.x();
            if (p.y() < smallestY) smallestY = p.y();
            if (p.x() > biggestX) biggestX = p.x();
            if (p.y() > biggestY) biggestY = p.y();
        }

        this.smallestX = smallestX;
        this.smallestY = smallestY;
        this.biggestX = biggestX;
        this.biggestY = biggestY;

        //precalculate all rectangle shapes and build spatial index
        for (var node : graph.getAllNode()) {
            var p = node.getData();
            var rec = new Rectangle((p.x() - smallestX) * SIZE, (p.y() - smallestY) * SIZE, SIZE, SIZE);
            rectangleList.put(node, rec);

            int gx = p.x() - smallestX;
            int gy = p.y() - smallestY;
            gridIndex.computeIfAbsent(gx, k -> new HashMap<>()).computeIfAbsent(gy, k -> new ArrayList<>()).add(node);
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Work on a copy so we can restore/destroy without affecting caller
        Graphics2D g2d = (Graphics2D) g.create();

        int compW = (int) (getWidth() * g2d.getTransform().getScaleX());
        int compH = (int) (getHeight() * g2d.getTransform().getScaleY());

        double s = scale <= 0 ? 1.0 : scale;

        AffineTransform at = new AffineTransform(s, 0, 0, s, translateX, translateY);
        g2d.setTransform(at);

        int viewX = (int) Math.floor((0 - translateX) / s);
        int viewY = (int) Math.floor((0 - translateY) / s);
        int viewW = (int) Math.ceil((double) compW / s);
        int viewH = (int) Math.ceil((double) compH / s);
        Rectangle unscaledView = new Rectangle(viewX, viewY, viewW, viewH);


        if (DEBUG) {
            java.awt.geom.AffineTransform tf = g2d.getTransform();
            System.out.println("[GraphGrid2DGui] comp=(" + compW + "," + compH + ") scale=" + s + " translate=(" + translateX + "," + translateY + ")");
            System.out.println("[GraphGrid2DGui] transform=" + tf + " unscaledView=" + unscaledView);
            g2d.setColor(Color.RED);
            g2d.draw(unscaledView);
            g2d.setColor(Color.black);
        }

        // If scale is very small, use the quad-tree renderer to avoid many draw calls
        if (s <= 0.25) {
            // If algorithm exposes interesting nodes, only draw those (fast path)
            if (currentAlgorithm != null) {
                Iterable<Node<GraphGrid2D.Point>> interesting = currentAlgorithm.getInterestingNodes();
                if (interesting != null) {
                    // Collect intersecting interesting nodes grouped by state
                    java.util.List<Node<GraphGrid2D.Point>> pathNodes = new java.util.ArrayList<>();
                    java.util.List<Node<GraphGrid2D.Point>> closedNodes = new java.util.ArrayList<>();
                    java.util.List<Node<GraphGrid2D.Point>> openNodes = new java.util.ArrayList<>();

                    double centerX = unscaledView.getCenterX();
                    double centerY = unscaledView.getCenterY();

                    for (Node<GraphGrid2D.Point> node : interesting) {
                        var rec = rectangleList.get(node);
                        if (rec == null) continue;
                        if (!rec.intersects(unscaledView)) continue;
                        Algorithm.NodeRenderState state = currentAlgorithm.getNodeRenderState(node);
                        switch (state) {
                            case PATH -> pathNodes.add(node);
                            case CLOSED -> closedNodes.add(node);
                            case OPEN -> openNodes.add(node);
                            default -> {} // skip DEFAULT
                        }
                    }

                    // Draw PATH nodes always
                    g2d.setColor(Color.DARK_GRAY);
                    for (var node : pathNodes) {
                        var rec = rectangleList.get(node);
                        g2d.fill(rec);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(rec.x, rec.y, Math.max(0, rec.width - 1), Math.max(0, rec.height - 1));
                        g2d.setColor(Color.DARK_GRAY);
                    }

                    // Merge closed and open lists for prioritized drawing
                    java.util.List<Node<GraphGrid2D.Point>> other = new java.util.ArrayList<>();
                    other.addAll(closedNodes);
                    other.addAll(openNodes);

                    int remaining = MAX_INTERESTING_RENDER - pathNodes.size();
                    if (remaining > 0 && !other.isEmpty()) {
                        // if too many, pick those closest to view center
                        if (other.size() > remaining) {
                            // compute distances and pick closest 'remaining'
                            java.util.List<java.util.Map.Entry<Node<GraphGrid2D.Point>, Double>> tmp = new java.util.ArrayList<>();
                            for (var node : other) {
                                var rec = rectangleList.get(node);
                                double dx = rec.getCenterX() - centerX;
                                double dy = rec.getCenterY() - centerY;
                                double d = dx * dx + dy * dy;
                                tmp.add(new java.util.AbstractMap.SimpleEntry<>(node, d));
                            }
                            tmp.sort((a, b) -> Double.compare(a.getValue(), b.getValue()));
                            for (int i = 0; i < remaining; i++) {
                                Node<GraphGrid2D.Point> node = tmp.get(i).getKey();
                                Algorithm.NodeRenderState state = currentAlgorithm.getNodeRenderState(node);
                                Color col = (state == Algorithm.NodeRenderState.CLOSED) ? Color.RED : Color.GREEN;
                                var rec = rectangleList.get(node);
                                g2d.setColor(col);
                                g2d.fill(rec);
                                g2d.setColor(Color.BLACK);
                                g2d.drawRect(rec.x, rec.y, Math.max(0, rec.width - 1), Math.max(0, rec.height - 1));
                            }
                        } else {
                            // draw all
                            for (var node : other) {
                                Algorithm.NodeRenderState state = currentAlgorithm.getNodeRenderState(node);
                                Color col = (state == Algorithm.NodeRenderState.CLOSED) ? Color.RED : Color.GREEN;
                                var rec = rectangleList.get(node);
                                g2d.setColor(col);
                                g2d.fill(rec);
                                g2d.setColor(Color.BLACK);
                                g2d.drawRect(rec.x, rec.y, Math.max(0, rec.width - 1), Math.max(0, rec.height - 1));
                            }
                        }
                    }
                    // debug overlays and return
                    if (DEBUG) {
                        Graphics2D dbg = (Graphics2D) g.create();
                        dbg.setTransform(new AffineTransform()); // device coords

                        // convert unscaledView corners to device coords
                        java.awt.geom.Point2D p1 = at.transform(new Point(unscaledView.x, unscaledView.y), null);
                        java.awt.geom.Point2D p2 = at.transform(new Point(unscaledView.x + unscaledView.width, unscaledView.y + unscaledView.height), null);
                        int dx = (int) Math.round(Math.min(p1.getX(), p2.getX()));
                        int dy = (int) Math.round(Math.min(p1.getY(), p2.getY()));
                        int dw = (int) Math.round(Math.abs(p2.getX() - p1.getX()));
                        int dh = (int) Math.round(Math.abs(p2.getY() - p1.getY()));

                        dbg.setColor(new Color(255, 0, 0, 48));
                        dbg.fillRect(dx, dy, dw, dh);

                        dbg.setColor(new Color(0, 0, 255, 48));
                        int graphW = (biggestX - smallestX + 1) * SIZE;
                        int graphH = (biggestY - smallestY + 1) * SIZE;
                        java.awt.geom.Point2D g0 = at.transform(new Point(0, 0), null);
                        dbg.fillRect((int) g0.getX(), (int) g0.getY(), (int) Math.round(graphW * s), (int) Math.round(graphH * s));

                        dbg.setColor(Color.BLACK);
                        String info = String.format("translate=(%.1f,%.1f) scale=%.3f comp=(%d,%d)", translateX, translateY, s, getWidth(), getHeight());
                        dbg.drawString(info, 10, 20);
                        dbg.dispose();
                    }

                    g2d.dispose();
                    return;
                }
            }

            quadTreeRenderer.render(g2d, unscaledView, s, currentAlgorithm);

            // draw debug overlays in device coords if requested
            if (DEBUG) {
                Graphics2D dbg = (Graphics2D) g.create();
                dbg.setTransform(new AffineTransform()); // device coords

                // convert unscaledView corners to device coords
                java.awt.geom.Point2D p1 = at.transform(new Point(unscaledView.x, unscaledView.y), null);
                java.awt.geom.Point2D p2 = at.transform(new Point(unscaledView.x + unscaledView.width, unscaledView.y + unscaledView.height), null);
                int dx = (int) Math.round(Math.min(p1.getX(), p2.getX()));
                int dy = (int) Math.round(Math.min(p1.getY(), p2.getY()));
                int dw = (int) Math.round(Math.abs(p2.getX() - p1.getX()));
                int dh = (int) Math.round(Math.abs(p2.getY() - p1.getY()));

                dbg.setColor(new Color(255, 0, 0, 48));
                dbg.fillRect(dx, dy, dw, dh);

                // graph bounds in device coords
                dbg.setColor(new Color(0, 0, 255, 48));
                int graphW = (biggestX - smallestX + 1) * SIZE;
                int graphH = (biggestY - smallestY + 1) * SIZE;
                java.awt.geom.Point2D g0 = at.transform(new Point(0, 0), null);
                dbg.fillRect((int) g0.getX(), (int) g0.getY(), (int) Math.round(graphW * s), (int) Math.round(graphH * s));

                dbg.setColor(Color.BLACK);
                String info = String.format("translate=(%.1f,%.1f) scale=%.3f comp=(%d,%d)", translateX, translateY, s, getWidth(), getHeight());
                dbg.drawString(info, 10, 20);
                dbg.dispose();
            }

            g2d.dispose();
            return;
        }

        // Otherwise draw individual cells that intersect the visible model rectangle
        int gxMin = (int) Math.floor((double) unscaledView.x / SIZE);
        int gxMax = (int) Math.floor((double) (unscaledView.x + unscaledView.width - 1) / SIZE);
        int gyMin = (int) Math.floor((double) unscaledView.y / SIZE);
        int gyMax = (int) Math.floor((double) (unscaledView.y + unscaledView.height - 1) / SIZE);

        // Build a small 2D cache of visible cell lists to avoid repeated Map.get calls in the inner loop.
        int gxRange = Math.max(0, gxMax - gxMin + 1);
        int gyRange = Math.max(0, gyMax - gyMin + 1);
        @SuppressWarnings("unchecked")
        java.util.List<Node<GraphGrid2D.Point>>[][] visibleListCache = new java.util.List[gxRange][gyRange];
        int numColumns = gridIndex.size();
        // If the visible x-range is small relative to total columns, query by index; otherwise iterate entrySet
        if (gxRange <= Math.max(4, numColumns / 4)) {
            // visible range is small -> call gridIndex.get for those gx only
            for (int gx = gxMin; gx <= gxMax; gx++) {
                var col = gridIndex.get(gx);
                int ix = gx - gxMin;
                if (col == null) continue;
                for (Map.Entry<Integer, List<Node<GraphGrid2D.Point>>> e : col.entrySet()) {
                    int gy = e.getKey();
                    if (gy < gyMin || gy > gyMax) continue;
                    visibleListCache[ix][gy - gyMin] = e.getValue();
                }
            }
        } else {
            // visible range is large relative to total columns -> iterate existing columns and fill cache for those in range
            for (Map.Entry<Integer, Map<Integer, List<Node<GraphGrid2D.Point>>>> colEntry : gridIndex.entrySet()) {
                int gx = colEntry.getKey();
                if (gx < gxMin || gx > gxMax) continue;
                Map<Integer, List<Node<GraphGrid2D.Point>>> col = colEntry.getValue();
                int ix = gx - gxMin;
                if (col == null) continue;
                for (Map.Entry<Integer, List<Node<GraphGrid2D.Point>>> e : col.entrySet()) {
                    int gy = e.getKey();
                    if (gy < gyMin || gy > gyMax) continue;
                    visibleListCache[ix][gy - gyMin] = e.getValue();
                }
            }
        }

        for (int gx = gxMin; gx <= gxMax; gx++) {
            for (int gy = gyMin; gy <= gyMax; gy++) {
                var list = visibleListCache[gx - gxMin][gy - gyMin];
                 if (list == null) continue;
                 for (var node : list) {
                     var rec = rectangleList.get(node);
                     if (rec == null) continue;

                     // Only draw if intersects the unscaledView (both in model coords)
                     if (!rec.intersects(unscaledView)) continue;

                     g2d.draw(rec);

                     if (currentAlgorithm != null) {
                         // draw algorithm overlay clipped to the cell
                         Shape oldClip = g2d.getClip();
                         g2d.setClip(rec);
                         g2d.translate(rec.x, rec.y);

                         currentAlgorithm.draw(g2d, node);

                         g2d.translate(-rec.x, -rec.y);
                         g2d.setClip(oldClip);
                     }
                 }
             }
         }

        // draw debug overlays in device coords if requested
        if (DEBUG) {
            Graphics2D dbg = (Graphics2D) g.create();
            dbg.setTransform(new AffineTransform()); // device coords

            // graph bounds
            dbg.setColor(new Color(0, 0, 255, 48));
            int graphW = (biggestX - smallestX + 1) * SIZE;
            int graphH = (biggestY - smallestY + 1) * SIZE;
            java.awt.geom.Point2D g0 = at.transform(new Point(0, 0), null);
            dbg.fillRect((int) g0.getX(), (int) g0.getY(), (int) Math.round(graphW * s), (int) Math.round(graphH * s));

            dbg.setColor(Color.BLACK);
            String info = String.format("translate=(%.1f,%.1f) scale=%.3f comp=(%d,%d)", translateX, translateY, s, getWidth(), getHeight());
            dbg.drawString(info, 10, 20);
            dbg.dispose();
        }

        g2d.dispose();
    }
}
