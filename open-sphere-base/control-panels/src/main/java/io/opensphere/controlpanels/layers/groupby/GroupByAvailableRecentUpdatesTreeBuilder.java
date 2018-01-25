package io.opensphere.controlpanels.layers.groupby;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.data.impl.GroupCategorizer;

/**
 * The Class GroupByTitleTreeBuilder.
 */
public class GroupByAvailableRecentUpdatesTreeBuilder extends GroupByDefaultTreeBuilder
{
    @Override
    public String getGroupByName()
    {
        return "Recent Updates";
    }

    @Override
    public GroupCategorizer getGroupCategorizer()
    {
        return new GroupCategorizer()
        {
            @Override
            public List<String> getAllCategories()
            {
                RecentUpdateCategory[] catArray = RecentUpdateCategory.values();

                List<String> allCats = New.list(catArray.length);
                for (RecentUpdateCategory cat : catArray)
                {
                    if (getCategories().contains(cat.toString()))
                    {
                        allCats.add(cat.toString());
                    }
                }
                return allCats;
            }

            @Override
            public Set<String> getGroupCategories(DataGroupInfo group)
            {
                Set<String> categories = New.set();
                for (DataTypeInfo dti : group.getMembers(false))
                {
                    categories.addAll(getTypeCategories(dti));
                }
                getCategories().addAll(categories);
                return categories;
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dti)
            {
                Set<String> catSet = null;

                long now = System.currentTimeMillis();

                if (dti != null)
                {
                    TimeExtents te = dti.getTimeExtents();
                    if (te != null)
                    {
                        TimeSpan ts = te.getExtent();
                        if (ts != null && !ts.isTimeless() && !ts.isUnboundedEnd() && !ts.isZero())
                        {
                            catSet = RecentUpdateCategory.getCategoryNamesForSample(now, ts.getEnd(), true);
                        }
                    }
                }

                return catSet == null || catSet.isEmpty()
                        ? Collections.singleton(RecentUpdateCategory.COULD_NOT_DETERMINE_ACTIVITY.toString()) : catSet;
            }
        };
    }

    /**
     * The Enum RecentUpdateCategory.
     */
    public enum RecentUpdateCategory
    {
        /** The COULD_NOT_DETERMINE_ACTIVITY. */
        COULD_NOT_DETERMINE_ACTIVITY("Could not determine activity", -1),

        /** The LAST_12_HOURS. */
        LAST_12_HOURS("Last 12 Hours", 12 * 60 * 60 * 1000),

        /** The LAST_15_MINUTES. */
        LAST_15_MINUTES("Last 15 Minutes", 15 * 60 * 1000),

        /** The LAST_2_WEEKS. */
        LAST_2_WEEKS("Last 2 Weeks", 14 * 24 * 60 * 60 * 1000),

        /** The LAST_24_HOURS. */
        LAST_24_HOURS("Last 24 Hours", 24 * 60 * 60 * 1000),

        /** The LAST_3_HOURS. */
        LAST_3_HOURS("Last 3 Hours", 3 * 60 * 60 * 1000),

        /** The LAST_30_DAYS. */
        LAST_30_DAYS("Last 30 Days", 30 * 24 * 60 * 60 * 1000),

        /** The LAST_30_MINUTES. */
        LAST_30_MINUTES("Last 30 Minutes", 30 * 60 * 1000),

        /** The LAST_48_HOURS. */
        LAST_48_HOURS("Last 48 Hours", 48 * 60 * 60 * 1000),

        /** The LAST_5_MINUTES. */
        LAST_5_MINUTES("Last 5 Minutes", 5 * 60 * 1000),

        /** The LAST_6_HOURS. */
        LAST_6_HOURS("Last 6 Hours", 6 * 60 * 60 * 1000),

        /** The LAST_60_DAYS. */
        LAST_60_DAYS("Last 60 Days", 60 * 24 * 60 * 60 * 1000),

