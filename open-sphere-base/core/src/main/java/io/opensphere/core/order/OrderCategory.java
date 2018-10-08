package io.opensphere.core.order;

import java.util.Comparator;

import org.apache.commons.lang3.Range;

/**
 * Interface for categories within an order manager. A category defines as set
 * of participants whose orders may be changed with respect to one another.
 * NOTE: implementer of this interface are required to implement {@code equals}
 * and {@code hashCode}.
 */
public interface OrderCategory
{
    /**
     * A static case insensitive alphabetical comparator for categories by
     * category id.
     */
    Comparator<OrderCategory> ourCompareByCaseInsensitiveCategoryId = new Comparator<>()
    {
        @Override
        public int compare(OrderCategory o1, OrderCategory o2)
        {
            return o1.getCategoryId().compareToIgnoreCase(o2.getCategoryId());
        }
    };

    /** A static lexicographical comparator for categories by category id. */
    Comparator<OrderCategory> ourCompareByCategoryId = new Comparator<>()
    {
        @Override
        public int compare(OrderCategory o1, OrderCategory o2)
        {
            return o1.getCategoryId().compareTo(o2.getCategoryId());
        }
    };

    /**
     * A static comparator for comparing {@link OrderCategory} by order range
     * where the range with the lowest minimum integer is less than another
     * range.
     */
    Comparator<OrderCategory> ourCompareByOrderRange = new Comparator<>()
    {
        @Override
        public int compare(OrderCategory o1, OrderCategory o2)
        {
            int o1MinInt = o1.getOrderRange() == null ? Integer.MAX_VALUE : o1.getOrderRange().getMinimum().intValue();
            int o2MinInt = o2.getOrderRange() == null ? Integer.MAX_VALUE : o2.getOrderRange().getMinimum().intValue();
            return o1MinInt == o2MinInt ? 0 : o1MinInt < o2MinInt ? -1 : 1;
        }
    };

    /**
     * Get the id of the category.
     *
     * @return The category id.
     */
    String getCategoryId();

    /**
     * Get the maximum allowable order for the category.
     *
     * @return The maximum allowable order.
     */
    int getOrderMax();

    /**
     * Get the minimum allowable order for the category.
     *
     * @return The minimum allowable order.
     */
    int getOrderMin();

    /**
     * Get the allowable order range for this category.
     *
     * @return The allowable order range for this category.
     */
    Range<Integer> getOrderRange();
}
