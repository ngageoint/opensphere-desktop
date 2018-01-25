package io.opensphere.filterbuilder.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.filterbuilder.config.FilterBuilderConfiguration;
import io.opensphere.filterbuilder.config.FilterBuilderConfigurationManager;
import io.opensphere.filterbuilder.controller.FilterBuilderController;
import io.opensphere.filterbuilder.controller.FilterBuilderService;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.filter.DataLayerFilter;

/**
 * FilterBuilderPluginToolboxImpl.
 */
public class FilterBuilderToolboxImpl implements FilterBuilderToolbox
{
    /** The Constant DESCRIPTION. */
    private static final String DESCRIPTION = "Filter Builder Plugin Toolbox";

    /** The main toolbox from the application. */
    private final Toolbox myMainToolbox;

    /** The toolbox provided by the mantle. */
    private MantleToolbox myMantletoToolbox;

    /** The props. */
    private final Properties myProps;

    /** The configuration settings of this plugin. */
    private final FilterBuilderConfigurationManager myConfig;

    /** The data sources. */
    private Map<DataGroupInfo, List<DataTypeInfo>> myDataSources;

    /** The Controller. */
    private FilterBuilderController myController;

    /**
     * The service through which the filter builder is invoked.
     */
    private FilterBuilderService myFilterBuilderService;

    /**
     * Instantiates a new filter builder toolbox implementation.
     *
     * @param pToolbox the toolbox
     * @param pProps the props
     */
    public FilterBuilderToolboxImpl(Toolbox pToolbox, Properties pProps)
    {
        myMainToolbox = pToolbox;
        myProps = pProps;
        myConfig = new FilterBuilderConfigurationManager(
                myMainToolbox.getPreferencesRegistry().getPreferences(FilterBuilderConfiguration.class), myProps);
    }

    /**
     * Assign a reference to the public service interface. This method should only be called during initialization by the
     * FilterBuilderPlugin.
     *
     * @param svc the service interface implementation
     */
    public void setFbService(FilterBuilderService svc)
    {
        myFilterBuilderService = svc;
    }

    /**
     * Get the reference to the public service interface for the filter builder.
     *
     * @return as suggested above
     */
    @Override
    public FilterBuilderService getFbService()
    {
        return myFilterBuilderService;
    }

    @Override
    public Map<DataGroupInfo, List<DataTypeInfo>> getAvailbleDataTypes()
    {
        myDataSources = new TreeMap<>(DataGroupInfo.DISPLAY_NAME_COMPARATOR);

        DataGroupInfo server = null;

        List<DataGroupInfo> dataGroups = getMantleToolBox().getDataGroupController().createGroupList(null, new DataLayerFilter());
        for (DataGroupInfo dgi : dataGroups)
        {
            server = dgi.getTopParent();
            for (DataTypeInfo type : dgi.getMembers(false))
            {
                if (type.getMetaDataInfo() != null)
                {
                    if (!myDataSources.containsKey(server))
                    {
                        myDataSources.put(dgi.getTopParent(), new ArrayList<DataTypeInfo>());
                    }
                    if (!myDataSources.get(server).contains(type))
                    {
                        myDataSources.get(server).add(type);
                    }
                }
            }
        }
        return myDataSources;
    }

    @Override
    public List<String> getColumnsForDataType(DataTypeInfo dti)
    {
        List<String> columns = null;
        if (dti != null)
        {
            MetaDataInfo meta = dti.getMetaDataInfo();
            if (meta != null)
            {
                if (meta.getOriginalKeyNames().size() > 0)
                {
                    columns = new ArrayList<>(meta.getOriginalKeyNames());
                }
                else
                {
                    columns = new ArrayList<>(meta.getKeyNames());
                }
            }
        }
        if (columns == null)
        {
            columns = Collections.<String>emptyList();
        }
        return columns;
    }

    @Override
    public FilterBuilderConfigurationManager getConfiguration()
    {
        return myConfig;
    }

    @Override
    public FilterBuilderController getController()
    {
        return myController;
    }

    @Override
    public DataFilterRegistry getDataFilterRegistry()
    {
        return getMainToolBox().getDataFilterRegistry();
    }

    @Override
    public DataTypeInfo getDateTypeInfoForKey(String pKey)
    {
        if (pKey != null)
        {
            for (DataTypeInfo dti : getFilterableDataTypes())
            {
                if (dti.getTypeKey().equals(pKey))
                {
                    return dti;
                }
            }
        }
        return null;
    }

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public List<DataTypeInfo> getFilterableDataTypes()
    {
        ArrayList<DataTypeInfo> result = new ArrayList<>();

        for (List<DataTypeInfo> lDti : getAvailbleDataTypes().values())
        {
            result.addAll(lDti);
        }
        return result;
    }

    @Override
    public Toolbox getMainToolBox()
    {
        return myMainToolbox;
    }

    @Override
    public MantleToolbox getMantleToolBox()
    {
        if (myMantletoToolbox == null)
        {
            myMantletoToolbox = getMainToolBox().getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        }
        return myMantletoToolbox;
    }

    /**
     * Sets the value of the {@link #myController} field.
     *
     * @param pController the value to store in the {@link #myController} field.
     */
    public void setFilterBuilderController(FilterBuilderController pController)
    {
        if (pController == null)
        {
            throw new IllegalArgumentException("parameter pController must not be NULL");
        }
        myController = pController;
    }
}
