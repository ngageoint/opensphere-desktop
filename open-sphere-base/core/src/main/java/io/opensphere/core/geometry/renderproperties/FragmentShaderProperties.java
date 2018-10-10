package io.opensphere.core.geometry.renderproperties;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.math.AbstractMatrix;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTFloatArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.lang.Pair;

/**
 * A set of properties which defines a fragment shader snippet to determine the
 * fragment color. Currently only uniform variables and a snippet defining
 * <code>vec4 getFragColor(sampler2D inTexture)</code> may be given to determine
 * fragment color. When changing uniform values, the same String instance should
 * be used for the shader snippet to avoid recompiling and reloading of the
 * code.
 */
public interface FragmentShaderProperties extends RenderProperties
{
    /**
     * Get the booleanUniforms.
     *
     * @return the booleanUniforms
     */
    Collection<? extends Pair<String, PetrifyableTIntArrayList>> getBooleanUniforms();

    /**
     * Get the floatUniforms.
     *
     * @return the floatUniforms
     */
    Collection<? extends Pair<String, PetrifyableTFloatArrayList>> getFloatUniforms();

    /**
     * Get the integerUniforms.
     *
     * @return the integerUniforms
     */
    Collection<? extends Pair<String, PetrifyableTIntArrayList>> getIntegerUniforms();

    /**
     * Get the matrixUniforms.
     *
     * @return the matrixUniforms
     */
    Collection<? extends Pair<String, AbstractMatrix>> getMatrixUniforms();

    /**
     * Get the shaderCode.
     *
     * @return the shaderCode
     */
    String getShaderCode();

    /**
     * All shader properties must be set at the same time to ensure that the
     * shader will function.
     *
     * @param propertiesSet The set of properties to apply when rendering.
     */
    void setupShader(ShaderPropertiesSet propertiesSet);

    /** A set of properties for a fragment shader. */
    class ShaderPropertiesSet
    {
        /** Uniform names and values of type boolean. */
        private volatile Collection<? extends Pair<String, PetrifyableTIntArrayList>> myBooleanUniforms = Collections.emptyList();

        /** Uniform names and values of type float. */
        private volatile Collection<? extends Pair<String, PetrifyableTFloatArrayList>> myFloatUniforms = Collections.emptyList();

        /** Uniform names and values of type int. */
        private volatile Collection<? extends Pair<String, PetrifyableTIntArrayList>> myIntegerUniforms = Collections.emptyList();

        /** Uniform names and values for matrices. */
        private volatile Collection<? extends Pair<String, AbstractMatrix>> myMatrixUniforms = Collections.emptyList();

        /**
         * The fragment shader snippet which determines the fragment color. The
         * function should implement
         * <code>vec4 getFragColor(sampler2D inTexture)</code> which will be
         * called from the main shader. Uniforms maybe defined and values maybe
         * provided and changed. When changing uniform values, the same String
         * instance should be used for the shader snippet to avoid recompiling
         * and reloading of the code.
         */
        private String myShaderCode;

        /**
         * Get the booleanUniforms.
         *
         * @return the booleanUniforms
         */
        public Collection<? extends Pair<String, PetrifyableTIntArrayList>> getBooleanUniforms()
        {
            return myBooleanUniforms;
        }

        /**
         * Get the floatUniforms.
         *
         * @return the floatUniforms
         */
        public Collection<? extends Pair<String, PetrifyableTFloatArrayList>> getFloatUniforms()
        {
            return myFloatUniforms;
        }

        /**
         * Get the integerUniforms.
         *
         * @return the integerUniforms
         */
        public Collection<? extends Pair<String, PetrifyableTIntArrayList>> getIntegerUniforms()
        {
            return myIntegerUniforms;
        }

        /**
         * Get the matrixUniforms.
         *
         * @return the matrixUniforms
         */
        public Collection<? extends Pair<String, AbstractMatrix>> getMatrixUniforms()
        {
            return myMatrixUniforms;
        }

        /**
         * Get the shaderCode.
         *
         * @return the shaderCode
         */
        public String getShaderCode()
        {
            return myShaderCode;
        }

        /**
         * Set the booleanUniforms.
         *
         * @param booleanUniforms the booleanUniforms to set
         */
        public void setBooleanUniforms(Collection<Pair<String, boolean[]>> booleanUniforms)
        {
            Collection<Pair<String, PetrifyableTIntArrayList>> col = New.collection(booleanUniforms.size());
            for (Pair<String, boolean[]> uniform : booleanUniforms)
            {
                if (uniform.getSecondObject().length > 4)
                {
                    throw new InvalidParameterException("Uniforms cannot contain more than 4 elements");
                }

                // convert the boolean values to integers (1 for true, 0 for
                // false).
                int[] values = new int[uniform.getSecondObject().length];
                for (int i = 0; i < values.length; ++i)
                {
                    values[i] = uniform.getSecondObject()[i] ? 1 : 0;
                }

                PetrifyableTIntArrayList petrifiedValues = new PetrifyableTIntArrayList(values);
                petrifiedValues.petrify();
                col.add(new Pair<>(uniform.getFirstObject(), petrifiedValues));
            }
            myBooleanUniforms = Collections.unmodifiableCollection(col);
        }

        /**
         * Set the floatUniforms.
         *
         * @param floatUniforms the floatUniforms to set
         */
        public void setFloatUniforms(Collection<Pair<String, float[]>> floatUniforms)
        {
            Collection<Pair<String, PetrifyableTFloatArrayList>> col = New.collection(floatUniforms.size());
            for (Pair<String, float[]> uniform : floatUniforms)
            {
                if (uniform.getSecondObject().length > 4)
                {
                    throw new InvalidParameterException("Uniforms cannot contain more than 4 elements");
                }

                PetrifyableTFloatArrayList petrifiedValues = new PetrifyableTFloatArrayList(uniform.getSecondObject());
                petrifiedValues.petrify();
                col.add(new Pair<>(uniform.getFirstObject(), petrifiedValues));
            }
            myFloatUniforms = Collections.unmodifiableCollection(col);
        }

        /**
         * Set the integerUniforms.
         *
         * @param integerUniforms the integerUniforms to set
         */
        public void setIntegerUniforms(Collection<Pair<String, int[]>> integerUniforms)
        {
            Collection<Pair<String, PetrifyableTIntArrayList>> col = New.collection(integerUniforms.size());
            for (Pair<String, int[]> uniform : integerUniforms)
            {
                if (uniform.getSecondObject().length > 4)
                {
                    throw new InvalidParameterException("Uniforms cannot contain more than 4 elements");
                }

                PetrifyableTIntArrayList petrifiedValues = new PetrifyableTIntArrayList(uniform.getSecondObject());
                petrifiedValues.petrify();
                col.add(new Pair<>(uniform.getFirstObject(), petrifiedValues));
            }
            myIntegerUniforms = Collections.unmodifiableCollection(col);
        }

        /**
         * Set the matrixUniforms.
         *
         * @param matrixUniforms the matrixUniforms to set
         */
        public void setMatrixUniforms(Collection<Pair<String, AbstractMatrix>> matrixUniforms)
        {
            myMatrixUniforms = New.unmodifiableCollection(matrixUniforms);
        }

        /**
         * Set the shaderCode.
         *
         * @param shaderCode the shaderCode to set
         */
        public void setShaderCode(String shaderCode)
        {
            myShaderCode = shaderCode;
        }
    }
}
