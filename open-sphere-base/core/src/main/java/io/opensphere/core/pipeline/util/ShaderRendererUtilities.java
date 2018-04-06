package io.opensphere.core.pipeline.util;

import com.jogamp.opengl.GL;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Interface for interacting with shaders.
 */
public interface ShaderRendererUtilities
{
    /**
     * The attribute name used with {@link #getVertAttrIndex(String)} for the
     * vertex time intervals.
     */
    String INTERVAL_FILTER_VERTEX_TIME_ATTRIBUTE_NAME = "vertexInterval";

    /**
     * Clean up any active shaders.
     *
     * @param gl The OpenGL context.
     */
    void cleanupShaders(GL gl);

    /**
     * Cleanup active shaders and delete all programs.
     *
     * @param gl The OpenGL context.
     */
    void clear(GL gl);

    /**
     * Enable the named tile shader.
     *
     * @param gl The OpenGL context.
     * @param shader Shader to enable.
     * @param shaderProps The properties which determine the specific shader
     *            code and shader variables to use.
     * @param imageTexCoords Texture coordinates for the image.
     */
    void enableShaderByName(GL gl, TileShader shader, FragmentShaderProperties shaderProps, TextureCoords imageTexCoords);

    /**
     * Get the index for the vertex attribute with the specified name.
     *
     * @param attributeName The name of the attribute.
     * @return The vertex attribute index.
     */
    int getVertAttrIndex(String attributeName);

    /**
     * Initializes the shader used for interval filtering.
     *
     * @param gl The OpenGL context.
     * @param span The interval during which vertices should be at full opacity.
     * @param groupTimeSpan The interval that represents that minimum and
     *            maximum of the vertex times.
     * @param fade The optional fade that specifies a translucent period on
     *            either side of the activeSpan.
     * @param pickEffect Indicates if the pick effect should be enabled in the
     *            shader, i.e., if the glColor rgb should be used rather than
     *            the texture rgb.
     * @param useTexture Indicates if texture sampling should be used for the
     *            fragment colors.
     */
    void initIntervalFilter(GL gl, TimeSpan span, TimeSpan groupTimeSpan, Fade fade, boolean pickEffect, boolean useTexture);

    /** Available tile shader methods. */
    enum TileShader
    {
        /** Draw debug feature with the tile. */
        DEBUG,

        /**
         * The debug feature which renders the tile in the color of the
         * projection from which it was produced.
         */
        DEBUG_PROJECTION_COLOR,

        /** Ordinary draw for a tile. */
        DRAW,

        /**
         * Draw a tile without blending with the background where the background
         * is transparent. Blending will still occur where the background has
         * color.
         */
        DRAW_NO_BLEND,

        /** Pick render for a tile. */
        PICK,

        /** Draw the pick color for the entire tile. */
        PICK_ONLY,

        ;
    }
}
