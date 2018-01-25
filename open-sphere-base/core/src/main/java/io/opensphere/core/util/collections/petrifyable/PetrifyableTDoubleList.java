package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.list.TDoubleList;
import gnu.trove.procedure.TDoubleProcedure;

/**
 * A {@link TDoubleList} that is also {@link Petrifyable}.
 */
public interface PetrifyableTDoubleList extends TDoubleList, PetrifyableTDoubleCollection
{
    @Override
    PetrifyableTDoubleList grep(TDoubleProcedure condition);

    @Override
    PetrifyableTDoubleList inverseGrep(TDoubleProcedure condition);

    @Override
    PetrifyableTDoubleList subList(int begin, int end);
}
