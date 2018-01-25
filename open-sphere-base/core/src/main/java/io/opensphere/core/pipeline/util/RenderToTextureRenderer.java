package io.opensphere.core.pipeline.util;

/**
 * Interface for those who wish to render to a texture.
 */
@FunctionalInterface
public interface RenderToTextureRenderer
{
    /**
     * This call back occurs after the frame buffer has been switched for
     * rendering to the texture.
     *
     * @param rc The render context.
     */
    void onRenderToTexture(RenderContext rc);
}
