package io.opensphere.wps.ui.detail;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.controlpanels.DetailPane;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupCategorizationUtilities;

/**
 * An implementation of the {@link DetailPane} in which a WPS preview / editor
 * is defined.
 */
public class WpsDetailPanel extends DetailPane
{
    /**
     * The field in which the provider information is entered.
     */
    private final Label myProviderField;

    /**
     * The field in which the type information is entered.
     */
    private final Label myTypeField;

    /**
     * The field in which the description information is entered.
     */
    private final Label myDescriptionField;

    /**
     * The field in which the URL information is entered.
     */
    private final Label myUrlField;

    /**
     * The field in which the provider information is entered.
     */
    private final Label myTagsField;

    /**
     * The button used to clone the WPS process configuration.
     */
    private final Button myCloneButton;

    /**
     * The button used to reset deviations from the saved configuration values.
     */
    private final Button myResetButton;

    /**
     * The button used to save deviations from the saved configuration values.
     */
    private final Button mySaveButton;

    /**
     * The bar component in which the {@link #mySaveButton} and
     * {@link #myResetButton}s are rendered.
     */
    private final ButtonBar myButtonBar;

    /**
     * The area in which the form is displayed.
     */
    private final GridPane myParameterFormArea;

    /**
     * The model in which detail data is contained for the U/I.
     */
    private final WpsProcessModel myModel;

    /**
     * Creates a new panel.
     *
     * @param pToolbox the toolbox through which application interactions occur.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public WpsDetailPanel(Toolbox pToolbox)
    {
        super(pToolbox);
        myModel = new WpsProcessModel();

        GridPane grid = createContainer();

        myProviderField = new Label();
        myProviderField.textProperty().bind(myModel.getProvider());
        grid.addRow(0, new Label("Provider:"), myProviderField);

        myTypeField = new Label();
        myTypeField.textProperty().bind(myModel.getTypes());
        grid.addRow(1, new Label("Type:"), myTypeField);

        myDescriptionField = new Label();
        myDescriptionField.textProperty().bind(myModel.getDescription());
        myDescriptionField.setWrapText(true);
        myDescriptionField.setFont(
                Font.font(myDescriptionField.getFont().getName(), FontPosture.ITALIC, myDescriptionField.getFont().getSize()));
        myDescriptionField.setPadding(new Insets(2, 2, 2, 2));
        ScrollPane descriptionPane = new ScrollPane(myDescriptionField);
        descriptionPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        descriptionPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        descriptionPane.setFitToWidth(true);

        grid.add(descriptionPane, 0, 2, 2, 1);
        GridPane.setFillWidth(descriptionPane, true);
        GridPane.setHgrow(descriptionPane, Priority.ALWAYS);
        GridPane.setVgrow(descriptionPane, Priority.SOMETIMES);

        myTagsField = new Label();
        myTagsField.textProperty().bind(myModel.getTags());
        grid.addRow(3, new Label("Tags:"), myTagsField);

        myUrlField = new Label();
        myUrlField.textProperty().bind(myModel.getUrl());
        grid.addRow(4, new Label("URL(s):"), myUrlField);

        myCloneButton = new Button("Clone", new ImageView(IconUtil.getResource(IconType.COPY)));
        grid.addRow(5, myCloneButton);

        myParameterFormArea = createProcessEditor();
        myParameterFormArea.setStyle("-fx-border-color: #676767; -fx-border-style: solid inside line-join miter;");
        GridPane.setFillHeight(myParameterFormArea, true);
        GridPane.setHgrow(myParameterFormArea, Priority.ALWAYS);
        GridPane.setVgrow(myParameterFormArea, Priority.ALWAYS);
        grid.add(myParameterFormArea, 0, 6, 2, 1);

        myButtonBar = new ButtonBar();

        myResetButton = new Button("Reset", new ImageView(IconUtil.getResource(IconType.RELOAD)));
        mySaveButton = new Button("Save", new ImageView(IconUtil.getResource(IconType.DISK)));

        myButtonBar.getButtons().add(myResetButton);
        myButtonBar.getButtons().add(mySaveButton);

        grid.add(myButtonBar, 0, 7, 2, 1);

        setCenter(grid);
    }

    /**
     * Creates and configures a GridPane into which all content will be added.
     *
     * @return a new GridPane into which all content will be added.
     */
    protected GridPane createContainer()
    {
        GridPane grid = new GridPane();
        grid.setVgap(5);

        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.NEVER);

        ColumnConstraints columnConstraints2 = new ColumnConstraints();
        columnConstraints2.setFillWidth(true);
        columnConstraints2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().add(columnConstraints1);
        grid.getColumnConstraints().add(columnConstraints2);
        return grid;
    }

    /**
     * Creates an implementation specific editor, in which a panel tied to a
     * specific WPS process is rendered.
     *
     * @return a new editor, configured for a specific WPS process.
     */
    protected GridPane createProcessEditor()
    {
        return new GridPane();
    }

    /**
     * Gets the value of the {@link #myParameterFormArea} field.
     *
     * @return the value stored in the {@link #myParameterFormArea} field.
     */
    public GridPane getParameterFormArea()
    {
        return myParameterFormArea;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPane#populate(io.opensphere.mantle.data.DataGroupInfo)
     */
    @Override
    public void populate(DataGroupInfo pDataGroup)
    {
        myModel.getTagList().clear();
        myModel.getTypeList().clear();
        myParameterFormArea.getChildren().clear();

        myModel.getProvider().set(pDataGroup.getTopParentDisplayName());
        myModel.getTypeList().addAll(StreamUtilities.map(GroupCategorizationUtilities.getGroupCategories(pDataGroup, false),
            input -> StringUtilities.trim(input, 's')));
        myModel.getDescription().set(pDataGroup.getSummaryDescription());

        Collection<String> tags = New.set();
        Collection<String> descriptions = New.insertionOrderSet();
        Collection<DataTypeInfo> members = pDataGroup.getMembers(true);
        Collection<String> urls = New.list(members.size());
        if (!members.isEmpty())
        {
            List<DataTypeInfo> memberList = New.list(members);
            Collections.sort(memberList, DataTypeInfo.DISPLAY_NAME_COMPARATOR);
            for (DataTypeInfo member : memberList)
            {
                tags.addAll(member.getTags());
                if (!StringUtils.isBlank(member.getDescription()))
                {
                    descriptions.add(member.getDescription());
                }
                if (member.getUrl() != null)
                {
                    urls.add(member.getUrl());
                }
            }
        }

        // Sort the tags
        if (!tags.isEmpty())
        {
            List<String> tagList = New.list(tags);
            Collections.sort(tagList);
            tags = tagList;
        }

        myModel.getTagList().addAll(tags);

        StringBuilder descriptionText = new StringBuilder();
        StringUtilities.join(descriptionText, "\n\n", descriptions);
        myModel.getDescription().set(descriptionText.toString());

        StringBuilder urlText = new StringBuilder();
        StringUtilities.join(urlText, "\n", urls);
        myModel.getUrl().set(urlText.toString());
    }
}
