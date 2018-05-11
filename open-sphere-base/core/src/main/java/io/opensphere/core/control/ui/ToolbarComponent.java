package io.opensphere.core.control.ui;

import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The Class ToolbarComponent.
 */
public class ToolbarComponent implements Comparable<ToolbarComponent>
{
    /** The Component. */
    private final JComponent myComponent;

    /** The insets to be used when laying out the component. */
    private final Insets myInsets;

    /** The Location. */
    private final int myLocation;

    /** The Name. */
    private final String myName;

    /** The my toolbar . */
    private final JPanel myToolbar;

    /** The Use separator. */
    private final SeparatorLocation myUseSeparator;

    /**
     * Instantiates a new toolbar component.
     *
     * @param tbm the tbm
     * @param componentName The component name, which may be used to remove the
     *            component later.
     * @param comp The component.
     * @param order Used to order components within the same toolbar.
     * @param separatorLocation The separator location.
     * @param insets Optional insets to be used when laying out the component in
     *            the toolbar.
     */
    public ToolbarComponent(JPanel tbm, String componentName, JComponent comp, int order, SeparatorLocation separatorLocation,
            Insets insets)
    {
        myName = componentName;
        myComponent = comp;
        myLocation = order;
        myUseSeparator = separatorLocation;
        myToolbar = tbm;
        myInsets = insets;
    }

    // TODO this violates (x.compareTo(y)==0) == (x.equals(y))
    @Override
    public int compareTo(ToolbarComponent comp)
    {
        return Double.compare(myLocation, comp.getLocation());
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
        ToolbarComponent other = (ToolbarComponent)obj;
        if (!getOuterType().equals(other.getOuterType()))
        {
            return false;
        }
        return EqualsHelper.equals(myComponent, other.myComponent, Integer.valueOf(myLocation), Integer.valueOf(other.myLocation),
                myName, other.myName, myUseSeparator, other.myUseSeparator);
    }

    /**
     * Gets the component.
     *
     * @return the component
     */
    public JComponent getComponent()
    {
        return myComponent;
    }

    /**
     * Get the insets to be used when laying out this component.
     *
     * @return The insets, or null if none.
     */
    public Insets getInsets()
    {
        return myInsets;
    }

    /**
     * Gets the location.
     *
     * @return the location
     */
    public int getLocation()
    {
        return myLocation;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the separator location.
     *
     * @return the separator
     */
    public SeparatorLocation getSeparatorLocation()
    {
        return myUseSeparator;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + getOuterType().hashCode();
        result = prime * result + (myComponent == null ? 0 : myComponent.hashCode());
        result = prime * result + myLocation;
        result = prime * result + (myName == null ? 0 : myName.hashCode());
        result = prime * result + (myUseSeparator == null ? 0 : myUseSeparator.hashCode());
        return result;
    }

    /**
     * Gets the outer type.
     *
     * @return the outer type
     */
    private JPanel getOuterType()
    {
        return myToolbar;
    }
}
