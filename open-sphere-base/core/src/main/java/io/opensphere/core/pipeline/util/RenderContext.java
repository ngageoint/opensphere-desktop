package io.opensphere.core.pipeline.util;

import java.util.Collection;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.Viewer2D;

/**
 * A context that contains the relevant information for rendering geometries.
 */
@SuppressWarnings("PMD.GodClass")
public class RenderContext
{
    /**
     * The render context for the current thread, which is {@code null} if there
     * is no GL context.
     */
    private static final ThreadLocal<RenderContext> ourCurrent = new ThreadLocal<>();

    /**
     * Flag indicating if {@link GL2#glBegin(int)} has been called more times
     * than {@link GL2#glEnd()}.
     */
    private boolean myBegun;

    /** The last color set. */
    private int myColor = -1;

    /**
     * The model coordinate origin of the processor associated with the
     * geometries currently being rendered.
     */
    private Vector3d myCurrentModelCenter = Vector3d.ORIGIN;

    /**
     * The depth mask controls whether new values are written to the depth
     * buffer. Depth buffer may still be enabled and existing values will be
     * used, but new values will not be written when the depth mask is false.
     */
    private boolean myDepthMask = true;

    /** The GL Context. */
    private final GL myGL;

    /** The GL version as a float. */
    private final float myGLVersion;

    /** The map context. */
    private final MapContext<?> myMapContext;

    /** The last point size set. */
    private float myPointSize;

    /** Flag indicating if point smoothing has been enabled. */
    private boolean myPointSmoothingEnabled;

    /** The current polygon offset factor. */
    private float myPolygonOffsetFactor;

    /** The current polygon offset units. */
    private float myPolygonOffsetUnits;

    /** The render mode. */
    private final AbstractGeometry.RenderMode myRenderMode;

    /** The shader renderer utilities. */
    private final ShaderRendererUtilities myShaderRendererUtilities;

    /** The time budget. */
    private final TimeBudget myTimeBudget;

    /**
     * Get the render context for the current thread, which is {@code null} if
     * there is no GL context.
     *
     * @return The render context.
     */
    public static RenderContext getCurrent()
    {
        return ourCurrent.get();
    }

    /**
     * Set the render context for the current thread.
     *
     * @param rc The render context.
     */
    public static void setCurrent(RenderContext rc)
    {
        ourCurrent.set(rc);
    }

    /**
     * Constructor.
     *
     * @param gl The GL context.
     * @param mapContext The map context.
     * @param renderMode The render mode.
     * @param shaderRendererUtilities The shader renderer utilities.
     * @param timeBudget The time budget.
     */
    public RenderContext(GL gl, MapContext<?> mapContext, RenderMode renderMode, ShaderRendererUtilities shaderRendererUtilities,
            TimeBudget timeBudget)
    {
        myGL = GLUtilities.isProduction() ? gl : new DebugGL2(gl.getGL2());
        myMapContext = mapContext;
        myRenderMode = renderMode;
        myShaderRendererUtilities = shaderRendererUtilities;
        myTimeBudget = timeBudget;
        myGLVersion = Utilities.parseSystemProperty("opensphere.pipeline.forceGLVersion",
                gl.getContext().getGLVersionNumber().getMajor() + (float)gl.getContext().getGLVersionNumber().getMinor() / 10);
    }

    /**
     * Check that the GL version is at least the given level.
     *
     * @param version The expected version.
     * @return {@code true} if the GL version is at least the expected version.
     */
    public boolean checkVersion(final float version)
    {
        return getGLVersion() >= version;
    }

    /**
     * Check that the GL version is at least the given level.
     *
     * @param version The expected version.
     * @param warningPrefix Prefix to be added to the warnings.
     * @param warnings If the version is not the given level, a warning is added
     *            to this optional collection.
     * @return {@code true} if the GL version is at least the expected version.
     */
    public boolean checkVersion(final float version, String warningPrefix, Collection<String> warnings)
    {
        if (checkVersion(version))
        {
            return true;
        }
        else
        {
            if (warnings != null)
            {
                warnings.add(warningPrefix + ": the GL version is " + getGLVersion() + " but " + version + " is required");
            }
            return false;
        }
    }

    /**
     * Create a new render context, using my values except for the provided
     * overrides.
     *
     * @param mapContext The override map contest.
     * @param renderMode The override render mode.
     * @return The render context.
     */
    public RenderContext derive(MapContext<?> mapContext, RenderMode renderMode)
    {
        return new RenderContext(getGL(), mapContext, renderMode, getShaderRendererUtilities(), getTimeBudget());
    }

