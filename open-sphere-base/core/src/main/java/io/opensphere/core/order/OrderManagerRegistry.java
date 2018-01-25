package io.opensphere.core.order;

import java.util.List;
import java.util.Set;

/**
 * The interface for the registry of order managers. The registry provides
 * access to order manager and handles creation of the managers. It is
 * sufficient to request the manager in order for it to be created. It is also
 * assumed that persisted orders for those managers will be retrieved on
 * creation. A manager's family is expected to be a broad group of items for
 * which the categories have a predefined order with the categories being sets
 * of items whose orders may be changed with respect to one another. It is
 * therefore recommended that the categories do not have overlapping ranges.
 */
public interface OrderManagerRegistry
{
    /**
     * Get all of the categories for which there are registered managers.
     *
     * @return All known category ids.
     */
    List<String> getAllCategories();

    /**
     * Gets the set of all the {@link OrderCategory} for a given family that
     * exist within the registry. Empty set if family is not currently in
     * registry.
     *
     * @param family the family for which to retrieve the OrderCategory set.
     * @return the categories for family or null if family is not within the
     *         registry.
     */
    Set<OrderCategory> getCategoriesForFamily(String family);

    /**
     * Gets the all families that currently exist within the registry.
     *
     * @return the all families
     */
    List<String> getFamilies();

    /**
     * Get the {@link OrderManager} which is applicable for the given key, if no
     * manager exists one will be created.
     *
     * @param participant the participant whose manager is desired.
     * @return the manager which matches the given key.
     */
    OrderManager getOrderManager(OrderParticipantKey participant);

    /**
     * Get the {@link OrderManager} for the provided family and category, if no
     * manager exists one will be created.
     *
     * @param family the category family by the manager.
     * @param category the category represented by the manager.
     * @return the manager for the family and category.
     */
    OrderManager getOrderManager(String family, OrderCategory category);
}
