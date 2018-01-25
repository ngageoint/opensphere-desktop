package io.opensphere.myplaces.specific;

import io.opensphere.core.api.Transformer;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * Interface to a my places renderer.
 *
 */
public interface Renderer
{
    /**
     * Indicates if this renderer can render.
     *
     * @return True if it can render, false otherwise.
     */
    boolean canRender();

    /**
     * Gets the render type for this renderer.
     *
     * @return The type of renderer.
     */
    MapVisualizationType getRenderType();

    /**
     * Gets the transformer this renderer uses.
     *
     * @return the transformer this renderer uses.
     */
    Transformer getTransformer();

    /**
     * Renders the elements in the group.
     *
     * @param group the group to render.
     */
    void render(RenderGroup group);

    /**
     * Sets the open listener to be notified when the transformer gets opened.
     *
     * @param openListener The open listener to set.
     */
    void setOpenListener(OpenListener openListener);
}
