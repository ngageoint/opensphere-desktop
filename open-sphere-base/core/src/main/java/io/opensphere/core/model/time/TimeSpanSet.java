package io.opensphere.core.model.time;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A set of timespans. Note that his does not fully conform to the set interface
 * entirely because overlapping timespans added to this set are merged, and when
 * TimeSpans are removed a larger TimeSpan may be sub-sected. So the number of
 * Timespans added may not represent the number of internal spans that exist.
 */
public interface TimeSpanSet extends Set<TimeSpan>
{
    /**
     * Adds the other {@link TimeSpanSet} to this set.
     *
     * Note that Timeless TimeSpan cannot be added to the set.
     *
     * @param other the other set to add to this set.
     * @return true, if the set was altered as a result of the add.
     */
    boolean add(TimeSpanSet other);

    /**
     * Checks to see if the set contains the specified date.
     *
     * @param aDate the a date to check
     * @return true, if contains
     */
    boolean contains(Date aDate);

    /**
     * Checks to see if the set contains the specified time.
     *
     * @param aTime the time to check
     * @return true, if contains
     */
    boolean contains(long aTime);

    /**
     * Checks to see if the set contains the specified time span.
     *
     * @param ts the time to check
     * @return true, if contains
     */
    boolean contains(TimeSpan ts);

    /**
     * Gets the list of TimeSpan in order that represents this set.
     *
     * Result is immutable.
     *
     * @return the time spans
     */
    List<TimeSpan> getTimeSpans();

    /**
     * Gets a new {@link TimeSpanSet} that represents the intersection of a
     * {@link TimeSpan} with this set.
     *
     * Note: That if ts is Timeless, the result set will be empty
     *
     * @param ts the {@link TimeSpan} to intersect with
     * @return the intersection, could be empty, but will not be null.
     */
    TimeSpanSet intersection(TimeSpan ts);

    /**
     * Gets a new {@link TimeSpanSet} that represents the intersection of a
     * another {@link TimeSpanSet} with this set.
     *
     * @param other the other to intersect with
     * @return the intersection, could be empty, but will not be null.
     */
    TimeSpanSet intersection(TimeSpanSet other);

    /**
     * True if the provided {@link TimeSpan} intersects ( overlaps ) with any.
     *
     * @param ts the to check for intersection.
     * @return true, if intersects. {@link TimeSpan} in this set.
     */
    boolean intersects(TimeSpan ts);

    /**
     * True if the provided {@link TimeSpanProvider} intersects ( overlaps )
     * with any of the internal {@link TimeSpan} in this set.
     *
     * @param ts the {@link TimeSpanProvider} check for intersection.
     * @return true, if intersects {@link TimeSpan} in this set.
     */
    boolean intersects(TimeSpanProvider ts);

    /**
     * True if the provided {@link TimeSpan} intersects ( overlaps ) with any of
     * the internal {@link TimeSpan} in this set.
     *
     * @param other the other to check for intersection
     * @return true, if intersects. {@link TimeSpan} in this set.
     *         {@link TimeSpan} in this set.
     */
    boolean intersects(TimeSpanSet other);

    /**
     * Removes the {@link TimeSpan} from the set.
     *
     * Note that Timeless TimeSpan cannot be removed from the set.
     *
     * @param ts the {@link TimeSpan} to remove
     * @return true, if the set was altered as a result of the remove.
     */
    boolean remove(TimeSpan ts);

    /**
     * Removes the other {@link TimeSpanSet} from this set.
     *
     * @param other the other to remove
     * @return true, if the set was altered as a result of the remove.
     */
    boolean remove(TimeSpanSet other);

    /**
     * The number of elements in this set.
     *
     * Note that this may be different than the number added because it merges
     * and sub-sects spans internally when they overlap etc.
     *
     * @return the number of {@link TimeSpan} in this set.
     */
    @Override
    int size();

    /**
     * Gets the union of this set and the other set and returns it as a new.
     *
     * @param other the other set to union with.
     * @return the union of this and the other set. {@link TimeSpanSet}.
     */
    TimeSpanSet union(TimeSpanSet other);
}
