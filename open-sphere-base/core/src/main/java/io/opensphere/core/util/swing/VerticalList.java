package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JComponent;

/**
 * This is a simple container that lays out its subcomponents vertically, giving
 * each its own preferred height and the preferred width of the widest
 * subcomponent. Its own preferred dimensions are the sum of the subcomponents'
 * preferred heights and the maximum among their preferred widths (plus any
 * insets). The order of the subcomponents, from top to bottom, is the same as
 * the order in which they are added to this container; changing their order
 * requires removing and reinserting them. <br>
 * <br>
 * To be precise, this class performs approximately the same function as a
 * vertical Box (i.e., a JPanel sporting the BoxLayout). However, unlike the
 * latter, this class always performs as expected, whereas the BoxLayout tries
 * to do some fancy (but unnecessary) thing that sometimes malfunctions.
 */
public class VerticalList extends JComponent
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doLayout()
    {
        Dimension d = getSize();
        Insets ins = getInsets();
        int y = ins.top;
        int x = ins.left;
        int dx = d.width - x - ins.right;
        for (Component c : getComponents())
        {
            Dimension cSize = c.getPreferredSize();
            c.setBounds(x, y, dx, cSize.height);
            y += cSize.height;
        }
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension d = new Dimension();
        Insets ins = getInsets();
        d.height += ins.top + ins.bottom;
        for (Component c : getComponents())
        {
            Dimension cSize = c.getPreferredSize();
            d.width = Math.max(d.width, cSize.width);
            d.height += cSize.height;
        }
        d.width += ins.left + ins.right;
        return d;
    }

    @Override
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }
}
