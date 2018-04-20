package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class GroupedStyleParameterEditorPanel.
 */
public class GroupedStyleParameterEditorPanel extends AbstractGroupedVisualizationControlPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Parameter groups. */
    private final List<StyleParameterEditorGroupPanel> myParameterGroups;

    /** The Reset panel. */
    private final JPanel myResetPanel;

    /**
     * Instantiates a new grouped style parameter editor panel.
     *
     * @param visualizationStyle the visualization style
     */
    public GroupedStyleParameterEditorPanel(MutableVisualizationStyle visualizationStyle)
    {
        this(visualizationStyle, false);
    }

    /**
     * Instantiates a new grouped style parameter editor panel.
     *
     * @param visualizationStyle the visualization style
     * @param liveUpdatePreviwableParameters the live update previwable
     *            parameters
     */
    public GroupedStyleParameterEditorPanel(MutableVisualizationStyle visualizationStyle, boolean liveUpdatePreviwableParameters)
    {
        super(visualizationStyle, liveUpdatePreviwableParameters);
        myParameterGroups = New.list();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JButton resetButton = new JButton("Restore Defaults");
        resetButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                revertToDefaultSettigns();
            }
        });
        myResetPanel = new JPanel(new BorderLayout());
        myResetPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        myResetPanel.setMaximumSize(new Dimension(1000, 40));
        myResetPanel.setPreferredSize(new Dimension(100, 40));
        myResetPanel.add(resetButton, BorderLayout.EAST);
        rebuild();
    }

    /**
     * Adds the group.
     *
     * @param group the group
     */
    @Override
    public void addGroup(StyleParameterEditorGroupPanel group)
    {
        synchronized (myParameterGroups)
        {
            myParameterGroups.add(group);
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
        synchronized (myParameterGroups)
        {
            for (StyleParameterEditorGroupPanel group : myParameterGroups)
            {
                group.update();
            }
        }
    }

    /**
     * Label panel.
     *
     * @param message the message
     * @param fontStyle the font style
     * @param fontSizeAddition the font size addition
     * @param height the height
     * @return the j panel
     */
    private JPanel labelPanel(String message, int fontStyle, int fontSizeAddition, int height)
    {
        JPanel labelPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(message);
        label.setFont(label.getFont().deriveFont(fontStyle, label.getFont().getSize() + fontSizeAddition));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        labelPanel.add(label);
        labelPanel.setMaximumSize(new Dimension(1000, height));
        labelPanel.setMinimumSize(new Dimension(100, height));
        labelPanel.setPreferredSize(new Dimension(300, height));
        return labelPanel;
    }

    /**
     * Rebuild.
     */
    private void rebuild()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                removeAll();
                add(labelPanel(getStyle().getStyleName(), Font.BOLD, 8, 30));
                if (!StringUtils.isBlank(getStyle().getStyleDescription()))
                {
                    add(textAreaPanel(getStyle().getStyleDescription(), Font.PLAIN, 2, 90));
                }
                synchronized (myParameterGroups)
                {
                    if (!myParameterGroups.isEmpty())
                    {
                        for (StyleParameterEditorGroupPanel pnl : myParameterGroups)
                        {
                            add(pnl);
                        }
                        add(myResetPanel);
                    }
                    else
                    {
                        add(Box.createVerticalGlue());
                        add(labelPanel("THIS STYLE HAS NO CONTROLS", Font.BOLD, 8, 30));
                    }
                }

                add(Box.createVerticalGlue());
                add(new JPanel());
                revalidate();
            }
        });
    }

    /**
     * Text area panel.
     *
     * @param message the message
     * @param fontStyle the font style
     * @param fontSizeAddition the font size addition
     * @param height the height
     * @return the j panel
     */
    private JPanel textAreaPanel(String message, int fontStyle, int fontSizeAddition, int height)
    {
        JPanel taPanel = new JPanel(new BorderLayout());
        taPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Style Description"),
                BorderFactory.createEmptyBorder(0, 20, 0, 20)));
        JTextArea ta = new JTextArea(message);
        ta.setEditable(false);
        ta.setBackground(taPanel.getBackground());
        ta.setFont(ta.getFont().deriveFont(fontStyle, ta.getFont().getSize() + fontSizeAddition));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane jsp = new JScrollPane(ta, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        taPanel.add(jsp);
        taPanel.setMaximumSize(new Dimension(1000, height));
        taPanel.setMinimumSize(new Dimension(100, height));
        taPanel.setPreferredSize(new Dimension(300, height));
        return taPanel;
    }

	@Override
	public void addGroupAtTop(StyleParameterEditorGroupPanel group) {
		return;
	}
}
