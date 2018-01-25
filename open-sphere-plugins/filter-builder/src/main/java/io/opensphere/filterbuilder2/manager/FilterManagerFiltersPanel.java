package io.opensphere.filterbuilder2.manager;

import java.awt.Font;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.datafilter.impl.DataFilterRegistryAdapter;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.input.controller.AbstractController;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.filterbuilder.controller.FilterBuilderController;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.filterbuilder.controller.FilterSet;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.FilterChangeEvent;
import io.opensphere.filterbuilder.filter.v1.FilterChangeListener;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The inner filter manager panel with all the individual filters.
 */
public class FilterManagerFiltersPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The map of filters to check boxes. Not ideal. */
    private final Map<Filter, JCheckBox> myCheckBoxMap = new HashMap<>();

    /** The controllers (for cleanup). */
    private final transient Collection<AbstractController<Logical, ? extends ViewModel<Logical>, JComponent>> myControllers = New
            .list();

    /**
     * The name of the key corresponding to the datatype bound to the panel.
     */
    private final String myDataTypeKey;

    /** The filter builder toolbox. */
    private final transient FilterBuilderToolbox myFbToolbox;

    /** The list of filter groups. */
    private final transient List<FilterGroup> myFilterGroups = New.list();

    /** The list of filters. */
    private final List<Filter> myFilters = new LinkedList<>();

    /**
     * The controller used to manage the filter builder panel's input.
     */
    private transient FilterBuilderController myFilterBuilderController;

    /**
     * A flag used to track the persistence mode of the panel. Defaults to true.
     */
    private boolean myPersistMode = true;

    /**
     * The filter used to narrow the set of filter selection choices in a dialog.
     */
    private transient FilterSet myFilterSet;

    /**
     * A change listener used to react to filter change events. This reference must be maintained because of weak reference
     * support.
     */
    private final transient FilterChangeListener myFilterEar = e -> EventQueueUtilities.runOnEDT(() -> handleFilterChanged(e));

    /**
     * A registry listener, in which spatial filter events are tracked. This reference must be maintained because of weak
     * reference support.
     */
    private final transient DataFilterRegistryAdapter myRegistryEar = new DataFilterRegistryAdapter()
    {
        @Override
        public void spatialFilterAdded(String typeKey, Geometry filter)
        {
            EventQueueUtilities.runOnEDT(FilterManagerFiltersPanel.this::sortAndRebuildPanel);
        }

        @Override
        public void spatialFilterRemoved(String typeKey, Geometry filter)
        {
            EventQueueUtilities.runOnEDT(FilterManagerFiltersPanel.this::sortAndRebuildPanel);
        }
    };

    /**
     * Constructor.
     *
     * @param fbToolbox the filter builder toolbox
     * @param typeKey the type key on which to base the filters panel.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public FilterManagerFiltersPanel(FilterBuilderToolbox fbToolbox, String typeKey)
    {
        myFbToolbox = fbToolbox;
        myFilterBuilderController = myFbToolbox.getController();
        myDataTypeKey = typeKey;
        myFilters.addAll(myFilterBuilderController.createVirtualFilters(myDataTypeKey));

        sortFilters();

        myFilterBuilderController.addFilterChangeListener(myFilterEar);
        myFbToolbox.getMainToolBox().getDataFilterRegistry().addListener(myRegistryEar);

        buildPanel();
    }

    // for the time being, we assume that persistMode is off
    //
    /**
     * Creates a new filters panel, configured with the supplied toolbox and filter set, defaulting to non-persistence mode.
     *
     * @param tools the toolbox from which to configure the panel.
     * @param fs the filter set used to configure the panel.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public FilterManagerFiltersPanel(FilterBuilderToolbox tools, FilterSet fs)
    {
        myPersistMode = false;
        myFilterSet = fs;
        myFbToolbox = tools;
        myDataTypeKey = fs.getTypeKey();

        regenFilterSet();
    }

    /**
     * Regenerates the set of filters for the panel.
     */
    public void regenFilterSet()
    {
        sortFilterSet();
        buildPanel();
    }

    /**
     * Sorts the set of filters that match the file set.
     */
    private void sortFilterSet()
    {
        myFilters.clear();
        myFilters.addAll(myFilterSet.getFilters());
        Collections.sort(myFilters, (left, right) -> left.getName().compareTo(right.getName()));
        FilterGroup g = new FilterGroup(myDataTypeKey, "Stuff");
        g.getFilters().addAll(myFilters);
        myFilterGroups.clear();
        myFilterGroups.add(g);
    }

    /**
     * Builds a filter panel.
     *
     * @param filter the filter
     * @return the panel
     */
    private JPanel buildFilterPanel(Filter filter)
    {
        FilterManagerFilterPanel panel = new FilterManagerFilterPanel(myFbToolbox, filter);
        panel.setPersistMode(myPersistMode);
        if (!myPersistMode)
        {
            panel.setDeleteEar(() ->
            {
                myFilterSet.getFilters().remove(panel.getFilter());
                regenFilterSet();
            });
        }
        panel.buildPanel();
        myCheckBoxMap.put(filter, panel.getCheckBox());
        return panel;
    }

    /**
     * Builds a group panel.
     *
     * @param filterGroup the filter group
     * @return the panel
     */
    private JPanel buildGroupPanel(FilterGroup filterGroup)
    {
        GridBagPanel panel = new GridBagPanel();
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel.fillHorizontal().setInsets(0, Constants.INSET, 0, Constants.INSET);
        panel.addRow(Box.createVerticalStrut(Constants.INSET));
        if (myDataTypeKey == null)
        {
            panel.addRow(buildLayerPanel(filterGroup.getLayerName(), filterGroup.getTypeKey()));
        }
        for (Filter filter : filterGroup.getFilters())
        {
            panel.setInsets(Constants.INSET, 0, 0, 0);
            panel.addRow(buildFilterPanel(filter));
        }
        if (filterGroup.getSpatialFilter() != null)
        {
            panel.setInsets(Constants.INSET, Constants.INSET, 0, Constants.INSET);
            panel.addRow(buildSpatialFilterPanel(filterGroup.getSpatialFilter(), filterGroup.getLayerName(),
                    filterGroup.getTypeKey()));
        }
        panel.setInsets(0, Constants.INSET, 0, Constants.INSET);
        panel.addRow(Box.createVerticalStrut(Constants.INSET));
        return panel;
    }

    /**
     * Builds a layer panel.
     *
     * @param layerName the layer name
     * @param typeKey the data type key
     * @return the panel
     */
    private JPanel buildLayerPanel(String layerName, String typeKey)
    {
        GridBagPanel panel = new GridBagPanel();

        JTextArea label = new JTextArea(layerName);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setBorder(null);
        label.setEditable(false);
        label.setBackground(panel.getBackground());
        label.setFont(label.getFont().deriveFont(Font.BOLD));

        JComponent operatorComponent = ControllerFactory
                .createComponent(myFbToolbox.getController().getCombinationOperator(typeKey), null, null, myControllers);

        GridBagPanel matchPanel = new GridBagPanel();
        matchPanel.setBorder(BorderFactory.createEmptyBorder(Constants.INSET, Constants.INSET, Constants.INSET, Constants.INSET));
        matchPanel.add(new JLabel("Match: "));
        matchPanel.add(operatorComponent);

        panel.fillHorizontal();
        panel.add(label);
        panel.fillNone();
        panel.add(Box.createHorizontalStrut(Constants.DOUBLE_INSET));
        panel.add(matchPanel);

        return panel;
    }

    /**
     * Builds the panel.
     */
    private void buildPanel()
    {
        init0();
        removeAll();
        fillHorizontal().setInsets(Constants.INSET, Constants.INSET, 0, Constants.INSET);
        for (FilterGroup filterGroup : myFilterGroups)
        {
            addRow(buildGroupPanel(filterGroup));
        }
        setInsets(Constants.INSET, 0, 0, 0);
        fillVerticalSpace();
        revalidate();
        repaint();
    }

    /**
     * Builds a spatial filter panel.
     *
     * @param spatialFilter the spatial filter
     * @param displayName the layer display name
     * @param typeKey the type key
     * @return the panel
     */
    private JPanel buildSpatialFilterPanel(Geometry spatialFilter, String displayName, String typeKey)
    {
        FilterManagerSpatialFilterPanel panel = new FilterManagerSpatialFilterPanel(myFbToolbox, spatialFilter, typeKey,
                displayName);
        return panel;
    }

    /**
     * Handles filter changed event.
     *
     * @param e The event.
     */
    private void handleFilterChanged(FilterChangeEvent e)
    {
        myFilters.clear();
        myFilters.addAll(myFilterBuilderController.createVirtualFilters(myDataTypeKey));
        sortAndRebuildPanel();
    }

    /**
     * Sorts the current filter list and rebuilds the panel.
     */
    private void sortAndRebuildPanel()
    {
        sortFilters();
        rebuildPanel();
    }

    /**
     * Rebuilds the panel.
     */
    private void rebuildPanel()
    {
        myCheckBoxMap.clear();
        removeAll();
        for (AbstractController<Logical, ? extends ViewModel<Logical>, JComponent> controller : myControllers)
        {
            controller.close();
        }
        myControllers.clear();

        buildPanel();
        getParent().validate();
        getParent().repaint();
    }

    /**
     * Sorts the current filter list.
     */
    private void sortFilters()
    {
        myFilterGroups.clear();

        Collections.sort(myFilters, (o1, o2) -> getDisplayName(o1).compareTo(getDisplayName(o2)));

        for (final Filter filter : myFilters)
        {
            FilterGroup matchingGroup = StreamUtilities.filterOne(myFilterGroups,
                filterGroup -> filterGroup.getTypeKey().equals(filter.getTypeKey()));
            if (matchingGroup == null)
            {
                matchingGroup = new FilterGroup(filter.getTypeKey(), getLayerName(filter));
                myFilterGroups.add(matchingGroup);
            }
            matchingGroup.getFilters().add(filter);
        }

        Collection<String> typeKeys = myDataTypeKey == null
                ? myFbToolbox.getMainToolBox().getDataFilterRegistry().getSpatialLoadFilterKeys()
                : Collections.singletonList(myDataTypeKey);
        for (final String typeKey : typeKeys)
        {
            FilterGroup matchingGroup = StreamUtilities.filterOne(myFilterGroups,
                filterGroup -> filterGroup.getTypeKey().equals(typeKey));
            if (matchingGroup == null)
            {
                DataTypeInfo dataType = getType(typeKey);
                String displayName = dataType != null ? dataType.getDisplayName() : typeKey;
                matchingGroup = new FilterGroup(typeKey, displayName);
                myFilterGroups.add(matchingGroup);
            }
            matchingGroup.setSpatialFilter(myFbToolbox.getMainToolBox().getDataFilterRegistry().getSpatialLoadFilter(typeKey));
        }
    }

    /**
     * Retrieves the {@link DataTypeInfo} that matches the supplied type key.
     *
     * @param typeKey the key for which to search.
     * @return the {@link DataTypeInfo} object corresponding to the supplied key, or null if none exists.
     */
    private DataTypeInfo getType(String typeKey)
    {
        return myFbToolbox.getMantleToolBox().getDataGroupController().findMemberById(typeKey);

        // GCD: doesn't work!
        // return myFbToolbox.getMantleToolBox().getDataTypeController().
        // getDataTypeInfoForType(typeKey);
    }

    /**
     * Gets the display name for the given filter.
     *
     * @param filter the filter
     * @return the display name
     */
    private String getDisplayName(Filter filter)
    {
        String displayName;
        if (myDataTypeKey == null)
        {
            displayName = StringUtilities.concat(getLayerName(filter), " - ", filter.getName());
        }
        else
        {
            displayName = filter.getName();
        }
        return displayName;
    }

    /**
     * Gets the layer name for the given filter.
     *
     * @param filter the filter
     * @return the layer name
     */
    private static String getLayerName(Filter filter)
    {
        return StringUtilities.concat(filter.getDataTypeDisplayName(), " - ", filter.getServerName());
    }
}
