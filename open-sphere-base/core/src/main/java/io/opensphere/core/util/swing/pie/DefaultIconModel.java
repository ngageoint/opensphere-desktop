package io.opensphere.core.util.swing.pie;

import java.awt.Image;
import java.util.Collection;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * Default IconModel.
 */
public class DefaultIconModel implements IconModel
{
    /** The icon info collection. */
    private final Collection<IconInfo> myIconInfos = New.list();

    /** The mouse-over icon. */
    private IconInfo myMouseOverIcon;

    /** The Arcs. */
    private final Collection<Arc> myArcs = New.list();

    /** The change support. */
    private final transient ChangeSupport<ChangeListener> myChangeSupport = new StrongChangeSupport<>();

    @Override
    public void addArc(Arc arc)
    {
        myArcs.add(arc);
    }

    @Override
    public void addChangeListener(ChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void addIcon(Image icon, float angle, Object userObject)
    {
        myIconInfos.add(new IconInfo(icon, angle, userObject));
    }

    @Override
    public void clearArcs()
    {
        myArcs.clear();
    }

    @Override
    public void clearIcons()
    {
        myIconInfos.clear();
    }

    @Override
    public Collection<Arc> getArcs()
    {
        return myArcs;
    }

    @Override
    public Collection<IconInfo> getIcons()
    {
        return New.list(myIconInfos);
    }

    @Override
    public IconInfo getMouseOverIcon()
    {
        return myMouseOverIcon;
    }

    @Override
    public void removeChangeListener(ChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void setMouseOverIcon(IconInfo iconInfo, Object source)
    {
        if (!EqualsHelper.equals(myMouseOverIcon, iconInfo))
        {
            myMouseOverIcon = iconInfo;
            fireStateChanged(ChangeType.MOUSE_OVER, source);
        }
    }

    /**
     * Fires a state change.
     *
     * @param changeType the change type
     * @param source the source of the change
     */
    protected void fireStateChanged(final ChangeType changeType, final Object source)
    {
        myChangeSupport.notifyListeners(listener -> listener.stateChanged(changeType, source));
    }
}