    /**
     * Get the currentModelCenter.
     *
     * @return the currentModelCenter
     */
    public Vector3d getCurrentModelCenter()
    {
        return myCurrentModelCenter;
    }

    /**
     * Get a string that identifies the current graphics environment.
     *
     * @return The identifier.
     */
    public String getEnvironmentIdentifier()
    {
        return getGL().glGetString(GL.GL_VENDOR) + " " + getGL().getContext().getGLVersion();
    }

    /**
     * Accessor for the GL context.
     *
     * @return The GL context.
     */
    public final GL getGL()
    {
        return myGL;
    }

    /**
     * Accessor for the GL2 context.
     *
     * @return The GL2 context.
     */
    public GL2 getGL2()
    {
        return myGL.getGL2();
    }

    /**
     * Get the GL version as a float.
     *
     * @return The GL version.
     */
    public float getGLVersion()
    {
        return myGLVersion;
    }

    /**
     * Get the amount of GPU memory in bytes. For ATI cards this will return the
     * amount of free memory at the time of the call.
     *
     * @return The number of bytes of dedicated video memory.
     */
    public long getGPUMemorySizeBytes()
    {
        return GPUMemoryHelper.getGPUMemorySizeBytes(this);
    }

    /**
     * Accessor for the map context.
     *
     * @return The map context.
     */
    public final MapContext<?> getMapContext()
    {
        return myMapContext;
    }

    /**
     * Accessor for the render mode.
     *
     * @return The render mode.
     */
    public final AbstractGeometry.RenderMode getRenderMode()
    {
        return myRenderMode;
    }

    /**
     * Accessor for the shader renderer utilities.
     *
     * @return The utilities.
     */
    public final ShaderRendererUtilities getShaderRendererUtilities()
    {
        return myShaderRendererUtilities;
    }

    /**
     * Accessor for the time budget.
     *
     * @return The time budget.
     */
    public final TimeBudget getTimeBudget()
    {
        return myTimeBudget;
    }

    /**
     * Call to {@link GL2#glBegin(int)} and set my begun state.
     *
     * @param drawMode The draw mode.
     */
    public void glBegin(int drawMode)
    {
        if (!myBegun)
        {
            getGL().getGL2().glBegin(drawMode);
            myBegun = true;
        }
    }

    /**
     * Set the color.
     *
     * @param color The color as an ARGB packed int.
     */
    public void glColorARGB(int color)
    {
        if (myColor != color)
        {
            GL2Utilities.glColorARGB(getGL2(), color);
            myColor = color;
        }
    }

    /**
     * The depth mask controls whether new values are written to the depth
     * buffer. Depth buffer may still be enabled and existing values will be
     * used, but new values will not be written when the depth mask is false.
     *
     * @param mask the new mask value
     */
    public void glDepthMask(boolean mask)
    {
        if (mask != myDepthMask)
        {
            getGL().glDepthMask(mask);
            myDepthMask = mask;
        }
    }

    /**
     * Call to {@link GL2#glEnd} and clear my begun state.
     */
    public void glEnd()
    {
        if (myBegun)
        {
            getGL().getGL2().glEnd();
            myBegun = false;
        }
    }

    /**
     * Set the point size.
     *
     * @param size The point size.
     */
    public void glPointSize(float size)
    {
        if (myPointSize != size)
        {
            glEnd();
            getGL().getGL2().glPointSize(size);
            myPointSize = size;
        }
    }

    /**
     * Set the polygon offset if it is supported.
     *
     * @param factor The offset factor.
     * @param units The offset units.
     */
    public void glPolygonOffset(float factor, float units)
    {
        if (isPolygonOffsetSupported() && (myPolygonOffsetFactor != factor || myPolygonOffsetUnits != units))
        {
            getGL().glPolygonOffset(factor, units);
        }
    }

    /**
     * Get if GL version 1.1 is available.
     *
     * @return {@code true} if available.
     */
    public boolean is11Available()
    {
        final float version = 1.1f;
        return checkVersion(version);
    }

    /**
     * Get if GL version 1.1 is available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if available.
     */
    public boolean is11Available(String warningPrefix, Collection<String> warnings)
    {
        final float version = 1.1f;
        return checkVersion(version, warningPrefix, warnings);
    }

    /**
     * Get if GL version 1.2 is available.
     *
     * @return {@code true} if available.
     */
    public boolean is12Available()
    {
        final float version = 1.2f;
        return checkVersion(version);
    }

