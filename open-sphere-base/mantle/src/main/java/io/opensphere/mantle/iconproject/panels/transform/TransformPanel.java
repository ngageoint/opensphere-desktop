package io.opensphere.mantle.iconproject.panels.transform;

import java.util.Map;

import org.controlsfx.control.SegmentedButton;

import io.opensphere.core.util.collections.New;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

/**
 * A panel in which the set of transform UIs are rendered.
 */
public class TransformPanel extends VBox
{
    /** The model in which transform state is maintained. */
    private final TransformModel myModel;

    /**
     * Creates a new Transform Panel.
     */
    public TransformPanel()
    {
        super(10);
        setAlignment(Pos.TOP_CENTER);
        myModel = new TransformModel();
        setMinHeight(120);

        Map<ToggleButton, AbstractTransform> transformDictionary = New.map();

        ToggleButton moveButton = new ToggleButton("Move");
        ToggleButton scaleButton = new ToggleButton("Scale");
        ToggleButton rotateButton = new ToggleButton("Rotate");
        SegmentedButton transformButtons = new SegmentedButton(moveButton, scaleButton, rotateButton);

        transformDictionary.put(moveButton, new MoveTransform(myModel));
        transformDictionary.put(rotateButton, new RotationTransform(myModel));
        transformDictionary.put(scaleButton, new ScaleTransform(myModel));

        transformButtons.getToggleGroup().selectedToggleProperty().addListener((obs, ov, nv) ->
        {
            getChildren().removeAll(transformDictionary.values());
            getChildren().add(transformDictionary.get(nv));
        });

        getChildren().add(transformButtons);
        getChildren().add(transformDictionary.get(moveButton));

        moveButton.selectedProperty().set(true);
        transformButtons.getToggleGroup().selectToggle(moveButton);
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
