package io.opensphere.core.pipeline.renderer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.media.opengl.GL;

import org.apache.log4j.Logger;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTFloatArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.lang.Pair;

/** A GLSL shader program and associated uniform variables. */
public class ShaderProgramGLSL
{
    /**
     * For <code>TobjectInHashMap</code> use this as the value which means
     * "no entry".
     */
    private static final int INT_NO_ENTRY_VALUE = -1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShaderProgramGLSL.class);

    /**
     * The extended properties which provided additional uniforms to set for
     * this program.
     */
    private final FragmentShaderProperties myExtendedProps;

    /** The last loaded values for the uniforms by name for float uniforms. */
    private final Map<String, float[]> myLastLoadedFloatUniforms = New.map();

    /**
     * The last loaded values for the uniforms by name for integer and boolean
     * uniforms.
     */
    private final Map<String, int[]> myLastLoadedIntUniforms = New.map();

    /** The OpenGL reference for the shader program. */
    private final int myShaderProgramRef;

    /** The uniform locations for each uniform. */
    private final TObjectIntMap<String> myUniformLocations = new TObjectIntHashMap<>(4, 0.5f, INT_NO_ENTRY_VALUE);

    /**
     * Constructor.
     *
     * @param shaderProgramRef The OpenGL reference for the shader program.
     * @param extendedProps The extended properties which provided additional
     *            uniforms to set for this program.
     */
    public ShaderProgramGLSL(int shaderProgramRef, FragmentShaderProperties extendedProps)
    {
        myShaderProgramRef = shaderProgramRef;
        myExtendedProps = extendedProps;
    }

    /**
     * Get the extendedProps.
     *
     * @return the extendedProps
     */
    public FragmentShaderProperties getExtendedProps()
    {
        return myExtendedProps;
    }

    /**
     * Get the shaderProgramRef.
     *
     * @return the shaderProgramRef
     */
    public int getShaderProgramRef()
    {
        return myShaderProgramRef;
    }

    /**
     * Get the uniformLocations.
     *
     * @return the uniformLocations
     */
    public TObjectIntMap<String> getUniformLocations()
    {
        return myUniformLocations;
    }

    /**
     * Set the values for the uniforms in the given shader properties. The
     * extended properties are also set with this call.
     *
     * @param gl The OpenGL context.
     * @param properties The properties for which the uniform values should be
     *            set or <code>null</code> to set only the extended properties
     *            uniform values.
     */
    public void setUniforms(GL gl, FragmentShaderProperties properties)
    {
        doSetUniforms(gl, myExtendedProps);
        doSetUniforms(gl, properties);
    }

    /**
     * A convenience method for doing the actual load of integer type uniforms
     * (booleans and integers).
     *
     * @param gl The OpenGL context.
     * @param uniforms The uniforms to load.
     */
    private void doLoadUniformsForIntegerType(GL gl, Collection<? extends Pair<String, PetrifyableTIntArrayList>> uniforms)
    {
        for (Pair<String, PetrifyableTIntArrayList> uniform : uniforms)
        {
            PetrifyableTIntArrayList uniformValues = uniform.getSecondObject();
            int[] valArray = uniformValues.toArray();
            int[] lastLoaded = myLastLoadedIntUniforms.get(uniform.getFirstObject());
            if (lastLoaded == null || !Arrays.equals(valArray, lastLoaded))
            {
                int uniformLoc = getUniformLocation(gl, uniform.getFirstObject());
                switch (uniform.getSecondObject().size())
                {
                    case 1:
                        gl.getGL2().glUniform1i(uniformLoc, valArray[0]);
                        break;
                    case 2:
                        gl.getGL2().glUniform2i(uniformLoc, valArray[0], valArray[1]);
                        break;
                    case 3:
                        gl.getGL2().glUniform3i(uniformLoc, valArray[0], valArray[1], valArray[2]);
                        break;
                    case 4:
                        gl.getGL2().glUniform4i(uniformLoc, valArray[0], valArray[1], valArray[2], valArray[3]);
                        break;
                    default:
                        LOGGER.error("Too many values for shader uniform.");
                        break;
                }
                myLastLoadedIntUniforms.put(uniform.getFirstObject(), valArray);
            }
        }
    }

    /**
     * Set the values for the uniforms in the given shader properties.
     *
     * @param gl The OpenGL context.
     * @param properties The properties for which the uniform values should be
     *            set.
     */
    private void doSetUniforms(GL gl, FragmentShaderProperties properties)
    {
        if (properties != null)
        {
            setIntegerUniforms(gl, properties);
            setFloatUniforms(gl, properties);
            setBooleanUniforms(gl, properties);
        }
    }

    /**
     * Get the uniform location. If it is for a previously unknown uniform,
     * cache the location. Otherwise, retrieve the know value.
     *
     * @param gl The OpenGL context.
     * @param uniformName The name of the uniform variable.
     * @return The uniform location.
     */
    private int getUniformLocation(GL gl, String uniformName)
    {
        int uniformLoc = myUniformLocations.get(uniformName);
        if (uniformLoc == INT_NO_ENTRY_VALUE)
        {
            uniformLoc = gl.getGL2().glGetUniformLocation(myShaderProgramRef, uniformName);
            myUniformLocations.put(uniformName, uniformLoc);
        }

        return uniformLoc;
    }

    /**
     * Set the specified uniform variables to the correct value before shader
     * execution.
     *
     * @param gl The OpenGL context.
     * @param shaderProps The properties whose values should be set.
     */
    private void setBooleanUniforms(GL gl, FragmentShaderProperties shaderProps)
    {
        doLoadUniformsForIntegerType(gl, shaderProps.getBooleanUniforms());
    }

    /**
     * Set the specified uniform variables to the correct value before shader
     * execution.
     *
     * @param gl The OpenGL context.
     * @param shaderProps The properties whose values should be set.
     */
    private void setFloatUniforms(GL gl, FragmentShaderProperties shaderProps)
    {
        for (Pair<String, PetrifyableTFloatArrayList> uniform : shaderProps.getFloatUniforms())
        {
            PetrifyableTFloatArrayList uniformValues = uniform.getSecondObject();
            float[] valArray = uniformValues.toArray();
            float[] lastLoaded = myLastLoadedFloatUniforms.get(uniform.getFirstObject());
            if (lastLoaded == null || !Arrays.equals(valArray, lastLoaded))
            {
                int uniformLoc = getUniformLocation(gl, uniform.getFirstObject());
                switch (uniform.getSecondObject().size())
                {
                    case 1:
                        gl.getGL2().glUniform1f(uniformLoc, valArray[0]);
                        break;
                    case 2:
                        gl.getGL2().glUniform2f(uniformLoc, valArray[0], valArray[1]);
                        break;
                    case 3:
                        gl.getGL2().glUniform3f(uniformLoc, valArray[0], valArray[1], valArray[2]);
                        break;
                    case 4:
                        gl.getGL2().glUniform4f(uniformLoc, valArray[0], valArray[1], valArray[2], valArray[3]);
                        break;
                    default:
                        LOGGER.error("Too many values for shader uniform.");
                        break;
                }
                myLastLoadedFloatUniforms.put(uniform.getFirstObject(), valArray);
            }
        }
    }

    /**
     * Set the specified uniform variables to the correct value before shader
     * execution.
     *
     * @param gl The OpenGL context.
     * @param shaderProps The properties whose values should be set.
     */
    private void setIntegerUniforms(GL gl, FragmentShaderProperties shaderProps)
    {
        doLoadUniformsForIntegerType(gl, shaderProps.getIntegerUniforms());
    }
}
