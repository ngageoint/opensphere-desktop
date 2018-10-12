package io.opensphere.core.pipeline.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.geometry.renderproperties.DefaultFragmentShaderProperties;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadValidator;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/** Utilities methods for renderers which use GLSL shaders. */
public final class ShaderRendererUtilitiesGLSL extends AbstractShaderRendererUtilities
{
    /** The key for the built-in interval shading fragment program. */
    private static final String INTERVAL_SHADER_KEY = "BUILT_IN_INTERVAL_SHADER";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShaderRendererUtilitiesGLSL.class);

    /**
     * The currently loaded fragment tile shader function, or <code>null</code>.
     */
    private TileShader myCurrentTileShader;

    /** The shader programs which have been compiled and loaded. */
    private final Map<String, ShaderProgramGLSL> myShaderPrograms = New.map();

    /** Map of attribute name to vertex attribute index. */
    private final Map<String, Integer> myVertAttributeIndexMap = New.map();

    @Override
    public void cleanupShaders(GL gl)
    {
        assert ThreadValidator.isSingleThread(this);

        gl.getGL2().glUseProgram(0);
        myCurrentTileShader = null;
    }

    @Override
    public void clear(GL gl)
    {
        assert ThreadValidator.isSingleThread(this);

        cleanupShaders(gl);

        for (ShaderProgramGLSL shaderProg : myShaderPrograms.values())
        {
            gl.getGL2().glDeleteProgram(shaderProg.getShaderProgramRef());
        }
        myShaderPrograms.clear();
    }

    @Override
    public void enableShaderByName(GL gl, TileShader shader, FragmentShaderProperties shaderProps, TextureCoords textureCoords)
    {
        assert ThreadValidator.isSingleThread(this);

        // TODO limit the number of shaders which are actually loaded to the
        // card?
        String shaderKey;
        if (shader == TileShader.DRAW)
        {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(shader.toString()).append(System.identityHashCode(shaderProps.getShaderCode()));
            shaderKey = keyBuilder.toString();
        }
        else
        {
            shaderKey = shader.toString();
        }
        ShaderProgramGLSL shaderProg = myShaderPrograms.get(shaderKey);
        if (!shader.equals(myCurrentTileShader))
        {
            myCurrentTileShader = shader;
            if (shaderProg == null)
            {
                int shaderProgramRef = gl.getGL2().glCreateProgram();
                FragmentShaderProperties texCoordProps = new DefaultFragmentShaderProperties();
                shaderProg = new ShaderProgramGLSL(shaderProgramRef, texCoordProps);

                // Load main program
                String shaderName = getShaderFilename(shader);
                int fragShader = loadAndCompileShader(gl, GL2ES2.GL_FRAGMENT_SHADER, "/GLSL/" + shaderName);
                gl.getGL2().glAttachShader(shaderProgramRef, fragShader);
                GLUtilities.checkGLErrors(gl, LOGGER, "Load main " + shaderName + " Program");

                int fragShader2 = 0;
                if (shader == TileShader.DRAW)
                {
                    // Load the shader code that picks the color
                    fragShader2 = compileShader(gl, GL2ES2.GL_FRAGMENT_SHADER, shaderProps.getShaderCode());
                    gl.getGL2().glAttachShader(shaderProgramRef, fragShader2);
                    GLUtilities.checkGLErrors(gl, LOGGER, "Load fragment color determination Program");
                }

                gl.getGL2().glLinkProgram(shaderProgramRef);
                GLUtilities.checkGLErrors(gl, LOGGER, "Link " + shaderKey + " Program");

                gl.getGL2().glDeleteShader(fragShader);
                if (shader == TileShader.DRAW)
                {
                    gl.getGL2().glDeleteShader(fragShader2);
                }

                myShaderPrograms.put(shaderKey, shaderProg);
            }
            gl.getGL2().glUseProgram(shaderProg.getShaderProgramRef());
        }

        FragmentShaderProperties texCoordProps = shaderProg.getExtendedProps();
        ShaderPropertiesSet texCoordSet = new ShaderPropertiesSet();
        texCoordSet.setShaderCode("");

        Collection<Pair<String, float[]>> texCoordUnis = New.collection();
        texCoordUnis.add(new Pair<>("textureCoordLimits", convertCoordsToFloatArray(textureCoords)));
        texCoordSet.setFloatUniforms(texCoordUnis);

        texCoordProps.setupShader(texCoordSet);

        shaderProg.setUniforms(gl, shader == TileShader.DRAW ? shaderProps : (FragmentShaderProperties)null);
    }

    @Override
    public int getVertAttrIndex(String attributeName)
    {
        assert ThreadValidator.isSingleThread(this);

        Integer value = myVertAttributeIndexMap.get(attributeName);
        return value == null ? -1 : value.intValue();
    }

    @Override
    public void initIntervalFilter(GL gl, float fadeMin, float min, float max, float fadeMax, boolean pickEffect,
            boolean useTexture)
    {
        assert ThreadValidator.isSingleThread(this);

        if (fadeMin > min || min > max || max > fadeMax)
        {
            throw new IllegalArgumentException(
                    "Interval values are in the wrong order: " + fadeMin + " <= " + min + " <= " + max + " <= " + fadeMax);
        }

        String programKey = INTERVAL_SHADER_KEY + (pickEffect ? "_PICK" : useTexture ? "_DRAW" : "");
        ShaderProgramGLSL shaderProg = myShaderPrograms.get(programKey);
        if (shaderProg == null)
        {
            int intervalFilterProgram = gl.getGL2().glCreateProgram();
            FragmentShaderProperties intervalProps = new DefaultFragmentShaderProperties();
            shaderProg = new ShaderProgramGLSL(intervalFilterProgram, intervalProps);

            int vertShader = loadAndCompileShader(gl, GL2ES2.GL_VERTEX_SHADER, "/GLSL/IntervalFilterVert.glsl");
            int fragShader = loadAndCompileShader(gl, GL2ES2.GL_FRAGMENT_SHADER, pickEffect ? "/GLSL/IntervalFilterPickFrag.glsl"
                    : useTexture ? "/GLSL/IntervalFilterDrawFrag.glsl" : "/GLSL/IntervalFilterFrag.glsl");
            gl.getGL2().glAttachShader(intervalFilterProgram, vertShader);
            gl.getGL2().glAttachShader(intervalFilterProgram, fragShader);
            gl.getGL2().glLinkProgram(intervalFilterProgram);
            GLUtilities.checkGLErrors(gl, LOGGER, "Link Program");

            IntBuffer intbuf = IntBuffer.allocate(16);
            gl.getGL2().glGetProgramiv(intervalFilterProgram, GL2ES2.GL_LINK_STATUS, intbuf);
            int linkStatus = intbuf.get();
            if (LOGGER.isDebugEnabled() || linkStatus != GL.GL_TRUE)
            {
                // get log of link operation
                intbuf.rewind();
                gl.getGL2().glGetProgramiv(intervalFilterProgram, GL2ES2.GL_INFO_LOG_LENGTH, intbuf);
                intbuf.rewind();
                int logLength = intbuf.get();
                ByteBuffer bytebuf = ByteBuffer.allocate(logLength + 1);
                gl.getGL2().glGetProgramInfoLog(intervalFilterProgram, logLength, intbuf, bytebuf);
                bytebuf.rewind();
                String logText = new String(bytebuf.array(), StringUtilities.DEFAULT_CHARSET);
                LOGGER.log(linkStatus == GL.GL_TRUE ? Level.DEBUG : Level.ERROR, "Interval shader link log: " + logText);
            }

            int vertAttrIndex = gl.getGL2().glGetAttribLocation(intervalFilterProgram, "vertexInterval");
            if (vertAttrIndex == -1)
            {
                throw new IllegalStateException("No index found for vertexInterval attribute.");
            }
            myVertAttributeIndexMap.put(INTERVAL_FILTER_VERTEX_TIME_ATTRIBUTE_NAME, Integer.valueOf(vertAttrIndex));

            gl.getGL2().glDeleteShader(vertShader);
            gl.getGL2().glDeleteShader(fragShader);

            myShaderPrograms.put(programKey, shaderProg);
        }
        gl.getGL2().glUseProgram(shaderProg.getShaderProgramRef());

        // Setup the extended properties for the interval uniforms.
        FragmentShaderProperties intervalProps = shaderProg.getExtendedProps();
        ShaderPropertiesSet intervalSet = new ShaderPropertiesSet();
        intervalSet.setShaderCode("");

        Collection<Pair<String, float[]>> texCoordUnis = New.collection();
        texCoordUnis.add(new Pair<>("activeInterval", new float[] { fadeMin, min, max, fadeMax }));
        intervalSet.setFloatUniforms(texCoordUnis);

        intervalProps.setupShader(intervalSet);

        shaderProg.setUniforms(gl, (FragmentShaderProperties)null);
    }

    /**
     * Compile the shader program.
     *
     * @param gl The OpenGL context.
     * @param type The shader type ({@link GL2ES2#GL_VERTEX_SHADER} or
     *            {@link GL2ES2#GL_FRAGMENT_SHADER}).
     * @param code the shader code.
     * @return The shader id.
     * @throws GLException If the shader cannot be loaded and compiled.
     */
    private int compileShader(GL gl, int type, String code) throws GLException
    {
        int shaderId = gl.getGL2().glCreateShader(type);
        gl.getGL2().glShaderSource(shaderId, 1, new String[] { code }, new int[] { code.length() }, 0);

        gl.getGL2().glCompileShader(shaderId);
        GLUtilities.checkGLErrors(gl, LOGGER, "Compile Shader");

        IntBuffer intbuf = IntBuffer.allocate(16);
        gl.getGL2().glGetShaderiv(shaderId, GL2ES2.GL_COMPILE_STATUS, intbuf);
        int compileStatus = intbuf.get();
        if (LOGGER.isDebugEnabled() || compileStatus != GL.GL_TRUE)
        {
            // get log of compile operation
            intbuf.rewind();
            gl.getGL2().glGetShaderiv(shaderId, GL2ES2.GL_INFO_LOG_LENGTH, intbuf);
            intbuf.rewind();
            int logLength = intbuf.get();
            ByteBuffer bytebuf = ByteBuffer.allocate(logLength + 1);
            gl.getGL2().glGetShaderInfoLog(shaderId, logLength, intbuf, bytebuf);
            bytebuf.rewind();
            String logText = new String(bytebuf.array(), StringUtilities.DEFAULT_CHARSET);
            LOGGER.log(compileStatus == GL.GL_TRUE ? Level.DEBUG : Level.ERROR, "Shader compile log: " + logText);
        }

        return shaderId;
    }

    /**
     * Build the filename for the shader.
     *
     * @param shader The shader for which the filename is desired.
     * @return The filename.
     */
    private String getShaderFilename(TileShader shader)
    {
        StringBuilder builder = new StringBuilder(32).append("Tile");
        switch (shader)
        {
            case DEBUG:
                builder.append("Debug");
                break;
            case DEBUG_PROJECTION_COLOR:
                builder.append("SolidColor");
                break;
            case DRAW:
                builder.append("Draw");
                break;
            case DRAW_NO_BLEND:
                builder.append("DrawNoBlend");
                break;
            case PICK:
                builder.append("Pick");
                break;
            case PICK_ONLY:
                builder.append("PickOnly");
                break;
            default:
                throw new UnexpectedEnumException(shader);
        }
        builder.append("Frag.glsl");
        return builder.toString();
    }

    /**
     * Read the code from the specified resource and create a shader from it.
     *
     * @param gl The OpenGL context.
     * @param type The shader type ({@link GL2ES2#GL_VERTEX_SHADER} or
     *            {@link GL2ES2#GL_FRAGMENT_SHADER}).
     * @param resource The resource to read.
     * @return The shader id.
     * @throws GLException If the shader cannot be loaded and compiled.
     */
    private int loadAndCompileShader(GL gl, int type, String resource) throws GLException
    {
        String code;
        InputStream strm = ShaderRendererUtilitiesGLSL.class.getResourceAsStream(resource);
        try
        {
            code = new StreamReader(strm, 512, -1).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
        }
        catch (IOException e)
        {
            throw new GLException("Could not read shader code from [" + resource + "]: " + e, e);
        }
        finally
        {
            try
            {
                strm.close();
            }
            catch (IOException e)
            {
                LOGGER.warn("Exception closing stream: " + e, e);
            }
        }

        return compileShader(gl, type, code);
    }
}
