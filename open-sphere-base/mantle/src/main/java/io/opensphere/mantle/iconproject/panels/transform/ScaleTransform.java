package io.opensphere.mantle.iconproject.panels.transform;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.WebHostingHubGlyphs;
import io.opensphere.core.util.fx.FxIcons;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/**
 * A transform panel in which the scale of the icon within its frame vertically
 * and horizontally is modified.
 */
public class ScaleTransform extends AbstractTransform
{

    /**
     * Creates a new transform panel bound to the supplied model.
     *
     * @param model the model in which transform state is maintained.
     */
    public ScaleTransform(TransformModel model)
    {
        Label verticalIcon = FxIcons.createClearIcon(WebHostingHubGlyphs.RESIZEVERTICALALT, Color.WHITE, 16);
        verticalIcon.setAlignment(Pos.CENTER);
        verticalIcon.setMinWidth(25);

        Label horizontalIcon = FxIcons.createClearIcon(WebHostingHubGlyphs.RESIZEHORIZONTALALT, Color.WHITE, 16);
        horizontalIcon.setMinWidth(25);
        horizontalIcon.setAlignment(Pos.CENTER);

        Slider verticalScale = createSlider(model.verticalScaleProperty(), .1, 5, 1.0);
        verticalScale.blockIncrementProperty().set(.1);

        Spinner<Double> verticalScaleSpinner = createSpinner(model.verticalScaleProperty(), .1, 5, 1);
        ((DoubleSpinnerValueFactory)verticalScaleSpinner.getValueFactory()).setAmountToStepBy(.05);
        Label verticalReset = FxIcons.createClearIcon(AwesomeIconSolid.TIMES_CIRCLE, Color.WHITE, 10);
        verticalReset.setOnMouseClicked(e -> model.verticalScaleProperty().set(1.0));
        verticalReset.setAlignment(Pos.CENTER);

        Slider horizontalScale = createSlider(model.horizontalScaleProperty(), .1, 5, 1.0);
        horizontalScale.blockIncrementProperty().set(.1);
        Spinner<Double> horizontalScaleSpinner = createSpinner(model.horizontalScaleProperty(), .1, 5, 1);
        ((DoubleSpinnerValueFactory)horizontalScaleSpinner.getValueFactory()).setAmountToStepBy(.05);
        Label horizontalReset = FxIcons.createClearIcon(AwesomeIconSolid.TIMES_CIRCLE, Color.WHITE, 10);
        horizontalReset.setOnMouseClicked(e -> model.horizontalScaleProperty().set(1.0));
        horizontalReset.setAlignment(Pos.CENTER);

        HBox.setHgrow(verticalIcon, Priority.NEVER);
        HBox.setHgrow(horizontalIcon, Priority.NEVER);
        HBox.setHgrow(horizontalScale, Priority.ALWAYS);
        HBox.setHgrow(verticalScale, Priority.ALWAYS);
        HBox.setHgrow(horizontalScaleSpinner, Priority.NEVER);
        HBox.setHgrow(verticalScaleSpinner, Priority.NEVER);
        HBox.setHgrow(verticalReset, Priority.NEVER);
        HBox.setHgrow(horizontalReset, Priority.NEVER);

        CheckBox lockPerspective = new CheckBox("Lock Together");
        lockPerspective.selectedProperty().set(true);
        verticalScale.valueProperty().bindBidirectional(horizontalScale.valueProperty());
        lockPerspective.selectedProperty().addListener((obs, ov, nv) ->
        {
            if (nv)
            {
                verticalScale.valueProperty().bindBidirectional(horizontalScale.valueProperty());
            }
            else
            {
                verticalScale.valueProperty().unbindBidirectional(horizontalScale.valueProperty());
            }
        });

        getChildren().add(new HBox(verticalIcon, verticalScale, verticalScaleSpinner, verticalReset));
        getChildren().add(new HBox(horizontalIcon, horizontalScale, horizontalScaleSpinner, horizontalReset));
        getChildren().add(lockPerspective);

        verticalScale.valueProperty().set(1.0);
        horizontalScale.valueProperty().set(1.0);
    }
}
