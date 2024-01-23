package v5;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

// Bug0 implementation?
public class bug0 {
    private static MapLocation prevGoal = null;
    private static HashSet<MapLocation> line = null;
    private static int obstacleStartDist = 0;
    private static int bugState = 0;
    private static MapLocation nextObstacle = null;
    private static int nextObstacleDist = 10000;
    private static Direction heading;

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
    static public void move_toward_goal2(RobotController rc, MapLocation goal) throws GameActionException
    {
        if (goal.equals(prevGoal))
        {
            prevGoal = goal;
            line = create_line(rc.getLocation(), goal);

            for (MapLocation loc : line)
            {
                rc.setIndicatorDot(loc, 255, 255, 255);
            }

            if (bugState == 0)
            {
                heading = rc.getLocation().directionTo(goal);
                if (rc.canMove(heading))
                {
                    rc.move(heading);
                }
                else
                {
                    if(line.contains(rc.getLocation()) && rc.getLocation().distanceSquaredTo(goal) < obstacleStartDist) {
                        bugState = 0;
                    }

                    for (int i = 0; i < 9; i++)
                    {
                        if (rc.canMove(heading))
                        {
                            rc.move(heading);
                            heading = heading.rotateRight().rotateRight();
                            break;
                        }
                        else
                        {
                            heading = heading.rotateLeft();
                        }
                    }
                }
            }

        }
    }


    private static HashSet<MapLocation> create_line(MapLocation a, MapLocation b) {
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
