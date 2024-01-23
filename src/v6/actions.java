package v5;

import battlecode.common.*;

public class actions extends Duck {
    public static int warriorTarget = 0; // id of the warrior's target
    public static boolean lockedIn = false; // The warrior rages and marks a target.

    public static void assign_role() throws GameActionException {
        if (comms.squadLeaders[squadID] == 0) {
            int id = rc.getID();
            comms.write_bits(squadID * 16, 16, id);
            comms.squadLeaders[squadID] = id;
            duckRole = role.builder;
            isLeader = true;

            //target create initial target.
            MapLocation randomLoc = rc.getLocation();
            target = new MapLocation(mapWidth - randomLoc.x, mapHeight - randomLoc.y);
            comms.update_squad_target(squadID, target);
            comms.update_shared_array_from_buffer(); // TODO put at end of duck's turn
        }
        if (!isLeader){
            if (squadID == 0) duckRole = role.warrior;
            if (squadID == 1) duckRole = role.healer;
            if (squadID == 2) duckRole = role.guardian;
        }
    }

    public static void builder_actions() throws GameActionException
    {
        buy_action();
        build_action();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(4);
        attack_action(nearbyRobots);
        heal_action(nearbyRobots);
        //heal action
    }

    public static void build_action() throws GameActionException
    {
        if (rc.isActionReady())
        {
            if ((rc.getRoundNum() <= GameConstants.SETUP_ROUNDS && rc.getCrumbs() > 1000) || (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS && rc.getCrumbs() > 1000)) {
                if (rc.getRoundNum() % 7 == 2)
                {
                    if (rc.canBuild(TrapType.WATER, rc.getLocation())) rc.build(TrapType.EXPLOSIVE, rc.getLocation());
                } else if (rc.getRoundNum() % 7 == 1)
                {
                    if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())) rc.build(TrapType.EXPLOSIVE, rc.getLocation());
                }
                else if (rc.getRoundNum() % 7 == 0)
                {
                    if (rc.canBuild(TrapType.STUN, rc.getLocation())) rc.build(TrapType.EXPLOSIVE, rc.getLocation());
                }
            }
        }
    }

    public static void warrior_actions() throws GameActionException
    {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(4);
        if (warriorTarget != 0)
        {
            if (rc.canSenseRobot(warriorTarget))
            {
                RobotInfo badguy = rc.senseRobot(warriorTarget);
                if (badguy.hasFlag())
                {
                    target = badguy.getLocation();
                    lockedIn = true;
                }
            }
            else
            {
                lockedIn = false;
            }
        }

        //warrior attack action
        if(rc.isActionReady())
        {
            if (lockedIn)
            {
                if (rc.canAttack(target)) rc.attack(target);
            }

            RobotInfo needsHealing = null;
            for (RobotInfo robot : nearbyRobots) {
                if (robot.hasFlag()) {
                    warriorTarget = robot.getID();
                    target = robot.getLocation();
                }

                if (rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                    if (!robot.hasFlag() && robot.getHealth() >= 0) {
                        Direction dir = rc.getLocation().directionTo(robot.getLocation());
                        target = rc.getLocation().add(dir.opposite());
                        rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
                        kite = true;
                    } else if (robot.getHealth() <= 0) {
                        if (robot.getID() == warriorTarget) {
                            warriorTarget = 0;
                        }
                    }
                }
            }
        }

        heal_action(nearbyRobots);

    }

    public static void healer_actions() throws GameActionException
    {
        // TODO: Replace attack action with warrior actions?
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(4);
        heal_action(nearbyRobots);
        attack_action(nearbyRobots);
    }

    public static void heal_action(RobotInfo[] nearbyRobots) throws GameActionException
    {
        if (rc.isActionReady()) {
            for (RobotInfo robot : nearbyRobots)
            {
                if (rc.canHeal(robot.getLocation())) rc.heal(robot.getLocation());
            }
        }
    }
    public static void attack_action(RobotInfo[] nearbyRobots) throws GameActionException
    {
        if (rc.isActionReady()) {
            for (RobotInfo robot : nearbyRobots)
            {
                if (rc.canAttack(robot.getLocation())) rc.attack(robot.getLocation());
            }
        }
    }

    public static void buy_action() throws GameActionException
    {
        if (isLeader) {
            if (rc.canBuyGlobal(GlobalUpgrade.ACTION)) rc.buyGlobal(GlobalUpgrade.ACTION);
            if (rc.canBuyGlobal(GlobalUpgrade.HEALING)) rc.buyGlobal(GlobalUpgrade.HEALING);
            if (rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) rc.buyGlobal(GlobalUpgrade.CAPTURING);
        }
    }

}