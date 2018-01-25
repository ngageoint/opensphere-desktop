package io.opensphere.mantle.data.filter;

import java.util.function.Predicate;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * A predicate that only accepts {@link DataGroupInfo}s that have members that
 * pass a specified predicate.
 */
public class HasPredicatedMemberPredicate implements Predicate<DataGroupInfo>
{
    /** The predicate for the members. */
    private final Predicate<? super DataTypeInfo> myMemberPredicate;

    /**
     * Constructor.
     *
     * @param memberPredicate The predicate to be applied to the members of the
     *            data groups.
     */
    public HasPredicatedMemberPredicate(Predicate<? super DataTypeInfo> memberPredicate)
    {
        myMemberPredicate = memberPredicate;
    }

    @Override
    public boolean test(DataGroupInfo value)
    {
        return value.getMembers(false).stream().anyMatch(myMemberPredicate);
    }
}
