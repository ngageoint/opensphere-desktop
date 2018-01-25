package io.opensphere.core.geometry.renderproperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;

/** Render properties specific to tile geometries. */
public class DefaultTileRenderProperties extends DefaultColorRenderProperties implements TileRenderProperties
{
    /**
     * This is the default fragment shader snippet for determining the color of
     * a tile fragment.
     */
    private static final String DEFAULT_DRAW_FRAGMENT;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultTileRenderProperties.class);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Properties for shader specific rendering. */
    private final FragmentShaderProperties myFragmentShaderProperties;

    static
    {
        String resource = "/GLSL/DefaultDrawFrag.glsl";
        String fragment = null;
        try (InputStream strm = DefaultTileRenderProperties.class.getResourceAsStream(resource))
        {
            fragment = new StreamReader(strm, 383, -1).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
        }
        catch (IOException e)
        {
            LOGGER.error("Could not read shader code from [" + resource + "]: " + e, e);
        }
        finally
        {
            DEFAULT_DRAW_FRAGMENT = fragment;
        }
    }

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     */
    public DefaultTileRenderProperties(int zOrder, boolean drawable, boolean pickable)
    {
        super(zOrder, drawable, pickable, true, TileRenderProperties.DEFAULT_COLOR, TileRenderProperties.DEFAULT_HIGHLIGHT_COLOR);
        setBlending(BlendingConfigGL.getDefaultBlending());

        myFragmentShaderProperties = new DefaultFragmentShaderProperties();
        ShaderPropertiesSet set = new ShaderPropertiesSet();
        set.setShaderCode(DEFAULT_DRAW_FRAGMENT);
        myFragmentShaderProperties.setupShader(set);
    }

    @Override
    public DefaultTileRenderProperties clone()
    {
        return (DefaultTileRenderProperties)super.clone();
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
        return myFragmentShaderProperties.equals(((DefaultTileRenderProperties)obj).myFragmentShaderProperties);
    }

    @Override
    public float getOpacity()
    {
        return getColor().getAlpha();
    }

    @Override
    public FragmentShaderProperties getShaderProperties()
    {
        return myFragmentShaderProperties;
    }

    @Override
    public Collection<? extends RenderProperties> getThisPlusDescendants()
    {
        return Arrays.asList(this, myFragmentShaderProperties);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myFragmentShaderProperties.hashCode();
        return result;
    }

    @Override
    public void resetShaderPropertiesToDefault()
    {
        ShaderPropertiesSet set = new ShaderPropertiesSet();
        set.setShaderCode(DEFAULT_DRAW_FRAGMENT);
        myFragmentShaderProperties.setupShader(set);
    }

    @Override
    public void setOpacity(float opacity)
    {
        setColorARGB(getColorARGB() & 0xffffff | (int)(opacity * 0xff) << 24);
    }
}
