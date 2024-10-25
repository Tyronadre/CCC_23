package contest.dataClasses;

public class ShipPathFinder3 {
    PirateMap map;
    Island island;

    public ShipPathFinder3(Island island, PirateMap map) {
        this.island = island;
        this.map = map;
    }

    public ShipPath findPath() {
        int highestX = 0;
        int highestY = 0;
        int lowestX = map.size;
        int lowestY = map.size;

        for (var coord : island.getCoodinatesList()) {
            if (coord.x() > highestX) highestX = coord.x();
            if (coord.y() > highestY) highestY = coord.y();
            if (coord.x() < lowestX) lowestX = coord.x();
            if (coord.y() < lowestY) lowestY = coord.y();
        }
        if (map.get(lowestX, lowestY) != MapData.WATER) {
            lowestX--;
        }if (map.get(lowestX, highestY) != MapData.WATER) {
            highestY++;
        }if (map.get(highestX, lowestY) != MapData.WATER) {
            lowestX--;
        }if (map.get(highestX, highestY) != MapData.WATER) {
            highestX++;
        }

        System.out.println("finding path1");
        var f1 = new ShipPathFinder(new Coordinate(lowestX,lowestY), new Coordinate(lowestX,highestY),map).findPath();
        System.out.println("finding path2");
        var f2 = new ShipPathFinder(new Coordinate(lowestX,highestY), new Coordinate(highestX,highestY),map).findPath();
        System.out.println("finding path3");
        var f3 = new ShipPathFinder(new Coordinate(highestX,highestY), new Coordinate(highestX,lowestY),map).findPath();
        System.out.println("finding path4");
        var f4 = new ShipPathFinder(new Coordinate(highestX,lowestY), new Coordinate(lowestX,lowestY),map).findPath();


        ShipPath path = f1.append(f2).append(f3).append(f4);
        path.getPath().remove(path.length()-1);

        if (path.length() > map.size *2){
            throw new RuntimeException("Path too long: Is " + path.length() + " should be " + map.size*2 + "max\n" + path);
        }


        System.out.println(path);
        return path;
    }
}
