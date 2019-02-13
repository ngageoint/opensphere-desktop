package io.opensphere.filterbuilder2.editor;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterItem;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.advanced.AdvancedEditorPanel;
import io.opensphere.filterbuilder2.editor.basic.BasicEditorPanel;
import io.opensphere.filterbuilder2.editor.model.FilterModel;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * The main filter editor panel.
 */
public class FilterEditorPanel extends JPanel implements Validatable
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private transient FilterModel myModel;

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /**
     * Adds the fields from group.
     *
     * @param group the group
     * @param fields the fields
     */
    private static void addFieldsFromGroup(DataFilterGroup group, List<String> fields)
    {
        String field = "";
        for (int i = 0; i < group.numItems(); i++)
        {
            DataFilterItem item = group.getItemAt(i);
            if (item instanceof DataFilterGroup)
            {
                addFieldsFromGroup((DataFilterGroup)item, fields);
            }
            else
            {
                field = ((Criteria)item).getField();
                if (!fields.contains(field))
                {
                    fields.add(field);
                }
            }
        }
    }

    /**
     * Adds a tab component to the tabbed pane.
     *
     * @param tabbedPane the tabbed pane
     * @param tabComponent the tab component
     */
    private static void addTab(JTabbedPane tabbedPane, JComponent tabComponent)
    {
        tabComponent.setBorder(BorderFactory.createEmptyBorder(Constants.DOUBLE_INSET, Constants.DOUBLE_INSET,
                Constants.DOUBLE_INSET, Constants.DOUBLE_INSET));
        tabbedPane.addTab(tabComponent.getName(), tabComponent);
    }

    /**
     * Updates the columns for the given filter.
     *
     * @param filter the filter
     * @param dataType the data type
     */
    private static void updateColumns(Filter filter, DataTypeInfo dataType)
    {
        List<String> columns = getColumns(dataType);

        // If the data type doesn't exist, get the columns from the filter
        // so they at least have something to edit.
        if (columns == null || columns.isEmpty())
        {
            columns = new ArrayList<>();
            addFieldsFromGroup(filter.getFilterGroup(), columns);
        }

        filter.setColumns(columns);
    }

    /**
     * Get the columns associated with the given data type.  This method
     * contains some fail-safes which account for the possibility that the
     * filter is edited while the layer is not loaded or not fully configured.
     *
     * @param dti the data type
     * @return the names of the columns
     */
    private static List<String> getColumns(DataTypeInfo dti)
    {
        if (dti == null)
        {
            return null;
        }
        MetaDataInfo meta = dti.getMetaDataInfo();
        if (meta == null)
        {
            return null;
        }

        List<String> columns;
        List<String> keys = meta.getKeyNames();
        if (keys.size() > 0)
        {
            columns = new ArrayList<>(keys);
        }
        else
        {
            columns = new ArrayList<>(meta.getOriginalKeyNames());
        }

        // Remove fields that don't make sense in filter builder
        String key;
        if ((key = meta.getLatitudeKey()) != null)
        {
            columns.remove(key);
        }
        if ((key = meta.getLongitudeKey()) != null)
        {
            columns.remove(key);
        }
        if ((key = meta.getGeometryColumn()) != null)
        {
            columns.remove(key);
        }
        columns.remove(MetaDataInfo.MGRS_DERIVED);

        return columns;
    }

    /** Construct with no other initializations. */
    private FilterEditorPanel()
    {
        super(new BorderLayout());
    }

    /**
     * Constructor.
     *
     * @param fbToolbox the filter builder toolbox
     * @param filter the filter
     * @param dataType the data type
     * @param isNew whether the filter is new
     */
    public FilterEditorPanel(FilterBuilderToolbox fbToolbox, Filter filter, DataTypeInfo dataType, boolean isNew)
    {
        super(new BorderLayout());

        // Set the columns in the filter
        updateColumns(filter, dataType);

        // Get the disallowed (already used) names; note:  an existing filter can keep its name
        Collection<String> disallowedNames =
                fbToolbox.getController().getAllFilters().stream().map(f -> f.getName()).collect(Collectors.toList());
        if (!isNew)
        {
            disallowedNames.remove(filter.getName());
        }

        // Create the GUI model
        createModel(filter, dataType, disallowedNames);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(fbToolbox.getMainToolBox().getPreferencesRegistry()), BorderLayout.CENTER);
    }

    /**
     * Create an instance that does not include the filter name.  This can be
     * used to edit a filter that is a subcomponent of another object, when
     * the filter name is not meaningful.
     *
     * @param tools the system toolbox
     * @param filter the filter
     * @param dataType the data type
     * @return the editor panel
     */
    public static FilterEditorPanel noNameEditor(Toolbox tools, Filter filter, DataTypeInfo dataType)
    {
        FilterEditorPanel pan = new FilterEditorPanel();

        // Set the columns in the filter
        updateColumns(filter, dataType);
        // Create the GUI model
        pan.createModel(filter, dataType, new ArrayList<>());
        // install the GUI components
        pan.add(pan.buildCenterPanel(tools.getPreferencesRegistry()), BorderLayout.CENTER);

        return pan;
    }

    /**
     * Construct the editor model and hook up to validator support.
     *
     * @param filter the filter to be edited
     * @param dataType the layer to which the filter applies
     * @param usedNames names already in use (considered invalid)
     */
    private void createModel(Filter filter, DataTypeInfo dataType, Collection<String> usedNames)
    {
        myModel = new FilterModel(dataType);
        myModel.set(filter);
        myModel.setChanged(false);
        myModel.getFilterName().setDisallowedNames(usedNames);
        myModel.addListener((o, v0, v1) ->
                myValidatorSupport.setValidationResult(myModel.getValidationStatus(), myModel.getErrorMessage()));
        myValidatorSupport.setValidationResult(myModel.getValidationStatus(), myModel.getErrorMessage());
    }

    /**
     * Applies GUI model changes to the underlying filter.
     */
    public void applyChanges()
    {
        if (myModel.isChanged())
        {
            myModel.applyChanges();
        }
    }

    /**
     * Gets the fields defined in the model.
     *
     * @return the fields defined in the model.
     */
    public Set<String> getFields()
    {
        return myModel.getFields();
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /**
     * Builds the center panel.
     *
     * @param prefs the system registry for user preferences
     * @return the center panel
     */
    private JTabbedPane buildCenterPanel(PreferencesRegistry prefs)
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        addTab(tabbedPane, new BasicEditorPanel(myModel));
        addTab(tabbedPane, new AdvancedEditorPanel(prefs, myModel));
        if (!BasicEditorPanel.acceptsFilter(myModel))
        {
            tabbedPane.setSelectedIndex(1);
        }
        return tabbedPane;
    }

    /**
     * Builds the top panel.
     *
     * @return the top panel
     */
    private JPanel buildTopPanel()
    {
        GridBagPanel panel = new GridBagPanel();
        panel.anchorWest();
        panel.setInsets(0, 0, Constants.INSET, Constants.INSET);
        panel.add(new JLabel("Filter Name:"));
        JComponent nameComponent = ControllerFactory.createComponent(myModel.getFilterName());
        if (nameComponent instanceof JTextField)
        {
            ((JTextField)nameComponent).setColumns(16);
        }
        panel.add(nameComponent);
        panel.add(Box.createHorizontalStrut(Constants.INSET));
        panel.add(new JLabel("Layer: " + myModel.get().getDataTypeDisplayName()));

        panel.setGridx(0).setGridy(1).add(new JLabel("Description:"));
        JComponent descComponent = ControllerFactory.createComponent(myModel.getFilterDescription());
        panel.setGridx(1).setGridwidth(3).fillHorizontal().add(descComponent);

        panel.fillHorizontalSpace();
        return panel;
    }
}
