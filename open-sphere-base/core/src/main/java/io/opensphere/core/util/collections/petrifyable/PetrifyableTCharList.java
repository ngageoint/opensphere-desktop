package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.list.TCharList;
import gnu.trove.procedure.TCharProcedure;

/**
 * A {@link TCharList} that is also {@link Petrifyable}.
 */
public interface PetrifyableTCharList extends TCharList, PetrifyableTCharCollection
{
    @Override
    PetrifyableTCharList grep(TCharProcedure condition);

    @Override
    PetrifyableTCharList inverseGrep(TCharProcedure condition);

    @Override
    PetrifyableTCharList subList(int begin, int end);
}
