package v8;

import battlecode.common.*;
import v8.util.FastMath;

import java.util.Random;

import static v8.util.FastMath.get_closest;

/**
 * Test class
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Team myTeam;
    static Team oppTeam;
    static int turnCount = 0;
    static Random rng;
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
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation[] centerSpawnLoc = {spawnLocs[5], spawnLocs[13], spawnLocs[22]};


        comms.init(rc);

        // While loop means duck is alive. Will run 1 iteration per turn
        while(true) {
            turnCount += 1;
            hadFlag = false;

            try {
                Duck.start_of_turn_actions();
                if (!rc.isSpawned()){

                    // Pick a random spawn location to attempt spawning in.
                    int randomInt = rng.nextInt(spawnLocs.length);
                    MapLocation randomLoc = spawnLocs[randomInt];
                    if (squadID == -1){
                        squadID = randomInt / 9;
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

                    target = comms.squadTargets[0];
                    Duck.squad_actions();
                    if (rc.canPickupFlag(rc.getLocation()) && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
                        oldFlag = rc.getLocation();
                        rc.pickupFlag(oldFlag);

                        rc.setIndicatorString("Holding a flag!");
                    }

                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
                        target = get_closest(rc.getLocation(), spawnLocs);
                        hadFlag = true;
                        comms.update_squad_target(0, target);
                        rc.setIndicatorLine(rc.getLocation(),target, 0, 0,255);
                    }
                    else if (!Duck.kite || actions.lockedIn){ // Don't hunt if you can kite.
                        if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) {
                            MapLocation[] nearbyCrumbs = rc.senseNearbyCrumbs(-1);
                            if (nearbyCrumbs.length > 0){
                                int randomInt = rng.nextInt(nearbyCrumbs.length);
                                target = nearbyCrumbs[randomInt];
                            }
                        }
                        else if (FastMath.get_distance(rc.getLocation(), target) < 5.0)
                        {
                            System.out.printf("LOC (%d, %d), (%d, %d) dist: %f", rc.getLocation().x, rc.getLocation().y, target.x, target.y, FastMath.get_distance(rc.getLocation(), target));
                            System.out.println();
                            FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1);

                            // check nearby flags
                            if (nearbyFlags.length > 0) {
                                for (FlagInfo nearbyFlag : nearbyFlags) {
                                    if (nearbyFlag.getTeam().equals(oppTeam) && !nearbyFlag.isPickedUp()) {
                                        target = nearbyFlag.getLocation();
                                        comms.update_squad_target(0, target);
                                        break;
                                    }
                                }
                            }
                            // check broad location of flags.
                            else
                            {
                                MapLocation[] hiddenFlags = rc.senseBroadcastFlagLocations();
                                if (hiddenFlags.length > 0)
                                {
                                    int randInt = rng.nextInt(hiddenFlags.length);
                                    target = hiddenFlags[randInt];
                                    comms.update_squad_target(0, target);
                                }
                                else { // fuck around I guess.
                                    target = rc.getLocation().add(directions[rng.nextInt(directions.length)]);
                                }
                            }
                        }
                        rc.setIndicatorLine(rc.getLocation(),target, 255, 255,255);
                    }

                    bug0.move_toward_goal2( rc, target);
                    Duck.squad_actions();

                    rc.setIndicatorString("id: " + Integer.toString(squadID)+ " Target : (" + Integer.toString(target.x) + "," + Integer.toString(target.y) + ")");
                    if (hadFlag){
                        if (!rc.hasFlag()){
                            System.out.println("Captured flag!");
                            target = oldFlag;
                            comms.update_squad_target(0, oldFlag);
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
