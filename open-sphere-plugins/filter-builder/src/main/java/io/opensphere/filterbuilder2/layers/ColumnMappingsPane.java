package io.opensphere.filterbuilder2.layers;

import java.util.Collection;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import io.opensphere.core.datafilter.columns.MutableColumnMappingController;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.fx.Editor;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.RollupEditor;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;

/** Column mappings pane. */
public final class ColumnMappingsPane extends BorderPane implements Editor
{
    /**
     * A constant in which the message header is defined.
     */
    private static final String HEADER_1 = "Some columns in the filter are not available in the selected layers.";

    /**
     * A constant in which the message header's second line is defined.
     */
    private static final String HEADER_2 = "Please choose the appropriate columns for each.";

    /** The column mapping controller. */
    private final MutableColumnMappingController myColumnMappingController;

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The filter. */
    private final String myTypeKey;

    /**
     * The set of fields applied to the filter.
     */
    private final Set<String> myFields;

    /** The main pane. */
    private final VBox myMainPane;

    /** The roll-up editor support. */
    private final RollupEditor<ColumnMappingPane> myEditorSupport = new RollupEditor<>();

    /**
     * Construct a ColumnMappingsPane for the specified type and set of fields.
     *
     * @param colCtrl the column mapping controller (mutable variety)
     * @param tools the toolbox for the Mantle
     * @param filterType the applicable data type
     * @param filterFields the set of fields used by the Filter
     */
    public ColumnMappingsPane(MutableColumnMappingController colCtrl, MantleToolbox tools, String filterType,
            Set<String> filterFields)
    {
        myColumnMappingController = colCtrl;
        myMantleToolbox = tools;
        myTypeKey = filterType;
        myFields = filterFields;

        myMainPane = new VBox(10);
        myMainPane.setPadding(new Insets(5));

        VBox label = FXUtilities.newVBox(new Label(HEADER_1), new Label(HEADER_2));
        setTop(label);
        setMargin(label, new Insets(0, 0, 5, 0));
        setCenter(new ScrollPane(myMainPane));
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myEditorSupport.getValidatorSupport();
    }

    @Override
    public void accept()
    {
        myEditorSupport.accept();
    }

    /**
     * Returns whether the panel has mappings.
     *
     * @return whether the panel has mappings
     */
    public boolean hasMappings()
    {
        return myEditorSupport.getChildren().stream().anyMatch(p -> p.hasMappings());
    }

    /**
     * Sets the data types.
     *
     * @param dataTypes the data types
     */
    public void setDataTypes(Collection<? extends DataTypeInfo> dataTypes)
    {
        myMainPane.getChildren().clear();
        myEditorSupport.clear();

        addDataTypes(dataTypes);
    }

    /**
     * Adds the data types.
     *
     * @param dataTypes the data types
     */
    private void addDataTypes(Collection<? extends DataTypeInfo> dataTypes)
    {
        for (DataTypeInfo dataType : dataTypes)
        {
            ColumnMappingPane mappingPane = new ColumnMappingPane(myColumnMappingController, myMantleToolbox, myTypeKey, myFields,
                    dataType);
            if (mappingPane.hasMappings())
            {
                myMainPane.getChildren().add(mappingPane);
                myEditorSupport.addChildEditor(mappingPane);
            }
        }
    }
}