        /** The LAST_7_DAYS. */
        LAST_7_DAYS("Last 7 Days", 7 * 24 * 60 * 60 * 1000),

        /** The LAST_HOUR. */
        LAST_HOUR("Last Hour", 60 * 60 * 1000),

        /** The LAST_MINUTE. */
        LAST_MINUTE("Last Minute", 60 * 1000),

        /** TheNO_RECENT_ACTIVITY. */
        NO_RECENT_ACTIVITY("No recent activity", -1),

        /** The REPORTS_FUTURE_ACTIVITY. */
        REPORTS_FUTURE_ACTIVITY("Reports future activity", -1);

        /** The cat label. */
        private final String myCatLabel;

        /** The Offset. */
        private final long myOffset;

        /**
         * Gets the categories for sample.
         *
         * @param currentTime the current time
         * @param sampleTime the sample time
         * @param greedy the if greedy only adds the first offset type then
         *            stops.
         * @return the categories for sample
         */
        public static Set<RecentUpdateCategory> getCategoriesForSample(long currentTime, long sampleTime, boolean greedy)
        {
            Set<RecentUpdateCategory> aSet = New.set();
            for (RecentUpdateCategory cat : RecentUpdateCategory.values())
            {
                if (cat.hasOffset() && cat.isWithinRange(currentTime, sampleTime))
                {
                    aSet.add(cat);
                    if (greedy)
                    {
                        break;
                    }
                }
            }
            if (sampleTime < currentTime - LAST_60_DAYS.getOffset())
            {
                aSet.add(NO_RECENT_ACTIVITY);
            }
            if (sampleTime > currentTime)
            {
                aSet.add(REPORTS_FUTURE_ACTIVITY);
            }
            if (aSet.isEmpty())
            {
                aSet.add(COULD_NOT_DETERMINE_ACTIVITY);
            }
            return aSet;
        }

        /**
         * Gets the categories for sample.
         *
         * @param currentTime the current time
         * @param sampleTime the sample time
         * @param greedy if greedy only adds the first offset type then stops
         * @return the categories for sample
         */
        public static Set<String> getCategoryNamesForSample(long currentTime, long sampleTime, boolean greedy)
        {
            Set<String> aSet = New.set();
            for (RecentUpdateCategory cat : RecentUpdateCategory.values())
            {
                if (cat.hasOffset() && cat.isWithinRange(currentTime, sampleTime))
                {
                    aSet.add(cat.toString());
                    if (greedy)
                    {
                        break;
                    }
                }
            }
            if (sampleTime < currentTime - LAST_60_DAYS.getOffset())
            {
                aSet.add(NO_RECENT_ACTIVITY.toString());
            }
            if (sampleTime > currentTime)
            {
                aSet.add(REPORTS_FUTURE_ACTIVITY.toString());
            }
            if (aSet.isEmpty())
            {
                aSet.add(COULD_NOT_DETERMINE_ACTIVITY.toString());
            }
            return aSet;
        }

        /**
         * Instantiates a new recent update category.
         *
         * @param catLabel the cat label
         * @param offset the offset in time from now to fall in bin or -1 if not
         *            an offset.
         */
        RecentUpdateCategory(String catLabel, int offset)
        {
            myCatLabel = catLabel;
            myOffset = offset;
        }

        /**
         * Gets the offset.
         *
         * @return the offset
         */
        public long getOffset()
        {
            return myOffset;
        }

        /**
         * Checks for offset.
         *
         * @return true, if successful
         */
        public boolean hasOffset()
        {
            return myOffset != -1;
        }

        /**
         * Checks if is within offset.
         *
         * @param currentTime the current time
         * @param sampleTime the sample time
         * @return true, if is within offset
         */
        public boolean isWithinRange(long currentTime, long sampleTime)
        {
            if (myOffset != -1)
            {
                return sampleTime > currentTime - myOffset && sampleTime <= currentTime;
            }
            return false;
        }

        @Override
        public String toString()
        {
            return myCatLabel;
        }
    }
}