    /**
     * Get if GL version 1.2 is available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if available.
     */
    public boolean is12Available(String warningPrefix, Collection<String> warnings)
    {
        final float version = 1.2f;
        return checkVersion(version, warningPrefix, warnings);
    }

    /**
     * Get if GL version 1.3 is available.
     *
     * @return {@code true} if available.
     */
    public boolean is13Available()
    {
        final float version = 1.3f;
        return checkVersion(version);
    }

    /**
     * Get if GL version 1.3 is available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if available.
     */
    public boolean is13Available(String warningPrefix, Collection<String> warnings)
    {
        final float version = 1.3f;
        return checkVersion(version, warningPrefix, warnings);
    }

    /**
     * Get if GL version 1.4 is available.
     *
     * @return {@code true} if available.
     */
    public boolean is14Available()
    {
        final float version = 1.4f;
        return checkVersion(version);
    }

    /**
     * Get if GL version 1.4 is available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if available.
     */
    public boolean is14Available(String warningPrefix, Collection<String> warnings)
    {
        final float version = 1.4f;
        return checkVersion(version, warningPrefix, warnings);
    }

    /**
     * Get if GL version 1.5 is available.
     *
     * @return {@code true} if available.
     */
    public boolean is15Available()
    {
        final float version = 1.5f;
        return checkVersion(version);
    }

    /**
     * Get if GL version 1.5 is available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if available.
     */
    public boolean is15Available(String warningPrefix, Collection<String> warnings)
    {
        final float version = 1.5f;
        return checkVersion(version, warningPrefix, warnings);
    }

    /**
     * Get if GL version 2 is available.
     *
     * @return {@code true} if available.
     */
    public boolean is20Available()
    {
        final float version = 2f;
        return checkVersion(version);
    }

    /**
     * Get if GL version 2 is available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if available.
     */
    public boolean is20Available(String warningPrefix, Collection<String> warnings)
    {
        final float version = 2f;
        return checkVersion(version, warningPrefix, warnings);
    }

    /**
     * Get if {@link javax.media.opengl.GL2GL3#GL_CLAMP_TO_BORDER} is supported.
     *
     * @return {@code true} if the feature is supported.
     */
    public boolean isClampToBorderSupported()
    {
        return is13Available();
    }

    /**
     * Get if {@link javax.media.opengl.GL2GL3#GL_CLAMP_TO_BORDER} is supported.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if the feature is supported.
     */
    public boolean isClampToBorderSupported(String warningPrefix, Collection<String> warnings)
    {
        return is13Available(warningPrefix, warnings);
    }

    /**
     * Get if an OpenGL extension is available.
     *
     * @param extensionName The name of the extension.
     * @return {@code true} if the extension is available.
     */
    public boolean isExtensionAvailable(String extensionName)
    {
        return getGL().isExtensionAvailable(extensionName);
    }

    /**
     * Get if an OpenGL extension is available.
     *
     * @param extensionName The name of the extension.
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if the extension is available.
     */
    public boolean isExtensionAvailable(String extensionName, String warningPrefix, Collection<String> warnings)
    {
        if (isExtensionAvailable(extensionName))
        {
            return true;
        }
        else
        {
            if (warnings != null)
            {
                warnings.add(warningPrefix + ": " + extensionName + " is not available");
            }
            return false;
        }
    }

    /**
     * Get if the frame buffer object extension is available.
     *
     * @return {@code true} if FBOs are available.
     */
    public boolean isFBOAvailable()
    {
        return isFBOAvailable(Nulls.STRING, (Collection<String>)null);
    }

    /**
     * Get if the frame buffer object extension is available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if FBOs are available.
     */
    public boolean isFBOAvailable(String warningPrefix, Collection<String> warnings)
    {
        final Collection<String> tmpWarnings = New.collection(2);
        if (isExtensionAvailable("GL_EXT_framebuffer_object", warningPrefix, tmpWarnings)
                || isExtensionAvailable("GL_ARB_framebuffer_object", warningPrefix, tmpWarnings))
        {
            return true;
        }
        else
        {
            warnings.addAll(tmpWarnings);
            return false;
        }
    }

    /**
     * Get if multi-texturing is available.
     *
     * @return {@code true} if multi-texturing is available.
     */
    public boolean isMultiTextureAvailable()
    {
        return isMultiTextureAvailable((String)null, (Collection<String>)null);
    }

