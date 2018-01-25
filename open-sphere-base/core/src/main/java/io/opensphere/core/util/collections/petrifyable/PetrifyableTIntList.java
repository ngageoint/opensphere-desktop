package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;

/**
 * A {@link TIntList} that is also {@link Petrifyable}.
 */
public interface PetrifyableTIntList extends TIntList, PetrifyableTIntCollection
{
    @Override
    PetrifyableTIntList grep(TIntProcedure condition);

    @Override
    PetrifyableTIntList inverseGrep(TIntProcedure condition);

    @Override
    PetrifyableTIntList subList(int begin, int end);
}
