package v1;

import battlecode.common.*;
import v0.util.FastLocIntMap;

import java.util.Random;

/**
 * Test class
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Team myTeam;
    static Team oppTeam;
    static int turnCount = 0;
    static Random rng;
    static int randNumber = 0;

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
    static int squadID = -1;
    static boolean hadFlag = false;
    static MapLocation oldFlag = null;
    public static void run(RobotController rc) throws GameActionException {
        // General setup. All ducks regardless of type will need this
        RobotPlayer.rc = rc;
        myTeam = rc.getTeam();
        oppTeam = rc.getTeam().opponent();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        turnCount = rc.getRoundNum();
        rng = new Random(rc.getID());



        // While loop means duck is alive. Will run 1 iteration per turn
        while(true) {
            turnCount += 1;

            try {
                Duck.start_of_turn_actions();
                if (!rc.isSpawned()){
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    // Pick a random spawn location to attempt spawning in.
                    int randomInt = rng.nextInt(spawnLocs.length);
                    MapLocation randomLoc = spawnLocs[randomInt];
                    if (squadID == -1){
                        squadID = randomInt % 3;
                        System.out.println("SQUAD ID: " + Integer.toString(squadID));
                    }

                    //System.out.println("SQUAD ID: " + Integer.toString(squadID));
                    if (rc.canSpawn(randomLoc))
                    {
                        rc.spawn(randomLoc);
                        target = new MapLocation(mapWidth - randomLoc.x, mapHeight - randomLoc.y);
                    }
                }
                else
                {
                    Duck.squad_actions(squadID);
                    target = Duck.squadTargets[squadID];
                    if (rc.canPickupFlag(rc.getLocation()) && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
                        oldFlag = rc.getLocation();
                        rc.pickupFlag(oldFlag);

                        rc.setIndicatorString("Holding a flag!");
                    }

                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                        target = spawnLocs[0];
                        hadFlag = true;
                        Duck.update_squad_target(squadID, target);
                    }


                    if (rc.getLocation().equals(target))
                    {
                        FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1);
                        if (nearbyFlags.length > 0)
                        {
                            for (FlagInfo nearbyFlag : nearbyFlags) {
                                if (nearbyFlag.getTeam().equals(oppTeam)) {
                                    target = nearbyFlag.getLocation();
                                    break;
                                }
                            }
                        }
                        else {
                            target = rc.getLocation().add(directions[rng.nextInt(directions.length)]);
                        }
                    }

                    bug0.move_toward_goal( rc, target);
                    if (hadFlag){
                        if (!rc.hasFlag()){
                            System.out.println("Captured flag!");
                            target = oldFlag;
                            Duck.update_squad_target(squadID, oldFlag);
                        }
                    }
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