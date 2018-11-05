package io.opensphere.mantle.iconproject.panels.transform;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FxIcons;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/**
 * A transform panel used to rotate an icon around a center point.
 */
public class RotationTransform extends AbstractTransform
{
    /**
     * Creates a new transform panel bound to the supplied model.
     *
     * @param model the model in which transform state is maintained.
     */
    public RotationTransform(TransformModel model)
    {
        Label rotate = FxIcons.createClearIcon(AwesomeIconSolid.SYNC, Color.WHITE, 16);
        rotate.setAlignment(Pos.CENTER);
        rotate.setMinWidth(25);

        Slider slider = createSlider(model.rotationProperty(), -180, 180, 0.0);
        Spinner<Double> spinner = createSpinner(model.rotationProperty(), -180, 180, 0.0);
        spinner.getValueFactory().setWrapAround(true);

        Label reset = FxIcons.createClearIcon(AwesomeIconSolid.TIMES_CIRCLE, Color.WHITE, 10);
        reset.setOnMouseClicked(e -> model.rotationProperty().set(0));
        reset.setAlignment(Pos.CENTER);

        HBox.setHgrow(rotate, Priority.NEVER);
        HBox.setHgrow(slider, Priority.ALWAYS);
        HBox.setHgrow(spinner, Priority.ALWAYS);
        HBox.setHgrow(reset, Priority.NEVER);

        HBox rotateBox = new HBox(5, rotate, slider, spinner, reset);
        rotateBox.setAlignment(Pos.CENTER);
        getChildren().add(rotateBox);
    }
}
