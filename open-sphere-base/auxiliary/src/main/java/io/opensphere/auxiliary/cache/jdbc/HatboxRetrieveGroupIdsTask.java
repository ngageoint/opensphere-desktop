package io.opensphere.auxiliary.cache.jdbc;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.RetrieveGroupIdsTask;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Extension to {@link RetrieveGroupIdsTask} that has no result filters, since
 * Hatbox takes care of it for us.
 */
public class HatboxRetrieveGroupIdsTask extends RetrieveGroupIdsTask
{
    /**
     * Constructor.
     *
     * @param category The data model category. Any <code>null</code> values are
     *            treated as wildcards.
     * @param parameters Optional interval parameters for the query.
     * @param databaseTaskFactory The database task factory.
     * @param expirationRange If not {@code null}, the range that the groups'
     *            expiration times must lie within. If {@code null}, the groups'
     *            expiration times must be {@code null}.
     * @param critical If not {@code null}, the required criticality of the
     *            groups.
     */
    public HatboxRetrieveGroupIdsTask(DataModelCategory category, Collection<? extends IntervalPropertyMatcher<?>> parameters,
            DatabaseTaskFactory databaseTaskFactory, TimeSpan expirationRange, Boolean critical)
    {
        super(category, parameters, databaseTaskFactory, expirationRange, critical);
    }

    @Override
    protected Collection<? extends IntervalPropertyMatcher<?>> getResultFilterParameters()
    {
        return Collections.emptySet();
    }
}
