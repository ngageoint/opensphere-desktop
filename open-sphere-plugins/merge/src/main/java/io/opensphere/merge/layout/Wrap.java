package io.opensphere.merge.layout;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Wrapper for subcomponents managed by a LinearLayout. The subcomponents can
 * come in three varieties, which are:
 * <ul>
 * <li>an unadorned JavaFX Node</li>
 * <li>a collection of elements managed by a SubLayout</li>
 * <li>nothing, in which case it is just used for spacing.</li>
 * </ul>
 * In the transverse direction, a simple placement rule is used (i.e., center
 * within the breadth or cling to one or both edges). In the longitudinal
 * direction, elements are placed one after the other and may use their natural
 * (preferred) lengths, fixed lengths (specified by the layout), or be
 * stretched, depending on circumstances.
 */
public class Wrap
{
    /** The orientation of the LinearLayout that spawned this. */
    public Orientation dir;

    /** The embedded JavaFX Node, if any. */
    public Node elt;

    /** The embedded SubLayout, if any. */
    public SubLayout sub;

    /** When true, use the subcomponent's preferred width. */
    public boolean prefW;

    /** Use this width when <i>prefW</i> is false. */
    public double width;

    /** When true, use the subcomponent's preferred height. */
    public boolean prefH;

    /** Use this height when <i>prefW</i> is false. */
    public double height;

    /** When positive, claim some of the available extra length. */
    public double stretch;

    /** Simple rule for handling layout in the transverse direction. */
    public LayMode mode;

    /**
     * Get a spacer with the specified fixed length.
     *
     * @param d the orientation
     * @param len the length
     * @return the Wrap
     */
    public static Wrap space(Orientation d, double len)
    {
        Wrap wr = new Wrap();
        wr.dir = d;
        if (d == Orientation.VERTICAL)
        {
            wr.height = len;
        }
        else if (d == Orientation.HORIZONTAL)
        {
            wr.width = len;
        }
        return wr;
    }

    /**
     * Get a spacer with the specified minimum length and stretchability.
     *
     * @param d the orientation
     * @param len the minimum length
     * @param str the stretch factor
     * @return the Wrap
     */
    public static Wrap space(Orientation d, double len, double str)
    {
        Wrap wr = space(d, len);
        wr.stretch = str;
        return wr;
    }

    /**
     * Insert a Node using its own preferred dimensions.
     *
     * @param d the orientation
     * @param n the subcomponent Node
     * @param lay the transverse layout rule
     * @return the Wrap
     */
    public static Wrap node(Orientation d, Node n, LayMode lay)
    {
        Wrap wr = new Wrap();
        wr.dir = d;
        wr.mode = lay;
        wr.elt = n;
        wr.prefW = true;
        wr.prefH = true;
        return wr;
    }

    /**
     * Insert a Node using a fixed length and its own preferred breadth.
     *
     * @param d the orientation
     * @param n the subcomponent Node
     * @param lay the transverse layout rule
     * @param len the length
     * @return the Wrap
     */
    public static Wrap node(Orientation d, Node n, LayMode lay, double len)
    {
        Wrap wr = node(d, n, lay);
        wr.fixLength(len);
        return wr;
    }

    /**
     * Insert a stretchable Node using its own preferred breadth.
     *
     * @param d the orientation
     * @param n the subcomponent Node
     * @param lay the transverse layout rule
     * @param len the length
     * @param str the stretch factor
     * @return the Wrap
     */
    public static Wrap node(Orientation d, Node n, LayMode lay, double len, double str)
    {
        Wrap wr = node(d, n, lay, len);
        wr.stretch = str;
        return wr;
    }

    /**
     * Insert a Node with fixed width and height.
     *
     * @param d the orientation
     * @param n the subcomponent Node
     * @param lay the transverse layout rule
     * @param w the width
     * @param h the height
     * @return the Wrap
     */
    public static Wrap fixed(Orientation d, Node n, LayMode lay, double w, double h)
    {
        Wrap wr = node(d, n, lay);
        wr.prefW = false;
        wr.width = w;
        wr.prefH = false;
        wr.height = h;
        return wr;
    }

    /**
     * Override node size (use zero) in case it doesn't know its own size.
     *
     * @param d the orientation
     * @param n the subcomponent Node
     * @param str the stretch factor
     * @return the Wrap
     */
    public static Wrap stretch(Orientation d, Node n, double str)
    {
        Wrap wr = fixed(d, n, LayMode.STRETCH, 0.0, 0.0);
        wr.stretch = str;
        return wr;
    }

    /**
     * Override node size with specified dimensions in case it doesn't know its
     * own size.
     *
     * @param d the orientation
     * @param n the subcomponent Node
     * @param w the width
     * @param h the height
     * @param str the stretch factor
     * @return the Wrap
     */
    public static Wrap stretch(Orientation d, Node n, double w, double h, double str)
    {
        Wrap wr = stretch(d, n, str);
        wr.width = w;
        wr.height = h;
        return wr;
    }

