package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.TLongCollection;
import io.opensphere.core.util.SizeProvider;

/**
 * A {@link TLongCollection} that is also {@link Petrifyable}.
 */
public interface PetrifyableTLongCollection extends TLongCollection, Petrifyable, SizeProvider
{
}
