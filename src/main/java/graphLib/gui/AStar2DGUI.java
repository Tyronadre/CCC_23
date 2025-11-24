package graphLib.gui;

import graphLib.alg.AStar;
import graphLib.graph2D.GraphGrid2D;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStar2DGUI extends JPanel {
    public static final int SIZE = 50;

    AStar<GraphGrid2D.Point> algorithm;
    GraphGrid2D graph;
    int smallestX;
    int smallestY;

    List<Rectangle> rectangleList = new ArrayList<>();
    Map<Integer, Object> nodeDraw = new HashMap<>();

    public AStar2DGUI(AStar<GraphGrid2D.Point> algorithm) {
        this.algorithm = algorithm;
        this.graph = (GraphGrid2D) algorithm.getGraph();

        calculateBaseLayout();
    }

    private void calculateBaseLayout() {
        var smallestX = Integer.MAX_VALUE;
        var smallestY = Integer.MAX_VALUE;
        var biggestX = Integer.MIN_VALUE;
        var biggestY = Integer.MIN_VALUE;

        for (var node : graph.getAllNode()){
            var p = node.getData();
            if (p.x() < smallestX) smallestX = p.x();
            if (p.y() < smallestY) smallestY = p.y();
            if (p.x() > biggestX) biggestX = p.x();
            if (p.y() > biggestY) biggestY = p.y();
        }

        this.smallestX = smallestX;
        this.smallestY = smallestY;

        //precalculate all rectangle shapes
        for (var node : graph.getAllNode()){
            var p = node.getData();
            var rec = new Rectangle(p.x() + smallestX, p.y() + smallestY, SIZE, SIZE);
            rectangleList.add(rec);
        }

        //make a hashmap from node to the text that will be drawn at that point
        


    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.black);

        for (var rec : rectangleList){
            g2d.draw(rec);
        }

    }
}
