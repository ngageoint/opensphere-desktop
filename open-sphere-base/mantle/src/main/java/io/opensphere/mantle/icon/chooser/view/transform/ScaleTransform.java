package io.opensphere.mantle.icon.chooser.view.transform;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.WebHostingHubGlyphs;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.mantle.icon.chooser.model.TransformModel;
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
    public ScaleTransform(final TransformModel model)
    {
        final Label verticalIcon = FxIcons.createClearIcon(WebHostingHubGlyphs.RESIZEVERTICALALT, Color.WHITE, 16);
        verticalIcon.setAlignment(Pos.CENTER);
        verticalIcon.setMinWidth(25);

        final Label horizontalIcon = FxIcons.createClearIcon(WebHostingHubGlyphs.RESIZEHORIZONTALALT, Color.WHITE, 16);
        horizontalIcon.setMinWidth(25);
        horizontalIcon.setAlignment(Pos.CENTER);

        final Slider verticalScale = createSlider(model.verticalScaleProperty(), .1, 5, TransformModel.DEFAULT_VERTICAL_SCALE);
        verticalScale.blockIncrementProperty().set(.1);

        final Spinner<Double> verticalScaleSpinner = createSpinner(model.verticalScaleProperty(), .1, 5,
                TransformModel.DEFAULT_VERTICAL_SCALE);
        ((DoubleSpinnerValueFactory)verticalScaleSpinner.getValueFactory()).setAmountToStepBy(.05);
        final Label verticalReset = FxIcons.createClearIcon(AwesomeIconSolid.TIMES, Color.ORANGERED, 14);
        verticalReset.setOnMouseClicked(e -> model.verticalScaleProperty().set(TransformModel.DEFAULT_VERTICAL_SCALE));
        verticalReset.setAlignment(Pos.CENTER);

        final Slider horizontalScale = createSlider(model.horizontalScaleProperty(), .1, 5,
                TransformModel.DEFAULT_HORIZONTAL_SCALE);
        horizontalScale.blockIncrementProperty().set(.1);
        final Spinner<Double> horizontalScaleSpinner = createSpinner(model.horizontalScaleProperty(), .1, 5,
                TransformModel.DEFAULT_HORIZONTAL_SCALE);
        ((DoubleSpinnerValueFactory)horizontalScaleSpinner.getValueFactory()).setAmountToStepBy(.05);
        final Label horizontalReset = FxIcons.createClearIcon(AwesomeIconSolid.TIMES, Color.ORANGERED, 14);
        horizontalReset.setOnMouseClicked(e -> model.horizontalScaleProperty().set(TransformModel.DEFAULT_HORIZONTAL_SCALE));
        horizontalReset.setAlignment(Pos.CENTER);

        HBox.setHgrow(verticalIcon, Priority.NEVER);
        HBox.setHgrow(horizontalIcon, Priority.NEVER);
        HBox.setHgrow(horizontalScale, Priority.ALWAYS);
        HBox.setHgrow(verticalScale, Priority.ALWAYS);
        HBox.setHgrow(horizontalScaleSpinner, Priority.NEVER);
        HBox.setHgrow(verticalScaleSpinner, Priority.NEVER);
        HBox.setHgrow(verticalReset, Priority.NEVER);
        HBox.setHgrow(horizontalReset, Priority.NEVER);

        final CheckBox lockPerspective = new CheckBox("Lock Together");
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

        final HBox verticalBox = new HBox(5, verticalIcon, verticalScale, verticalScaleSpinner, verticalReset);
        verticalBox.setAlignment(Pos.CENTER);
        getChildren().add(verticalBox);
        final HBox horizontalBox = new HBox(5, horizontalIcon, horizontalScale, horizontalScaleSpinner, horizontalReset);
        horizontalBox.setAlignment(Pos.CENTER);
        getChildren().add(horizontalBox);
        getChildren().add(lockPerspective);

        verticalScale.valueProperty().set(1.0);
        horizontalScale.valueProperty().set(1.0);
    }
}
