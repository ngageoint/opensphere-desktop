package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.TCharCollection;
import io.opensphere.core.util.SizeProvider;

/**
 * A {@link TCharCollection} that is also {@link Petrifyable}.
 */
public interface PetrifyableTCharCollection extends TCharCollection, Petrifyable, SizeProvider
{
}
