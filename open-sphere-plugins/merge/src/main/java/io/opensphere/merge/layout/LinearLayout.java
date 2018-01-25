package io.opensphere.merge.layout;

import java.util.LinkedList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * A LinearLayout is a very generic layout strategy that places subcomponents
 * into a horizontal or vertical list, depending on the orientation parameter.
 * Subcomponents can be JavaFX nodes, other SubLayout implementations, or even
 * empty spaces. This class's glib methods greatly simplify the process of
 * assembling a JavaFX GUI from basic components.
 */
public class LinearLayout implements SubLayout
{
    /** The orientation, either HORIZONTAL or VERTICAL. */
    protected Orientation dir = Orientation.VERTICAL;

    /** The root Pane, parent of all managed subcomponents. */
    protected Pane root;

    /** The ordered list of managed subcomponents. */
    protected List<Wrap> kids = new LinkedList<>();

    /**
     * Factory method for creating rows.
     *
     * @return a LinearLayout
     */
    public static LinearLayout row()
    {
        LinearLayout lay = new LinearLayout();
        lay.dir = Orientation.HORIZONTAL;
        return lay;
    }

    /**
     * Factory method for creating columns.
     *
     * @return a LinearLayout
     */
    public static LinearLayout col()
    {
        LinearLayout lay = new LinearLayout();
        lay.dir = Orientation.VERTICAL;
        return lay;
    }

    @Override
    public void setRoot(Pane r)
    {
        for (Wrap wr : kids)
        {
            wr.replaceRoot(root, r);
        }
        root = r;
    }

    /** Drop all managed subcomponents. */
    public void clear()
    {
        for (Wrap wr : kids)
        {
            wr.replaceRoot(root, null);
        }
        kids.clear();
    }

    /**
     * Add a managed subcomponent and introduce it to the root Pane.
     *
     * @param wr a Wrap
     */
    private void addKid(Wrap wr)
    {
        kids.add(wr);
        wr.replaceRoot(null, root);
    }

    /**
     * Add a fixed amount of longitudinal space to the layout.
     *
     * @param sp pixels
     */
    public void addSpace(double sp)
    {
        kids.add(Wrap.space(dir, sp));
    }

    /**
     * Add a stretchable longitudinal space to the layout.
     *
     * @param sp pixels
     * @param str stretch factor
     */
    public void addSpace(double sp, double str)
    {
        kids.add(Wrap.space(dir, sp, str));
    }

    /**
     * Add a Node with default settings: center justify and use its own
     * preferred width and height.
     *
     * @param n a Node
     */
    public void add(Node n)
    {
        add(n, LayMode.CENTER);
    }

    /**
     * Add a Node with specified justification rule and use its own preferred
     * width and height.
     *
     * @param n a Node
     * @param lay justification rule
     */
    public void add(Node n, LayMode lay)
    {
        addKid(Wrap.node(dir, n, lay));
    }

    /**
     * Add a Node with specified justification and fixed length.
     *
     * @param n a Node
     * @param lay justification rule
     * @param len length
     */
    public void add(Node n, LayMode lay, double len)
    {
        addKid(Wrap.node(dir, n, lay, len, 0.0));
    }

    /**
     * Add a stretchable Node with specified justification and fixed length.
     *
     * @param n a Node
     * @param lay justification rule
     * @param len length
     * @param str stretch factor (longitudinal)
     */
    public void add(Node n, LayMode lay, double len, double str)
    {
        addKid(Wrap.node(dir, n, lay, len, str));
    }

    /**
     * Add a Node with specified justification rule and fixed dimensions.
     *
     * @param n a Node
     * @param lay justification rule
     * @param w width
     * @param h height
     */
    public void addFixed(Node n, LayMode lay, double w, double h)
    {
        addKid(Wrap.fixed(dir, n, lay, w, h));
    }

    /**
     * Add a Node and stretch or shrink it to fit the available space.
     *
     * @param n a Node
     * @param str stretch factor (longitudinal)
     */
    public void addStretch(Node n, double str)
    {
        addKid(Wrap.stretch(dir, n, str));
    }

    /**
     * Add a Node with fixed minimum dimensions and stretch it if extra space is
     * available.
     *
     * @param n a Node
     * @param w width
     * @param h height
     * @param str stretch factor (longitudinal)
     */
    public void addStretch(Node n, double w, double h, double str)
    {
        addKid(Wrap.stretch(dir, n, w, h, str));
    }

    /**
     * Add a Node with fixed minimum dimensions and stretch it longitudinally if
     * extra space is available. Transverse justification is specified.
     *
     * @param n a Node
     * @param lay justification rule
     * @param w width
     * @param h height
     * @param str stretch factor (longitudinal)
     */
    public void addStretch(Node n, LayMode lay, double w, double h, double str)
    {
        Wrap wr = Wrap.stretch(dir, n, w, h, str);
        wr.mode = lay;
        addKid(wr);
    }

    /**
     * Add a SubLayout and treat it as a default transverse list. I.e., stretch
     * it to fit the entire breadth and give it its preferred length.
     *
     * @param sub a SubLayout
     */
    public void addAcross(SubLayout sub)
    {
        addKid(Wrap.across(dir, sub));
    }

