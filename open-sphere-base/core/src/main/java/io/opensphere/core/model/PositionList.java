package io.opensphere.core.model;

import java.util.List;

/**
 * Marker interface for a list of {@link Position}s.
 *
 * @param <E> The position type.
 */
public interface PositionList<E extends Position> extends List<E>
{
}
