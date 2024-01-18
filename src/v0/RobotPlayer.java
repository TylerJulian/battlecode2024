package v0;

import battlecode.common.*;

import java.util.Random;

/**
 * Test class
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Team myTeam;
    static Team oppTeam;
    static int turnCount = 0;
    static Random rng = new Random(0xdeadbeef);

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    static MapLocation spawn;

    static MapLocation target;
    static int mapWidth, mapHeight;

    public static void run(RobotController rc) throws GameActionException {
        // General setup. All ducks regardless of type will need this
        RobotPlayer.rc = rc;
        myTeam = rc.getTeam();
        oppTeam = rc.getTeam().opponent();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        turnCount = rc.getRoundNum();



        // While loop means duck is alive. Will run 1 iteration per turn
        while(true) {
            turnCount += 1;

            try {
                if (!rc.isSpawned()){
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    // Pick a random spawn location to attempt spawning in.
                    MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
                    if (rc.canSpawn(randomLoc))
                    {
                        rc.spawn(randomLoc);
                        target = new MapLocation(mapWidth - randomLoc.x, mapHeight - randomLoc.y);
                    }
                }
                else
                {
                    Duck.start_of_turn_actions();

                    if (rc.canPickupFlag(rc.getLocation())){
                        rc.pickupFlag(rc.getLocation());
                        rc.setIndicatorString("Holding a flag!");
                    }

                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                        target = spawnLocs[0];
                    }

                    if (rc.getLocation().equals(target))
                    {
                        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1);
                        if (nearbyFlags.length > 0)
                        {
                            for (int i = 0; i < nearbyFlags.length; i++)
                            {
                                if (nearbyFlags[i].getTeam().equals(oppTeam))
                                {
                                    target = nearbyFlags[i].getLocation();
                                    break;
                                }
                            }
                        }
                        else {
                            target = rc.getLocation().add(directions[rng.nextInt(directions.length)]);
                        }
                    }

                    bug0.move_toward_goal( rc, target);
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // End turn and wait for next
                Clock.yield();
            }
        }
    }
}
