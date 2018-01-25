package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

/**
 * A {@link JPanel} that will adjust its preferred size based on the layout of
 * its components.
 */
public class AutosizePanel extends JPanel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AutosizePanel()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param isDoubleBuffered If double-buffering should be used.
     */
    public AutosizePanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
    }

    /**
     * Constructor.
     *
     * @param layout The layout manager.
     */
    public AutosizePanel(LayoutManager layout)
    {
        super(layout);
    }

    /**
     * Constructor.
     *
     * @param layout The layout manager.
     * @param isDoubleBuffered If double-buffering should be used.
     */
    public AutosizePanel(LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
    }

    /**
     * Do layout.
     */
    @Override
    public void doLayout()
    {
        super.doLayout();

        Component[] components = getComponents();
        if (components.length > 0)
        {
            List<Component> list = Arrays.asList(components);

            Dimension dim = new Dimension();
            dim.height = list.stream().mapToInt(c -> c.getY() + c.getHeight()).max().getAsInt();
            dim.width = list.stream().mapToInt(c -> c.getX() + c.getWidth()).max().getAsInt();

            if (!dim.equals(getPreferredSize()))
            {
                setPreferredSize(dim);
                EventQueue.invokeLater(this::revalidate);
            }
        }
    }
}
