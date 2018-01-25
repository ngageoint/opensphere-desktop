package io.opensphere.xyztile.mantle;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import io.opensphere.xyztile.model.XYZSettings;

/**
 * The panel for the XYZ settings.
 */
public class XYZSettingsPanel extends BorderPane
{
    /**
     * The max zoom level spinner.
     */
    private final Spinner<Integer> myMaxZoomSpinner;

    /**
     * The model to update.
     */
    private final XYZSettings myModel;

    /**
     * Constructs a new panel.
     *
     * @param settings The settings the user can modify.
     */
    public XYZSettingsPanel(XYZSettings settings)
    {
        myModel = settings;

        Label maxZoomLabel = new Label("Max Zoom:");
        myMaxZoomSpinner = new Spinner<>(myModel.getMinZoomLevel(), settings.getMaxZoomLevelDefault(), settings.getMaxZoomLevelCurrent());
        myMaxZoomSpinner.setPrefWidth(60);

        GridPane pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setHgap(20);
        GridPane.setConstraints(maxZoomLabel, 0, 0);
        pane.getChildren().add(maxZoomLabel);

        GridPane.setConstraints(myMaxZoomSpinner, 1, 0);
        pane.getChildren().add(myMaxZoomSpinner);

        myMaxZoomSpinner.valueProperty().addListener(this::updateConfidenceValue);

        setCenter(pane);
    }

    /**
     * Gets the max zoom spinner.
     *
     * @return The max zoom spinner.
     */
    protected Spinner<Integer> getMaxZoomSpinner()
    {
        return myMaxZoomSpinner;
    }

    /**
     * Updates the confidence value based on the new value in the slider.
     *
     * @param ov The observable.
     * @param oldVal The old value.
     * @param newVal The new value.
     */
    private void updateConfidenceValue(ObservableValue<? extends Integer> ov, Integer oldVal, Integer newVal)
    {
        int newValue = newVal.intValue();
        myModel.setMaxZoomLevelCurrent(newValue);
    }
}
