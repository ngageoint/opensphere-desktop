package io.opensphere.core.viewbookmark;

import java.io.Serializable;
import java.util.Comparator;

import io.opensphere.core.viewer.impl.Viewer2D.ViewerPosition2D;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * The Interface ViewBookmark.
 */
public interface ViewBookmark
{
    /**
     * Gets the viewer pos2 d.
     *
     * @return the viewer pos2 d
     */
    ViewerPosition2D getViewerPos2D();

    /**
     * Gets the viewer pos3 d.
     *
     * @return the viewer pos3 d
     */
    ViewerPosition3D getViewerPos3D();

    /**
     * Gets the view name.
     *
     * @return the view name
     */
    String getViewName();

    /**
     * Checks if is 2 d.
     *
     * @return true, if is 2 d
     */
    boolean is2D();

    /**
     * Checks if is 3 d.
     *
     * @return true, if is 3 d
     */
    boolean is3D();

    /**
     * The Class NameComparator.
     */
    @SuppressWarnings("serial")
    final class NameComparator implements Comparator<ViewBookmark>, Serializable
    {
        @Override
        public int compare(ViewBookmark o1, ViewBookmark o2)
        {
            String name1 = o1 == null ? null : o1.getViewName();
            String name2 = o2 == null ? null : o2.getViewName();
            return name1 == null ? name2 == null ? 0 : -1 : name2 == null ? 1 : name1.compareTo(name2);
        }
    }

    /**
     * The Class NameComparator.
     */
    @SuppressWarnings("serial")
    final class TypeAndNameComparator implements Comparator<ViewBookmark>, Serializable
    {
        @Override
        public int compare(ViewBookmark o1, ViewBookmark o2)
        {
            boolean o1Is3D = o1 == null || o1.is3D();
            boolean o2Is3D = o2 == null || o2.is3D();
            if (o1Is3D && !o2Is3D)
            {
                return -1;
            }
            else if (!o1Is3D && o2Is3D)
            {
                return 1;
            }
            else
            {
                String name1 = o1 == null ? null : o1.getViewName();
                String name2 = o2 == null ? null : o2.getViewName();
                return name1 == null ? name2 == null ? 0 : -1 : name2 == null ? 1 : name1.compareTo(name2);
            }
        }
    }
}
