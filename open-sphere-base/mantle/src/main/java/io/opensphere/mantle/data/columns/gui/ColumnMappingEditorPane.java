package io.opensphere.mantle.data.columns.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.datafilter.columns.ColumnMapping;
import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.Editor;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;

/** JavaFX column mapping editor pane. */
public class ColumnMappingEditorPane extends BorderPane implements Editor
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ColumnMappingEditorPane.class);

    /** The column mapping resources. */
    private final ColumnMappingResources myResources;

    /** The defined column. */
    private final String myDefinedColumn;

    /** The list of layer references. */
    private final List<DataTypeRef> layers = new LinkedList<>();

    /** The column name field. */
    private TextField myName;

    /** The column description field. */
    private TextArea myDescription;

    /** The list of mappings. */
    private final ObservableList<ColumnMapping> myMappingList = FXCollections.observableArrayList();

    /** The validator. */
    private final DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /**
     * Constructor.
     *
     * @param definedColumn the defined column
     * @param resources the column mapping resources
     */
    public ColumnMappingEditorPane(String definedColumn, ColumnMappingResources resources)
    {
        super();

        myResources = resources;
        myDefinedColumn = definedColumn;

        layers.addAll(resources.getLayers());
        Collections.sort(layers, (left, right) -> left.getType().getDisplayName().compareTo(
                right.getType().getDisplayName()));

        setTop(createTopPane());
        setCenter(createMainPane());
        setMargin(getTop(), new Insets(0, 0, Constants.GAP, 0));

        initState();

        validate(myName.getText());

        myName.textProperty().addListener((obs, o, n) -> validate(n));
        myMappingList.addListener((ListChangeListener<ColumnMapping>)change ->
        {
            if (change.next())
            {
                validate(myName.getText());
            }
        });
    }

    @Override
    public void accept()
    {
        final String definedColumn = myName.getText();

        if (myDefinedColumn != null && !myDefinedColumn.equals(definedColumn))
        {
            myResources.getController().rename(myDefinedColumn, definedColumn);
        }

        myResources.getController().clearMappings(definedColumn);
        for (ColumnMapping mapping : myMappingList)
        {
            myResources.getController().addMapping(definedColumn, mapping.getLayerKey(), mapping.getLayerColumn(), true);
        }

        myResources.getController().setDescription(definedColumn, myDescription.getText(), true);

        Set<String> types = New.set();
        for (ColumnMapping mapping : myMappingList)
        {
            types.add(myResources.getType(mapping.getLayerKey(), mapping.getLayerColumn()));
        }
        myResources.getController().setType(definedColumn, types.size() == 1 ? types.iterator().next() : null, true);
        if (types.size() != 1)
        {
            String message = types.isEmpty() ? "No types" : "Multiple types: " + types;
            LOGGER.warn(message);
        }
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /** Initializes the state of the UI. */
    private void initState()
    {
        if (myDefinedColumn != null)
        {
            myName.setText(myDefinedColumn);
            myDescription.setText(myResources.getController().getDescription(myDefinedColumn));
            myMappingList.setAll(myResources.getController().getMappings(myDefinedColumn));
        }
        else
        {
            addMapping();
        }
    }

    /**
     * Performs validation.
     *
     * @param name the name
     */
    private void validate(String name)
    {
        ValidationStatus status = ValidationStatus.VALID;
        String error = null;

        // Errors

        if (StringUtils.isBlank(name))
        {
            error = "Name cannot be blank.";
        }

        if (error == null)
        {
            Collection<String> disallowedNames = myResources.getController().getDefinedColumns();
            if (myDefinedColumn != null)
            {
                disallowedNames.remove(myDefinedColumn);
            }
            if (disallowedNames.contains(name))
            {
                error = "The name \"" + name + "\" already exists.";
            }
        }

        if (error == null)
        {
            Set<String> uniqueLayers = myMappingList.stream().map(m -> m.getLayerKey()).collect(Collectors.toSet());
            if (uniqueLayers.size() != myMappingList.size())
            {
                List<String> allLayers = myMappingList.stream().map(m -> m.getLayerKey()).collect(Collectors.toList());
                CollectionUtilities.subtract(allLayers, uniqueLayers);
                String fistDupName = myResources.getLayerDisplayName(allLayers.iterator().next());
                error = "Duplicate layers are not allowed (" + fistDupName + ").";
            }
        }

        if (error != null)
        {
            status = ValidationStatus.ERROR;
        }

        // Warnings

        if (error == null && myMappingList.stream().anyMatch(m -> !ColumnMappingController.ALL_LAYERS.equals(m.getLayerKey())
            && myResources.getLayerColumns(m.getLayerKey()).isEmpty()))
        {
            error = "Columns are not available for inactive layer(s).";
            status = ValidationStatus.WARNING;
        }

        myValidatorSupport.setValidationResult(status, error);
    }

    /**
     * Creates the top pane.
     *
     * @return the pane
     */
    private Node createTopPane()
    {
        GridPane pane = new GridPane();
        pane.setVgap(Constants.GAP);
        pane.setHgap(Constants.GAP);
        pane.getColumnConstraints().addAll(newConstraints(Priority.NEVER), newConstraints(Priority.NEVER));
        pane.add(new Label("Name:"), 0, 0);
        myName = new TextField();
        myName.setTooltip(new Tooltip("The name of the column"));
        myName.setPrefWidth(400);
        pane.add(myName, 1, 0);
        pane.add(new Label("Description:"), 0, 1);
        myDescription = new TextArea();
        myDescription.setPrefWidth(400);
        myDescription.setPrefRowCount(2);
        myDescription.setTooltip(new Tooltip("An optional description of the column"));
        pane.add(myDescription, 1, 1);
        return pane;
    }

    /**
     * Creates the main pane.
     *
     * @return the pane
     */
    private Node createMainPane()
    {
        BorderPane pane = new BorderPane();

        Button addButton = FXUtilities.newIconButton("Add", IconType.PLUS, Color.GREEN);
        addButton.setTooltip(new Tooltip("Add a new " + Constants.COLUMN_MAPPING.normalCase()));
        addButton.setOnAction(e -> addMapping());

        HBox top = FXUtilities.newHBox(new Label(Constants.COLUMN_MAPPING.pluralTitleCase() + ":"), FXUtilities.newHSpacer(),
                addButton);
        pane.setTop(top);

        ListView<ColumnMapping> mappingsList = new ListView<>(myMappingList);
        mappingsList.setCellFactory(param -> new ColumnMappingCell(myResources, myMappingList, layers));
        pane.setCenter(mappingsList);

        setMargin(pane.getTop(), new Insets(0, 0, Constants.GAP, 0));

        return pane;
    }

    /** Adds a mapping. */
    private void addMapping()
    {
        if (layers.isEmpty())
        {
            return;
        }
        DataTypeRef t = layers.get(0);
        String key = t.getType().getTypeKey();
        String layerColumn = getIfAny(myResources.getLayerColumns(key), 0);
        myMappingList.add(new ColumnMapping(null, key, layerColumn));
    }

    /**
     * Much simpler than "CollectionUtilities.getItemOrNull".
     * @param stuff a List
     * @param i an index
     * @return the item in <i>stuff</i> at index <i>i</i>, if any
     */
    private static <T> T getIfAny(List<T> stuff, int i)
    {
        if (stuff == null || i < 0 || stuff.size() <= i)
        {
            return null;
        }
        return stuff.get(i);
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
