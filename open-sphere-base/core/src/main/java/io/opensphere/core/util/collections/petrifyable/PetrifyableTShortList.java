package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.list.TShortList;
import gnu.trove.procedure.TShortProcedure;

/**
 * A {@link TShortList} that is also {@link Petrifyable}.
 */
public interface PetrifyableTShortList extends TShortList, PetrifyableTShortCollection
{
    @Override
    PetrifyableTShortList grep(TShortProcedure condition);

    @Override
    PetrifyableTShortList inverseGrep(TShortProcedure condition);

    @Override
    PetrifyableTShortList subList(int begin, int end);
}
