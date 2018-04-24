package io.opensphere.mantle.data.geom.style.impl.ui;

import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/** Additional abstraction over Grouped Style Panels. */
public abstract class AbstractGroupedVisualizationControlPanel extends AbstractVisualizationControlPanel
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * @param visualizationStyle
     */
    public AbstractGroupedVisualizationControlPanel(MutableVisualizationStyle visualizationStyle)
    {
        super(visualizationStyle);
    }

    /**
     * @param visualizationStyle
     * @param liveUpdatePreviwable
     */
    public AbstractGroupedVisualizationControlPanel(MutableVisualizationStyle visualizationStyle, boolean liveUpdatePreviwable)
    {
        super(visualizationStyle, liveUpdatePreviwable);
    }

    /**
     * Adds the group to the panel.
     *
     * @param group the group
     */
    public abstract void addGroup(StyleParameterEditorGroupPanel group);

    /**
     * Adds the group at top.
     *
     * @param group the group
     */
    public abstract void addGroupAtTop(StyleParameterEditorGroupPanel group);
}
