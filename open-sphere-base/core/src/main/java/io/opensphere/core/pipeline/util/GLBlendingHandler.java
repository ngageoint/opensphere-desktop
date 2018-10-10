package io.opensphere.core.pipeline.util;

import java.util.EnumMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;

import io.opensphere.core.geometry.renderproperties.BlendingConfigGL;
import io.opensphere.core.geometry.renderproperties.BlendingConfigGL.BlendFactor;
import io.opensphere.core.geometry.renderproperties.BlendingConfigGL.BlendFunction;
import io.opensphere.core.util.Utilities;

/**
 * Manager for setting the OpenGL states for blending and switching those states
 * as necessary. This also handles the mapping of the configuration values to
 * the associated OpenGL values.
 */
public final class GLBlendingHandler
{
    /** Mapping of blend factor enums to the associated OpenGL values. */
    private static final Map<BlendFactor, Integer> ourBlendFactorTypeMap = new EnumMap<>(BlendFactor.class);

    /** Mapping of blend function enums to the associated OpenGL values. */
    private static final Map<BlendFunction, Integer> ourBlendFunctionTypeMap = new EnumMap<>(BlendFunction.class);

    static
    {
        ourBlendFactorTypeMap.put(BlendFactor.CONSTANT_ALPHA, Integer.valueOf(GL2ES2.GL_CONSTANT_ALPHA));
        ourBlendFactorTypeMap.put(BlendFactor.CONSTANT_COLOR, Integer.valueOf(GL2ES2.GL_CONSTANT_COLOR));
        ourBlendFactorTypeMap.put(BlendFactor.DST_ALPHA, Integer.valueOf(GL.GL_DST_ALPHA));
        ourBlendFactorTypeMap.put(BlendFactor.DST_COLOR, Integer.valueOf(GL.GL_DST_COLOR));
        ourBlendFactorTypeMap.put(BlendFactor.ONE, Integer.valueOf(GL.GL_ONE));
        ourBlendFactorTypeMap.put(BlendFactor.ONE_MINUS_CONSTANT_ALPHA, Integer.valueOf(GL2ES2.GL_ONE_MINUS_CONSTANT_ALPHA));
        ourBlendFactorTypeMap.put(BlendFactor.ONE_MINUS_CONSTANT_COLOR, Integer.valueOf(GL2ES2.GL_ONE_MINUS_CONSTANT_COLOR));
        ourBlendFactorTypeMap.put(BlendFactor.ONE_MINUS_DST_ALPHA, Integer.valueOf(GL.GL_ONE_MINUS_DST_ALPHA));
        ourBlendFactorTypeMap.put(BlendFactor.ONE_MINUS_DST_COLOR, Integer.valueOf(GL.GL_ONE_MINUS_DST_COLOR));
        ourBlendFactorTypeMap.put(BlendFactor.ONE_MINUS_SRC_ALPHA, Integer.valueOf(GL.GL_ONE_MINUS_SRC_ALPHA));
        ourBlendFactorTypeMap.put(BlendFactor.ONE_MINUS_SRC_COLOR, Integer.valueOf(GL.GL_ONE_MINUS_SRC_COLOR));
        ourBlendFactorTypeMap.put(BlendFactor.SRC_ALPHA, Integer.valueOf(GL.GL_SRC_ALPHA));
        ourBlendFactorTypeMap.put(BlendFactor.SRC_ALPHA_SATURATE, Integer.valueOf(GL.GL_SRC_ALPHA_SATURATE));
        ourBlendFactorTypeMap.put(BlendFactor.SRC_COLOR, Integer.valueOf(GL.GL_SRC_COLOR));
        ourBlendFactorTypeMap.put(BlendFactor.ZERO, Integer.valueOf(GL.GL_ZERO));

        ourBlendFunctionTypeMap.put(BlendFunction.FUNC_ADD, Integer.valueOf(GL.GL_FUNC_ADD));
        ourBlendFunctionTypeMap.put(BlendFunction.FUNC_REVERSE_SUBTRACT, Integer.valueOf(GL.GL_FUNC_REVERSE_SUBTRACT));
        ourBlendFunctionTypeMap.put(BlendFunction.FUNC_SUBTRACT, Integer.valueOf(GL.GL_FUNC_SUBTRACT));
        ourBlendFunctionTypeMap.put(BlendFunction.MAX, Integer.valueOf(GL2ES3.GL_MAX));
        ourBlendFunctionTypeMap.put(BlendFunction.MIN, Integer.valueOf(GL2ES3.GL_MIN));
    }

    /**
     * Setup for the blending as necessary for use in rendering.
     *
     * @param rc The render context.
     * @param previousBlend blending config which was previously loaded.
     * @param blend blending config to load.
     *
     * @return The blending config, if any, which was loaded by this method
     */
    public static BlendingConfigGL setBlending(RenderContext rc, BlendingConfigGL previousBlend, BlendingConfigGL blend)
    {
        if (Utilities.sameInstance(blend, previousBlend))
        {
            return blend;
        }

        if (blend == null)
        {
            rc.getGL().glDisable(GL.GL_BLEND);
            return null;
        }

        rc.getGL().glEnable(GL.GL_BLEND);

        rc.getGL2().glBlendColor(blend.getBlendColorRed(), blend.getBlendColorGreen(), blend.getBlendColorBlue(),
                blend.getBlendColorAlpha());

        int colorSrcFactor = ourBlendFactorTypeMap.get(blend.getColorSrcBlendFactor()).intValue();
        int alphaSrcFactor = ourBlendFactorTypeMap.get(blend.getAlphaSrcBlendFactor()).intValue();

        int colorDstFactor = ourBlendFactorTypeMap.get(blend.getColorDstBlendFactor()).intValue();
        int alphaDstFactor = ourBlendFactorTypeMap.get(blend.getAlphaDstBlendFactor()).intValue();

        int colorBlendEq = ourBlendFunctionTypeMap.get(blend.getColorBlendFuction()).intValue();
        int alphaBlendEq = ourBlendFunctionTypeMap.get(blend.getAlphaBlendFuction()).intValue();

        if (colorSrcFactor == alphaSrcFactor && colorDstFactor == alphaDstFactor && colorBlendEq == alphaBlendEq)
        {
            rc.getGL().glBlendFunc(colorSrcFactor, colorDstFactor);
            rc.getGL().glBlendEquation(colorBlendEq);
        }
        else
        {
            rc.getGL().glBlendFuncSeparate(colorSrcFactor, colorDstFactor, alphaSrcFactor, alphaDstFactor);
            rc.getGL().glBlendEquationSeparate(colorBlendEq, alphaBlendEq);
        }

        return blend;
    }

    /** Disallow instantiation. */
    private GLBlendingHandler()
    {
    }
}
