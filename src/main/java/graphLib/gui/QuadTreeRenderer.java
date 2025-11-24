package graphLib.gui;

import graphLib.base.Node;
import graphLib.graph2D.GraphGrid2D;
import graphLib.alg.Algorithm;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class QuadTreeRenderer {
    private final Map<Integer, Map<Integer, List<Node<GraphGrid2D.Point>>>> gridIndex;
    private final int nodeSizePx;

    // Renderer-side cache for finished algorithm state -> maps node id to precomputed NodeRenderInfo
    private Algorithm<GraphGrid2D.Point, ?> cachedFinishedAlgorithm = null;
    private java.util.Map<Integer, NodeRenderInfo> finishedRenderInfoCache = null;

    public QuadTreeRenderer(Map<Integer, Map<Integer, List<Node<GraphGrid2D.Point>>>> gridIndex, int nodeSizePx) {
        this.gridIndex = gridIndex;
        this.nodeSizePx = nodeSizePx;
    }

    public void render(Graphics2D g2d, Rectangle unscaledView, double scale, Algorithm<GraphGrid2D.Point, ?> currentAlgorithm) {
        int gxMin = (int) Math.floor((double) unscaledView.x / nodeSizePx);
        int gxMax = (int) Math.floor((double) (unscaledView.x + unscaledView.width - 1) / nodeSizePx);
        int gyMin = (int) Math.floor((double) unscaledView.y / nodeSizePx);
        int gyMax = (int) Math.floor((double) (unscaledView.y + unscaledView.height - 1) / nodeSizePx);

        // keep or build renderer-side finished cache when the algorithm finished
        if (currentAlgorithm == null) {
            cachedFinishedAlgorithm = null;
            finishedRenderInfoCache = null;
        } else if (currentAlgorithm.getResult() != null && currentAlgorithm.getResult().isDone()) {
            if (finishedRenderInfoCache == null || cachedFinishedAlgorithm != currentAlgorithm) {
                buildFinishedRenderCache(currentAlgorithm);
                cachedFinishedAlgorithm = currentAlgorithm;
            }
        } else {
            // algorithm not finished - drop any finished cache
            cachedFinishedAlgorithm = null;
            finishedRenderInfoCache = null;
        }

        // Fast limited-scan mode: if algorithm does not expose interesting nodes, scan the grid
        // only until we found a bounded number of interesting nodes to render. This avoids
        // scanning the whole grid when zoomed out and many nodes are DEFAULT.
        final int MAX_DRAW_NODES = 1000;
        if (currentAlgorithm != null && currentAlgorithm.getInterestingNodes() == null) {
            java.util.List<int[]> toDraw = new java.util.ArrayList<>(Math.min(MAX_DRAW_NODES, 128));
            // iterate only existing columns and rows
            outerScan:
            for (Map.Entry<Integer, Map<Integer, List<Node<GraphGrid2D.Point>>>> colEntry : gridIndex.entrySet()) {
                int gx = colEntry.getKey();
                if (gx < gxMin || gx > gxMax) continue;
                var col = colEntry.getValue();
                if (col == null) continue;
                for (Map.Entry<Integer, List<Node<GraphGrid2D.Point>>> rowEntry : col.entrySet()) {
                    int gy = rowEntry.getKey();
                    if (gy < gyMin || gy > gyMax) continue;
                    List<Node<GraphGrid2D.Point>> list = rowEntry.getValue();
                    if (list == null || list.isEmpty()) continue;
                    for (var node : list) {
                        NodeRenderInfo info = getNodeRenderInfo(node, currentAlgorithm);
                        // consider non-default or path/high-priority nodes as interesting
                        if (info.priority > 10 || info.isPath) {
                            toDraw.add(new int[]{gx, gy, info.color.getRGB()});
                            if (toDraw.size() >= MAX_DRAW_NODES) break outerScan;
                        }
                    }
                }
            }
            if (!toDraw.isEmpty()) {
                for (int[] e : toDraw) {
                    int gx = e[0];
                    int gy = e[1];
                    Color c = new Color(e[2]);
                    g2d.setColor(c);
                    int x = gx * nodeSizePx;
                    int y = gy * nodeSizePx;
                    g2d.fillRect(x, y, nodeSizePx, nodeSizePx);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, Math.max(0, nodeSizePx - 1), Math.max(0, nodeSizePx - 1));
                }
                return;
            }
        }

        final double targetBlockPx = 200.0; // heuristic
        double nodePixel = nodeSizePx * scale; // pixels per node at current scale
        int nodesPerBlock = (int) Math.max(1, Math.ceil(targetBlockPx / Math.max(1.0, nodePixel)));

        int bxMin = (int) Math.floor((double) gxMin / nodesPerBlock);
        int bxMax = (int) Math.floor((double) gxMax / nodesPerBlock);
        int byMin = (int) Math.floor((double) gyMin / nodesPerBlock);
        int byMax = (int) Math.floor((double) gyMax / nodesPerBlock);

        // Build a lightweight cell cache (best NodeRenderInfo per visible cell) so recursion only reads small objects.
        int gxRange = Math.max(0, gxMax - gxMin + 1);
        int gyRange = Math.max(0, gyMax - gyMin + 1);
        NodeRenderInfo[][] cellCache = new NodeRenderInfo[gxRange][gyRange];
        // iterate only actual columns present in gridIndex to avoid repeated gridIndex.get(gx) for missing columns
        for (Map.Entry<Integer, Map<Integer, List<Node<GraphGrid2D.Point>>>> colEntry : gridIndex.entrySet()) {
            int gx = colEntry.getKey();
            if (gx < gxMin || gx > gxMax) continue;
            Map<Integer, List<Node<GraphGrid2D.Point>>> col = colEntry.getValue();
            int ix = gx - gxMin;
            if (col == null) continue;
            // iterate only present rows in this column to avoid repeated Map.get for missing keys
            for (Map.Entry<Integer, List<Node<GraphGrid2D.Point>>> e : col.entrySet()) {
                int gy = e.getKey();
                if (gy < gyMin || gy > gyMax) continue;
                List<Node<GraphGrid2D.Point>> list = e.getValue();
                if (list == null || list.isEmpty()) continue;
                // pick best NodeRenderInfo by priority for this cell
                NodeRenderInfo best = null;
                for (var node : list) {
                    NodeRenderInfo info = getNodeRenderInfo(node, currentAlgorithm);
                    if (best == null || info.priority > best.priority) best = info;
                }
                cellCache[ix][gy - gyMin] = best;
            }
        }

        for (int bx = bxMin; bx <= bxMax; bx++) {
            for (int by = byMin; by <= byMax; by++) {
                int startNodeX = bx * nodesPerBlock;
                int startNodeY = by * nodesPerBlock;
                if (startNodeX > gxMax || startNodeY > gyMax) continue;
                int maxSizeX = gxMax - startNodeX + 1;
                int maxSizeY = gyMax - startNodeY + 1;
                int actualSize = Math.min(nodesPerBlock, Math.max(1, Math.min(maxSizeX, maxSizeY)));
                drawQuadtreeBlock(g2d, startNodeX, startNodeY, actualSize, gxMin, gxMax, gyMin, gyMax, cellCache, gxMin, gyMin);
            }
        }
    }

    private static class NodeRenderInfo {
        Color color;
        int priority;
        boolean isPath;

        NodeRenderInfo(Color color, int priority, boolean isPath) {
            this.color = color;
            this.priority = priority;
            this.isPath = isPath;
        }
    }

    private NodeRenderInfo getNodeRenderInfo(Node<GraphGrid2D.Point> node, Algorithm<GraphGrid2D.Point, ?> currentAlgorithm) {
        // if we have a finished-cache, prefer that and avoid recomputing
        if (finishedRenderInfoCache != null) {
            NodeRenderInfo cached = finishedRenderInfoCache.get(node.getId());
            if (cached != null) return cached;
            // fallthrough: if node wasn't present in gridIndex when cache was built, compute on the fly
        }

        // defer defaults to switch to avoid duplicate assignment warnings
        Color colForNode;
        int priority;
        boolean isPath = false;

        if (currentAlgorithm == null) return new NodeRenderInfo(Color.LIGHT_GRAY, 10, false);

        Algorithm.NodeRenderState state = currentAlgorithm.getNodeRenderState(node);
        switch (state) {
            case PATH:
                colForNode = Color.DARK_GRAY;
                priority = 40;
                isPath = true;
                break;
            case CLOSED:
                colForNode = Color.RED;
                priority = 30;
                break;
            case OPEN:
                colForNode = Color.GREEN;
                priority = 20;
                break;
            case DEFAULT:
            default:
                colForNode = Color.LIGHT_GRAY;
                priority = 10;
                break;
        }

        return new NodeRenderInfo(colForNode, priority, isPath);
    }

    private void buildFinishedRenderCache(Algorithm<GraphGrid2D.Point, ?> algo) {
        java.util.Map<Integer, NodeRenderInfo> map = new java.util.HashMap<>();
        for (Map.Entry<Integer, Map<Integer, List<Node<GraphGrid2D.Point>>>> colEntry : gridIndex.entrySet()) {
            Map<Integer, List<Node<GraphGrid2D.Point>>> col = colEntry.getValue();
            if (col == null) continue;
            for (Map.Entry<Integer, List<Node<GraphGrid2D.Point>>> rowEntry : col.entrySet()) {
                List<Node<GraphGrid2D.Point>> list = rowEntry.getValue();
                if (list == null) continue;
                for (Node<GraphGrid2D.Point> node : list) {
                    // compute node render info using the algorithm's (now cached) node render state
                    Algorithm.NodeRenderState state = algo.getNodeRenderState(node);
                    Color colForNode;
                    int priority;
                    boolean isPath = false;
                    switch (state) {
                        case PATH:
                            colForNode = Color.DARK_GRAY;
                            priority = 40;
                            isPath = true;
                            break;
                        case CLOSED:
                            colForNode = Color.RED;
                            priority = 30;
                            break;
                        case OPEN:
                            colForNode = Color.GREEN;
                            priority = 20;
                            break;
                        case DEFAULT:
                        default:
                            colForNode = Color.LIGHT_GRAY;
                            priority = 10;
                            break;
                    }
                    map.put(node.getId(), new NodeRenderInfo(colForNode, priority, isPath));
                }
            }
        }
        finishedRenderInfoCache = map;
    }

    private void drawQuadtreeBlock(Graphics2D g2d, int startNodeX, int startNodeY, int sizeNodes,
                                   int gxMin, int gxMax, int gyMin, int gyMax,
                                   NodeRenderInfo[][] cellCache, int gxBase, int gyBase) {
          Color firstColor = null;
          boolean homogeneous = true;
          boolean containsPath = false;

          int endX = startNodeX + sizeNodes - 1;
          int endY = startNodeY + sizeNodes - 1;

         int gxLen = (cellCache == null) ? 0 : cellCache.length;
         int gyLen = (gxLen == 0) ? 0 : cellCache[0].length;

         outer:
         for (int gx = startNodeX; gx <= endX; gx++) {
             // direct lookup from cellCache (fast array access) when in visible range
             int idxX = gx - gxBase;
             for (int gy = startNodeY; gy <= endY; gy++) {
                 int idxY = gy - gyBase;
                 NodeRenderInfo info = null;
                 if (idxX >= 0 && idxX < gxLen && idxY >= 0 && idxY < gyLen) info = cellCache[idxX][idxY];
                 // info will be non-null only if this cell had a NodeRenderInfo in the cache
                 if (info == null) {
                     if (firstColor == null) firstColor = Color.LIGHT_GRAY;
                     else if (!firstColor.equals(Color.LIGHT_GRAY)) { homogeneous = false; break outer; }
                     continue;
                 }
                 if (firstColor == null) firstColor = info.color;
                 else if (!firstColor.equals(info.color)) { homogeneous = false; containsPath = containsPath || info.isPath; break outer; }
                 containsPath = containsPath || info.isPath;
             }
         }

         if (homogeneous && !containsPath) {
             g2d.setColor(firstColor != null ? firstColor : Color.LIGHT_GRAY);
             // Clip drawing to the visible grid bounds so partial blocks at the viewport edge are rendered correctly
             int drawStartX = Math.max(startNodeX, gxMin);
             int drawEndX = Math.min(endX, gxMax);
             int drawStartY = Math.max(startNodeY, gyMin);
             int drawEndY = Math.min(endY, gyMax);

             if (drawStartX <= drawEndX && drawStartY <= drawEndY) {
                 int x = drawStartX * nodeSizePx;
                 int y = drawStartY * nodeSizePx;
                 int w = (drawEndX - drawStartX + 1) * nodeSizePx;
                 int h = (drawEndY - drawStartY + 1) * nodeSizePx;
                 g2d.fillRect(x, y, w, h);
             }
             // draw a thin border so adjacent blocks with same color remain visually separate
             Color old = g2d.getColor();
             g2d.setColor(Color.BLACK);
             // draw border around the clipped area if any
             if (drawStartX <= drawEndX && drawStartY <= drawEndY) {
                 int bx = drawStartX * nodeSizePx;
                 int by = drawStartY * nodeSizePx;
                 int bw = (drawEndX - drawStartX + 1) * nodeSizePx;
                 int bh = (drawEndY - drawStartY + 1) * nodeSizePx;
                 g2d.drawRect(bx, by, Math.max(0, bw - 1), Math.max(0, bh - 1));
             }
             g2d.setColor(old);
             return;
         }

         if (sizeNodes <= 1) {
             // skip single cells outside the view bounds
             if (startNodeX < gxMin || startNodeX > gxMax || startNodeY < gyMin || startNodeY > gyMax) return;

            // use precomputed cellCache entry (fast) if available
            NodeRenderInfo info = null;
            int ix = startNodeX - gxBase;
            int iy = startNodeY - gyBase;
            if (ix >= 0 && ix < gxLen && iy >= 0 && iy < gyLen) info = cellCache[ix][iy];
            Color drawColor = (info != null && info.color != null) ? info.color : Color.LIGHT_GRAY;
             g2d.setColor(drawColor);
             int x = startNodeX * nodeSizePx;
             int y = startNodeY * nodeSizePx;
             g2d.fillRect(x, y, nodeSizePx, nodeSizePx);
             // draw border for single cell as well
             Color old = g2d.getColor();
             g2d.setColor(Color.BLACK);
             g2d.drawRect(x, y, Math.max(0, nodeSizePx - 1), Math.max(0, nodeSizePx - 1));
             g2d.setColor(old);
             return;
         }

         int half = sizeNodes / 2;
         int rem = sizeNodes - half;
        drawQuadtreeBlock(g2d, startNodeX, startNodeY, half, gxMin, gxMax, gyMin, gyMax, cellCache, gxBase, gyBase);
        drawQuadtreeBlock(g2d, startNodeX + half, startNodeY, rem, gxMin, gxMax, gyMin, gyMax, cellCache, gxBase, gyBase);
        drawQuadtreeBlock(g2d, startNodeX, startNodeY + half, rem, gxMin, gxMax, gyMin, gyMax, cellCache, gxBase, gyBase);
        drawQuadtreeBlock(g2d, startNodeX + half, startNodeY + half, rem, gxMin, gxMax, gyMin, gyMax, cellCache, gxBase, gyBase);
      }
  }
