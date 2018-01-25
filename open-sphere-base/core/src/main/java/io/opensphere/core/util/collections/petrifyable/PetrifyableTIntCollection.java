package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.TIntCollection;
import io.opensphere.core.util.SizeProvider;

/**
 * A {@link TIntCollection} that is also {@link Petrifyable}.
 */
public interface PetrifyableTIntCollection extends TIntCollection, Petrifyable, SizeProvider
{
}
