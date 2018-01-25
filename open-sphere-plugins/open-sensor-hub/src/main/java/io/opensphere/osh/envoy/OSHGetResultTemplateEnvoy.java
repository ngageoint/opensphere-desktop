package io.opensphere.osh.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.sos.GetResultTemplateHandler;
import io.opensphere.osh.util.OSHRegistryUtils;

/** OpenSensorHub get result template envoy. */
public class OSHGetResultTemplateEnvoy extends AbstractOSHEnvoy
{
    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public OSHGetResultTemplateEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return OSHRegistryUtils.GET_RESULT_TEMPLATE_FAMILY.equals(category.getFamily());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws QueryException
    {
        try (TaskActivity ta = TaskActivity.createActive("Querying OpenSensorHub result template"))
        {
            getToolbox().getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);
            query(category, queryReceiver);
        }
    }

    /**
     * Performs the query.
     *
     * @param category The data model category.
     * @param queryReceiver An object that will receive {@link Query} objects
     *            produced by this data provider.
     * @throws QueryException If there is a problem with the query.
     */
    void query(DataModelCategory category, CacheDepositReceiver queryReceiver) throws QueryException
    {
        URL baseUrl = buildUrl(category);

        try (InputStream responseStream = performRequest(baseUrl))
        {
            Output result = parseResponse(responseStream);
            if (result != null)
            {
                queryReceiver.receive(new SimpleSessionOnlyCacheDeposit<>(category,
                        OSHRegistryUtils.GET_RESULT_TEMPLATE_DESCRIPTOR, Collections.singleton(result)));
            }
        }
        catch (IOException | CacheException e)
        {
            throw new QueryException(e);
        }
    }

    /**
     * Parses the input stream into a java object.
     *
     * @param stream the input stream to parse
     * @return the java object
     * @throws IOException if a problem occurred reading the stream
     */
    private Output parseResponse(InputStream stream) throws IOException
    {
        return GetResultTemplateHandler.parse(stream);
    }

    /**
     * Builds the URL for the category.
     *
     * @param category The data model category.
     * @return the URL
     */
    private URL buildUrl(DataModelCategory category)
    {
        StringBuilder urlString = new StringBuilder(256);
        urlString.append(OSHRegistryUtils.getUrl(category));
        urlString.append("?service=SOS&version=2.0&request=GetResultTemplate&offering=")
                .append(OSHRegistryUtils.getOfferingId(category));
        urlString.append("&observedProperty=").append(OSHRegistryUtils.getProperty(category));
        return UrlUtilities.toURL(urlString.toString());
    }
}