    /**
     * Add a SubLayout with a fixed length and stretch it to fit the entire
     * breadth.
     *
     * @param sub a SubLayout
     * @param len length
     */
    public void addAcross(SubLayout sub, double len)
    {
        addKid(Wrap.across(dir, sub, len));
    }

    @Override
    public double getWidth()
    {
        if (dir == Orientation.HORIZONTAL)
        {
            return getLength();
        }
        else if (dir == Orientation.VERTICAL)
        {
            return getBreadth();
        }
        throw new IllegalStateException("orientation is not specified");
    }

    @Override
    public double getHeight()
    {
        if (dir == Orientation.HORIZONTAL)
        {
            return getBreadth();
        }
        else if (dir == Orientation.VERTICAL)
        {
            return getLength();
        }
        throw new IllegalStateException("orientation is not specified");
    }

    @Override
    public void doLayout(double x0, double y0, double dx, double dy)
    {
        // first measure the total of preferred heights and stretches
        double lenTot = 0.0;
        double sTot = 0.0;
        for (Wrap w : kids)
        {
            lenTot += Math.max(0.0, w.getLength());
            sTot += Math.max(0.0, w.stretch);
        }
        // subtract from the available height to get the amount of stretch
        double strLength = Math.max(0.0, lCoord(dx, dy) - lenTot);

        double pos = lCoord(x0, y0);
        for (Wrap wr : kids)
        {
            double len = wr.getLength();
            // if stretchy, then snag a portion of the extra height
            if (wr.stretch > 0.0)
            {
                double dLen = strLength * wr.stretch / sTot;
                sTot -= wr.stretch;
                strLength -= dLen;
                len += dLen;
            }
            Interval cross = layAcross(wr, bCoord(x0, y0), bCoord(dx, dy));
            if (dir == Orientation.HORIZONTAL)
            {
                wr.doLayout(pos, cross.v0, len, cross.dv);
            }
            else
            {
                wr.doLayout(cross.v0, pos, cross.dv, len);
            }
            pos += len;
        }
    }

    /**
     * Find the transverse Interval to allocate to a subcomponent based upon its
     * preferred breadth and justification rule.
     *
     * @param wr the subcomponent wrapper
     * @param v0 the minimum allowable value
     * @param dv the maximum allowable range
     * @return the calculated Interval
     */
    protected static Interval layAcross(Wrap wr, double v0, double dv)
    {
        if (wr.mode == null)
        {
            return new Interval();
        }
        return getIvl(wr.getBreadth(), wr.mode, v0, dv);
    }

    /**
     * Technical helper implementation for layAcross (q.v.).
     *
     * @param brd the desired breadth
     * @param mode the justification rule
     * @param v0 the minimum allowable value
     * @param dv the maximum allowable range
     * @return the calculated Interval
     */
    private static Interval getIvl(double brd, LayMode mode, double v0, double dv)
    {
        Interval ivl = new Interval();
        if (mode == LayMode.MIN)
        {
            ivl.set(v0, Math.min(dv, brd));
        }
        else if (mode == LayMode.CENTER)
        {
            ivl.set(v0 + Math.max(0, (dv - brd) / 2.0), Math.min(dv, brd));
        }
        else if (mode == LayMode.MAX)
        {
            ivl.set(v0 + Math.max(0.0, dv - brd), Math.min(dv, brd));
        }
        else if (mode == LayMode.STRETCH)
        {
            ivl.set(v0, dv);
        }
        return ivl;
    }

    /** Utility struct for a real interval by left endpoint and length. */
    protected static class Interval
    {
        /** Left endpoint. */
        public double v0;

        /** Length. */
        public double dv;

        /**
         * Set the values for this Interval.
         *
         * @param x0 left endpoint
         * @param dx length
         */
        public void set(double x0, double dx)
        {
            v0 = x0;
            dv = dx;
        }
    }

    /**
     * Get the length by summing the lengths of the subcomponents.
     *
     * @return length
     */
    protected double getLength()
    {
        double len = 0.0;
        for (Wrap wr : kids)
        {
            len += wr.getLength();
        }
        return len;
    }

    /**
     * Get the breadth by finding the maximum breadth among the subcomponents.
     *
     * @return breadth
     */
    protected double getBreadth()
    {
        double brd = 0.0;
        for (Wrap w : kids)
        {
            brd = Math.max(brd, w.getBreadth());
        }
        return brd;
    }

    /**
     * Given x- and y-coordinates, select the one that aligns with the layout
     * orientation.
     *
     * @param x x
     * @param y y
     * @return either x or y
     */
    protected double lCoord(double x, double y)
    {
        if (dir == Orientation.HORIZONTAL)
        {
            return x;
        }
        else if (dir == Orientation.VERTICAL)
        {
            return y;
        }
        throw new IllegalStateException("orientation is not specified");
    }

    /**
     * Given x- and y-coordinates, select the one transverse to the layout
     * orientation.
     *
     * @param x x
     * @param y y
     * @return either x or y
     */
    protected double bCoord(double x, double y)
    {
        if (dir == Orientation.HORIZONTAL)
        {
            return y;
        }
        else if (dir == Orientation.VERTICAL)
        {
            return x;
        }
        throw new IllegalStateException("orientation is not specified");
    }
}