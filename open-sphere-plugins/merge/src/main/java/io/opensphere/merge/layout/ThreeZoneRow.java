package io.opensphere.merge.layout;

import java.util.LinkedList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Lay out subcomponents in a row that is divided into three zones that are on
 * the left, in the center, and on the right, some of which may be empty. This
 * is generally used to place buttons, for example, at the bottom of a dialog.
 */
public class ThreeZoneRow implements SubLayout
{
    /** Root Pane is the parent of all managed subcomponents. */
    private Pane root;

    /** The left zone. */
    private final List<Node> lZone = new LinkedList<>();

    /** The center zone. */
    private final List<Node> cZone = new LinkedList<>();

    /** The right zone. */
    private final List<Node> rZone = new LinkedList<>();

    /** Horizontal margin (default is 1.0). */
    private double hMargin = 1.0;

    /** Vertical margin (default is 1.0). */
    private double vMargin = 1.0;

    /** Spacing between adjacent subcomponents. */
    private double spacer = 3.0;

    /** Minimum spacing between adjacent zones. */
    private double zoneGap = 10.0;

    @Override
    public void setRoot(Pane r)
    {
        disengage();
        root = r;
        engage();
    }

    /**
     * Override the default margins.
     *
     * @param h horizontal margin
     * @param v vertical margin
     */
    public void setMargins(double h, double v)
    {
        hMargin = h;
        vMargin = v;
    }

    /**
     * Override the default space between adjacent subcomponents.
     *
     * @param s spacer
     */
    public void setSpacer(double s)
    {
        spacer = s;
    }

    /**
     * Override the default interzone spacing.
     *
     * @param z zone gap
     */
    public void setZoneGap(double z)
    {
        zoneGap = z;
    }

    /**
     * Add a Node to the left zone.
     *
     * @param n a Node
     */
    public void addLeft(Node n)
    {
        lZone.add(n);
        if (root != null)
        {
            root.getChildren().add(n);
        }
    }

    /**
     * Add a Node to the center zone.
     *
     * @param n a Node
     */
    public void addCenter(Node n)
    {
        cZone.add(n);
        if (root != null)
        {
            root.getChildren().add(n);
        }
    }

    /**
     * Add a Node to the right zone.
     *
     * @param n a Node
     */
    public void addRight(Node n)
    {
        rZone.add(n);
        if (root != null)
        {
            root.getChildren().add(n);
        }
    }

    /** Drop all managed subcomponents. */
    public void clear()
    {
        disengage();
        lZone.clear();
        cZone.clear();
        rZone.clear();
    }

    /** Remove managed subcomponents from the current root, if applicable. */
    private void disengage()
    {
        if (root == null)
        {
            return;
        }
        root.getChildren().removeAll(lZone);
        root.getChildren().removeAll(cZone);
        root.getChildren().removeAll(rZone);
    }

    /** Add managed subcomponents to the current root, if applicable. */
    private void engage()
    {
        if (root == null)
        {
            return;
        }
        root.getChildren().addAll(lZone);
        root.getChildren().addAll(cZone);
        root.getChildren().addAll(rZone);
    }

    /**
     * Find the preferred dimensions of one of the three zones.
     *
     * @param zone the zone
     * @return the Size2d
     */
    private Size2d measure(List<Node> zone)
    {
        Size2d r = new Size2d();
        if (zone.isEmpty())
        {
            return r;
        }
        for (Node n : zone)
        {
            r.x += n.prefWidth(0.0);
            r.y = Math.max(r.y, n.prefHeight(0.0));
        }
        if (zone.size() > 1)
        {
            r.x += spacer * (zone.size() - 1);
        }
        return r;
    }

    @Override
    public double getWidth()
    {
        Size2d lSize = measure(lZone);
        Size2d cSize = measure(cZone);
        Size2d rSize = measure(rZone);
        int nZones = 0;
        if (lSize.x > 0.0)
        {
            nZones++;
        }
        if (cSize.x > 0.0)
        {
            nZones++;
        }
        if (rSize.x > 0.0)
        {
            nZones++;
        }
        return 2.0 * hMargin + lSize.x + cSize.x + rSize.x + Math.max(0, zoneGap * (nZones - 1));
    }

    @Override
    public double getHeight()
    {
        Size2d lSize = measure(lZone);
        Size2d cSize = measure(cZone);
        Size2d rSize = measure(rZone);
        return Math.max(lSize.y, Math.max(cSize.y, rSize.y)) + 2.0 * vMargin;
    }

    @Override
    public void doLayout(double x0, double y0, double dx, double dy)
    {
        Size2d lSize = measure(lZone);
        Size2d cSize = measure(cZone);
        Size2d rSize = measure(rZone);

        // left zone always starts at the left margin, if populated
        layZone(lZone, x0 + hMargin, y0 + vMargin, lSize.x, dy - 2.0 * vMargin);

        // find the boundaries for the center zone
        double lcz = x0 + hMargin + lSize.x;
        if (lSize.x > 0.0)
        {
            lcz += zoneGap;
        }
        lcz += hMargin;
        double rPrefX0 = x0 + dx - hMargin - rSize.x;
        double rcz = rPrefX0;
        if (rSize.x > 0.0)
        {
            rcz -= zoneGap;
        }
        // sort through that to find out where it should be and place it
        double cx0 = Math.max(lcz, Math.min(rcz - cSize.x, x0 + (dx - cSize.x) / 2.0));
        layZone(cZone, cx0, y0, cSize.x, dy);

        // find the left boundary for the right zone
        double lrz = lcz + cSize.x;
        if (cSize.x > 0.0)
        {
            lrz += zoneGap;
        }
        // lay it in
        double rx0 = Math.max(lrz, rPrefX0);
        layZone(rZone, rx0, y0, rSize.x, dy);
    }

    /**
     * Layout the components in a zone using the specified rectangular region.
     *
     * @param zone the zone
     * @param x0 left side
     * @param y0 top side
     * @param dx width
     * @param dy height
     */
    private void layZone(List<Node> zone, double x0, double y0, double dx, double dy)
    {
        double x = x0;
        for (Node n : zone)
        {
            double w = n.prefWidth(0.0);
            layNode(n, x, y0, w, dy);
            x += w + spacer;
        }
    }

    /**
     * Layout the a Node using the specified rectangular region.
     *
     * @param n the Node
     * @param x0 left side
     * @param y0 top side
     * @param dx width
     * @param dy height
     */
    private void layNode(Node n, double x0, double y0, double dx, double dy)
    {
        n.setLayoutX(x0);
        n.setLayoutY(y0);
        n.resize(dx, dy);
    }

    /** Two-dimensional size struct. */
    private static class Size2d
    {
        /** Width. */
        public double x;

        /** Height. */
        public double y;
    }
}