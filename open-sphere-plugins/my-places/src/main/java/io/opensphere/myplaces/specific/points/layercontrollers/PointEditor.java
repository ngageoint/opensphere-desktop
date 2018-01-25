package io.opensphere.myplaces.specific.points.layercontrollers;

import io.opensphere.myplaces.editor.PlacesEditor;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.editor.view.AnnotationEditorPanel;
import io.opensphere.myplaces.specific.points.editor.PointAnnotationEditorPanel;

/**
 * Edits a single point.
 *
 */
public class PointEditor extends PlacesEditor
{
    /**
     * Constructs a new point editor.
     *
     * @param controller The edit controller.
     */
    public PointEditor(AnnotationEditController controller)
    {
        super(controller);
    }

    @Override
    protected AnnotationEditorPanel instantiateEditor(AnnotationEditController controller)
    {
        return new PointAnnotationEditorPanel(controller);
    }
}
