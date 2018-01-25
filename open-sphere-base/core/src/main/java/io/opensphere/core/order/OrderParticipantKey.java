package io.opensphere.core.order;

/**
 * A key for participants in an order manager. Implementers of this interface
 * should include enough information to uniquely identify the participating
 * entity. NOTE: implementer of this interface are required to implement
 * {@code equals} and {@code hashCode}.
 */
public interface OrderParticipantKey
{
    /**
     * Get the category.
     *
     * @return the category
     */
    OrderCategory getCategory();

    /**
     * Get the family.
     *
     * @return the family
     */
    String getFamily();

    /**
     * Get the uniquely identifying ID for this key.
     *
     * @return the unique ID.
     */
    String getId();
}
