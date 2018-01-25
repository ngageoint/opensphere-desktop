package io.opensphere.core.geometry.renderproperties;

/** Basic render properties that apply to all renderable geometries. */
public interface BaseRenderProperties extends ZOrderRenderProperties
{
    @Override
    BaseRenderProperties clone();

    /**
     * Get the blending configuration.
     *
     * @return the blending configuration to get.
     */
    BlendingConfigGL getBlending();

    /**
     * Get the lighting.
     *
     * @return the lighting
     */
    LightingModelConfigGL getLighting();

    /**
     * Get if this geometry is drawable.
     *
     * @return <code>true</code> if the geometry is drawable.
     */
    boolean isDrawable();

    /**
     * Get the hidden.
     *
     * @return the hidden
     */
    boolean isHidden();

    /**
     * Get if this geometry is pickable.
     *
     * @return <code>true</code> if the geometry is pickable.
     */
    boolean isPickable();

    /**
     * Set the blending configuration.
     *
     * @param blend The blending configuration to set.
     */
    void setBlending(BlendingConfigGL blend);

    /**
     * Set the hidden.
     *
     * @param hidden the hidden to set
     */
    void setHidden(boolean hidden);

    /**
     * Set the lighting.
     *
     * @param lighting the lighting to set
     */
    void setLighting(LightingModelConfigGL lighting);
}
