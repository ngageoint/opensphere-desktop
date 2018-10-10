package io.opensphere.core.pipeline.util;

import java.util.EnumMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.ColorMaterialModeParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.FaceParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightModelParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightModelVectorParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightVectorParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.MaterialParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.MaterialVectorParameterType;
import io.opensphere.core.util.Utilities;

/** Handle setup for the light as necessary for use in rendering. */
public final class GL2LightHandler
{
    /** Map of parameter types to GL constants. */
    private static final Map<ColorMaterialModeParameterType, Integer> ourColorMaterialModeParameterTypeMap;

    /** Map of parameter types to GL constants. */
    private static final Map<FaceParameterType, Integer> ourFaceParameterTypeMap;

    /** Map of parameter types to GL constants. */
    private static final Map<LightModelParameterType, Integer> ourLightModelParameterTypeMap;

    /** Map of parameter types to GL constants. */
    private static final Map<LightModelVectorParameterType, Integer> ourLightModelVectorParameterTypeMap;

    /** Map of parameter types to GL constants. */
    private static final Map<LightParameterType, Integer> ourLightParameterTypeMap;

    /** Map of parameter types to GL constants. */
    private static final Map<LightVectorParameterType, Integer> ourLightVectorParameterTypeMap;

    /** Map of parameter types to GL constants. */
    private static final Map<MaterialParameterType, Integer> ourMaterialParameterTypeMap;

    /** Map of parameter types to GL constants. */
    private static final Map<MaterialVectorParameterType, Integer> ourMaterialVectorParameterTypeMap;

