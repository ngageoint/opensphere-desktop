package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;
import java.util.Collection;

import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * A {@link TileRenderProperties} that clones all values from an existing
 * {@link DefaultTileRenderProperties}. When any values are changed with this
 * render properties the listeners of the cloned
 * {@link DefaultTileRenderProperties} are notified of the change.
 */
public class ProxyTileRenderProperties implements TileRenderProperties
{
    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The cloned render properties.
     */
    private final DefaultTileRenderProperties myCloned;

    /**
     * The original render properties.
     */
    private final DefaultTileRenderProperties myOriginal;

    /**
     * Constructs a new proxying tile render properties.
     *
     * @param renderProperties The render properties to clone.
     */
    public ProxyTileRenderProperties(DefaultTileRenderProperties renderProperties)
    {
        myOriginal = renderProperties;
        myCloned = myOriginal.clone();
    }

    @Override
    public void addListener(RenderPropertyChangeListener listen)
    {
        myOriginal.addListener(listen);
    }

    @Override
    public ProxyTileRenderProperties clone()
    {
        try
        {
            return (ProxyTileRenderProperties)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public BlendingConfigGL getBlending()
    {
        return myCloned.getBlending();
    }

    @Override
    public Color getColor()
    {
        return myCloned.getColor();
    }

    @Override
    public int getColorARGB()
    {
        return myCloned.getColorARGB();
    }

    @Override
    public Color getHighlightColor()
    {
        return myCloned.getHighlightColor();
    }

    @Override
    public int getHighlightColorARGB()
    {
        return myCloned.getHighlightColorARGB();
    }

    @Override
    public LightingModelConfigGL getLighting()
    {
        return myCloned.getLighting();
    }

    @Override
    public float getOpacity()
    {
        return myCloned.getOpacity();
    }

    @Override
    public int getRenderingOrder()
    {
        return myCloned.getRenderingOrder();
    }

    @Override
    public FragmentShaderProperties getShaderProperties()
    {
        return myCloned.getShaderProperties();
    }

    @Override
    public Collection<? extends RenderProperties> getThisPlusDescendants()
    {
        return myCloned.getThisPlusDescendants();
    }

    @Override
    public int getZOrder()
    {
        return myCloned.getZOrder();
    }

    @Override
    public boolean isDrawable()
    {
        return myCloned.isDrawable();
    }

    @Override
    public boolean isHidden()
    {
        return myOriginal.isHidden();
    }

    @Override
    public boolean isObscurant()
    {
        return myCloned.isObscurant();
    }

    @Override
    public boolean isPickable()
    {
        return myCloned.isPickable();
    }

    @Override
    public void opacitizeColor(float opacity)
    {
        myCloned.opacitizeColor(opacity);
        myOriginal.notifyChanged();
    }

    @Override
    public void removeListener(RenderPropertyChangeListener listen)
    {
        myOriginal.removeListener(listen);
    }

    @Override
    public void resetShaderPropertiesToDefault()
    {
        myCloned.resetShaderPropertiesToDefault();
        myOriginal.notifyChanged();
    }

    @Override
    public void setBlending(BlendingConfigGL blend)
    {
        myCloned.setBlending(blend);
        myOriginal.notifyChanged();
    }

    @Override
    public void setColor(Color color)
    {
        myCloned.setColor(color);
        myOriginal.notifyChanged();
    }

    @Override
    public void setColorARGB(int color)
    {
        myCloned.setColorARGB(color);
        myOriginal.notifyChanged();
    }

    @Override
    public void setHidden(boolean hidden)
    {
        myOriginal.setHidden(hidden);
    }

    @Override
    public void setHighlightColor(Color color)
    {
        myCloned.setHighlightColor(color);
        myOriginal.notifyChanged();
    }

    @Override
    public void setHighlightColorARGB(int color)
    {
        myCloned.setHighlightColorARGB(color);
        myOriginal.notifyChanged();
    }

    @Override
    public void setLighting(LightingModelConfigGL lighting)
    {
        myCloned.setLighting(lighting);
        myOriginal.notifyChanged();
    }

    @Override
    public void setObscurant(boolean obscurant)
    {
        myCloned.setObscurant(obscurant);
        myOriginal.notifyChanged();
    }

    @Override
    public void setOpacity(float opacity)
    {
        myCloned.setOpacity(opacity);
        myOriginal.notifyChanged();
    }

    @Override
    public void setRenderingOrder(int order)
    {
        myCloned.setRenderingOrder(order);
        myOriginal.notifyChanged();
    }
}
