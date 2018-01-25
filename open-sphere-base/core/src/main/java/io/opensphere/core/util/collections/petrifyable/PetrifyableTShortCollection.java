package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.TShortCollection;
import io.opensphere.core.util.SizeProvider;

/**
 * A {@link TShortCollection} that is also {@link Petrifyable}.
 */
public interface PetrifyableTShortCollection extends TShortCollection, Petrifyable, SizeProvider
{
}
