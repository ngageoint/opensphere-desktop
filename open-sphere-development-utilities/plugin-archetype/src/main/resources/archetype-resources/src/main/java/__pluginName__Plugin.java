#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;

/**
 * TODO: Document me! 
 */
public class ${pluginName}Plugin extends PluginAdapter
{
	/**
	 * The <code>Logger</code> instance used to capture output.
	 */
	public static final Logger LOG = Logger.getLogger(${pluginName}Plugin.class);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see io.opensphere.core.api.adapter.PluginAdapter#initialize(PluginLoaderData, Toolbox)
	 */
    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
    	// TODO: Implement the initialize method. 
    	// Typical initialization routines include generating transformers, setting up the plugin's toolbox, etc.
    }
}
