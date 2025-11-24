package graphLib.alg;

import graphLib.base.Graph;
import graphLib.base.Node;
import graphLib.gui.event.AlgorithmDrawEvent;
import graphLib.gui.event.EventBus;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class Algorithm<T extends Comparable<T>, V> {
    Graph<T> graph;
    boolean pause;
    boolean step;
    int delay;
    CompletableFuture<V> result = new CompletableFuture<>();

    // Cache that holds the computed render state for each node once the algorithm finished.
    // Keyed by node id for efficient lookup.
    private volatile Map<Integer, NodeRenderState> finishedStateCache = null;

    // --- Timing fields ---
    // Wall-clock ms when the current run started, or -1 if not running / not set
    private volatile long runStartTimeMillis = -1;
    // Wall-clock ms when the run ended, or -1 if still running / not set
    private volatile long runEndTimeMillis = -1;

    // Step timings (measured in nanoseconds)
    private volatile long stepCount = 0;
    private volatile long totalStepTimeNanos = 0;
    private volatile long lastStepTimeNanos = 0;

    // EventBus post timings (measured in nanoseconds)
    private volatile long eventCount = 0;
    private volatile long totalEventTimeNanos = 0;
    private volatile long lastEventTimeNanos = 0;

    // Stats print control (milliseconds)
    private volatile long lastStatsPrintMillis = 0;
    private volatile long statsPrintIntervalMillis = 1000; // print every 1s by default

    public Algorithm(Graph<T> graph) {
        this.graph = graph;
    }

    /**
     * @return a completable future for the result of this algorithm;
     */
    public CompletableFuture<V> getResult() {
        return result;
    }

    /**
     * Pauses the algorithm
     */
    public void pause() {
        this.pause = true;
    }

    /**
     * Steps one time, if the algorithm is paused
     */
    public void step() {
        if (pause)
            this.step = true;
    }

    /**
     * Continues the execution of the algorithm
     */
    public void resume() {
        this.pause = false;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Helper that posts a draw event and measures how long EventBus.post takes.
     */
    private void postDrawEvent() {
        long t0 = System.nanoTime();
        EventBus.instance.post(new AlgorithmDrawEvent());
        long t1 = System.nanoTime();
        long dt = t1 - t0;
        eventCount++;
        totalEventTimeNanos += dt;
        lastEventTimeNanos = dt;
    }

    /**
     * Starts the given algorithm in a new Thread.
     */
    public void start() {
        new Thread(() -> {
            // record the wall-clock start time for the run
            runStartTimeMillis = System.currentTimeMillis();
            runEndTimeMillis = -1;
            // reset per-step timing counters
            stepCount = 0;
            totalStepTimeNanos = 0;
            lastStepTimeNanos = 0;
            // reset event counters
            eventCount = 0;
            totalEventTimeNanos = 0;
            lastEventTimeNanos = 0;
            lastStatsPrintMillis = System.currentTimeMillis();

            // clear any previous finished cache when a new run begins
            finishedStateCache = null;
            initAlgorithm();
            try {
                boolean stop = false;
                while (!stop) {
                    Thread.sleep(delay);
                    if (pause) {
                        if (step) {
                            long t0 = System.nanoTime();
                            stop = Algorithm.this.stepAlgorithm();
                            long t1 = System.nanoTime();
                            long dt = t1 - t0;
                            stepCount++;
                            totalStepTimeNanos += dt;
                            lastStepTimeNanos = dt;

                            postDrawEvent();
                            step = false;
                        } else {
                            Thread.sleep(100);
                        }
                    } else {
                        long t0 = System.nanoTime();
                        stop = Algorithm.this.stepAlgorithm();
                        long t1 = System.nanoTime();
                        long dt = t1 - t0;
                        stepCount++;
                        totalStepTimeNanos += dt;
                        lastStepTimeNanos = dt;

                        postDrawEvent();
                    }

                    // Periodic console statistics
                    long now = System.currentTimeMillis();
                    if (now - lastStatsPrintMillis >= statsPrintIntervalMillis) {
                        lastStatsPrintMillis = now;
                        String stats = String.format(
                                "[Alg Stats] step=%d, avgStep=%.3fms, lastStep=%.3fms, avgEvent=%.3fms, lastEvent=%.3fms, run=%.0fms",
                                stepCount,
                                stepCount == 0 ? 0.0 : (totalStepTimeNanos / (double) stepCount) / 1_000_000.0,
                                lastStepTimeNanos / 1_000_000.0,
                                eventCount == 0 ? 0.0 : (totalEventTimeNanos / (double) eventCount) / 1_000_000.0,
                                lastEventTimeNanos / 1_000_000.0,
                                (double) (now - runStartTimeMillis)
                        );
                        System.out.println(stats);
                    }
                }

                // algorithm run finished - build a snapshot of node render states so renderers
                // can cheaply query states for a large number of nodes without re-evaluating
                // dynamic algorithm internals.
                buildFinishedStateCache();
                // notify a final draw so UI can use the cached states
                postDrawEvent();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // record end time
            runEndTimeMillis = System.currentTimeMillis();

            // Final nicely formatted summary
            String summary = String.format(
                    "Algorithm finished in %dms. steps=%d, avgStep=%.3fms, lastStep=%.3fms, events=%d, avgEvent=%.3fms, lastEvent=%.3fms",
                    (runEndTimeMillis - runStartTimeMillis),
                    stepCount,
                    stepCount == 0 ? 0.0 : (totalStepTimeNanos / (double) stepCount) / 1_000_000.0,
                    lastStepTimeNanos / 1_000_000.0,
                    eventCount,
                    eventCount == 0 ? 0.0 : (totalEventTimeNanos / (double) eventCount) / 1_000_000.0,
                    lastEventTimeNanos / 1_000_000.0
            );
            System.out.println(summary);
        }).start();
    }

    /**
     * Initialization for the algorithm, if this cannot be handled in the constructor.
     * Will be called ONCE when the algorithm is started.
     */
    protected abstract void initAlgorithm();

    /**
     * Main Method for the algorithm. each time this method is called, the algorithm should calculate one step of its
     * execution.
     * Returns true when the algorithm terminated after this step, otherwise false
     */
    protected abstract boolean stepAlgorithm();

    /**
     *
     * @return the graph this algorithm runs on
     */
    public Graph<T> getGraph() {
        return graph;
    }

    /**
     * Draws information specific for this algorithm for this node.
     * @param g2d the graphics
     * @param node the node
     */
    public void draw(Graphics2D g2d, Node<T> node) {
        g2d.drawString("N/A", 0, 0);
    }

    // --- New API for rendering/querying node state ---
    public enum NodeRenderState {
        DEFAULT,
        OPEN,
        CLOSED,
        PATH,
        FINISHED
    }

    /**
     * Subclasses override this protected method to provide the logic that maps a node to a render state.
     * This method is called during the algorithm run to evaluate states. After the algorithm finished the
     * base class will build a snapshot of these states and further calls to {@link #getNodeRenderState(Node)}
     * will be served from that snapshot for efficiency.
     */
    protected NodeRenderState nodeRenderState(Node<T> node) {
        return NodeRenderState.DEFAULT;
    }

    /**
     * Returns the render state for a node. This method is final so the base class can return a cached value
     * after algorithm completion to speed up rendering of many nodes. Subclasses should override
     * {@link #nodeRenderState(Node)} instead of this method.
     */
    public final NodeRenderState getNodeRenderState(Node<T> node) {
        var cache = finishedStateCache;
        if (cache != null) {
            return cache.getOrDefault(node.getId(), NodeRenderState.DEFAULT);
        }
        return nodeRenderState(node);
    }

    /**
     * Optional: returns an iterable over nodes that are "interesting" for rendering
     * (non-default state). Default is null meaning algorithm does not provide an optimized list.
     */
    public Iterable<Node<T>> getInterestingNodes() {
        return null;
    }

    /**
     * Build a finished-state snapshot for all nodes. This method is called once after the run loop ends.
     */
    private void buildFinishedStateCache() {
        try {
            Map<Integer, NodeRenderState> map = new HashMap<>();
            for (Node<T> n : graph.getAllNode()) {
                NodeRenderState s = nodeRenderState(n);
                map.put(n.getId(), s == null ? NodeRenderState.DEFAULT : s);
            }
            finishedStateCache = map;
        } catch (Throwable t) {
            // ensure we never leave the cache in a partially built state
            finishedStateCache = null;
        }
    }

    // --- Timing getters ---

    /**
     * Returns the run start time in milliseconds since epoch, or -1 if not set.
     */
    public long getRunStartTimeMillis() {
        return runStartTimeMillis;
    }

    /**
     * Returns the run end time in milliseconds since epoch, or -1 if run hasn't finished.
     */
    public long getRunEndTimeMillis() {
        return runEndTimeMillis;
    }

    /**
     * Returns the total run duration in milliseconds. If the run is still active, returns elapsed time so far.
     */
    public long getRunDurationMillis() {
        if (runStartTimeMillis <= 0) return 0;
        if (runEndTimeMillis > 0) return runEndTimeMillis - runStartTimeMillis;
        return System.currentTimeMillis() - runStartTimeMillis;
    }

    /**
     * Number of algorithm steps executed so far (or total after finish).
     */
    public long getStepCount() {
        return stepCount;
    }

    /**
     * Total time spent in stepAlgorithm() in nanoseconds.
     */
    public long getTotalStepTimeNanos() {
        return totalStepTimeNanos;
    }

    /**
     * Average time per step in milliseconds. Returns 0 if no steps executed yet.
     */
    public double getAverageStepTimeMillis() {
        if (stepCount == 0) return 0.0;
        return (totalStepTimeNanos / (double) stepCount) / 1_000_000.0;
    }

    /**
     * Last step duration in milliseconds.
     */
    public double getLastStepTimeMillis() {
        return lastStepTimeNanos / 1_000_000.0;
    }
}
