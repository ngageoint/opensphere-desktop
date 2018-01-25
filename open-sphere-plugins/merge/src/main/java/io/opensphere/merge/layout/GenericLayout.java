package io.opensphere.merge.layout;

import javafx.geometry.Insets;

/**
 * Extends BaseLayout to use the nestable SubLayout framework for laying out
 * subcomponents in rows and columns. This code also respects the Insets that
 * may be attached to the root Pane.
 */
public class GenericLayout extends BaseLayout
{
    /** The layout delegate. */
    private final SubLayout lay;

    /**
     * Create.
     *
     * @param sub the layout delegate
     */
    public GenericLayout(SubLayout sub)
    {
        lay = sub;
        lay.setRoot(root);
    }

    @Override
    protected double layoutHeight()
    {
        Insets ins = root.getInsets();
        double h = ins.getTop() + ins.getBottom();
        if (lay != null)
        {
            h += lay.getHeight();
        }
        return h;
    }

    @Override
    protected double layoutWidth()
    {
        Insets ins = root.getInsets();
        double w = ins.getLeft() + ins.getRight();
        if (lay != null)
        {
            w += lay.getWidth();
        }
        return w;
    }

    @Override
    protected void doLayout()
    {
        Insets ins = root.getInsets();
        lay.doLayout(ins.getLeft(), ins.getTop(), width - ins.getLeft() - ins.getRight(),
                height - ins.getTop() - ins.getBottom());
    }
}