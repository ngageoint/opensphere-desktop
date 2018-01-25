package io.opensphere.controlpanels.layers.groupby;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.time.TimelineUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoActiveHistoryRecord;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupCategorizer;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class GroupByTitleTreeBuilder.
 */
public class GroupByAvailableRecentlyActiveTreeBuilder extends GroupByDefaultTreeBuilder
{
    /** The my1 day ago. */
    private final TimeSpan my1DayAgo = TimelineUtilities.getYesterday();

    /** The active ids. */
    private final Set<String> myActiveIds;

    /** The my active id to record map. */
    private final Map<String, DataGroupInfoActiveHistoryRecord> myActiveIdToRecordMap;

    /** The my current time. */
    private final long myCurrentTime = System.currentTimeMillis();

    /** The my last week. */
    private final TimeSpan myLastWeek;

    /** The my this week. */
    private final TimeSpan myThisWeek = TimelineUtilities.getPartialWeek();

    /** The my today. */
    private final TimeSpan myToday = TimelineUtilities.getToday();

    /** The Two weeks ago. */
    private final TimeSpan myTwoWeeksAgo;

    {
        myLastWeek = TimeSpan.get(TimelineUtilities.getPrecedingWeeks(2, true).getStart(), myThisWeek.getStart());
        myTwoWeeksAgo = TimeSpan.get(TimelineUtilities.getPrecedingWeeks(3, true).getStart(), myLastWeek.getStart());
    }

    /**
     * Instantiates a new group by title tree builder.
     */
    public GroupByAvailableRecentlyActiveTreeBuilder()
    {
        myActiveIds = New.set();
        myActiveIdToRecordMap = New.map();
    }

    @Override
    public String getGroupByName()
    {
        return "Recently Used";
    }

    @Override
    public GroupCategorizer getGroupCategorizer()
    {
        return new GroupCategorizer()
        {
            @Override
            public List<String> getAllCategories()
            {
                TimeCategory[] array = TimeCategory.values();
                List<String> allCats = New.list(array.length);
                for (TimeCategory tc : array)
                {
                    if (getCategories().contains(tc.toString()))
                    {
                        allCats.add(tc.toString());
                    }
                }
                return allCats;
            }

            @Override
            public Set<String> getGroupCategories(DataGroupInfo dgi)
            {
                DataGroupInfoActiveHistoryRecord rec = myActiveIdToRecordMap.get(dgi.getId());
                Set<TimeCategory> tcSet = getTimeCategory(rec.getDate());
                Set<String> result = New.set();
                for (TimeCategory tc : tcSet)
                {
                    result.add(tc.toString());
                }
                getCategories().addAll(result);
                return result;
            }

            @Override
            public Set<String> getTypeCategories(DataTypeInfo dti)
            {
                return Collections.<String>emptySet();
            }
        };
    }

    @Override
    public Predicate<DataGroupInfo> getGroupFilter()
    {
        return new Predicate<DataGroupInfo>()
        {
            @Override
            public boolean test(DataGroupInfo value)
            {
                return value.hasMembers(false) && myActiveIds.contains(value.getId());
            }
        };
    }

    @Override
    public void initializeForAvailable(Toolbox toolbox)
    {
        super.initializeForAvailable(toolbox);
        List<DataGroupInfoActiveHistoryRecord> activityHistory = MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController()
                .getActiveHistoryList();
        Collections.sort(activityHistory, new Comparator<DataGroupInfoActiveHistoryRecord>()
        {
            @Override
            public int compare(DataGroupInfoActiveHistoryRecord o1, DataGroupInfoActiveHistoryRecord o2)
            {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        long timeCutOff = myCurrentTime - 21 * 24 * 60 * 60000;

        for (DataGroupInfoActiveHistoryRecord rec : activityHistory)
        {
            if (rec.getDate().getTime() > timeCutOff && myActiveIds.add(rec.getId()))
            {
                myActiveIdToRecordMap.put(rec.getId(), rec);
            }
        }
    }

    /**
     * Gets the time category.
     *
     * @param aDate the a date
     * @return the time category
     */
    private Set<TimeCategory> getTimeCategory(Date aDate)
    {
        Set<TimeCategory> set = New.set();
        if (myToday.overlaps(aDate.getTime()))
        {
            set.add(TimeCategory.TODAY);
        }
        if (my1DayAgo.overlaps(aDate.getTime()))
        {
            set.add(TimeCategory.YESTERDAY);
        }
        part2(aDate, set);
        return set;
    }

    /**
     * Part2.
     *
     * @param aDate the a date
     * @param set the set
     */
    private void part2(Date aDate, Set<TimeCategory> set)
    {
        if (myThisWeek.overlaps(aDate.getTime()))
        {
            set.add(TimeCategory.THIS_WEEK);
        }
        if (myLastWeek.overlaps(aDate.getTime()))
        {
            set.add(TimeCategory.LAST_WEEK);
        }
        if (myTwoWeeksAgo.overlaps(aDate.getTime()))
        {
            set.add(TimeCategory.TWO_WEEKS_AGO);
        }
    }

    /**
     * The Enum TimeCategory.
     */
    public enum TimeCategory
    {
        /** The LAST_WEEK. */
        LAST_WEEK("Last Week"),

        /** The THIS_WEEK. */
        THIS_WEEK("This Week"),

        /** The TODAY. */
        TODAY("Today"),

        /** The LAST_WEEK. */
        TWO_WEEKS_AGO("Two Weeks Ago"),

        /** The YESTERDAY. */
        YESTERDAY("Yesterday");

        /** The label. */
        private final String myLabel;

        /**
         * Instantiates a new activity category.
         *
         * @param label the label
         */
        TimeCategory(String label)
        {
            myLabel = label;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }
    }
}
