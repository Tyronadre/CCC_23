package contest_25;

import util.Framework;

import java.util.ArrayList;
import java.util.List;

public class T5 {
    public static void main(String[] args) {
        final int level = 5;
        for (int i = 0; i <= 0; i++) {

            var lines = Framework.readFile(level, i);
            Framework.writeOutput(level, i, solve(lines));
        }
    }

    private static String solve(List<String> lines) {
        for (int i = 1; i < lines.size(); ) {
            var line1 = lines.get(i++).split(" ");
            var pos = line1[0].split(",");
            var spaceMap = new SpaceMap(new SpaceShip(0, 0), Integer.parseInt(line1[1]), new SpaceObject(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), 0));


            String line2 = lines.get(i++);
            pos = line2.split(",");
            spaceMap.addSpaceObject(new SpaceObject(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), 2));

        }


        return "";
    }
}

class SpaceMap {
    List<SpaceObject> spaceObjects = new ArrayList<>();


    SpaceShip spaceShip;
    SpaceObject spaceStation;
    int timeLimit;
    FlightPath path;

    SpaceMap(SpaceShip spaceShip, int timeLimit, SpaceObject spaceStation) {
        this.spaceStation = spaceStation;
        this.timeLimit = timeLimit;
        this.spaceShip = spaceShip;


        calculateFlightPath();
    }

    public void calculateFlightPath() {
        System.out.println("SpaceShip: " + spaceShip);
        System.out.println("SpaceStation: " + spaceStation);

        // mache den schnellsten pfad
        var movement1 = generateMovement(spaceStation.posX, timeLimit);
        var movement2 = generateMovement(spaceStation.posY, timeLimit);

        this.path = new FlightPath(movement1, movement2);


        System.out.println("Path: " + path);

    }

    public List<Integer> generateMovement(Integer nextSpaceStation, Integer timeLimit) {
        var result = new ArrayList<Integer>();
        var time = 0;
        result.add(0);
        var distance = Math.abs(nextSpaceStation);
        if (distance == 0) return result;
        var direction = nextSpaceStation < 0 ? -1 : 1;
        var pace = 5;
        while (pace > 0) {
            if (result.size() > 1000)
                System.out.println("result to big");
            if (6 - pace == distance) {
                result.add(pace * direction);
                time += pace;
                pace++;
                if (pace == 6) pace = 0;
                distance--;
                continue;
            }
            if (6 - pace < distance - 1) {
                result.add(pace * direction);
                time += pace;
                if (pace != 1) pace--;
                distance--;
                continue;
            }

            result.add(pace * direction);
            distance--;
            time += pace;
        }
        result.add(0);
        if (time > timeLimit) {
            throw new RuntimeException("time limit exceeded");
        }

        return result;
    }


    public void addSpaceObject(SpaceObject spaceObject) {
        this.spaceObjects.add(spaceObject);
    }
}

class FlightPath {
    private List<Integer> xPath;
    private List<Integer> yPath;

    public FlightPath(List<Integer> xPath, List<Integer> yPath) {
        this.xPath =xPath;
        this.yPath = yPath;

    }

    public List<Point> points() {
        var xPoints = new ArrayList<Integer>();
        var xPos = 0;
        for (Integer integer : xPath) {
            if (integer == 0) xPoints.add(0);
            for (int j = 0; j < integer; j++)
                xPoints.add(xPos);
            xPos++;
        }
        var yPoints = new ArrayList<Integer>();
        for (Integer integer : yPath) {
            if (integer == 0) yPoints.add(0);
            for (int j = 0; j < integer; j++) {
                yPoints.add(j);
            }
        }

        var res = new ArrayList<Point>();
        int i = 0;
        while (true){

            if (i >= xPoints.size() && i >= yPoints.size()) break;
            int x = i >= xPoints.size() ? xPoints.get(xPoints.size() - 1 ) : xPoints.get(i);
            int y = i >= yPoints.size() ? yPoints.get(yPoints.size() - 1 ) : yPoints.get(i);
            i++;
            res.add(new Point(x,y));
        }
        return res;
    }

    @Override
    public String toString() {
        return points().toString();
    }
}

record Point(int x, int y) {}

class SpaceShip extends SpaceObject {
    int pace = 0;

    public SpaceShip(int posX, int posY) {
        super(posX, posY, 0);
    }

    public void accelerate() {
        if (pace == 0) pace = 5;
        else if (pace == 1) return;
        else pace++;
    }

    public void decelerate() {
        if (pace == 0) pace = 5;
        else if (pace == 1) return;
        else pace--;
    }

    public List<List<Integer>> getPath(SpaceMap spaceMap) {
        var spaceStation = spaceMap.spaceStation;



        var asteroid = spaceMap.spaceObjects.get(0);

        // pfad von x0 nach x1 machen

        List<Integer> pathX = new ArrayList<>();
        pathX.add(0);
        var directionX = this.posX - spaceStation.posX;
        if (directionX < 0) this.accelerate();
        else if (directionX > 0) this.decelerate();
        if (directionX != 0) {
            for (int x = posX; x <= Math.abs(spaceStation.posX); x++) {
                pathX.add(pace);
                this.posX++;
            }
            pathX.add(0);
        }

        List<Integer> pathY = new ArrayList<>();
        pathY.add(0);
        var directionY = this.posY - spaceStation.posY;
        if (directionY < 0) this.accelerate();
        else if (directionY > 0) this.decelerate();
        if (directionY != 0) {
            for (int y = posY; y <= Math.abs(spaceStation.posY); y++) {
                pathY.add(pace);
            }
            pathY.add(0);
        }


        return List.of(pathX, pathY);

    }
}


class SpaceObject {
    int posX;
    int posY;
    int radius;

    public SpaceObject(int posX, int posY, int radius) {
        this.posX = Math.abs(posX);
        this.posY = Math.abs(posY);
        this.radius = radius;
    }

    public boolean collides(SpaceObject spaceObject) {
        return collides(spaceObject.posX, spaceObject.posY);
    }

    public boolean collides(int x, int y) {
        var xCollide = false;
        var yCollide = false;
        for (int i = posX - radius; i < posX + radius; i++) {
            if (x == i) {
                xCollide = true;
                break;
            }
        }
        for (int i = posY - radius; i < posY + radius; i++) {
            if (x == i) {
                yCollide = true;
                break;
            }
        }
        return xCollide && yCollide;
    }

    @Override
    public String toString() {
        return "SpaceObject{" +
            "posY=" + posY +
            ", posX=" + posX +
            '}';
    }
}
