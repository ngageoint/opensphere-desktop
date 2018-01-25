package io.opensphere.core.pipeline.processor;

import java.util.Collection;

import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.Position;
import io.opensphere.core.pipeline.util.RenderContext;

/**
 * A geometry processor is responsible for doing the coordinate transformations
 * necessary to render a geometry on the screen. The processor typically
 * delegates to a renderer to do the actual GL commands, which allows for
 * different renderers for different render methods.
 *
 * @param <E> The type of geometry handled by this processor.
 */
public interface RenderableGeometryProcessor<E extends Geometry> extends GeometryProcessor<E>
{
    /**
     * Generate geometries for dry-run purposes.
     */
    void generateDryRunGeometries();

    /**
     * Get the position type of the geometries in this processor.
     *
     * @return The position type.
     */
    Class<? extends Position> getPositionType();

    /**
     * Get if this processor is viable for a given GL context. This tests that
     * all the GL extensions that the processor requires are available in the
     * given context.
     *
     * @param rc The render context.
     * @param warnings Optional collection of warnings to be populated if the
     *            processor is not viable.
     * @return {@code true} if the renderer is viable.
     */
    boolean isViable(RenderContext rc, Collection<String> warnings);

    /**
     * Determine if this processor needs to render.
     *
     * @param mode The render mode.
     * @return {@code true} if rendering is needed.
     */
    boolean needsRender(RenderMode mode);

    /**
     * When this processor is ready to render all of its geometries, it will run
     * the given task.
     *
     * @param task The task to run when all geometries are ready.
     */
    void notifyWhenReady(Runnable task);

    /**
     * Render my geometries to OpenGL.
     *
     * @param renderContext The rendering context.
     */
    void render(RenderContext renderContext);
}
