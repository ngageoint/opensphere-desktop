package io.opensphere.controlpanels.layers.availabledata.detail;

import java.util.Collection;

import javafx.beans.property.Property;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;

import io.opensphere.controlpanels.DetailPane;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.GroupCategorizationUtilities;

/**
 * A standard detail pane, in which text is displayed.
 */
public class TextDetailPane extends DetailPane
{
    /**
     * The text area in which the preview will be rendered.
     */
    private final TextArea myTextArea;

    /**
     * The scroll pane wrapping the {@link #myTextArea}.
     */
    private final ScrollPane myScrollPane;

    /**
     * Creates a new detail pane.
     *
     * @param pToolbox The toolbox through which system interactions occur.
     */
    public TextDetailPane(Toolbox pToolbox)
    {
        super(pToolbox);

        myTextArea = new TextArea();
        myTextArea.autosize();
        myTextArea.setWrapText(true);

        myScrollPane = new ScrollPane(myTextArea);
        myScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        myScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        myScrollPane.setFitToHeight(true);
        myScrollPane.setFitToWidth(true);
//        getChildren().add(myScrollPane);
        setCenter(myScrollPane);
    }

    /**
     * Creates a new detail pane bound to the supplied data provider.
     *
     * @param pToolbox The toolbox through which system interactions occur.
     * @param dataSource the text data source to which the text area will be
     *            bound.
     */
    public TextDetailPane(Toolbox pToolbox, Property<String> dataSource)
    {
        this(pToolbox);

        myTextArea.textProperty().bind(dataSource);
    }

    /**
     * Gets the property into which changes can be injected to reflect data
     * updates.
     *
     * @return the property with which data changes can be bound.
     */
    public Property<String> textProperty()
    {
        return myTextArea.textProperty();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.DetailPane#populate(io.opensphere.mantle.data.DataGroupInfo)
     */
    @Override
    public void populate(DataGroupInfo pDataGroup)
    {
        String provider = pDataGroup.getTopParentDisplayName();
        Collection<String> categories = StreamUtilities.map(GroupCategorizationUtilities.getGroupCategories(pDataGroup, false),
            input -> StringUtilities.trim(input, 's'));
        String type = StringUtilities.join(", ", categories);
        String summary = pDataGroup.getSummaryDescription();

        String value = StringUtilities.concat("Provider: ", provider, "\n", "Type: ", type, "\n\n", summary, "\n");

        myTextArea.textProperty().setValue(value);
    }
}
