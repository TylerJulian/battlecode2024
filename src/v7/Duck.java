package v7.v6;

import battlecode.common.GameActionException;

public class Duck extends RobotPlayer {

    public enum role {
        unassigned,
        builder,
        guardian,
        healer,
        warrior
    }
    public static boolean isLeader = false;
    public static role duckRole = role.unassigned;


    public static boolean kite = false;

    public static void start_of_turn_actions() throws GameActionException {

        comms.get_shared_array();
        //todo Add other actions to perform at start of turn
    }

    public static void squad_actions() throws GameActionException
    {
        kite = false;
        assert squadID < 3;
        if (duckRole == role.unassigned)
        {
            actions.assign_role();
        }
        else if (duckRole == role.builder)
        {
            actions.builder_actions();
        }
        else if (duckRole == role.warrior)
        {
            actions.warrior_actions();
        }
        else if (duckRole == role.healer)
        {
            actions.healer_actions();
        }
        else // shouldn't be called, but calls just in case.
        {
            actions.warrior_actions();
        }

        // If leader then declare a target.
    }




}