    static
    {
        ourColorMaterialModeParameterTypeMap = new EnumMap<>(
                ColorMaterialModeParameterType.class);
        ourColorMaterialModeParameterTypeMap.put(ColorMaterialModeParameterType.AMBIENT,
                Integer.valueOf(GLLightingFunc.GL_AMBIENT));
        ourColorMaterialModeParameterTypeMap.put(ColorMaterialModeParameterType.AMBIENT_AND_DIFFUSE,
                Integer.valueOf(GLLightingFunc.GL_AMBIENT_AND_DIFFUSE));
        ourColorMaterialModeParameterTypeMap.put(ColorMaterialModeParameterType.DIFFUSE,
                Integer.valueOf(GLLightingFunc.GL_DIFFUSE));
        ourColorMaterialModeParameterTypeMap.put(ColorMaterialModeParameterType.EMISSION,
                Integer.valueOf(GLLightingFunc.GL_EMISSION));
        ourColorMaterialModeParameterTypeMap.put(ColorMaterialModeParameterType.SPECULAR,
                Integer.valueOf(GLLightingFunc.GL_SPECULAR));

        ourFaceParameterTypeMap = new EnumMap<>(FaceParameterType.class);
        ourFaceParameterTypeMap.put(FaceParameterType.BACK, Integer.valueOf(GL.GL_BACK));
        ourFaceParameterTypeMap.put(FaceParameterType.FRONT, Integer.valueOf(GL.GL_FRONT));
        ourFaceParameterTypeMap.put(FaceParameterType.FRONT_AND_BACK, Integer.valueOf(GL.GL_FRONT_AND_BACK));

        ourLightModelParameterTypeMap = new EnumMap<>(LightModelParameterType.class);
        ourLightModelParameterTypeMap.put(LightModelParameterType.LIGHT_MODEL_COLOR_CONTROL,
                Integer.valueOf(GL2.GL_LIGHT_MODEL_COLOR_CONTROL));
        ourLightModelParameterTypeMap.put(LightModelParameterType.LIGHT_MODEL_LOCAL_VIEWER,
                Integer.valueOf(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER));
        ourLightModelParameterTypeMap.put(LightModelParameterType.LIGHT_MODEL_TWO_SIDE,
                Integer.valueOf(GL2ES1.GL_LIGHT_MODEL_TWO_SIDE));

        ourLightModelVectorParameterTypeMap = new EnumMap<>(
                LightModelVectorParameterType.class);
        ourLightModelVectorParameterTypeMap.put(LightModelVectorParameterType.LIGHT_MODEL_AMBIENT,
                Integer.valueOf(GL2ES1.GL_LIGHT_MODEL_AMBIENT));
        ourLightModelVectorParameterTypeMap.put(LightModelVectorParameterType.LIGHT_MODEL_COLOR_CONTROL,
                Integer.valueOf(GL2.GL_LIGHT_MODEL_COLOR_CONTROL));
        ourLightModelVectorParameterTypeMap.put(LightModelVectorParameterType.LIGHT_MODEL_LOCAL_VIEWER,
                Integer.valueOf(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER));
        ourLightModelVectorParameterTypeMap.put(LightModelVectorParameterType.LIGHT_MODEL_TWO_SIDE,
                Integer.valueOf(GL2ES1.GL_LIGHT_MODEL_TWO_SIDE));

        ourLightParameterTypeMap = new EnumMap<>(LightParameterType.class);
        ourLightParameterTypeMap.put(LightParameterType.CONSTANT_ATTENUATION,
                Integer.valueOf(GLLightingFunc.GL_CONSTANT_ATTENUATION));
        ourLightParameterTypeMap.put(LightParameterType.LINEAR_ATTENUATION,
                Integer.valueOf(GLLightingFunc.GL_LINEAR_ATTENUATION));
        ourLightParameterTypeMap.put(LightParameterType.QUADRATIC_ATTENUATION,
                Integer.valueOf(GLLightingFunc.GL_QUADRATIC_ATTENUATION));
        ourLightParameterTypeMap.put(LightParameterType.SPOT_CUTOFF, Integer.valueOf(GLLightingFunc.GL_SPOT_CUTOFF));
        ourLightParameterTypeMap.put(LightParameterType.SPOT_EXPONENT, Integer.valueOf(GLLightingFunc.GL_SPOT_EXPONENT));

        ourLightVectorParameterTypeMap = new EnumMap<>(LightVectorParameterType.class);
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.AMBIENT, Integer.valueOf(GLLightingFunc.GL_AMBIENT));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.CONSTANT_ATTENUATION,
                Integer.valueOf(GLLightingFunc.GL_CONSTANT_ATTENUATION));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.DIFFUSE, Integer.valueOf(GLLightingFunc.GL_DIFFUSE));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.LINEAR_ATTENUATION,
                Integer.valueOf(GLLightingFunc.GL_LINEAR_ATTENUATION));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.POSITION, Integer.valueOf(GLLightingFunc.GL_POSITION));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.QUADRATIC_ATTENUATION,
                Integer.valueOf(GLLightingFunc.GL_QUADRATIC_ATTENUATION));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.SPECULAR, Integer.valueOf(GLLightingFunc.GL_SPECULAR));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.SPOT_CUTOFF, Integer.valueOf(GLLightingFunc.GL_SPOT_CUTOFF));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.SPOT_DIRECTION,
                Integer.valueOf(GLLightingFunc.GL_SPOT_DIRECTION));
        ourLightVectorParameterTypeMap.put(LightVectorParameterType.SPOT_EXPONENT,
                Integer.valueOf(GLLightingFunc.GL_SPOT_EXPONENT));

        ourMaterialParameterTypeMap = new EnumMap<>(MaterialParameterType.class);
        ourMaterialParameterTypeMap.put(MaterialParameterType.SHININESS, Integer.valueOf(GLLightingFunc.GL_SHININESS));

        ourMaterialVectorParameterTypeMap = new EnumMap<>(MaterialVectorParameterType.class);
        ourMaterialVectorParameterTypeMap.put(MaterialVectorParameterType.AMBIENT, Integer.valueOf(GLLightingFunc.GL_AMBIENT));
        ourMaterialVectorParameterTypeMap.put(MaterialVectorParameterType.AMBIENT_AND_DIFFUSE,
                Integer.valueOf(GLLightingFunc.GL_AMBIENT_AND_DIFFUSE));
        ourMaterialVectorParameterTypeMap.put(MaterialVectorParameterType.COLOR_INDEXES, Integer.valueOf(GL2.GL_COLOR_INDEXES));
        ourMaterialVectorParameterTypeMap.put(MaterialVectorParameterType.DIFFUSE, Integer.valueOf(GLLightingFunc.GL_DIFFUSE));
        ourMaterialVectorParameterTypeMap.put(MaterialVectorParameterType.EMISSION, Integer.valueOf(GLLightingFunc.GL_EMISSION));
        ourMaterialVectorParameterTypeMap.put(MaterialVectorParameterType.SHININESS,
                Integer.valueOf(GLLightingFunc.GL_SHININESS));
        ourMaterialVectorParameterTypeMap.put(MaterialVectorParameterType.SPECULAR, Integer.valueOf(GLLightingFunc.GL_SPECULAR));
    }

    /**
     * Setup for the light as necessary for use in rendering.
     *
     * @param rc The render context.
     * @param previousLight Lighting model which was previously loaded.
     * @param light Lighting model to load.
     *
     * @return The lighting model, if any, which was loaded by this method
     */
    public static LightingModelConfigGL setLight(RenderContext rc, LightingModelConfigGL previousLight,
            LightingModelConfigGL light)
    {
        if (Utilities.sameInstance(light, previousLight))
        {
            return light;
        }

        if (light == null)
        {
            rc.getGL().glDisable(GLLightingFunc.GL_LIGHTING);
            return null;
        }

        rc.getGL().glEnable(GLLightingFunc.GL_NORMALIZE);

        rc.getGL2().glShadeModel(GLLightingFunc.GL_SMOOTH);
        rc.getGL().glEnable(GLLightingFunc.GL_LIGHTING);

        activateLightModels(rc, light);

        activateLightModelVectors(rc, light);

        activateLights(rc, light, getGLLightNumber(light.getLightNumber()));

        activateLightVectors(rc, light, getGLLightNumber(light.getLightNumber()));

        rc.getGL().glEnable(getGLLightNumber(light.getLightNumber()));
        if (light.getColorMaterialMode() != null)
        {
            rc.getGL().glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
            rc.getGL2().glColorMaterial(getGLCode(light.getFace()), getGLCode(light.getColorMaterialMode()));
        }

        activateMaterials(rc, light);

        activateMaterialVectors(rc, light);

        return light;
    }

    /**
     * Activate the GL parameter settings.
     *
     * @param rc The render context.
     * @param light Lighting model to load.
     */
    private static void activateLightModels(RenderContext rc, LightingModelConfigGL light)
    {
        if (!light.getLightModelParameters().isEmpty())
        {
            for (LightingModelConfigGL.SingleParameter<LightModelParameterType> param : light.getLightModelParameters())
            {
                rc.getGL2().glLightModelf(getGLCode(param.getParameterType()), param.getValue());
            }
        }
    }

    /**
     * Activate the GL parameter settings.
     *
     * @param rc The render context.
     * @param light Lighting model to load.
     */
    private static void activateLightModelVectors(RenderContext rc, LightingModelConfigGL light)
    {
        if (!light.getLightModelVectorParameters().isEmpty())
        {
            for (LightingModelConfigGL.VectorParameter<LightModelVectorParameterType> param : light
                    .getLightModelVectorParameters())
            {
                int glCode = getGLCode(param.getParameterType());
                if (glCode != GL2.GL_LIGHT_MODEL_COLOR_CONTROL || rc.is12Available())
                {
                    rc.getGL2().glLightModelfv(glCode, param.getValues(), 0);
                }
            }
        }
    }

    /**
     * Activate the GL parameter settings.
     *
     * @param rc The render context.
     * @param light Lighting model to load.
     * @param lightNumber The light number associated with the parameters.
     */
    private static void activateLights(RenderContext rc, LightingModelConfigGL light, int lightNumber)
    {
        if (!light.getLightParameters().isEmpty())
        {
            for (LightingModelConfigGL.SingleParameter<LightParameterType> param : light.getLightParameters())
            {
                rc.getGL2().glLightf(getGLLightNumber(lightNumber), getGLCode(param.getParameterType()), param.getValue());
            }
        }
    }

    /**
     * Activate the GL parameter settings.
     *
     * @param rc The render context.
     * @param light Lighting model to load.
     * @param glLightNumber The light number associated with the parameters.
     */
    private static void activateLightVectors(RenderContext rc, LightingModelConfigGL light, int glLightNumber)
    {
        if (!light.getLightVectorParameters().isEmpty())
        {
            for (LightingModelConfigGL.VectorParameter<LightVectorParameterType> param : light.getLightVectorParameters())
            {
                rc.getGL2().glLightfv(glLightNumber, getGLCode(param.getParameterType()), param.getValues(), 0);
            }
        }
    }

    /**
     * Activate the GL parameter settings.
     *
     * @param rc The render context.
     * @param light Lighting model to load.
     */
    private static void activateMaterials(RenderContext rc, LightingModelConfigGL light)
    {
        if (!light.getMaterialParameters().isEmpty())
        {
            for (LightingModelConfigGL.SingleParameter<MaterialParameterType> param : light.getMaterialParameters())
            {
                rc.getGL2().glMaterialf(getGLCode(light.getFace()), getGLCode(param.getParameterType()), param.getValue());
            }
        }
    }

    /**
     * Activate the GL parameter settings.
     *
     * @param rc The render context.
     * @param light Lighting model to load.
     */
    private static void activateMaterialVectors(RenderContext rc, LightingModelConfigGL light)
    {
        if (!light.getMaterialVectorParameters().isEmpty())
        {
            for (LightingModelConfigGL.VectorParameter<MaterialVectorParameterType> param : light.getMaterialVectorParameters())
            {
                rc.getGL2().glMaterialfv(getGLCode(light.getFace()), getGLCode(param.getParameterType()), param.getValues(), 0);
            }
        }
    }

    /**
     * Get the GL code for a light parameter.
     *
     * @param type The parameter type.
     * @return The GL code.
     */
    private static int getGLCode(ColorMaterialModeParameterType type)
    {
        return ourColorMaterialModeParameterTypeMap.get(type).intValue();
    }

    /**
     * Get the GL code for a light parameter.
     *
     * @param type The parameter type.
     * @return The GL code.
     */
    private static int getGLCode(FaceParameterType type)
    {
        return ourFaceParameterTypeMap.get(type).intValue();
    }

    /**
     * Get the GL code for a light parameter.
     *
     * @param type The parameter type.
     * @return The GL code.
     */
    private static int getGLCode(LightModelParameterType type)
    {
        return ourLightModelParameterTypeMap.get(type).intValue();
    }

    /**
     * Get the GL code for a light parameter.
     *
     * @param type The parameter type.
     * @return The GL code.
     */
    private static int getGLCode(LightModelVectorParameterType type)
    {
        return ourLightModelVectorParameterTypeMap.get(type).intValue();
    }

    /**
     * Get the GL code for a light parameter.
     *
     * @param type The parameter type.
     * @return The GL code.
     */
    private static int getGLCode(LightParameterType type)
    {
        return ourLightParameterTypeMap.get(type).intValue();
    }

    /**
     * Get the GL code for a light parameter.
     *
     * @param type The parameter type.
     * @return The GL code.
     */
    private static int getGLCode(LightVectorParameterType type)
    {
        return ourLightVectorParameterTypeMap.get(type).intValue();
    }

    /**
     * Get the GL code for a light parameter.
     *
     * @param type The parameter type.
     * @return The GL code.
     */
    private static int getGLCode(MaterialParameterType type)
    {
        return ourMaterialParameterTypeMap.get(type).intValue();
    }

    /**
     * Get the GL code for a light parameter.
     *
     * @param type The parameter type.
     * @return The GL code.
     */
    private static int getGLCode(MaterialVectorParameterType type)
    {
        return ourMaterialVectorParameterTypeMap.get(type).intValue();
    }

    /**
     * Get the GL code for a light number.
     *
     * @param number The light number.
     * @return The GL code.
     */
    private static int getGLLightNumber(int number)
    {
        switch (number)
        {
            case 0:
                return GLLightingFunc.GL_LIGHT0;
            case 1:
                return GLLightingFunc.GL_LIGHT1;
            case 2:
                return GLLightingFunc.GL_LIGHT2;
            case 3:
                return GLLightingFunc.GL_LIGHT3;
            case 4:
                return GLLightingFunc.GL_LIGHT4;
            case 5:
                return GLLightingFunc.GL_LIGHT5;
            case 6:
                return GLLightingFunc.GL_LIGHT6;
            case 7:
                return GLLightingFunc.GL_LIGHT7;
            default:
                throw new IllegalArgumentException("Illegal light number: " + number);
        }
    }

    /** Disallow construction. */
    private GL2LightHandler()
    {
    }
}
