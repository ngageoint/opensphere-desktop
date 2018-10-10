package io.opensphere.mantle.data.geom.style.impl.ui;

import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class GroupedMiniStyleEditorPanel.
 */
public class GroupedMiniStyleEditorPanel extends AbstractGroupedVisualizationControlPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Parameter groups. */
    private final List<StyleParameterEditorGroupPanel> myParamGroups;

    /**
     * Instantiates a new grouped style parameter editor panel.
     *
     * @param visualizationStyle the visualization style
     */
    public GroupedMiniStyleEditorPanel(MutableVisualizationStyle visualizationStyle)
    {
        super(visualizationStyle, true);
        myParamGroups = New.list();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        rebuild();
    }

    /**
     * Adds the group to the panel.
     *
     * @param group the group
     */
    @Override
    public void addGroup(StyleParameterEditorGroupPanel group)
    {
        synchronized (myParamGroups)
        {
            myParamGroups.add(group);
        }
        rebuild();
    }

    /**
     * Adds the group at top.
     *
     * @param group the group
     */
    @Override
    public void addGroupAtTop(StyleParameterEditorGroupPanel group)
    {
        synchronized (myParamGroups)
        {
            myParamGroups.add(0, group);
        }
        rebuild();
    }

    @Override
    public void revertToDefaultSettigns()
    {
        getChangedStyle().revertToDefaultParameters(this);
    }

    @Override
    public void update()
    {
        synchronized (myParamGroups)
        {
            for (StyleParameterEditorGroupPanel group : myParamGroups)
            {
                group.update();
            }
        }
    }

    /**
     * Rebuild.
     */
    private void rebuild()
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            removeAll();
            synchronized (myParamGroups)
            {
                if (!myParamGroups.isEmpty())
                {
                    for (StyleParameterEditorGroupPanel pnl : myParamGroups)
                    {
                        add(pnl);
                    }
                }
            }

            add(Box.createVerticalGlue());
            add(new JPanel());
            revalidate();
        });
    }
}
