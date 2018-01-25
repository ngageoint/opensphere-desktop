package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.TDoubleCollection;
import io.opensphere.core.util.SizeProvider;

/**
 * A {@link TDoubleCollection} that is also {@link Petrifyable}.
 */
public interface PetrifyableTDoubleCollection extends TDoubleCollection, Petrifyable, SizeProvider
{
}
