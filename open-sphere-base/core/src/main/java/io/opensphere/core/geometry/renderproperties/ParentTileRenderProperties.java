package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/** A {@link TileRenderProperties} that has children. */
public class ParentTileRenderProperties implements TileRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The delegate render properties. */
    private final TileRenderProperties myRenderProperties;

    /** The children render properties. */
    private final Collection<TileRenderProperties> myChildren = new CopyOnWriteArrayList<>();

    /**
     * Constructor.
     *
     * @param renderProperties the render properties
     */
    public ParentTileRenderProperties(TileRenderProperties renderProperties)
    {
        myRenderProperties = renderProperties;
    }

    /**
     * Gets the children render properties.
     *
     * @return the children
     */
    public Collection<TileRenderProperties> getChildren()
    {
        return myChildren;
    }

    @Override
    public BlendingConfigGL getBlending()
    {
        return myRenderProperties.getBlending();
    }

    @Override
    public int getRenderingOrder()
    {
        return myRenderProperties.getRenderingOrder();
    }

    @Override
    public Color getColor()
    {
        return myRenderProperties.getColor();
    }

    @Override
    public TileRenderProperties clone()
    {
        return myRenderProperties.clone();
    }

    @Override
    public LightingModelConfigGL getLighting()
    {
        return myRenderProperties.getLighting();
    }

    @Override
    public float getOpacity()
    {
        return myRenderProperties.getOpacity();
    }

    @Override
    public int getColorARGB()
    {
        return myRenderProperties.getColorARGB();
    }

    @Override
    public boolean isDrawable()
    {
        return myRenderProperties.isDrawable();
    }

    @Override
    public Color getHighlightColor()
    {
        return myRenderProperties.getHighlightColor();
    }

    @Override
    public FragmentShaderProperties getShaderProperties()
    {
        return myRenderProperties.getShaderProperties();
    }

    @Override
    public boolean isHidden()
    {
        return myRenderProperties.isHidden();
    }

    @Override
    public int getZOrder()
    {
        return myRenderProperties.getZOrder();
    }

    @Override
    public int getHighlightColorARGB()
    {
        return myRenderProperties.getHighlightColorARGB();
    }

    @Override
    public void resetShaderPropertiesToDefault()
    {
        myRenderProperties.resetShaderPropertiesToDefault();
        for (TileRenderProperties child : myChildren)
        {
            child.resetShaderPropertiesToDefault();
        }
    }

    @Override
    public boolean isPickable()
    {
        return myRenderProperties.isPickable();
    }

    @Override
    public void setColor(Color color)
    {
        myRenderProperties.setColor(color);
        for (TileRenderProperties child : myChildren)
        {
            child.setColor(color);
        }
    }

    @Override
    public void addListener(RenderPropertyChangeListener listen)
    {
        myRenderProperties.addListener(listen);
    }

    @Override
    public void opacitizeColor(float opacity)
    {
        myRenderProperties.opacitizeColor(opacity);
        for (TileRenderProperties child : myChildren)
        {
            child.opacitizeColor(opacity);
        }
    }

    @Override
    public boolean isObscurant()
    {
        return myRenderProperties.isObscurant();
    }

    @Override
    public void setBlending(BlendingConfigGL blend)
    {
        myRenderProperties.setBlending(blend);
        for (TileRenderProperties child : myChildren)
        {
            child.setBlending(blend);
        }
    }

    @Override
    public void setHidden(boolean hidden)
    {
        myRenderProperties.setHidden(hidden);
        for (TileRenderProperties child : myChildren)
        {
            child.setHidden(hidden);
        }
    }

    @Override
    public void setColorARGB(int color)
    {
        myRenderProperties.setColorARGB(color);
        for (TileRenderProperties child : myChildren)
        {
            child.setColorARGB(color);
        }
    }

    @Override
    public Collection<? extends RenderProperties> getThisPlusDescendants()
    {
        return myRenderProperties.getThisPlusDescendants();
    }

    @Override
    public void setLighting(LightingModelConfigGL lighting)
    {
        myRenderProperties.setLighting(lighting);
        for (TileRenderProperties child : myChildren)
        {
            child.setLighting(lighting);
        }
    }

    @Override
    public void setObscurant(boolean obscurant)
    {
        myRenderProperties.setObscurant(obscurant);
        for (TileRenderProperties child : myChildren)
        {
            child.setObscurant(obscurant);
        }
    }

    @Override
    public void setHighlightColor(Color color)
    {
        myRenderProperties.setHighlightColor(color);
        for (TileRenderProperties child : myChildren)
        {
            child.setHighlightColor(color);
        }
    }

    @Override
    public void removeListener(RenderPropertyChangeListener listen)
    {
        myRenderProperties.removeListener(listen);
    }

    @Override
    public void setOpacity(float opacity)
    {
        myRenderProperties.setOpacity(opacity);
        for (TileRenderProperties child : myChildren)
        {
            child.setOpacity(opacity);
        }
    }

    @Override
    public void setRenderingOrder(int order)
    {
        myRenderProperties.setRenderingOrder(order);
        for (TileRenderProperties child : myChildren)
        {
            child.setRenderingOrder(order);
        }
    }

    @Override
    public void setHighlightColorARGB(int color)
    {
        myRenderProperties.setHighlightColorARGB(color);
        for (TileRenderProperties child : myChildren)
        {
            child.setHighlightColorARGB(color);
        }
    }
}
