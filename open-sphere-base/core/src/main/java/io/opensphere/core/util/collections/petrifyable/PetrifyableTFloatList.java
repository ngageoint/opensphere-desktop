package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.list.TFloatList;
import gnu.trove.procedure.TFloatProcedure;

/**
 * A {@link TFloatList} that is also {@link Petrifyable}.
 */
public interface PetrifyableTFloatList extends TFloatList, PetrifyableTFloatCollection
{
    @Override
    PetrifyableTFloatList grep(TFloatProcedure condition);

    @Override
    PetrifyableTFloatList inverseGrep(TFloatProcedure condition);

    @Override
    PetrifyableTFloatList subList(int begin, int end);
}
