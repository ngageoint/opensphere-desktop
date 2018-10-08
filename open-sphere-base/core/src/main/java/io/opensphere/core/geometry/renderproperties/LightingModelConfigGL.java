package io.opensphere.core.geometry.renderproperties;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;

/** A set of lighting model settings to use when rendering a geometry. */
public class LightingModelConfigGL implements Serializable
{
    /**
     * The default lighting. This may be used so that when lighting is the same
     * for a set of geometries, they can easily share an instance.
     */
    private static final LightingModelConfigGL ourDefaultLight;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Color material mode. */
    private final ColorMaterialModeParameterType myColorMaterialMode;

    /** The facing which the light is applied to. */
    private final FaceParameterType myFace;

    /** Single light model parameters. */
    private final List<? extends SingleParameter<LightModelParameterType>> myLightModelParameters;

    /** Vector light model parameters. */
    private final List<? extends VectorParameter<LightModelVectorParameterType>> myLightModelVectorParameters;

    /** The light number. */
    private final int myLightNumber;

    /** Single light parameters. */
    private final List<? extends SingleParameter<LightParameterType>> myLightParameters;

    /** Vector light parameters. */
    private final List<? extends VectorParameter<LightVectorParameterType>> myLightVectorParameters;

    /** Single material parameters. */
    private final List<? extends SingleParameter<MaterialParameterType>> myMaterialParameters;

    /** Vector material parameters. */
    private final List<? extends VectorParameter<MaterialVectorParameterType>> myMaterialVectorParameters;

    static
    {
        LightingModelConfigGL.Builder builder = new LightingModelConfigGL.Builder();
        builder.setLightNumber(0);
        builder.setFace(FaceParameterType.FRONT);
        builder.setColorMaterialMode(ColorMaterialModeParameterType.AMBIENT_AND_DIFFUSE);

        final float[] ambientLight = { 0.15f, 0.15f, 0.15f, 1f };
        final float[] diffuseLight = { 1f, 1f, 1f, 1f };
        final float[] position = { -0.17f, 0.61f, 0.81f, 0f };
        final float[] specular = { 0.74f, 0.77f, 0.77f, 1f };
        final float[] specularReflectivity = { 0.9f, 0.9f, 0.9f, 1f };
        final float shininess = 80f;

        builder.addLightModelVectorParameter(LightModelVectorParameterType.LIGHT_MODEL_AMBIENT, ambientLight);
        builder.addLightParameterVector(LightVectorParameterType.DIFFUSE, diffuseLight);
        builder.addLightParameterVector(LightVectorParameterType.POSITION, position);
        builder.addLightParameterVector(LightVectorParameterType.SPECULAR, specular);
        builder.addMaterialVectorParameter(MaterialVectorParameterType.SPECULAR, specularReflectivity);
        builder.addMaterialShininessParameter(shininess);

        ourDefaultLight = new LightingModelConfigGL(builder);
    }

    /**
     * Get the defaultLight.
     *
     * @return The defaultLight.
     */
    public static LightingModelConfigGL getDefaultLight()
    {
        return ourDefaultLight;
    }

    /**
     * Constructor.
     *
     * @param builder The builder object.
     */
    public LightingModelConfigGL(Builder builder)
    {
        myColorMaterialMode = builder.getColorMaterialMode();
        myFace = builder.getFace();
        myLightModelParameters = New.unmodifiableList(builder.getLightModelParameters());
        myLightModelVectorParameters = New.unmodifiableList(builder.getLightModelVectorParameters());
        myLightNumber = builder.getLightNumber();
        myLightParameters = New.unmodifiableList(builder.getLightParameters());
        myLightVectorParameters = New.unmodifiableList(builder.getLightVectorParameters());
        myMaterialParameters = New.unmodifiableList(builder.getMaterialParameters());
        myMaterialVectorParameters = New.unmodifiableList(builder.getMaterialVectorParameters());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        LightingModelConfigGL other = (LightingModelConfigGL)obj;
        return myLightNumber == other.myLightNumber && EqualsHelper.equals(myColorMaterialMode, other.myColorMaterialMode)
                && EqualsHelper.equals(myFace, other.myFace)
                && EqualsHelper.equals(myLightModelParameters, other.myLightModelParameters)
                && EqualsHelper.equals(myLightModelVectorParameters, other.myLightModelVectorParameters)
                && EqualsHelper.equals(myLightParameters, other.myLightParameters)
                && EqualsHelper.equals(myLightVectorParameters, other.myLightVectorParameters)
                && EqualsHelper.equals(myMaterialParameters, other.myMaterialParameters)
                && EqualsHelper.equals(myMaterialVectorParameters, other.myMaterialVectorParameters);
    }

