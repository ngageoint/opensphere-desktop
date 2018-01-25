package io.opensphere.mantle.data.filter;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * A predicate that only accepts {@link DataGroupInfo}s that have one of the
 * specified top-level parents.
 */
public class HasTopParentPredicate implements Predicate<DataGroupInfo>
{
    /** The allowed top parents. */
    private final Collection<? extends DataGroupInfo> myTopParents;

    /**
     * Constructor.
     *
     * @param topParents The approved top-level data group infos.
     */
    public HasTopParentPredicate(List<DataGroupInfo> topParents)
    {
        myTopParents = New.unmodifiableCollection(Utilities.checkNull(topParents, "topParents"));
    }

    @Override
    public boolean test(DataGroupInfo value)
    {
        return myTopParents.contains(value.getTopParent());
    }
}
