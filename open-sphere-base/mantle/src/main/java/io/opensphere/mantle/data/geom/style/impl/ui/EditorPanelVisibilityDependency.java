package io.opensphere.mantle.data.geom.style.impl.ui;

import java.util.Arrays;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel.FeatureVisualizationControlPanelListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;

/**
 * The Class EditorPanelVisibilityDependency.
 */
public class EditorPanelVisibilityDependency implements FeatureVisualizationControlPanelListener
{
    /** The Style to monitor. */
    private final AbstractVisualizationControlPanel myControlPanelToMonitor;

    /** The Panel to show hide. */
    private final Set<AbstractStyleParameterEditorPanel> myPanelsToShowHide;

    /** The Visibility constraints. */
    private final Set<VisibilityConstraint> myVisibilityConstraints;

    /**
     * Instantiates a new editor panel visibility dependency.
     *
     * @param controlPanelToMonitor the style to monitor
     * @param panelsToShowHide the panels to show hide
     */
    public EditorPanelVisibilityDependency(AbstractVisualizationControlPanel controlPanelToMonitor,
            AbstractStyleParameterEditorPanel... panelsToShowHide)
    {
        myPanelsToShowHide = New.set();
        myVisibilityConstraints = New.set();
        if (panelsToShowHide != null)
        {
            myPanelsToShowHide.addAll(Arrays.asList(panelsToShowHide));
        }
        myControlPanelToMonitor = controlPanelToMonitor;
        myControlPanelToMonitor.addListener(this);
    }

    /**
     * Adds the constraint.
     *
     * @param c the c
     */
    public void addConstraint(VisibilityConstraint c)
    {
        myVisibilityConstraints.add(c);
    }

    /**
     * Evaluate style.
     */
    public void evaluateStyle()
    {
        boolean allVisible = true;
        VisualizationStyle styleToCheck = myControlPanelToMonitor.getChangedStyle();
        if (styleToCheck != null)
        {
            for (VisibilityConstraint c : myVisibilityConstraints)
            {
                if (!c.isVisible(styleToCheck))
                {
                    allVisible = false;
                    break;
                }
            }
        }
        setPanelsVisibility(allVisible);
    }

    @Override
    public void performLiveParameterUpdate(String dtiKey, Class<? extends VisualizationSupport> convertedClass,
            Class<? extends VisualizationStyle> vsClass, Set<VisualizationStyleParameter> updateSet)
    {
        // Do nothing.
    }

    @Override
    public void styleChanged(boolean hasChangesFromBase)
    {
        evaluateStyle();
    }

    @Override
    public void styleChangesAccepted()
    {
        evaluateStyle();
    }

    @Override
    public void styleChangesCancelled()
    {
        evaluateStyle();
    }

    /**
     * Sets the panels visibility.
     *
     * @param visible the new panels visibility
     */
    protected void setPanelsVisibility(final boolean visible)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            myPanelsToShowHide.forEach(p -> p.setVisible(visible));
            myControlPanelToMonitor.revalidate();
            myControlPanelToMonitor.repaint();
        });
    }
}