    /**
     * Get the color material mode.
     *
     * @return The color material mode.
     */
    public ColorMaterialModeParameterType getColorMaterialMode()
    {
        return myColorMaterialMode;
    }

    /**
     * Get the face.
     *
     * @return The face.
     */
    public FaceParameterType getFace()
    {
        return myFace;
    }

    /**
     * Get the light model parameters.
     *
     * @return The light model parameters.
     */
    public List<? extends SingleParameter<LightModelParameterType>> getLightModelParameters()
    {
        return myLightModelParameters;
    }

    /**
     * Get the light model vector parameters.
     *
     * @return The light model vector parameters.
     */
    public List<? extends VectorParameter<LightModelVectorParameterType>> getLightModelVectorParameters()
    {
        return myLightModelVectorParameters;
    }

    /**
     * Get the light number.
     *
     * @return The light number.
     */
    public int getLightNumber()
    {
        return myLightNumber;
    }

    /**
     * Get the light parameters.
     *
     * @return The light parameters.
     */
    public List<? extends SingleParameter<LightParameterType>> getLightParameters()
    {
        return myLightParameters;
    }

    /**
     * Get the light vector parameters.
     *
     * @return The light vector parameters.
     */
    public List<? extends VectorParameter<LightVectorParameterType>> getLightVectorParameters()
    {
        return myLightVectorParameters;
    }

    /**
     * Get the material parameters.
     *
     * @return The material parameters.
     */
    public List<? extends SingleParameter<MaterialParameterType>> getMaterialParameters()
    {
        return myMaterialParameters;
    }

