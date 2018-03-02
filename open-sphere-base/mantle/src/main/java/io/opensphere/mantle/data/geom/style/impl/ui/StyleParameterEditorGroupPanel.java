package io.opensphere.mantle.data.geom.style.impl.ui;

import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.EtchedBorder;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * The Class StyleParameterEditorGroupPanel.
 */
public class StyleParameterEditorGroupPanel extends GridBagPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Editor panels. */
    private final List<AbstractStyleParameterEditorPanel> myEditorPanels;

    /** The Show border. */
    private final boolean myShowBorder;

    /** The Spacing. */
    private final int mySpacing;

    /**
     * Instantiates a new style parameter editor group panel.
     */
    public StyleParameterEditorGroupPanel()
    {
        this(null, Collections.<AbstractStyleParameterEditorPanel>emptyList(), true, 4);
    }

    /**
     * Instantiates a new style parameter editor group panel.
     *
     * @param panels the panels
     */
    public StyleParameterEditorGroupPanel(List<AbstractStyleParameterEditorPanel> panels)
    {
        this(null, panels, true, 4);
    }

    /**
     * Instantiates a new style parameter editor group panel.
     *
     * @param borderTitle the border title
     */
    public StyleParameterEditorGroupPanel(String borderTitle)
    {
        this(borderTitle, Collections.<AbstractStyleParameterEditorPanel>emptyList(), true, 4);
    }

    /**
     * Instantiates a new style parameter editor group panel.
     *
     * @param borderTitle the border title
     * @param panels the panels
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public StyleParameterEditorGroupPanel(String borderTitle, List<AbstractStyleParameterEditorPanel> panels)
    {
        this(borderTitle, panels, true, 4);
    }

    /**
     * Instantiates a new style parameter editor group panel.
     *
     * @param borderTitle the border title
     * @param panels the panels
     * @param showBorders the show borders
     * @param panelSpacing the panel spacing
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public StyleParameterEditorGroupPanel(String borderTitle, List<AbstractStyleParameterEditorPanel> panels, boolean showBorders,
            int panelSpacing)
    {
        super();
        anchorWest();
        fillHorizontal();
        myEditorPanels = New.list();
        if (borderTitle != null && !borderTitle.isEmpty())
        {
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), borderTitle));
        }
        myEditorPanels.addAll(panels);
        myShowBorder = showBorders;
        mySpacing = panelSpacing;
        rebuild();
    }

    /**
     * Adds the editor panel.
     *
     * @param panel the panel
     */
    public void addEditorPanel(AbstractStyleParameterEditorPanel panel)
    {
        myEditorPanels.add(panel);
        rebuild();
    }

    /**
     * Rebuild internal.
     */
    public void rebuildInternal()
    {
        removeAll();
        for (AbstractStyleParameterEditorPanel pnl : myEditorPanels)
        {
            JComponent panel = pnl;
            if (!pnl.getSiblingComponents().isEmpty())
            {
                GridBagPanel multiPanel = new GridBagPanel();
                multiPanel.anchorWest();
                multiPanel.add(pnl);
                multiPanel.fillHorizontal();
                for (AbstractStyleParameterEditorPanel sibling : pnl.getSiblingComponents())
                {
                    multiPanel.add(sibling);
                }
                panel = multiPanel;
            }

            if (myShowBorder)
            {
                panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20), BorderFactory
                        .createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(2, 0, 2, 2))));
            }
            addRow(panel);

            if (mySpacing > 0)
            {
                addRow(Box.createVerticalStrut(mySpacing));
            }
        }
        revalidate();
    }

    /**
     * Update.
     */
    public void update()
    {
        for (AbstractStyleParameterEditorPanel pnl : myEditorPanels)
        {
            pnl.updateAll();
        }
    }

    /**
     * Rebuild.
     */
    private void rebuild()
    {
        EventQueueUtilities.runOnEDT(this::rebuildInternal);
    }
}
