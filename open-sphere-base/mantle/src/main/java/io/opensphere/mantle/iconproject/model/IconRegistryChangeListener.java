package io.opensphere.mantle.iconproject.model;

import java.util.Collections;
import java.util.List;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;

/**
 *
 */
@FunctionalInterface
public interface IconRegistryChangeListener
{
    /**
     * Represents a report of changes done to the {@link IconRegistry}.
     *
     * Each change must be one of the following:
     * <ul>
     * <li><b>Add or remove change</b> : In this case, at least one of the
     * {@link #wasAdded()}, {@link #wasRemoved()} returns true.
     * <p>
     * The {@link #getRemoved()} method returns a list of elements that have
     * been replaced or removed from the list.
     * </ul>
     */
    public static class Change
    {
        /** The list of added IconRecords. */
        private final List<IconRecord> myAddedList;

        /** The list of removed IconRecords. */
        private final List<IconRecord> myRemovedList;

        /**
         * Constructs a new Change instance on the given list.
         *
         * @param addedList the list of additions to the registry, may be null.
         * @param removedList the list of removals to the registry, may be null.
         *
         */
        public Change(List<IconRecord> addedList, List<IconRecord> removedList)
        {
            if (addedList != null)
            {
                myAddedList = Collections.unmodifiableList(addedList);
            }
            else
            {
                myAddedList = Collections.emptyList();
            }
            if (removedList != null)
            {
                myRemovedList = Collections.unmodifiableList(removedList);
            }
            else
            {
                myRemovedList = Collections.emptyList();
            }
        }

        /**
         * An immutable list of removed/replaced elements. If no elements were
         * removed from the list, an empty list is returned.
         *
         * @return a list with all the removed elements
         * @throws IllegalStateException if this Change instance is in initial
         *             state
         */
        public List<IconRecord> getRemoved()
        {
            return myRemovedList;
        }

        /**
         * Indicates if elements were added during this change.
         *
         * @return true if something was added to the list
         * @throws IllegalStateException if this Change instance is in initial
         *             state
         */
        public boolean wasAdded()
        {
            return !getAdded().isEmpty();
        }

        /**
         * Indicates if elements were removed during this change. Note that
         * using set will also produce a change with {@code wasRemoved()}
         * returning true.
         *
         * @return true if something was removed from the list
         * @throws IllegalStateException if this Change instance is in initial
         *             state
         */
        public boolean wasRemoved()
        {
            return !getRemoved().isEmpty();
        }

        /**
         * Returns a subList view of the list that contains only the elements
         * added. This is actually a shortcut to
         * <code>c.getList().subList(c.getFrom(), c.getTo());</code>
         *
         * <pre>
         * {@code
         * for (Node n : change.getAddedSubList()) {
         *       // do something
         * }
         * }
         * </pre>
         *
         * @return the newly created sublist view that contains all the added
         *         elements.
         * @throws IllegalStateException if this Change instance is in initial
         *             state
         */
        public List<IconRecord> getAdded()
        {
            return myAddedList;
        }

        /**
         * Returns the size of {@link #getRemoved()} list.
         *
         * @return the number of removed items
         * @throws IllegalStateException if this Change instance is in initial
         *             state
         */
        public int getRemovedSize()
        {
            return getRemoved().size();
        }

        /**
         * Returns the size of the interval that was added.
         *
         * @return the number of added items
         * @throws IllegalStateException if this Change instance is in initial
         *             state
         */
        public int getAddedSize()
        {
            return getAdded().size();
        }
    }

    /**
     * Called after a change has been made to an ObservableList.
     *
     * @param c an object representing the change that was done
     * @see Change
     */
    public void onChanged(Change c);
}
