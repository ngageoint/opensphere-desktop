package io.opensphere.core.pipeline;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.awt.GLCanvas;

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
    private final float myDPIScale;

    /**
     * Constructor.
     * @param capsReqUser GL capabilities.
     */
    public PipelineGLCanvas(GLCapabilitiesImmutable capsReqUser)
    {
        super(capsReqUser);
        double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int gdHeight = gd.getDisplayMode().getHeight();
        myDPIScale = (float)(gdHeight /  screenHeight);
    }

    @Override
    public int getWidth()
    {
        return (int)(super.getWidth() * myDPIScale);
    }

    @Override
    public int getHeight()
    {
        return (int)(super.getHeight() * myDPIScale);
    }
}