    /**
     * Get the material vector parameters.
     *
     * @return The material parameter vectors.
     */
    public List<? extends VectorParameter<MaterialVectorParameterType>> getMaterialVectorParameters()
    {
        return myMaterialVectorParameters;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myColorMaterialMode);
        result = prime * result + HashCodeHelper.getHashCode(myFace);
        result = prime * result + HashCodeHelper.getHashCode(myLightModelParameters);
        result = prime * result + HashCodeHelper.getHashCode(myLightModelVectorParameters);
        result = prime * result + myLightNumber;
        result = prime * result + HashCodeHelper.getHashCode(myLightParameters);
        result = prime * result + HashCodeHelper.getHashCode(myLightVectorParameters);
        result = prime * result + HashCodeHelper.getHashCode(myMaterialParameters);
        result = prime * result + HashCodeHelper.getHashCode(myMaterialVectorParameters);
        return result;
    }

    /**
     * A builder class for {@link LightingModelConfigGL}. This allows the
     * constructed {@link LightingModelConfigGL} objects to be immutable.
     */
    public static class Builder
    {
        /** Color material mode. */
        private ColorMaterialModeParameterType myColorMaterialMode;

        /** The facing which the light is applied to. */
        private FaceParameterType myFace;

        /** Single light model parameters. */
        private final List<SingleParameter<LightModelParameterType>> myLightModelParameters = New.list();

        /** Vector light model parameters. */
        private final List<VectorParameter<LightModelVectorParameterType>> myLightModelVectorParameters = New.list();

        /** The light number. */
        private int myLightNumber;

        /** Single light parameters. */
        private final List<SingleParameter<LightParameterType>> myLightParameters = New.list();

        /** Vector light parameters. */
        private final List<VectorParameter<LightVectorParameterType>> myLightVectorParameters = New.list();

        /** Single material parameters. */
        private final List<SingleParameter<MaterialParameterType>> myMaterialParameters = New.list();

        /** Vector material parameters. */
        private final List<VectorParameter<MaterialVectorParameterType>> myMaterialVectorParameters = New.list();

        /**
         * Add a light model parameter to activate during rendering
         * (glLightModelv()).
         *
         * @param paramType The parameter type.
         * @param value Parameter value.
         */
        public void addLightModelParameter(LightModelParameterType paramType, float value)
        {
            myLightModelParameters.add(new SingleParameter<>(paramType, value));
        }

        /**
         * Add a light model vector parameter to activate during rendering.
         *
         * @param paramType The parameter type.
         * @param values Parameter values.
         */
        public void addLightModelVectorParameter(LightModelVectorParameterType paramType, float[] values)
        {
            myLightModelVectorParameters.add(new VectorParameter<>(paramType, values));
        }

        /**
         * Add a light parameter to activate during rendering (glLightf()).
         *
         * @param paramType The parameter type.
         * @param value Parameter value.
         */
        public void addLightParameter(LightParameterType paramType, float value)
        {
            myLightParameters.add(new SingleParameter<>(paramType, value));
        }

        /**
         * Add a light vector parameter to activate during rendering
         * (glLightfv()).
         *
         * @param paramType The parameter type.
         * @param values Parameter values.
         */
        public void addLightParameterVector(LightVectorParameterType paramType, float[] values)
        {
            myLightVectorParameters.add(new VectorParameter<>(paramType, values));
        }

        /**
         * Add a material shininess parameter to activate during rendering.
         *
         * @param value Parameter value.
         */
        public void addMaterialShininessParameter(float value)
        {
            myMaterialParameters.add(new SingleParameter<>(MaterialParameterType.SHININESS, value));
        }

        /**
         * Add a material vector parameter to activate during rendering.
         *
         * @param paramType The parameter type.
         * @param values Parameter value.
         */
        public void addMaterialVectorParameter(MaterialVectorParameterType paramType, float[] values)
        {
            myMaterialVectorParameters.add(new VectorParameter<>(paramType, values));
        }

        /**
         * Get the color material mode for the builder.
         *
         * @return The color material mode.
         */
        public ColorMaterialModeParameterType getColorMaterialMode()
        {
            return myColorMaterialMode;
        }

        /**
         * Get the face for the builder.
         *
         * @return The face.
         */
        public FaceParameterType getFace()
        {
            return myFace;
        }

        /**
         * Get the light model parameters for the builder.
         *
         * @return The light model parameters.
         */
        public List<SingleParameter<LightModelParameterType>> getLightModelParameters()
        {
            return myLightModelParameters;
        }

        /**
         * Get the light model vector parameters for the builder.
         *
         * @return The light model vector parameters.
         */
        public List<VectorParameter<LightModelVectorParameterType>> getLightModelVectorParameters()
        {
            return myLightModelVectorParameters;
        }

        /**
         * Get the light number for the builder.
         *
         * @return The light number.
         */
        public int getLightNumber()
        {
            return myLightNumber;
        }

        /**
         * Get the light parameters for the builder.
         *
         * @return The light parameters.
         */
        public List<SingleParameter<LightParameterType>> getLightParameters()
        {
            return myLightParameters;
        }

        /**
         * Get the light vector parameters for the builder.
         *
         * @return The light vector parameters.
         */
        public List<VectorParameter<LightVectorParameterType>> getLightVectorParameters()
        {
            return myLightVectorParameters;
        }

        /**
         * Get the material parameters for the builder.
         *
         * @return The material parameters.
         */
        public List<SingleParameter<MaterialParameterType>> getMaterialParameters()
        {
            return myMaterialParameters;
        }

        /**
         * Get the material vector parameters for the builder.
         *
         * @return The material vector parameters.
         */
        public List<VectorParameter<MaterialVectorParameterType>> getMaterialVectorParameters()
        {
            return myMaterialVectorParameters;
        }

        /**
         * Set the color material mode for the builder.
         *
         * @param colorMaterialMode The color material mode.
         */
        public void setColorMaterialMode(ColorMaterialModeParameterType colorMaterialMode)
        {
            myColorMaterialMode = colorMaterialMode;
        }

        /**
         * Set the face for the builder.
         *
         * @param face The face.
         */
        public void setFace(FaceParameterType face)
        {
            myFace = face;
        }

        /**
         * Set the light number for the builder.
         *
         * @param number The light number.
         */
        public void setLightNumber(int number)
        {
            myLightNumber = number;
        }
    }

    /** Color material modes. */
    public enum ColorMaterialModeParameterType
    {
        /** Ambient. */
        AMBIENT,

        /** Ambient and diffuse. */
        AMBIENT_AND_DIFFUSE,

        /** Diffuse. */
        DIFFUSE,

        /** Emission. */
        EMISSION,

        /** Specular. */
        SPECULAR,
    }

    /** Valid facing parameters. */
    public enum FaceParameterType
    {
        /** Back face. */
        BACK,

        /** Front face. */
        FRONT,

        /** Both faces. */
        FRONT_AND_BACK,
    }

    /** Valid single light model parameters. */
    public enum LightModelParameterType
    {
        /** GL.GL_LIGHT_MODEL_COLOR_CONTROL. */
        LIGHT_MODEL_COLOR_CONTROL,

        /** GL.GL_LIGHT_MODEL_LOCAL_VIEWER. */
        LIGHT_MODEL_LOCAL_VIEWER,

        /** GL.GL_LIGHT_MODEL_TWO_SIDE. */
        LIGHT_MODEL_TWO_SIDE,
    }

    /** Valid vector light model parameters. */
    public enum LightModelVectorParameterType
    {
        /** GL.GL_LIGHT_MODEL_AMBIENT. */
        LIGHT_MODEL_AMBIENT,

        /** GL.GL_LIGHT_MODEL_COLOR_CONTROL. */
        LIGHT_MODEL_COLOR_CONTROL,

        /** GL.GL_LIGHT_MODEL_LOCAL_VIEWER. */
        LIGHT_MODEL_LOCAL_VIEWER,

        /** GL.GL_LIGHT_MODEL_TWO_SIDE. */
        LIGHT_MODEL_TWO_SIDE,
    }

    /** Valid light model parameters. */
    public enum LightParameterType
    {
        /** GL.GL_CONSTANT_ATTENUATION. */
        CONSTANT_ATTENUATION,

        /** GL.GL_LINEAR_ATTENUATION. */
        LINEAR_ATTENUATION,

        /** GL.GL_QUADRATIC_ATTENUATION. */
        QUADRATIC_ATTENUATION,

        /** GL.GL_SPOT_CUTOFF. */
        SPOT_CUTOFF,

        /** GL.GL_SPOT_EXPONENT. */
        SPOT_EXPONENT,
    }

    /** Valid light model parameters. */
    public enum LightVectorParameterType
    {
        /** GL.GL_AMBIENT. */
        AMBIENT,

        /** GL.GL_CONSTANT_ATTENUATION. */
        CONSTANT_ATTENUATION,

        /** GL.GL_DIFFUSE. */
        DIFFUSE,

        /** GL.GL_LINEAR_ATTENUATION. */
        LINEAR_ATTENUATION,

        /** GL.GL_POSITION. */
        POSITION,

        /** GL.GL_QUADRATIC_ATTENUATION. */
        QUADRATIC_ATTENUATION,

        /** GL.GL_SPECULAR. */
        SPECULAR,

        /** GL.GL_SPOT_CUTOFF. */
        SPOT_CUTOFF,

        /** GL.GL_SPOT_DIRECTION. */
        SPOT_DIRECTION,

        /** GL.GL_SPOT_EXPONENT. */
        SPOT_EXPONENT,
    }

    /** Valid shininess parameters. */
    public enum MaterialParameterType
    {
        /** SHININESS parameter. */
        SHININESS,
    }

    /** Valid light model parameters. */
    public enum MaterialVectorParameterType
    {
        /** GL.GL_AMBIENT. */
        AMBIENT,

        /** GL.GL_AMBIENT_AND_DIFFUSE. */
        AMBIENT_AND_DIFFUSE,

        /** GL.GL_COLOR_INDEXES. */
        COLOR_INDEXES,

        /** GL.GL_DIFFUSE. */
        DIFFUSE,

        /** GL.GL_EMISSION. */
        EMISSION,

        /** GL.GL_SHININESS. */
        SHININESS,

        /** GL.GL_SPECULAR. */
        SPECULAR,
    }

    /**
     * A single parameter which will be activated for rendering.
     *
     * @param <T> The parameter type.
     */
    public static class Parameter<T extends Enum<T>> implements Serializable
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Enumeration value indicating the parameter type. */
        private final T myType;

        /**
         * Constructor.
         *
         * @param type The parameter type.
         */
        public Parameter(T type)
        {
            myType = type;
        }

        /**
         * Get the parameter type.
         *
         * @return The parameter type.
         */
        public T getParameterType()
        {
            return myType;
        }
    }

    /**
     * A single parameter which will be activated for rendering.
     *
     * @param <T> The parameter type.
     */
    public static class SingleParameter<T extends Enum<T>> extends Parameter<T>
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Parameter value. */
        private final float myValue;

        /**
         * Constructor.
         *
         * @param type The parameter type.
         * @param value Parameter value.
         */
        public SingleParameter(T type, float value)
        {
            super(type);
            myValue = value;
        }

        /**
         * Get the value.
         *
         * @return The value.
         */
        public float getValue()
        {
            return myValue;
        }
    }

    /**
     * An array parameters which will be activated for rendering.
     *
     * @param <T> The parameter type.
     */
    public static class VectorParameter<T extends Enum<T>> extends Parameter<T>
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Parameter values. */
        private final float[] myValues;

        /**
         * Constructor.
         *
         * @param type The parameter type.
         * @param values Parameter values.
         */
        public VectorParameter(T type, float[] values)
        {
            super(type);
            myValues = values.clone();
        }

        /**
         * Get the values.
         *
         * @return The values.
         */
        public float[] getValues()
        {
            return myValues.clone();
        }
    }
}
