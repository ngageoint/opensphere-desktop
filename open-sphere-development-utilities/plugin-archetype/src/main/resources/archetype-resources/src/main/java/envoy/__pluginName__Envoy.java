#set($symbol_pound='#')
#set($symbol_dollar='$')
#set($symbol_escape='\')
package ${package}.envoy;

import java.lang.UnsupportedOperationException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;

/**
 * TODO: Document me!
 */
public class ${pluginName}Envoy extends AbstractEnvoy
{
	/**
	 * The <code>Logger</code> instance used to capture output.
	 */
	public static final Logger LOG = Logger.getLogger(${pluginName}Envoy.class);

    /**
     * Constructs a new {@link ${pluginName}Envoy}.
     *
     * @param pToolbox The object through which system interaction occurs.
     */
    public ${pluginName}Envoy(Toolbox pToolbox)
    {
        super(pToolbox);
    }

	/**
	 * {@inheritDoc}
	 * 
	 * @see io.opensphere.core.api.adapter.AbstractEnvoy#open()
	 */
    @Override
    public void open()
    {
        // TODO: implement me!
		throw new UnsupportedOperationException("Not yet implemented.");
    }

	/**
	 * {@inheritDoc}
	 * 
	 * @see io.opensphere.core.api.adapter.AbstractEnvoy#providesDataFor(DataModelCategory)
	 */
	@Override
	public boolean providesDataFor(DataModelCategory category) 
	{
		// TODO: implement me!
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see io.opensphere.core.api.adapter.AbstractEnvoy#query(DataModelCategory,
	 *      Collection, List, List, int, Collection, CacheDepositReceiver)
	 */
	@Override
	public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
			List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
			Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
			throws InterruptedException, QueryException 
	{
		// TODO: Implement me!
		throw new UnsupportedOperationException("Not yet implemented.");
	}
}
