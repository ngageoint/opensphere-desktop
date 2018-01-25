package io.opensphere.core.util.collections.petrifyable;

import gnu.trove.list.TLongList;
import gnu.trove.procedure.TLongProcedure;

/**
 * A {@link TLongList} that is also {@link Petrifyable}.
 */
public interface PetrifyableTLongList extends TLongList, PetrifyableTLongCollection
{
    @Override
    TLongList grep(TLongProcedure condition);

    @Override
    TLongList inverseGrep(TLongProcedure condition);

    @Override
    TLongList subList(int begin, int end);
}
