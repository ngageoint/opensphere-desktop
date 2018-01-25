package io.opensphere.core.cache;

import java.util.Arrays;

import io.opensphere.core.util.Utilities;

/**
 * Utilities for cache ids.
 */
public final class CacheIdUtilities
{
    /** The mask used to extract a data id from a combined id. */
    private static final long DATA_ID_MASK;

    /** The amount to shift a combined id to get a group id. */
    private static final int GROUP_ID_SHIFT;

    static
    {
        GROUP_ID_SHIFT = Integer.SIZE;
        DATA_ID_MASK = (1L << GROUP_ID_SHIFT) - 1L;
    }

    /**
     * Iterate through an array of combined ids and coagulate them into clusters
     * that each have a single group associated with them, then call the given
     * functor on each cluster.
     *
     * @param ids The combined ids.
     * @param functor The functor.
     * @throws CacheException If the functor throws an exception.
     */
    public static void forEachGroup(long[] ids, CacheIdUtilities.DatabaseGroupFunctor functor) throws CacheException
    {
        if (ids.length == 0)
        {
            return;
        }
        int lastGroupId = getGroupIdFromCombinedId(ids[0]);
        int firstIndexForGroup = 0;
        for (int index = 1; index < ids.length; ++index)
        {
            int groupId = getGroupIdFromCombinedId(ids[index]);
            if (groupId != lastGroupId)
            {
                long[] range = Arrays.copyOfRange(ids, firstIndexForGroup, index);
                functor.run(range, lastGroupId, getDataIdsFromCombinedIds(range));

                lastGroupId = groupId;
                firstIndexForGroup = index;
            }
        }
        if (firstIndexForGroup == 0)
        {
            functor.run(ids, lastGroupId, getDataIdsFromCombinedIds(ids));
        }
        else if (firstIndexForGroup < ids.length)
        {
            long[] range = Arrays.copyOfRange(ids, firstIndexForGroup, ids.length);
            functor.run(range, lastGroupId, getDataIdsFromCombinedIds(range));
        }
    }

    /**
     * Compile a combined id from a group id and data id.
     *
     * @param groupId The group id.
     * @param dataId The data id.
     * @return The combined id.
     */
    public static long getCombinedId(int groupId, int dataId)
    {
        return (long)groupId << GROUP_ID_SHIFT | dataId & DATA_ID_MASK;
    }

    /**
     * Extract a data id from a combined id.
     *
     * @param combinedId The combined id.
     * @return The data id.
     */
    public static int getDataIdFromCombinedId(long combinedId)
    {
        return (int)combinedId;
    }

    /**
     * Get the data ids from some combined ids.
     *
     * @param ids The combined ids.
     * @return The data ids.
     */
    public static int[] getDataIdsFromCombinedIds(final long[] ids)
    {
        int[] dataIds = new int[ids.length];

        for (int index = 0; index < ids.length;)
        {
            dataIds[index] = getDataIdFromCombinedId(ids[index++]);
        }

        return dataIds;
    }

    /**
     * Extract a group id from a combined id.
     *
     * @param combinedId The combined id.
     * @return The group id.
     */
    public static int getGroupIdFromCombinedId(long combinedId)
    {
        return (int)(combinedId >>> GROUP_ID_SHIFT);
    }

    /**
     * Extract some group ids from some combined ids.
     *
     * @param combinedIds The combined ids.
     * @param distinct Indicates if only distinct group ids should be returned.
     * @return
     *         <ul>
     *         <li>If <code>distinct</code> is <code>false</code>, the group
     *         ids, one for each element. If there is no group, the value is set
     *         to <code>0</code>.</li>
     *         <li>If <code>distinct</code> is <code>true</code>, the distinct
     *         group ids.</li>
     *         </ul>
     */
    public static int[] getGroupIdsFromCombinedIds(long[] combinedIds, boolean distinct)
    {
        int[] groupIds = new int[combinedIds.length];

        for (int index = 0; index < combinedIds.length;)
        {
            groupIds[index] = getGroupIdFromCombinedId(combinedIds[index++]);
        }

        if (distinct && combinedIds.length > 1)
        {
            Arrays.sort(groupIds);
            groupIds = Utilities.unique(groupIds);
        }

        return groupIds;
    }

    /** Disallow instantiation. */
    private CacheIdUtilities()
    {
    }

    /**
     * Interface for a functor that performs actions on a set of data ids that
     * all belong to the same group.
     */
    @FunctionalInterface
    public interface DatabaseGroupFunctor
    {
        /**
         * The behavior for this functor.
         *
         * @param combinedIds the combined ids for the cluster.
         * @param groupId The group id for the cluster.
         * @param dataIds The data ids for the cluster.
         * @throws CacheException If the functor encounters an error.
         */
        void run(long[] combinedIds, int groupId, int[] dataIds) throws CacheException;
    }
}
