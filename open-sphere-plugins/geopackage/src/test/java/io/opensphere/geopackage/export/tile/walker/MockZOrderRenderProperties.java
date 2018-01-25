package io.opensphere.geopackage.export.tile.walker;

import java.util.Collection;

import io.opensphere.core.geometry.renderproperties.RenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangeListener;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A mocked cloneable {@link ZOrderRenderProperties} used for testing.
 */
public class MockZOrderRenderProperties implements ZOrderRenderProperties
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void addListener(RenderPropertyChangeListener listen)
    {
    }

    @Override
    public Collection<? extends RenderProperties> getThisPlusDescendants()
    {
        return null;
    }

    @Override
    public void removeListener(RenderPropertyChangeListener listen)
    {
    }

    @Override
    public MockZOrderRenderProperties clone()
    {
        try
        {
            final MockZOrderRenderProperties props = (MockZOrderRenderProperties)super.clone();
            return props;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public int getRenderingOrder()
    {
        return 0;
    }

    @Override
    public int getZOrder()
    {
        return 0;
    }

    @Override
    public boolean isObscurant()
    {
        return false;
    }

    @Override
    public void setObscurant(boolean obscurant)
    {
    }

    @Override
    public void setRenderingOrder(int order)
    {
    }
}
