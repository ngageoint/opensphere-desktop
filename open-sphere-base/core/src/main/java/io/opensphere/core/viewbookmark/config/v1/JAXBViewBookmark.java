package io.opensphere.core.viewbookmark.config.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewer.impl.Viewer2D.ViewerPosition2D;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * The Class JAXBViewBookmark.
 */
@XmlRootElement(name = "ViewBookmark")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBViewBookmark implements ViewBookmark
{
    /** The View name. */
    @XmlAttribute(name = "name")
    private String myViewName;

    /** The my is2 d. */
    @XmlAttribute(name = "is2D", required = false)
    private boolean myIs2D;

    /** The Location. */
    @XmlElement(name = "loc")
    private JAXBVector3d myLoc;

    /** The Direction. */
    @XmlElement(name = "dir", required = false)
    private JAXBVector3d myDir;

    /** The Up. */
    @XmlElement(name = "up", required = false)
    private JAXBVector3d myUp;

    /** The my scale2d. */
    @XmlElement(name = "scale2d", required = false)
    private double myScale2d;

    /**
     * Instantiates a new jAXB view bookmark.
     */
    public JAXBViewBookmark()
    {
    }

    /**
     * Instantiates a new jAXB view book mark.
     *
     * @param view the view
     */
    public JAXBViewBookmark(ViewBookmark view)
    {
        myViewName = view.getViewName();
        if (view.is3D())
        {
            myIs2D = false;
            myLoc = new JAXBVector3d(view.getViewerPos3D().getLocation());
            myDir = new JAXBVector3d(view.getViewerPos3D().getDir());
            myUp = new JAXBVector3d(view.getViewerPos3D().getUp());
        }
        else
        {
            myIs2D = true;
            myLoc = new JAXBVector3d(view.getViewerPos2D().getLocation());
            myScale2d = view.getViewerPos2D().getScale();
        }
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    public final JAXBVector3d getDir()
    {
        return myDir;
    }

    /**
     * Gets the loc.
     *
     * @return the loc
     */
    public final JAXBVector3d getLoc()
    {
        return myLoc;
    }

    /**
     * Gets the up.
     *
     * @return the up
     */
    public final JAXBVector3d getUp()
    {
        return myUp;
    }

    @Override
    public ViewerPosition2D getViewerPos2D()
    {
        return new ViewerPosition2D(myLoc.getVector3d(), myScale2d);
    }

    @Override
    public ViewerPosition3D getViewerPos3D()
    {
        return new ViewerPosition3D(myLoc.getVector3d(), myDir.getVector3d(), myUp.getVector3d());
    }

    @Override
    public String getViewName()
    {
        return myViewName;
    }

    @Override
    public boolean is2D()
    {
        return myIs2D;
    }

    @Override
    public boolean is3D()
    {
        return !myIs2D;
    }

    /**
     * Sets the dir.
     *
     * @param dir the new dir
     */
    public final void setDir(JAXBVector3d dir)
    {
        myDir = dir;
    }

    /**
     * Sets the loc.
     *
     * @param loc the new loc
     */
    public final void setLoc(JAXBVector3d loc)
    {
        myLoc = loc;
    }

    /**
     * Sets the up.
     *
     * @param up the new up
     */
    public final void setUp(JAXBVector3d up)
    {
        myUp = up;
    }

    /**
     * Sets the view name.
     *
     * @param viewName the new view name
     */
    public void setViewName(String viewName)
    {
        myViewName = viewName;
    }
}
