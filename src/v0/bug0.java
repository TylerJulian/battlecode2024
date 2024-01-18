package v0;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

// Bug0 implementation?
public class bug0 {
    static Direction heading;

    static public void move_toward_goal(RobotController rc, MapLocation goal) throws GameActionException
    {
        // Check if you are at goal
        if (rc.getLocation().equals(goal))
        {
            return;
        }
        if (!rc.isActionReady())
        {
            return;
        }
        Direction dir = rc.getLocation().directionTo(goal);
        // FOllow if possible
        if (rc.canMove(dir))
        {
            rc.move(dir);
        }
        else
        {
            MapLocation planned_location = rc.getLocation().add(dir);
            if (heading == null)
            {
                heading = dir;
            }
            if (rc.canAttack(planned_location))
            {
                rc.attack((planned_location));
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
                    if (rc.canAttack(planned_location))
                    {
                        rc.attack((planned_location));
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
