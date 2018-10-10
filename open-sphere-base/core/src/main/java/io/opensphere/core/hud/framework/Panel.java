package io.opensphere.core.hud.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.util.collections.CollectionUtilities;

/**
 * Panel for a HUD component.
 *
 * @param <S> Layout constraint type.
 * @param <T> Layout type.
 */
public abstract class Panel<S extends LayoutConstraints, T extends AbstractLayout<S>> extends Component
{
    /**
     * Border inset into this panel.
     */
    private Border myBorder;

    /**
     * Components which exist within this component.
     */
    private final Collection<Component> myChildren = new ArrayList<>();

    /** Layout for the panel. */
    private T myLayout;

    /**
     * Construct me.
     *
     * @param parent parent component.
     */
    public Panel(Component parent)
    {
        super(parent);
    }

    /**
     * Construct me.
     *
     * @param parent parent component.
     * @param location frame location.
     */
    public Panel(Component parent, ScreenBoundingBox location)
    {
        super(parent, location);
    }

    /**
     * Set up the geometry to match my layout.
     *
     * @param subComp component to add.
     * @param constraint parameters describing the position of the component in
     *            the layout.
     */
    public void add(Component subComp, S constraint)
    {
        if (subComp == null)
        {
            return;
        }
        getChildren().add(subComp);
        myLayout.add(subComp, constraint);
    }

    /**
     * Removes the supplied component from the panel and layout.
     *
     * @param component the component to remove from the panel and layout.
     */
    public void remove(Component component)
    {
        if (component == null)
        {
            return;
        }

        getChildren().remove(component);
        myLayout.remove(component);
    }

    @Override
    public void clearGeometries()
    {
        for (Component child : myChildren)
        {
            child.clearGeometries();
        }
    }

    /**
     * Get the border.
     *
     * @return the border
     */
    public Border getBorder()
    {
        return myBorder;
    }

    /**
     * Get the children.
     *
     * @return the children
     */
    public Collection<Component> getChildren()
    {
        return myChildren;
    }

    @Override
    public Set<Geometry> getGeometries()
    {
        HashSet<Geometry> geoms = new HashSet<>();
        for (Component child : myChildren)
        {
            geoms.addAll(child.getGeometries());
        }

        if (CollectionUtilities.hasContent(getBorder().getGeometries()))
        {
            geoms.addAll(getBorder().getGeometries());
        }
        return geoms;
    }

    /**
     * Get the layout.
     *
     * @return the layout
     */
    public T getLayout()
    {
        return myLayout;
    }

    @Override
    public void handleCleanupListeners()
    {
        for (Component child : myChildren)
        {
            child.handleCleanupListeners();
        }
    }

    @Override
    public void handleWindowMoved()
    {
        for (Component child : myChildren)
        {
            child.handleWindowMoved();
        }
    }

    /**
     * Initialize the border, or if I don't have one, create an empty one.
     */
    public void initBorder()
    {
        if (myBorder == null)
        {
            myBorder = new EmptyBorder();
        }
        myBorder.init();
    }

    /**
     * Set the border.
     *
     * @param border the border to set
     */
    public void setBorder(Border border)
    {
        // TODO enzio - if there are sub-components, reset their positions.
        // (unless the
        // old border is the same size)
        myBorder = border;
    }

    /**
     * Set the border only if the border is not already set. If the border is
     * set before init() is called the panel should use the one provided,
     * otherwise this method should be used to ensure that a default border is
     * used.
     *
     * @param border the border to set
     */
    public void setDefaultBorder(Border border)
    {
        if (myBorder == null)
        {
            setBorder(border);
        }
    }

    /**
     * Set the layout. Clear the children since their dimensions are no longer
     * correct.
     *
     * @param layout the layout to set
     */
    public void setLayout(T layout)
    {
        myChildren.clear();
        myLayout = layout;
    }
}
