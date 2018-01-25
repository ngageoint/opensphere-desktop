package io.opensphere.core.geometry.renderproperties;

/** Z-Order for the associated geometry. */
public interface ZOrderRenderProperties extends RenderProperties
{
    /** Constant for the top Z level (the one that will be drawn last). */
    int TOP_Z = Integer.MAX_VALUE;

    @Override
    ZOrderRenderProperties clone();

    /**
     * Get the rendering order for this geometry. For geometries which share the
     * same z order, this property provides a hint to the renderer to help
     * ordering. In cases where the geometries do not have the same renderer,
     * this hint will not be honored. This is a mutable property.
     *
     * @return the rendering order
     */
    int getRenderingOrder();

    /**
     * Get the rendering order for this geometry. Geometries are drawn in
     * ascending Z order, within position type. This is an immutable property.
     *
     * @return the rendering order
     */
    int getZOrder();

    /**
     * Tell whether geometries with this render property should obscure other
     * geometries based on depth from the viewer. When this is false, render
     * precedence will be based solely on z-order.
     *
     * @return true when the geometry can obscure other geometries.
     */
    boolean isObscurant();

    /**
     * Set whether the geometries can obscure other geometries base on depth
     * from the viewer.
     *
     * @param obscurant the new obscurance setting.
     */
    void setObscurant(boolean obscurant);

    /**
     * Set the rendering order. For geometries which share the same z order,
     * this property provides a hint to the renderer to help ordering. In cases
     * where the geometries do not have the same renderer, this hint will not be
     * honored.
     *
     * @param order the rendering order.
     */
    void setRenderingOrder(int order);
}
