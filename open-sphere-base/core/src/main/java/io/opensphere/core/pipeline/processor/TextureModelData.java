package io.opensphere.core.pipeline.processor;

import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.util.TextureGroup;
import io.opensphere.core.util.Utilities;

/**
 * The model data for a texture geometry. This comprises the texture handles as
 * well as the coordinates required to render the texture.
 */
public class TextureModelData implements AbstractRenderer.ModelData
{
    /** The non-texture data. */
    private final AbstractRenderer.ModelData myModelData;

    /** The textures for the geometry. */
    private final TextureGroup myTextureGroup;

    /**
     * Constructor.
     *
     * @param modelData The non-texture model data.
     * @param texture The textures for the geometry.
     */
    public TextureModelData(AbstractRenderer.ModelData modelData, TextureGroup texture)
    {
        myModelData = modelData;
        myTextureGroup = texture;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof TextureModelData)
        {
            TextureModelData model = (TextureModelData)obj;
            if (Utilities.sameInstance(myModelData, model.getModelData()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the non-texture model data.
     *
     * @return The model data.
     */
    public AbstractRenderer.ModelData getModelData()
    {
        return myModelData;
    }

    /**
     * Get the texture group.
     *
     * @return The texture group.
     */
    public TextureGroup getTextureGroup()
    {
        return myTextureGroup;
    }

    @Override
    public int hashCode()
    {
        if (myModelData != null)
        {
            return myModelData.hashCode();
        }
        return super.hashCode();
    }
}
