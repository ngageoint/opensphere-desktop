package io.opensphere.search.mapzen.view;

import io.opensphere.core.Toolbox;
import io.opensphere.core.options.impl.AbstractJFXOptionsProvider;
import io.opensphere.search.mapzen.model.MapZenSettingsModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/** The options provider for MapZen configurations. */
public class MapZenOptionsProvider extends AbstractJFXOptionsProvider
{
    /** The text field in which API Key is modified. */
    private TextField myApiKey;

    /** The text field in which URL template is modified. */
    private TextField mySearchUrlTemplate;

    /** The model in which banner configuration state is maintained. */
    private final MapZenSettingsModel myModel;

    /**
     * Creates a new options provider, bound to the supplied model.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param model the settings model to which the provider is bound.
     */
    public MapZenOptionsProvider(Toolbox toolbox, MapZenSettingsModel model)
    {
        super("MapZen");
        myModel = model;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.impl.AbstractJFXOptionsProvider#getJFXOptionsPanel()
     */
    @Override
    public Node getJFXOptionsPanel()
    {
        GridPane form = new GridPane();
        form.hgapProperty().set(10);
        form.vgapProperty().set(5);

        mySearchUrlTemplate = new TextField();
        mySearchUrlTemplate.prefColumnCountProperty().set(25);
        mySearchUrlTemplate.textProperty().bindBidirectional(myModel.searchUrlTemplateProperty());

        myApiKey = new TextField();
        myApiKey.prefColumnCountProperty().set(25);
        myApiKey.textProperty().bindBidirectional(myModel.apiKeyProperty());

        int index = 0;
        form.addRow(index++, new Label("Search URL Template:"), mySearchUrlTemplate);
        form.addRow(index++, new Label("MapZen API Key:"), myApiKey);

        GridPane.setHgrow(mySearchUrlTemplate, Priority.ALWAYS);
        GridPane.setHgrow(myApiKey, Priority.ALWAYS);

        return form;
    }
}
