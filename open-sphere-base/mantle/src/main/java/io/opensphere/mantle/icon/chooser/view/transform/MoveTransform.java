package io.opensphere.mantle.icon.chooser.view.transform;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.mantle.icon.chooser.model.TransformModel;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/**
 * A transform panel in which the movement of the icon within its frame
 * vertically and horizontally is modified.
 */
public class MoveTransform extends AbstractTransform
{

    /**
     * Creates a new transform panel bound to the supplied model.
     *
     * @param model the model in which transform state is maintained.
     */
    public MoveTransform(TransformModel model)
    {
        Label verticalIcon = FxIcons.createClearIcon(AwesomeIconSolid.ARROWS_ALT_V, Color.WHITE, 16);
        verticalIcon.setAlignment(Pos.CENTER);
        verticalIcon.setMinWidth(25);
        Label horizontalIcon = FxIcons.createClearIcon(AwesomeIconSolid.ARROWS_ALT_H, Color.WHITE, 16);
        horizontalIcon.setMinWidth(25);
        horizontalIcon.setAlignment(Pos.CENTER);

        Label recenterHorizontal = FxIcons.createClearIcon(AwesomeIconSolid.TIMES, Color.ORANGERED, 14);
        recenterHorizontal.setOnMouseClicked(e -> model.horizontalMoveProperty().set(TransformModel.DEFAULT_HORIZONTAL_MOVE));
        recenterHorizontal.setAlignment(Pos.CENTER);
        Label recenterVertical = FxIcons.createClearIcon(AwesomeIconSolid.TIMES, Color.ORANGERED, 14);
        recenterVertical.setOnMouseClicked(e -> model.verticalMoveProperty().set(TransformModel.DEFAULT_VERTICAL_MOVE));
        recenterVertical.setAlignment(Pos.CENTER);

        Slider horizontalMoveSlider = createSlider(model.horizontalMoveProperty(), -100, 100,
                TransformModel.DEFAULT_HORIZONTAL_MOVE);
        Spinner<Double> horizontalMoveSpinner = createSpinner(model.horizontalMoveProperty(), -100, 100,
                TransformModel.DEFAULT_HORIZONTAL_MOVE);

        Slider verticalMoveSlider = createSlider(model.verticalMoveProperty(), -100, 100, TransformModel.DEFAULT_VERTICAL_MOVE);
        Spinner<Double> verticalMoveSpinner = createSpinner(model.verticalMoveProperty(), -100, 100,
                TransformModel.DEFAULT_VERTICAL_MOVE);

        HBox.setHgrow(verticalIcon, Priority.NEVER);
        HBox.setHgrow(horizontalIcon, Priority.NEVER);
        HBox.setHgrow(horizontalMoveSlider, Priority.ALWAYS);
        HBox.setHgrow(verticalMoveSlider, Priority.ALWAYS);
        HBox.setHgrow(verticalMoveSpinner, Priority.NEVER);
        HBox.setHgrow(horizontalMoveSpinner, Priority.NEVER);
        HBox.setHgrow(recenterHorizontal, Priority.NEVER);
        HBox.setHgrow(recenterVertical, Priority.NEVER);

        HBox verticalAdjustBox = new HBox(5, verticalIcon, verticalMoveSlider, verticalMoveSpinner, recenterVertical);
        verticalAdjustBox.setAlignment(Pos.CENTER);
        getChildren().add(verticalAdjustBox);
        HBox horizontalAdjustBox = new HBox(5, horizontalIcon, horizontalMoveSlider, horizontalMoveSpinner, recenterHorizontal);
        horizontalAdjustBox.setAlignment(Pos.CENTER);
        getChildren().add(horizontalAdjustBox);
    }
}
