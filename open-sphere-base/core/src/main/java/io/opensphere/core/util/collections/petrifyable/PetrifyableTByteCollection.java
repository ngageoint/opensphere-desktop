package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.TByteCollection;
import io.opensphere.core.util.SizeProvider;

/**
 * A {@link TByteCollection} that is also {@link Petrifyable}.
 */
public interface PetrifyableTByteCollection extends TByteCollection, Petrifyable, SizeProvider
{
}
