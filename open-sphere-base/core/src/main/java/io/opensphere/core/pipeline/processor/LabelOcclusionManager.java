package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.util.collections.New;

/**
 * Manager for handling occlusion of label geometries. This manager can be used
 * to ensure that labels' bounding boxes do not overlap.
 */
public class LabelOcclusionManager
{
    /**
     * A collection of label bounding boxes for each participating label
     * processor.
     */
    private final Map<LabelProcessor, Collection<ScreenBoundingBox>> myLabelBounds = New.map();

    /**
     * Determine whether the location is occluded. If the label is not occluded,
     * the location is saved and future requests which overlap this location
     * will be occluded.
     *
     * @param processor The processor which owns the label.
     * @param location The location of the label on screen.
     * @return true when the label is occluded.
     */
    public boolean isOccluded(LabelProcessor processor, ScreenBoundingBox location)
    {
        Collection<ScreenBoundingBox> bounds = myLabelBounds.get(processor);
        if (bounds == null)
        {
            bounds = New.collection();
            myLabelBounds.put(processor, bounds);
        }

        for (ScreenBoundingBox box : bounds)
        {
            if (box.overlaps(location, 0.))
            {
                return true;
            }
        }

        bounds.add(location);
        return false;
    }

    /**
     * Reset all occlusion regions for the processor.
     *
     * @param processor The processor for which to reset all occlusion regions.
     */
    public void reset(LabelProcessor processor)
    {
        myLabelBounds.remove(processor);
    }
}
