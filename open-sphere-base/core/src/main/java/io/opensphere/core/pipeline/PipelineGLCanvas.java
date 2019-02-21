package io.opensphere.core.pipeline;

import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.awt.GLCanvas;

import io.opensphere.core.control.ui.UIRegistry;

/**
 * A {@link GLCanvas} that takes into account highDpi screens with old man eye scaling enabled.
 */
public class PipelineGLCanvas extends GLCanvas
{
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The scaling percentage.
     */
    private final ScaleDetector myDPIScale;

    /**
     * Constructor.
     * @param capsReqUser GL capabilities.
     * @param uiRegistry Used to figure out scaling settings for monitors.
     */
    public PipelineGLCanvas(GLCapabilitiesImmutable capsReqUser, UIRegistry uiRegistry)
    {
        super(capsReqUser);
        myDPIScale = new ScaleDetector(uiRegistry);
    }

    @Override
    public int getWidth()
    {
        return (int)(super.getWidth() * myDPIScale.getScale());
    }

    @Override
    public int getHeight()
    {
        return (int)(super.getHeight() * myDPIScale.getScale());
    }
}
