package v0;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import v0.util.*;
public class Duck extends RobotPlayer{
    private static final int ARRAY_SIZE = 64;

    private static int[] shared_array_buf = new int[ARRAY_SIZE];
    private static v0.util.FastIterableIntSet shared_array_changed_indexes = new FastIterableIntSet(ARRAY_SIZE);

    public static MapLocation[] allied_spawn_locations = {null, null, null};
    public static MapLocation[] shitter_spawn_locations = {null, null, null};

    public static void start_of_turn_actions() throws GameActionException {
        for (int i = 0; i < ARRAY_SIZE; i++)
        {
            if (rc.readSharedArray(i) != shared_array_buf[i])
            {
                //TODO set flags for changed values


                shared_array_buf[i] = rc.readSharedArray(i);
            }
        }

        //todo Add other actions to perform at start of turn
    }

    public static void update_shared_array_from_buffer() throws GameActionException
    {
        if (shared_array_changed_indexes.size > 0) {
            shared_array_changed_indexes.updateIterable();
            int[] indexes = shared_array_changed_indexes.ints;
            for (int i = shared_array_changed_indexes.size; --i >= 0; )
            {
                rc.writeSharedArray(indexes[i], shared_array_buf[indexes[i]]);
            }
            shared_array_changed_indexes.clear();
        }
    }

    public static int read_bits(int index, int len)
    {
        int end_bit = index + len - 1;
        int retVal = 0;
        while (index <= end_bit)
        {
            int curr_index = Math.min(index | 0x0F, end_bit);
            retVal = (retVal << (curr_index - index + 1)) + ((shared_array_buf[index/16] % (1<<(16-index % 16))) >> (15 - end_bit % 16));
            index = curr_index + 1;
        }
        return retVal;
    }

    public static int write_bits(int index, int len, int newVal)
    {
        if (newVal < (1 << len)) {
            int remaining_bits = len;
            int curr_end, curr_len, curr_index, old_value, curr_value;
            while (remaining_bits > 0)
            {
                curr_end = index + remaining_bits -1;
                curr_len = Math.min(curr_end%16 + 1, remaining_bits);
                curr_index = curr_end - curr_len + 1;
                old_value = (shared_array_buf[curr_index/16] % (1 << (16 - curr_index%16))) >> (15 - curr_end % 16);
                curr_value = newVal % (1 << len);
                newVal >>= len;
                if (curr_value != old_value)
                {
                    shared_array_changed_indexes.add(remaining_bits / 16);
                    shared_array_buf[curr_end / 16] ^= (curr_value ^ old_value) << (15 - curr_end);
                }
            }
        }
        else
        {
            return -1;
        }
        return len;
    }

    public static MapLocation int_to_location(int val)
    {
       if (val == 0)
       {
           return null;
       }
       else
       {
           return new MapLocation(val/64 -1, val % 64 -1);
       }
    }

    public static int Location_to_int(MapLocation val)
    {
        return ((val.x + 1) * 64) + (val.y +1);
    }

}
