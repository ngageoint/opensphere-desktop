package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.TFloatCollection;
import io.opensphere.core.util.SizeProvider;

/**
 * A {@link TFloatCollection} that is also {@link Petrifyable}.
 */
public interface PetrifyableTFloatCollection extends TFloatCollection, Petrifyable, SizeProvider
{
}
