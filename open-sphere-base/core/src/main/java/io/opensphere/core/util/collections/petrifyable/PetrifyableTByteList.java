package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.list.TByteList;
import gnu.trove.procedure.TByteProcedure;

/**
 * A {@link TByteList} that is also {@link Petrifyable}.
 */
public interface PetrifyableTByteList extends TByteList, PetrifyableTByteCollection
{
    @Override
    PetrifyableTByteList grep(TByteProcedure condition);

    @Override
    PetrifyableTByteList inverseGrep(TByteProcedure condition);

    @Override
    PetrifyableTByteList subList(int begin, int end);
}
