package io.opensphere.core.geometry.renderproperties;

/** Standard implementation of {@link ZOrderRenderProperties}. */
public class DefaultZOrderRenderProperties extends AbstractRenderProperties implements ZOrderRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** When true, the geometry can obscure other geometries. */
    private boolean myObscurant;

    /**
     * For geometries which share the same processor, this property can be used
     * to change the rendering order.
     */
    private int myRenderingOrder;

    /** The order that this geometry is rendered. */
    private int myZOrder;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     */
    public DefaultZOrderRenderProperties(int zOrder, boolean obscurant)
    {
        myZOrder = zOrder;
        myObscurant = obscurant;
    }

    @Override
    public DefaultZOrderRenderProperties clone()
    {
        return (DefaultZOrderRenderProperties)super.clone();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultZOrderRenderProperties other = (DefaultZOrderRenderProperties)obj;
        return myObscurant == other.myObscurant && myRenderingOrder == other.myRenderingOrder && myZOrder == other.myZOrder;
    }

    @Override
    public int getRenderingOrder()
    {
        return myRenderingOrder;
    }

    @Override
    public int getZOrder()
    {
        return myZOrder;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myObscurant ? 1231 : 1237);
        result = prime * result + myRenderingOrder;
        result = prime * result + myZOrder;
        return result;
    }

    @Override
    public boolean isObscurant()
    {
        return myObscurant;
    }

    @Override
    public void setObscurant(boolean obscurant)
    {
        myObscurant = obscurant;
        notifyChanged();
    }

    @Override
    public void setRenderingOrder(int order)
    {
        myRenderingOrder = order;
        notifyChanged();
    }

    /**
     * Sets the z order for the render property.
     *
     * Note: This is not a monitored render property setting this value on a in
     * use render property will not result in any dynamic change.
     *
     * @param zOrder the new z order
     */
    public void setZOrder(int zOrder)
    {
        myZOrder = zOrder;
    }
}
