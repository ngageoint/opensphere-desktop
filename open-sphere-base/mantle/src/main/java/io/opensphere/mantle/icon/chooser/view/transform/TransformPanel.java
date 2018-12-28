package io.opensphere.mantle.icon.chooser.view.transform;

import java.util.Map;

import org.controlsfx.control.SegmentedButton;

import io.opensphere.core.function.Procedure;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.mantle.icon.chooser.model.TransformModel;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * A panel in which the set of transform UIs are rendered.
 */
public class TransformPanel extends VBox
{
    /** The model in which transform state is maintained. */
    private final TransformModel myModel;

    /** The listener called when the save event is invoked. */
    private final Procedure mySaveListener;

    /**
     * Creates a new Transform Panel.
     *
     * @param saveListener the procedure called when the user chooses to save
     *            transformation operations.
     */
    public TransformPanel(Procedure saveListener)
    {
        super(10);
        mySaveListener = saveListener;
        setAlignment(Pos.TOP_CENTER);
        myModel = new TransformModel();
        setMinHeight(120);

        Map<ToggleButton, AbstractTransform> transformDictionary = New.map();

        ToggleButton moveButton = new ToggleButton("Move");
        ToggleButton scaleButton = new ToggleButton("Scale");
        ToggleButton rotateButton = new ToggleButton("Rotate");
        SegmentedButton transformButtons = new SegmentedButton(moveButton, scaleButton, rotateButton);
        // enforce persistent toggle, so that the user cannot untoggle all of
        // the buttons:
        transformButtons.toggleGroupProperty().get().selectedToggleProperty().addListener((obs, ov, nv) ->
        {
            if (nv == null)
            {
                ov.setSelected(true);
            }
        });

        transformDictionary.put(moveButton, new MoveTransform(myModel));
        transformDictionary.put(rotateButton, new RotationTransform(myModel));
        transformDictionary.put(scaleButton, new ScaleTransform(myModel));

        VBox controlBox = new VBox(10, transformDictionary.get(moveButton));

        transformButtons.getToggleGroup().selectedToggleProperty().addListener((obs, ov, nv) ->
        {
            controlBox.getChildren().removeAll(transformDictionary.values());
            controlBox.getChildren().add(transformDictionary.get(nv));
        });

        getChildren().add(transformButtons);
        getChildren().add(controlBox);

        moveButton.selectedProperty().set(true);
        transformButtons.getToggleGroup().selectToggle(moveButton);

        Label saveButton = FxIcons.createClearIcon(AwesomeIconSolid.CHECK_CIRCLE, Color.LIMEGREEN, 18);
        saveButton.disableProperty().bind(Bindings.not(myModel.changedProperty()));
        saveButton.onMouseClickedProperty().set(e -> mySaveListener.invoke());

        Label resetButton = FxIcons.createClearIcon(AwesomeIconSolid.TIMES_CIRCLE, Color.ORANGERED, 18);
        resetButton.disableProperty().bind(Bindings.not(myModel.changedProperty()));
        resetButton.onMouseClickedProperty().set(e -> myModel.resetAllToDefault());

        Region regionLeft = new Region();
        Region regionRight = new Region();
        HBox buttonBox = new HBox(10, regionLeft, saveButton, resetButton, regionRight);
        HBox.setHgrow(regionLeft, Priority.ALWAYS);
        HBox.setHgrow(regionRight, Priority.ALWAYS);
        getChildren().add(buttonBox);
    }

    /**
     * Gets the value of the {@link #myModel} field.
     *
     * @return the value stored in the {@link #myModel} field.
     */
    public TransformModel getModel()
    {
        return myModel;
    }
}