    /**
     * Get if multi-texturing is available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if multi-texturing is available.
     */
    public boolean isMultiTextureAvailable(String warningPrefix, Collection<String> warnings)
    {
        final Collection<String> tmpWarnings = New.collection(2);
        if (is14Available(warningPrefix, tmpWarnings) || isExtensionAvailable("GL_ARB_multitexture", warningPrefix, tmpWarnings))
        {
            return true;
        }
        else
        {
            warnings.addAll(tmpWarnings);
            return false;
        }
    }

    /**
     * Get if point sprites are available.
     *
     * @return {@code true} if point sprites are available.
     */
    public boolean isPointSpriteAvailable()
    {
        return isPointSpriteAvailable(Nulls.STRING, (Collection<String>)null);
    }

    /**
     * Get if point sprites are available.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if point sprites are available.
     */
    public boolean isPointSpriteAvailable(String warningPrefix, Collection<String> warnings)
    {
        final Collection<String> tmpWarnings = New.collection(2);
        if (is15Available(warningPrefix, tmpWarnings) || isExtensionAvailable("GL_OES_point_sprite", warningPrefix, tmpWarnings))
        {
            return true;
        }
        else
        {
            warnings.addAll(tmpWarnings);
            return false;
        }
    }

    /**
     * Get if {@link GL#glPolygonOffset(float, float)} is supported.
     *
     * @return {@code true} if polygon offsets are supported.
     */
    public boolean isPolygonOffsetSupported()
    {
        return isPolygonOffsetSupported((String)null, (Collection<String>)null);
    }

    /**
     * Get if {@link GL#glPolygonOffset(float, float)} is supported.
     *
     * @param warningPrefix A prefix to be added to the warnings.
     * @param warnings If the feature is not available, a warning is added to
     *            this optional collection.
     * @return {@code true} if polygon offsets are supported.
     */
    public boolean isPolygonOffsetSupported(String warningPrefix, Collection<String> warnings)
    {
        return is11Available(warningPrefix, warnings);
    }

    /**
     * Reset attributes to default state.
     */
    public void popAttributes()
    {
        myPointSize = 0f;
        myPointSmoothingEnabled = false;
        myColor = -1;
        glDepthMask(true);
    }

    /**
     * Replace the model-view and projections matrices. When required for the
     * projection, the model-view matrix will be adjusted to accommodate a
     * translated model center.
     *
     * @param positionType The position type is used to determine which matrices
     *            to push. For screen positions the matrices are retrieved from
     *            the screen viewer, otherwise the standard viewer is used.
     * @param projection The projection which is currently being used for
     *            rendering.
     */
    public void setModelViewAndProjection(Class<? extends Position> positionType, Projection projection)
    {
        final Viewer viewer = positionType == ScreenPosition.class ? getMapContext().getScreenViewer()
                : getMapContext().getStandardViewer();

        Matrix4d adjust = null;
        if (projection == null || projection.getModelViewAdjustment() == null || positionType == ScreenPosition.class
                || getMapContext().getStandardViewer() instanceof Viewer2D)
        {
            myCurrentModelCenter = Vector3d.ORIGIN;
        }
        else
        {
            myCurrentModelCenter = projection.getModelCenter();
            adjust = projection.getModelViewAdjustment();
        }

        final float[] projectionMatrix = viewer.getProjectionMatrixClipped(false);
        float[] modelViewMatrix;
        if (adjust == null)
        {
            modelViewMatrix = viewer.getModelViewMatrix();
        }
        else
        {
            modelViewMatrix = viewer.getAdjustedModelViewMatrix(adjust);
        }

        myGL.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        myGL.getGL2().glLoadMatrixf(modelViewMatrix, 0);

        myGL.getGL2().glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        myGL.getGL2().glLoadMatrixf(projectionMatrix, 0);
    }

    /**
     * Set point smoothing enabled.
     *
     * @param enable If point smoothing should be enabled.
     */
    public void setPointSmoothing(boolean enable)
    {
        if (enable)
        {
            if (!myPointSmoothingEnabled)
            {
                glEnd();
                getGL().getGL2ES1().glEnable(GL2ES1.GL_POINT_SMOOTH);
                myPointSmoothingEnabled = true;
            }
        }
        else
        {
            if (myPointSmoothingEnabled)
            {
                glEnd();
                getGL().getGL2ES1().glDisable(GL2ES1.GL_POINT_SMOOTH);
                myPointSmoothingEnabled = false;
            }
        }
    }
}
