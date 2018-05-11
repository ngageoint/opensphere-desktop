package io.opensphere.filterbuilder2.layers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import io.opensphere.core.datafilter.columns.MutableColumnMappingController;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.fx.Editor;
import io.opensphere.core.util.fx.NewAutoCompleteComboBoxListener;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/** Column mapping pane. */
public final class ColumnMappingPane extends GridPane implements Editor
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ColumnMappingPane.class);

    /** The column mapping controller. */
    private final MutableColumnMappingController colMapCtrl;

    /**
     * The type key of the filter being copied.
     */
    private final String myTypeKey;

    /**
     * The set of fields added to the mapping.
     */
    private final Set<String> myNewFields;

    /** The data type to copy from. */
    private final DataTypeInfo srcType;

    /** The data type to copy to. */
    private final DataTypeInfo tgtType;

    /**
     * The name of the key sought as the target in the mapping.
     */
    private final String tgtTypeKey;

    /** The map of column to combo box. */
    private final Map<String, ComboBox<String>> myFieldToCombo = new TreeMap<>();

    /** The validator. */
    private final DefaultValidatorSupport validator = new DefaultValidatorSupport(this);

    /** True in case of error. Duh. */
    private boolean error;

    /**
     * Constructs the ColumnMappingPane using the source type key, filter
     * fields, and the desired target data type.
     *
     * @param colCtrl the column mapping controller (mutable variety)
     * @param tools the toolbox for the Mantle
     * @param filterType the type key for the source Filter
     * @param fields the fields referenced in the source Filter
     * @param toType the data type to which the filter may be applied
     */
    public ColumnMappingPane(MutableColumnMappingController colCtrl, MantleToolbox tools, String filterType, Set<String> fields,
            DataTypeInfo toType)
    {
        colMapCtrl = colCtrl;
        myTypeKey = filterType;
        myNewFields = fields;
        srcType = tools.getDataGroupController().getActiveMembers(false).stream().filter(t -> t.getTypeKey().equals(myTypeKey))
                .findAny().orElse(null);
        tgtType = toType;
        tgtTypeKey = tgtType.getTypeKey();

        if (tgtType.equals(srcType))
        {
            return;
        }

        // If the target type is not loaded, we do not allow the creation of
        // new column associations. If the existing associations suffice, then
        // let it slide; otherwise display an error message.
        List<String> keyNames = tgtType.getMetaDataInfo().getKeyNames();
        // if keyNames is an empty list, then the type must not be loaded;
        // in this case, any unmapped column is fatal
        if (keyNames.isEmpty())
        {
            buildWithoutKeys();
        }
        else
        {
            buildWithKeys(keyNames);
        }
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return validator;
    }

    /**
     * Executes a selection to search for the named field.
     *
     * @param field the field for which to search.
     * @return the selected item corresponding to the supplied field.
     */
    private String selectionFor(String field)
    {
        ComboBox<String> c = myFieldToCombo.get(field);
        return c == null ? null : c.getSelectionModel().getSelectedItem();
    }

    /**
     * Gets the value of the field from the source datatype.
     *
     * @param field the field for which to get the value.
     * @return the value of the field, or null if none could be found.
     */
    private String valTypeOf(String field)
    {
        return getTypeString(valClassOf(srcType, field));
    }

    /**
     * Gets the Class from the type metadata, if possible.
     *
     * @param type the data type
     * @param field the field (or column)
     * @return the type of data in the field or null
     */
    private static Class<?> valClassOf(DataTypeInfo type, String field)
    {
        if (type == null)
        {
            return null;
        }
        MetaDataInfo meta = type.getMetaDataInfo();
        if (meta == null)
        {
            return null;
        }
        return meta.getKeyClassType(field);
    }

    /**
     * Delegates to the column mapping controller.
     *
     * @param f1 the name of the column for which to search.
     * @param t2 the name of the second type to test.
     * @param f2 the name of the column for which to search.
     * @return true if the associate method would succeed, false otherwise.
     */
    private boolean canAssociate(String f1, String t2, String f2)
    {
        return colMapCtrl.canAssociate(myTypeKey, f1, t2, f2);
    }

    @Override
    public void accept()
    {
        // at this point, failure is not an option
        for (String f : myNewFields)
        {
            colMapCtrl.associate(myTypeKey, f, tgtTypeKey, selectionFor(f), valTypeOf(f));
        }
    }

    /**
     * Returns whether the panel has mappings. In fact, the value returned by
     * this method controls whether the panel is displayed. Therefore, also
     * return true in case there is an error.
     *
     * @return whether the panel has mappings
     */
    public boolean hasMappings()
    {
        return error || !myFieldToCombo.isEmpty();
    }

    /**
     * Builds the panel in the case where the target type is not loaded. It must
     * either succeed without creating new column mappings or fail.
     */
    private void buildWithoutKeys()
    {
        boolean okay = true;
        for (String srcCol : myNewFields)
        {
            String defCol = colMapCtrl.getDefinedColumn(myTypeKey, srcCol);
            if (defCol == null)
            {
                defCol = srcCol;
            }
            String tgtCol = colMapCtrl.getLayerColumn(tgtType.getTypeKey(), defCol);
            okay &= tgtCol != null;
            if (!okay)
            {
                break;
            }
        }
        if (okay)
        {
            return;
        }

        error = true;
        setVgap(5);
        setHgap(5);
        getColumnConstraints().addAll(newConstraints(Priority.NEVER), newConstraints(Priority.NEVER));

        Label title = new Label(tgtType.getDisplayName() + " Columns:");
        add(title, 0, 0, 2, 1);

        Label msg = new Label("Layer is not loaded.  Cannot add column mappings.");
        msg.setStyle("-fx-text-fill: red;");
        add(msg, 0, 1, 2, 1);

        validator.setValidationResult(ValidationStatus.ERROR, "Cannot map changes to non-loaded layers.");
    }

    /**
     * Builds the panel in the case where the target type is loaded.
     *
     * @param keyNames field names in the target type
     */
    private void buildWithKeys(List<String> keyNames)
    {
        setVgap(5);
        setHgap(5);
        getColumnConstraints().addAll(newConstraints(Priority.NEVER), newConstraints(Priority.NEVER));

        Label title = new Label(tgtType.getDisplayName() + " Columns:");
        add(title, 0, 0, 2, 1);

        int row = 1;
        for (String srcCol : myNewFields)
        {
            String defCol = colMapCtrl.getDefinedColumn(myTypeKey, srcCol);
            if (defCol == null)
            {
                defCol = srcCol;
            }

            String tgtCol = colMapCtrl.getLayerColumn(tgtType.getTypeKey(), defCol);
            if (tgtCol == null)
            {
                ComboBox<String> columnCombo = buildComboBox(srcCol, keyNames);
                myFieldToCombo.put(srcCol, columnCombo);
                add(new Label(defCol + ": "), 0, row);
                add(columnCombo, 1, row);
                ++row;
            }
        }

        performValidation();
    }

    /**
     * Builds the combo box.
     *
     * @param sourceColumn the source column
     * @param keyNames the names of the fields in the target type
     * @return the combo box
     */
    private ComboBox<String> buildComboBox(String sourceColumn, List<String> keyNames)
    {
        ComboBox<String> columnCombo = new ComboBox<>(FXCollections.observableArrayList(keyNames));

        NewAutoCompleteComboBoxListener listener = new NewAutoCompleteComboBoxListener();
        listener.setupComboBox(columnCombo);

        String selection = getInitialSelection(sourceColumn, keyNames);
        if (selection != null)
        {
            columnCombo.getSelectionModel().select(selection);
        }

        columnCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> performValidation());

        return columnCombo;
    }

    /**
     * Gets the initial selection for the combo box.
     *
     * @param sourceColumn the source column
     * @param keyNames the key names
     * @return the selection, or null
     */
    private String getInitialSelection(String sourceColumn, List<String> keyNames)
    {
        String selection = null;
        // Default to the source column if it exists
        if (keyNames.contains(sourceColumn))
        {
            selection = sourceColumn;
        }
        // Otherwise pick the first one with the right data type
        else if (srcType != null)
        {
            Class<?> sourceType = valClassOf(srcType, sourceColumn);
            selection = keyNames.stream().filter(k -> valClassOf(tgtType, k) == sourceType).findFirst().orElse(null);
        }

        if (selection == null && !keyNames.isEmpty())
        {
            selection = keyNames.get(0);
        }
        return selection;
    }

    /** Performs validation. */
    private void performValidation()
    {
        for (String f1 : myNewFields)
        {
            String f2 = selectionFor(f1);
            // GCD: can this actually happen?
            if (f2 == null)
            {
                validator.setValidationResult(ValidationStatus.ERROR, "Please select a value for " + f1 + ".");
                return;
            }

            // check the ColumnMappingController for permission
            if (!canAssociate(f1, tgtTypeKey, f2))
            {
                validator.setValidationResult(ValidationStatus.ERROR, "Error:  Conflict in mappings for " + f1 + ".");
                return;
            }

            // check for same value types
            Class<?> c1 = valClassOf(srcType, f1);
            Class<?> c2 = valClassOf(tgtType, f2);
            if (c1 != null && c2 != null && Utilities.notSameInstance(c1, c2))
            {
                validator.setValidationResult(ValidationStatus.WARNING, "Warning:  Type mismatch on " + f1 + ".");
                return;
            }
        }
        validator.setValidationResult(ValidationStatus.VALID, null);
    }

    /**
     * Gets the type string from the class.
     *
     * @param keyClass the class
     * @return the type string
     */
    private static String getTypeString(Class<?> keyClass)
    {
        String typeString;
        if (keyClass == String.class)
        {
            typeString = "string";
        }
        else if (keyClass == Double.class)
        {
            typeString = "double";
        }
        else if (keyClass == java.util.Date.class)
        {
            typeString = "date";
        }
        else if (keyClass == com.vividsolutions.jts.geom.Geometry.class)
        {
            typeString = "geometry";
        }
        else
        {
            typeString = "string";
            LOGGER.error("unknown class: " + keyClass);
        }
        return typeString;
    }

    /**
     * Creates constraints.
     *
     * @param priority the hgrow priority
     * @return the constraints
     */
    private static ColumnConstraints newConstraints(Priority priority)
    {
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(priority);
        return columnConstraints;
    }
}