    /**
     * Insert a transverse component list that stretches to the full breadth and
     * uses its own preferred length.
     *
     * @param d the orientation
     * @param sub the subcomponent layout
     * @return the Wrap
     */
    public static Wrap across(Orientation d, SubLayout sub)
    {
        Wrap wr = new Wrap();
        wr.dir = d;
        wr.mode = LayMode.STRETCH;
        wr.sub = sub;
        wr.prefW = true;
        wr.prefH = true;
        return wr;
    }

    /**
     * Insert a transverse component list that stretches to the full breadth and
     * has the specified fixed length.
     *
     * @param d the orientation
     * @param sub the subcomponent layout
     * @param len the length
     * @return the Wrap
     */
    public static Wrap across(Orientation d, SubLayout sub, double len)
    {
        Wrap wr = across(d, sub);
        if (d == Orientation.HORIZONTAL)
        {
            wr.prefH = false;
            wr.height = len;
        }
        else if (d == Orientation.VERTICAL)
        {
            wr.prefW = false;
            wr.width = len;
        }
        return wr;
    }

    /**
     * Substitute a new root Pane for the old one.
     *
     * @param oldP the old root Pane
     * @param newP the new root Pane
     */
    public void replaceRoot(Pane oldP, Pane newP)
    {
        if (elt != null)
        {
            if (oldP != null)
            {
                oldP.getChildren().remove(elt);
            }
            if (newP != null)
            {
                newP.getChildren().add(elt);
            }
        }
        else if (sub != null)
        {
            sub.setRoot(newP);
        }
    }

    /**
     * Lay the managed subcomponents into the rectangular region specified by
     * the owner LinearLayout.
     *
     * @param x0 left side
     * @param y0 top side
     * @param dx width
     * @param dy height
     */
    public void doLayout(double x0, double y0, double dx, double dy)
    {
        if (elt != null)
        {
            elt.setLayoutX(x0);
            elt.setLayoutY(y0);
            elt.resize(dx, dy);
        }
        else if (sub != null)
        {
            sub.doLayout(x0, y0, dx, dy);
        }
    }

    /**
     * Get the dimension that aligns with the orientation. I.e., get the width
     * in a HORIZONTAL list and the height in a VERTICAL list.
     *
     * @return the length
     */
    public double getLength()
    {
        if (dir == Orientation.HORIZONTAL)
        {
            return getWidth();
        }
        else if (dir == Orientation.VERTICAL)
        {
            return getHeight();
        }
        throw new IllegalStateException("orientation unspecified");
    }

    /**
     * Get the dimension transverse to the orientation. I.e., get the height in
     * a HORIZONTAL list and the width in a VERTICAL list.
     *
     * @return the breadth
     */
    public double getBreadth()
    {
        if (dir == Orientation.HORIZONTAL)
        {
            return getHeight();
        }
        else if (dir == Orientation.VERTICAL)
        {
            return getWidth();
        }
        throw new IllegalStateException("orientation unspecified");
    }

    /**
     * Specify a fixed length, according to orientation.
     *
     * @param len the length
     */
    private void fixLength(double len)
    {
        if (dir == Orientation.HORIZONTAL)
        {
            fixWidth(len);
        }
        else if (dir == Orientation.VERTICAL)
        {
            fixHeight(len);
        }
    }

    /**
     * Specify a fixed width.
     *
     * @param w the width
     */
    private void fixWidth(double w)
    {
        prefW = false;
        width = w;
    }

    /**
     * Specify a fixed height.
     *
     * @param h the height
     */
    private void fixHeight(double h)
    {
        prefH = false;
        height = h;
    }

    /**
     * Measure the width of the managed component(s). If a fixed width has been
     * specified, that will be returned. Otherwise the subcomponent(s) will be
     * polled for their preferred width.
     *
     * @return the width
     */
    private double getWidth()
    {
        if (!prefW)
        {
            return width;
        }
        if (sub != null)
        {
            return sub.getWidth();
        }
        if (elt != null)
        {
            return Math.max(0.0, elt.prefWidth(0.0));
        }
        return width;
    }

    /**
     * Measure the height of the managed component(s). If a fixed height has
     * been specified, that will be returned. Otherwise the subcomponent(s) will
     * be polled for their preferred height.
     *
     * @return the height
     */
    private double getHeight()
    {
        if (!prefH)
        {
            return height;
        }
        if (sub != null)
        {
            return sub.getHeight();
        }
        if (elt != null)
        {
            return Math.max(0.0, elt.prefHeight(0.0));
        }
        return height;
    }
}