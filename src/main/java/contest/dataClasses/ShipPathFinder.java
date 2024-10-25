package contest.dataClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ShipPathFinder {
    Coordinate start;
    Coordinate end;
    PirateMap map;
    Map<Coordinate, ArrayList<Coordinate>> pathFromStart = new HashMap<>();
    Map<Coordinate, Double> distanceToEnd = new HashMap<>();

    public ShipPathFinder(Coordinate start, Coordinate end, PirateMap map) {
        this.start = start;
        this.end = end;
        this.map = map;
    }

    public ShipPath findPath() {
        //not over land
        //not any gaps
        //no self intersects
        //as short as possible
        pathFromStart.put(start, new ArrayList<>(Collections.singleton(start)));
        distanceToEnd.put(start, euclidianDistance(start, end));
        var usedCoordinates = new ArrayList<Coordinate>();
        var currentCord = start;
        while (true) {
            // System.out.println("Current: " + currentCord);
            usedCoordinates.add(currentCord);
            //calculate the distance around current coordinate
            double bestDistance = Double.MAX_VALUE;
            for (var cT : currentCord.getAdjacentCoordinates(map.size)) {
                if (cT.x() < map.size && cT.y() < map.size &&  cT.x() > 0 && cT.y() > 0 && map.get(cT) != MapData.WATER || pathFromStart.containsKey(cT)) {
                    //we dont want that coordinate
                } else if (cT.equals(end)) {
                    var endPath = new ArrayList<>(pathFromStart.get(currentCord));
                    endPath.add(cT);
                    return new ShipPath(endPath);
                } else {
                    double distanceToEnd = euclidianDistance(cT, end);
                    if (distanceToEnd < bestDistance) {
                        bestDistance = distanceToEnd;
                    }
                    this.distanceToEnd.put(cT, distanceToEnd);
                    var newPath = new ArrayList<>(pathFromStart.get(currentCord));
                    newPath.add(cT);
                    pathFromStart.put(cT, newPath);
                }
            }
            //else {
                //we need to backtrack to find a new coordinate
                double bestBackTrackDistance = Double.MAX_VALUE;
                Coordinate bestBacktrackCord = null;
                for (var entry : distanceToEnd.entrySet()) {
                    if (usedCoordinates.contains(entry.getKey())) {
                        continue;
                    }
                    if (entry.getValue() < bestBackTrackDistance) {
                        bestBackTrackDistance = entry.getValue();
                        bestBacktrackCord = entry.getKey();
                    }
                }
                if (bestBacktrackCord == null) {
                    //we have no more options
                    throw new RuntimeException("No more options");
                }
                currentCord = bestBacktrackCord;
            //}
        }
    }

    private double euclidianDistance(Coordinate c1, Coordinate c2) {
        return Math.sqrt(Math.pow(c1.x() - c2.x(), 2) + Math.pow(c1.y() - c2.y(), 2));
    }
}
