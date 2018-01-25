package io.opensphere.auxiliary.cache.jdbc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.RetrieveGroupValuesTask;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;

/**
 * Extension to {@link RetrieveGroupValuesTask} that has no result filters,
 * since Hatbox takes care of it for us.
 */
public class HatboxRetrieveGroupValuesTask extends RetrieveGroupValuesTask
{
    /**
     * Constructor.
     *
     * @param category The optional data model category. Null values in the
     *            category indicate any value is acceptable.
     * @param parameters The optional parameters on the query.
     * @param resultMap An input/output map that contains the descriptors for
     *            the properties to be retrieved, and a list of results to be
     *            populated for each one.
     * @param databaseTaskFactory The database task factory.
     */
    public HatboxRetrieveGroupValuesTask(DataModelCategory category, Collection<? extends IntervalPropertyMatcher<?>> parameters,
            Map<? extends PropertyDescriptor<?>, ? extends List<?>> resultMap, DatabaseTaskFactory databaseTaskFactory)
    {
        super(category, parameters, resultMap, databaseTaskFactory);
    }

    @Override
    protected Collection<? extends IntervalPropertyMatcher<?>> getResultFilterParameters()
    {
        return Collections.emptySet();
    }
}
