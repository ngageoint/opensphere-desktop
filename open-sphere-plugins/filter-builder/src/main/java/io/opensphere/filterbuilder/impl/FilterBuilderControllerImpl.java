package io.opensphere.filterbuilder.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.export.Exporters;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.filterbuilder.config.FilterBuilderConfiguration;
import io.opensphere.filterbuilder.controller.FilterBuilderController;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.filterbuilder.controller.FilterSet;
import io.opensphere.filterbuilder.filter.v1.CombinationRule;
import io.opensphere.filterbuilder.filter.v1.CombinationRules;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.FilterChangeEvent;
import io.opensphere.filterbuilder.filter.v1.FilterChangeListener;
import io.opensphere.filterbuilder.filter.v1.FilterList;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.filterbuilder.filter.v1.Source;
import io.opensphere.filterbuilder2.copy.FilterCopier;
import io.opensphere.filterbuilder2.manager.FilterManagerDialog;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.mdfilter.CustomBinaryLogicOpType;
import io.opensphere.mantle.data.element.mdfilter.CustomFilter;

/**
 * The implementation of FilterBuilderController.
 */
@SuppressWarnings("PMD.GodClass")
public class FilterBuilderControllerImpl implements FilterBuilderController
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(FilterBuilderControllerImpl.class);

    /** The load filters preference key. */
    private static final String LOAD_FILTERS_PREF = "loadFilters";

    /** The combination rules preference key. */
    private static final String COMBINATION_RULES_PREF = "combinationRules";

    /** The filters. */
    private final FilterList myFilters;

    /** The combination rules. */
    private CombinationRules myCombinationRules;

    /** The map from data type key to operator GUI model. */
    private final Map<String, ChoiceModel<Logical>> myOperatorModelMap;

    /** The default file. */
    private final File myDefaultFile;

    /** The Toolbox. */
    private final FilterBuilderToolbox myToolbox;

    /**
     * The registry in which filters are published.
     */
    private final DataFilterRegistry myFilterRegistry;

    /** The executor for saving the config. */
    private final Executor mySaveExecutor = new ProcrastinatingExecutor(Executors.newScheduledThreadPool(1), 100);

    /** The filter change listener that's hooked up to each filter. */
    private final FilterChangeListener myIndividualFilterChangeListener;

    /** The change support. */
    private final ChangeSupport<FilterChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /** The Preferences registry. */
    private final PreferencesRegistry myPrefsRegistry;

    /** The filter copier. */
    private final FilterCopier myCopier;

    /**
     * Get a FilterSet representing the filters that apply to the specified data
     * type. The set optionally includes inactive as well as active filters.
     *
     * @param key the key used to narrow the filter set.
     * @param all true to return all filters, false to narrow the set using the
     *            active filters.
     * @return the stuff
     */
    public FilterSet getFilterSet(String key, boolean all)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("data type must be specified");
        }
        FilterSet fs = new FilterSet(key);
        fs.setFilterOp(myCombinationRules.get(key));
        myFilters.stream()
                .forEach(f -> f.getOtherSources().stream().filter(s -> (all || s.isActive()) && s.getTypeKey().equals(key))
                        .forEach(s -> fs.getFilters().add(myCopier.copyFilter(f, key))));
        return fs;
    }

    /**
     * Displays a dialog to the user to allow for the filter set to be changed.
     *
     * @param fs the filter set to edit.
     * @return true if the user accepted changes, false otherwise.
     */
    public boolean editFilterSet(FilterSet fs)
    {
        return FilterManagerDialog.showFilterSet(myToolbox, fs);
    }

    /**
     * Combine the active filters in the provided FilterSet into a single Filter
     * using the logical operator found in the FilterSet.
     *
     * @param fs the FilterSet
     * @return the Filter
     */
    public Filter fuseFilters(FilterSet fs)
    {
        Filter f = new Filter(fs.getTypeKey() + " Master Filter", Source.fromTypeKey(fs.getTypeKey()));
        Group g = f.getFilterGroup();
        g.setLogicOperator(fs.getLogicOp());

        fs.getFilters().stream().filter(v -> v.isActive())
                .forEach(v -> g.addFilterGroup(myCopier.copyFilter(v).getFilterGroup()));

        return f;
    }

    /**
     * Instantiates a new filter builder controller implementation.
     *
     * @param pToolbox the toolbox
     */
    public FilterBuilderControllerImpl(FilterBuilderToolbox pToolbox)
    {
        if (pToolbox == null)
        {
            throw new IllegalArgumentException("the FilterBuilderToolbox parameter (pToolbox) must not be null");
        }
        myToolbox = pToolbox;
        myFilterRegistry = myToolbox.getDataFilterRegistry();
        myFilters = new FilterList();
        myOperatorModelMap = new HashMap<>();
        myDefaultFile = myToolbox.getConfiguration().getLastFile();
        myPrefsRegistry = myToolbox.getMainToolBox().getPreferencesRegistry();
        myCopier = new FilterCopier(myFilterRegistry.getColumnMappingController());
        myIndividualFilterChangeListener = new FilterChangeListener()
        {
            @Override
            public void filterChanged(FilterChangeEvent e)
            {
                if (!e.getFilter().isVirtual())
                {
                    LOGGER.warn("FBCI::<<CONS>>::new FCL::filterChanged:  fired by a prototype");
                }
                if (e.getChangeType() == FilterChangeEvent.ACTIVE_STATE)
                {
                    updateActiveFilter(e.getFilter().getTypeKey());
                }
                fireFilterChangeEvent(e);
            }
        };

        loadDefaultFile();
    }

    @Override
    public boolean addFilter(DataFilter filter)
    {
        if (filter instanceof Filter)
        {
            addFilter((Filter)filter);
        }
        else
        {
            throw new UnsupportedOperationException(
                    "FilterBuilderControllerImpl.addFilter(DataFilter filter not yet implemented");
        }
        return true;
    }

    @Override
    public void updateFilter(Filter f)
    {
        if (!f.isVirtual())
        {
            LOGGER.warn("updated a prototype");
            return;
        }

        // discard the old prototype and maybe replace it with a new one
        boolean deleted = f.getOtherSources().isEmpty();
        Filter oldPar = f.getParent();
        myFilters.remove(oldPar);
        if (!deleted)
        {
            Filter newPar = myCopier.copyFilter(f);
            f.setParent(newPar);
            myFilters.add(newPar);
        }

        // reconstruct the active filter mechanisms
        f.getOtherSources().stream().forEach(s -> updateActiveFilter(s.getTypeKey()));
        if (deleted)
        {
            fireFilterChangeEvent(oldPar, FilterChangeEvent.FILTER_REMOVED);
        }
        else
        {
            fireFilterChangeEvent(f, FilterChangeEvent.STRUCTURE_CHANGED);
        }
    }

    @Override
    public void addFilterChangeListener(FilterChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    public void exportFilters(File file, String dataTypeKey, boolean isActiveOnly) throws JAXBException
    {
        List<Filter> filters = getFilters(dataTypeKey);
        if (isActiveOnly)
        {
            filters = filters.stream().filter(f -> f.isActive()).collect(Collectors.toList());
        }

        try
        {
            Exporters.getExporter(filters, myToolbox.getMainToolBox(), File.class).export(file);
        }
        catch (IOException | ExportException e)
        {
            String msg = "Failed to export filters: " + e;
            UserMessageEvent.error(myToolbox.getMainToolBox().getEventManager(), msg);
            LOGGER.error(msg, e);
        }

        myToolbox.getConfiguration().setLastFile(file);
    }

    @Override
    public FilterList getAllFilters()
    {
        return myFilters;
    }

    @Override
    public ChoiceModel<Logical> getCombinationOperator(String dataTypeKey)
    {
        ChoiceModel<Logical> operator = myOperatorModelMap.get(dataTypeKey);
        if (operator != null)
        {
            return operator;
        }
        // not there => create it
        CombinationRule rule = new CombinationRule();
        rule.setTypeKey(dataTypeKey);
        rule.setOperator(Logical.AND);
        addCombinationRule(rule);
        // it should be there now
        return myOperatorModelMap.get(dataTypeKey);
    }

    @Override
    public List<Filter> getFilters(String typeKey)
    {
        return createVirtualFiltersNoListener(typeKey);
    }

    @Override
    public List<Filter> createVirtualFilters(String typeKey)
    {
        List<Filter> filters = createVirtualFiltersNoListener(typeKey);
        for (Filter filter : filters)
        {
            filter.addFilterChangeListener(myIndividualFilterChangeListener);
        }
        return filters;
    }

    @Override
    public void importFilters(File file)
    {
        try
        {
            boolean filterAdded = false;
            List<CustomFilter> importFilters = new FilterReader().readFilters(file);
            if (importFilters != null)
            {
                for (CustomFilter filter : importFilters)
                {
                    if (filter instanceof CustomBinaryLogicOpType)
                    {
                        // Fill in server name as website doesn't fill it in
                        if (filter.getServerName() == null)
                        {
                            filter.setServerName(getServerName(filter.getUrlKey()));
                        }

                        addFilter(convert((CustomBinaryLogicOpType)filter));
                        filterAdded = true;
                    }
                }
            }
            myToolbox.getConfiguration().setLastFile(file);
            if (filterAdded)
            {
                saveFilters();
            }
        }
        catch (JAXBException e)
        {
            LOGGER.error("Unable to unmarshal filter(s) from: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void removeFilter(DataFilter filter)
    {
        if (filter instanceof Filter)
        {
            removeFilter((Filter)filter);
        }
        else
        {
            throw new UnsupportedOperationException(
                    "FilterBuilderControllerImpl.removeFilter(DataFilter filter not yet implemented");
        }
    }

    @Override
    public void removeFilterChangeListener(FilterChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void saveFilters()
    {
        mySaveExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Preferences preferences = myPrefsRegistry.getPreferences(FilterBuilderConfiguration.class);
                preferences.putJAXBObject(LOAD_FILTERS_PREF, myFilters, false, this);
            }
        });
    }

    @Override
    public void setCombinationOperator(String dataTypeKey, Logical logicOp)
    {
        ChoiceModel<Logical> operator = myOperatorModelMap.get(dataTypeKey);
        if (operator == null)
        {
            CombinationRule rule = new CombinationRule();
            rule.setTypeKey(dataTypeKey);
            rule.setOperator(logicOp);
            addCombinationRule(rule);
        }
    }

    /**
     * Adds a combination rule.
     *
     * @param rule the rule
     */
    private void addCombinationRule(CombinationRule rule)
    {
        myCombinationRules.add(rule);
        myOperatorModelMap.put(rule.getTypeKey(), createOperatorModel(rule));
        saveCombinationRules();
    }

    /**
     * Adds the filter.
     *
     * @param filter the filter
     */
    private void addFilter(Filter filter)
    {
        myFilters.add(filter);
        // update for all active supported types
        filter.getOtherSources().stream().filter(s -> s.isActive()).forEach(s -> updateActiveFilter(s.getTypeKey()));

        // Check for combination rules and add if necessary.
        if (!myOperatorModelMap.containsKey(filter.getTypeKey()))
        {
            setCombinationOperator(filter.getTypeKey(), Logical.AND);
        }

        // GCD: this listener should never fire from a filter prototype.
        filter.addFilterChangeListener(myIndividualFilterChangeListener);
        Filter vf = myCopier.virtualFilter(filter, filter.getTypeKey());
        fireFilterChangeEvent(vf, FilterChangeEvent.FILTER_ADDED);
    }

    /**
     * Perform the filter conversion and set parameters in the filter's source.
     * The layer key should contain at least one layer separator character (!!)
     * in order to get the layer name. The method will also check for namespaces
     * that are prepended to the layer name. They will be in the form
     * 'namespace:layerName'.
     *
     * @param filter the filter to convert
     * @return the converted filter
     */
    private Filter convert(CustomBinaryLogicOpType filter)
    {
        Filter dataFilter = new WFS110FilterToDataFilterConverter().apply(filter);
        String typeKey = filter.getUrlKey();
        String typeName = null;

        String[] keyTok = filter.getUrlKey().split("!!");
        if (keyTok.length >= 2)
        {
            // Assume the layer name is the last part of the url key.
            typeName = keyTok[keyTok.length - 1];
            StringBuilder sb = new StringBuilder(keyTok[0]);
            sb.append("!!");
            // Check for and remove any namespace from the layer name.
            String[] layerTok = keyTok[1].split(":");
            if (layerTok.length == 2)
            {
                typeName = layerTok[1];
                sb.append(layerTok[1]);
                typeKey = sb.toString();
            }
        }
        else
        {
            keyTok = filter.getUrlKey().split("/");
            typeName = keyTok[keyTok.length - 1];
        }

        dataFilter.getSource().setTypeKey(typeKey);
        dataFilter.getSource().setTypeName(typeName);
        dataFilter.getSource().setTypeDisplayName(typeName);
        dataFilter.getSource().setServerName(filter.getServerName());

        return dataFilter;
    }

    /**
     * Creates a filter combination operator model for the given data type key.
     *
     * @param rule the rule
     * @return the operator model
     */
    private ChoiceModel<Logical> createOperatorModel(CombinationRule rule)
    {
        ChoiceModel<Logical> operator = new ChoiceModel<>();
        operator.setOptions(new Logical[] { Logical.AND, Logical.OR });
        operator.setNameAndDescription("Operator", "Whether data must pass all/any active filters for the layer");
        operator.set(rule.getOperator());
        operator.addListener(new ChangeListener<Logical>()
        {
            @Override
            public void changed(ObservableValue<? extends Logical> observable, Logical oldValue, Logical newValue)
            {
                rule.setOperator(operator.get());
                updateActiveFilter(rule.getTypeKey());
                saveCombinationRules();
            }
        });
        return operator;
    }

    /**
     * Creates virtual filters for the type key.
     *
     * @param typeKey the type key
     * @return the virtual filters
     */
    private List<Filter> createVirtualFiltersNoListener(String typeKey)
    {
        return myFilters.stream()
                .flatMap(f -> f.getOtherSources().stream().filter(s -> typeKey == null || typeKey.equals(s.getTypeKey()))
                        .map(s -> myCopier.virtualFilter(f, s.getTypeKey())))
                .collect(Collectors.toList());
    }

    /**
     * Fires a FilterChangeEvent.
     *
     * @param filter the filter
     * @param changeType the change type
     */
    private void fireFilterChangeEvent(Filter filter, int changeType)
    {
        fireFilterChangeEvent(new FilterChangeEvent(filter, this, changeType));
    }

    /**
     * Fires a FilterChangeEvent.
     *
     * @param event the event
     */
    private void fireFilterChangeEvent(final FilterChangeEvent event)
    {
        myChangeSupport.notifyListeners(listener -> listener.filterChanged(event));
    }

    /**
     * Gets a server name from the given URL key.
     *
     * @param urlKey the URL key
     * @return the server name
     */
    private String getServerName(final String urlKey)
    {
        String serverName = null;
        if (StringUtils.isNotEmpty(urlKey))
        {
            // Since server name is not necessarily included in a filter, check
            // valid server's URL's
            // that match the incoming filter's UrlKey and get the server name
            // from the server's group info.
            Set<DataGroupInfo> groups = myToolbox.getMantleToolBox().getDataGroupController().getDataGroupInfoSet();
            for (DataGroupInfo group : groups)
            {
                if (group.getProviderType().equals("OGC Server"))
                {
                    Set<DataTypeInfo> matchingTypes = group.findMembers(type -> urlKey.startsWith(type.getUrl()), true, true);
                    if (!matchingTypes.isEmpty())
                    {
                        serverName = group.getDisplayName();
                    }
                }
            }

            // If we couldn't find the server in our configuration, just set it
            // to the URL
            if (serverName == null)
            {
                String[] keyTok = urlKey.split("!!");
                serverName = keyTok.length >= 2 ? keyTok[0] : urlKey;
            }
        }
        // Final check to ensure
        if (serverName == null)
        {
            serverName = "UNKNOWN";
        }
        return serverName;
    }

    /**
     * Load default file.
     */
    private void loadDefaultFile()
    {
        if (myFilters.isEmpty())
        {
            // Load the default filters on an envoy thread to avoid having to
            // create a new thread
            DefaultFileEnvoy envoy = new DefaultFileEnvoy(myToolbox.getMainToolBox());
            myToolbox.getMainToolBox().getEnvoyRegistry().addObjectsForSource(this, Collections.singleton(envoy));
        }
    }

    /**
     * Load filters.
     *
     * @param file the file
     */
    private void loadFilters(File file)
    {
        try
        {
            Preferences preferences = myPrefsRegistry.getPreferences(FilterBuilderConfiguration.class);
            FilterList filters = preferences.getJAXBObject(FilterList.class, LOAD_FILTERS_PREF, null);

            // If the user has no filters, they may be using 5.0 for the first
            // time, so read their current filters in. Once all users are on 5.0
            // or greater, this can be removed.
            if (filters == null && file != null && file.exists())
            {
                filters = XMLUtilities.readXMLObject(file, FilterList.class);
                myToolbox.getConfiguration().setLastFile(file);
            }

            myCombinationRules = preferences.getJAXBObject(CombinationRules.class, COMBINATION_RULES_PREF,
                    new CombinationRules());
            myCombinationRules.getRules().stream().forEach(r -> myOperatorModelMap.put(r.getTypeKey(), createOperatorModel(r)));

            myFilters.clear();
            if (filters != null)
            {
                for (Filter f : filters)
                {
                    // Make sure the native type is included among the
                    // supported types--old files may not include it in the
                    // list, as it is implicitly supported.
                    Source nativeSource = f.getSource();
                    nativeSource.setActive(f.isActive());
                    String nativeType = nativeSource.getTypeKey();
                    boolean found = f.getOtherSources().stream().anyMatch(s -> s.getTypeKey().equals(nativeType));
                    if (!found)
                    {
                        f.getOtherSources().add(nativeSource);
                    }

                    // GCD: setting the group name is probably unnecessary
                    f.getFilterGroup().setName(f.getName());
                    addFilter(f);
                }
            }
        }
        catch (JAXBException e)
        {
            LOGGER.error("Unable to unmarshal filter(s) from: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Removes the filter's support for its native datatype. There are three
     * cases describing how this method behaves relative to the type being
     * unsupported in the prototype:
     * <ul>
     * <li><b>A non-native type</b>: the typeKey is dropped from the list of
     * supported types</li>
     * <li><b>The only supported type</b>: the prototype is deleted</li>
     * <li><b>The native type with others supported</b>: A new prototype is
     * created with using one of the other supported datatypes as its native
     * type; the deleted typeKey is not included in the new list of supported
     * types.</li>
     * </ul>
     *
     * @param filter the filter
     */
    private void removeFilter(Filter filter)
    {
        if (!filter.isVirtual())
        {
            LOGGER.warn("removed a prototype!");
            return;
        }
        String typeKey = filter.getTypeKey();
        Filter par = filter.getParent();
        if (par.getOtherSources().size() == 1)
        {
            // if the prototype has only one supported type, deleting support
            // for that one type causes the prototype to be deleted
            myFilters.remove(par);
        }
        else if (!par.getTypeKey().equals(typeKey))
        {
            // if support is removed for a type other than the prototype's
            // native type, just take the Source out of its support list
            filter.removeFromParent();
        }
        else
        {
            // if support for the prototype's native type is removed, then
            // another prototype will have to be created
            String altType = par.getOtherSources().stream().map(s -> s.getTypeKey()).filter(t -> !t.equals(typeKey)).findAny()
                    .get();
            // altType cannot be null--more than one type must be present
            Filter copy = myCopier.copyFilter(par, altType);
            copy.getOtherSources().removeIf(s -> s.getTypeKey().equals(par.getTypeKey()));
            myFilters.remove(par);
            myFilters.add(copy);
        }

        // if the filter was active on the type for which support was removed,
        // reconstruct the active filtering mechanism
        if (filter.isActive())
        {
            updateActiveFilter(typeKey);
        }

        filter.removeFilterChangeListener(myIndividualFilterChangeListener);
        fireFilterChangeEvent(filter, FilterChangeEvent.FILTER_REMOVED);
    }

    /**
     * Saves the combination rules to the preferences.
     */
    private void saveCombinationRules()
    {
        Preferences preferences = myPrefsRegistry.getPreferences(FilterBuilderConfiguration.class);
        preferences.putJAXBObject(COMBINATION_RULES_PREF, myCombinationRules, false, this);
    }

    /**
     * Updates the active filter for the given data type key.
     *
     * @param dataTypeKey the data type key
     */
    private void updateActiveFilter(String dataTypeKey)
    {
        FilterSet fs = getFilterSet(dataTypeKey, false);
        if (fs.getFilters().isEmpty())
        {
            myFilterRegistry.removeLoadFilter(dataTypeKey, this);
        }
        else
        {
            myFilterRegistry.addLoadFilter(fuseFilters(fs), this);
        }
    }

    /**
     * Envoy to load the default filters.
     */
    private class DefaultFileEnvoy extends AbstractEnvoy
    {
        /**
         * Constructor.
         *
         * @param toolbox The toolbox
         */
        public DefaultFileEnvoy(Toolbox toolbox)
        {
            super(toolbox);
        }

        @Override
        public void open()
        {
            // Load filters from file system on envoy thread
            loadFilters(myDefaultFile);

            // Remove this envoy now that we're done
            myToolbox.getMainToolBox().getEnvoyRegistry().removeObjectsForSource(FilterBuilderControllerImpl.this,
                    Collections.singleton(this));
        }
    }
}
