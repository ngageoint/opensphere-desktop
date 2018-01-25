package io.opensphere.core.viewbookmark.impl;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewer.impl.Viewer2D.ViewerPosition2D;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * The Class DefaultViewBookmark. A viewer bookmark consists of the view name
 * and the current viewer position.
 */
public final class DefaultViewBookmark implements ViewBookmark, Comparable<ViewBookmark>
{
    /** The Name. */
    private final String myName;

    /** The Viewer position. */
    private ViewerPosition3D myViewerPosition3D;

    /** The my viewer position2 d. */
    private ViewerPosition2D myViewerPosition2D;

    /**
     * Instantiates a new default 2d view book mark.
     *
     * @param name the book mark name.
     * @param pos the {@link ViewerPosition2D}
     *
     */
    public DefaultViewBookmark(String name, ViewerPosition2D pos)
    {
        Utilities.checkNull(name, "name");
        Utilities.checkNull(pos, "pos");
        myName = name;
        myViewerPosition2D = new ViewerPosition2D(pos.getLocation(), pos.getScale());
    }

    /**
     * Instantiates a new default view book mark.
     *
     * @param name the book mark name.
     * @param pos the {@link ViewerPosition3D}
     *
     */
    public DefaultViewBookmark(String name, ViewerPosition3D pos)
    {
        Utilities.checkNull(name, "name");
        Utilities.checkNull(pos, "pos");
        myName = name;
        myViewerPosition3D = new ViewerPosition3D(pos.getLocation(), pos.getDir(), pos.getUp());
    }

    /**
     * Instantiates a new default view book mark.
     *
     * @param other the other
     */
    public DefaultViewBookmark(ViewBookmark other)
    {
        Utilities.checkNull(other, "other");
        myName = other.getViewName();
        if (other.is3D())
        {
            myViewerPosition3D = new ViewerPosition3D(other.getViewerPos3D().getLocation(), other.getViewerPos3D().getDir(),
                    other.getViewerPos3D().getUp());
        }
        else
        {
            myViewerPosition2D = new ViewerPosition2D(other.getViewerPos2D().getLocation(), other.getViewerPos2D().getScale());
        }
    }

    @Override
    public int compareTo(ViewBookmark o)
    {
        NameComparator nc = new NameComparator();
        return nc.compare(this, o);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultViewBookmark other = (DefaultViewBookmark)obj;
        return EqualsHelper.equals(myName, other.myName, myViewerPosition3D, other.myViewerPosition3D);
    }

    @Override
    public ViewerPosition2D getViewerPos2D()
    {
        return myViewerPosition2D;
    }

    @Override
    public ViewerPosition3D getViewerPos3D()
    {
        return myViewerPosition3D;
    }

    @Override
    public String getViewName()
    {
        return myName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myName == null ? 0 : myName.hashCode());
        result = prime * result + (myViewerPosition3D == null ? 0 : myViewerPosition3D.hashCode());
        return result;
    }

    @Override
    public boolean is2D()
    {
        return myViewerPosition2D != null;
    }

    @Override
    public boolean is3D()
    {
        return myViewerPosition3D != null;
    }
}
