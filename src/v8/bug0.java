package v7;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

// Bug0 implementation?
public class bug0 {
    private static MapLocation prevDest = null;
    private static HashSet<MapLocation> line = null;
    private static int obstacleStartDist = 0;
    private static int bugState = 0;
    private static MapLocation nextObstacle = null;
    private static int nextObstacleDist = 10000;
    private static Direction heading;
    private static Direction bugDir;

    static public void move_toward_goal(RobotController rc, MapLocation goal) throws GameActionException
    {
        // Check if you are at goal
        if (rc.getLocation().equals(goal))
        {
            return;
        }
        if (!rc.isMovementReady())
        {
            return;
        }
        Direction dir = rc.getLocation().directionTo(goal);
        // FOllow if possible
        if (rc.canMove(dir))
        {
            rc.move(dir);
            heading = null;
        }
        else
        {
            MapLocation planned_location = rc.getLocation().add(dir);
            if (heading == null)
            {
                heading = dir;
            }
            //cycle through dirs
            for (int i = 0; i < 8; i++)
            {
                if (rc.canMove(heading))
                {
                    rc.move(heading);
                    heading = heading.rotateRight();
                    break;
                }
                else
                {

                    planned_location = rc.getLocation().add(heading);
                    if (rc.canFill(planned_location)){
                        rc.fill(planned_location);
                    }

                    heading = heading.rotateLeft();

                }
            }
        }
    }


    static public void reset_bugState()
    {
        bugState = 0;
        nextObstacle = null;
        nextObstacleDist = 10000;
        heading = null;
    }

    public static void move_toward_goal1(RobotController rc, MapLocation destination) throws GameActionException{
        if(bugState == 0) {
            bugDir = rc.getLocation().directionTo(destination);
            if(rc.canMove(bugDir)){
                rc.move(bugDir);
            } else {
                bugState = 1;
                nextObstacle = null;
                nextObstacleDist = 10000;
            }
        } else {
            if(rc.getLocation().equals(nextObstacle)){
                bugState = 0;
            }

            if(rc.getLocation().distanceSquaredTo(destination) < nextObstacleDist){
                nextObstacleDist = rc.getLocation().distanceSquaredTo(destination);
                nextObstacle = rc.getLocation();
            }

            for(int i = 0; i < 9; i++){
                if(rc.canMove(bugDir)){
                    rc.move(bugDir);
                    bugDir = bugDir.rotateRight();
                    bugDir = bugDir.rotateRight();
                    break;
                } else {
                    bugDir = bugDir.rotateLeft();
                }
            }
        }
    }



    public static void bugNav2(RobotController rc, MapLocation destination) throws GameActionException{

        if(!destination.equals(prevDest)) {
            prevDest = destination;
            line = createLine(rc.getLocation(), destination);
        }

        for(MapLocation loc : line) {
            rc.setIndicatorDot(loc, 255, 0, 0);
        }

        if(bugState == 0) {
            bugDir = rc.getLocation().directionTo(destination);
            if(rc.canMove(bugDir)){
                rc.move(bugDir);
            } else {
                bugState = 1;
                obstacleStartDist = rc.getLocation().distanceSquaredTo(destination);
                bugDir = rc.getLocation().directionTo(destination);
            }
        } else {
            if(line.contains(rc.getLocation()) && rc.getLocation().distanceSquaredTo(destination) < obstacleStartDist) {
                bugState = 0;
            }

            for(int i = 0; i < 9; i++){
                if(rc.canMove(bugDir)){
                    rc.move(bugDir);
                    bugDir = bugDir.rotateRight();
                    bugDir = bugDir.rotateRight();
                    break;
                } else {
                    bugDir = bugDir.rotateLeft();
                }
            }
        }
    }



    private static HashSet<MapLocation> createLine(MapLocation a, MapLocation b) {
        HashSet<MapLocation> locs = new HashSet<>();
        int x = a.x, y = a.y;
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        int sx = (int) Math.signum(dx);
        int sy = (int) Math.signum(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        int d = Math.max(dx,dy);
        int r = d/2;
        if (dx > dy) {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                x += sx;
                r += dy;
                if (r >= dx) {
                    locs.add(new MapLocation(x, y));
                    y += sy;
                    r -= dx;
                }
            }
        }
        else {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                y += sy;
                r += dx;
                if (r >= dy) {
                    locs.add(new MapLocation(x, y));
                    x += sx;
                    r -= dy;
                }
            }
        }
        locs.add(new MapLocation(x, y));
        return locs;
    }
}
