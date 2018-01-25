package io.opensphere.hud.launcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.HUDFrame;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.hud.glswing.GLSwingEventManager;
import io.opensphere.hud.glswing.GLSwingInternalFrame;

/** Helper class for launching new HUD windows. */
public class HUDLauncher implements GenericSubscriber<HUDFrame>
{
    /** Mapping of JInternalFrame to the GLSwingInternalFrame which owns it. */
    private final Map<HUDFrame, GLSwingInternalFrame> myFrames = new HashMap<>();

    /** Helper class for handling publishing of geometries. */
    private final TransformerHelper myHelper;

    /** Tool box. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param helper Helper class for handling publishing of geometries.
     * @param toolbox Toolbox.
     */
    public HUDLauncher(TransformerHelper helper, Toolbox toolbox)
    {
        myHelper = helper;
        myToolbox = toolbox;
    }

    /**
     * Get the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Perform initialization which needs to take place after the transformer is
     * open.
     */
    public void init()
    {
        GLSwingEventManager.getInstance().init(myToolbox.getUIRegistry().getComponentRegistry());
        synchronized (myFrames)
        {
            List<HUDFrame> frames = myToolbox.getUIRegistry().getComponentRegistry().getObjects();
            myToolbox.getUIRegistry().getComponentRegistry().addSubscriber(this);

            for (HUDFrame frame : frames)
            {
                if (frame instanceof HUDJInternalFrame && myFrames.get(frame) == null)
                {
                    GLSwingInternalFrame glFrame = new GLSwingInternalFrame(myHelper, (HUDJInternalFrame)frame);
                    myFrames.put(frame, glFrame);
                }
            }
        }
    }

    @Override
    public void receiveObjects(Object source, Collection<? extends HUDFrame> adds, Collection<? extends HUDFrame> removes)
    {
        synchronized (myFrames)
        {
            for (HUDFrame frame : removes)
            {
                // In case the frame is being removed from the registry without
                // being closed, do all of the things we would do on a normal
                // close.
                GLSwingInternalFrame glFrame = myFrames.remove(frame);
                glFrame.handleFrameClosed();
            }

            for (HUDFrame frame : adds)
            {
                if (frame instanceof HUDJInternalFrame && myFrames.get(frame) == null)
                {
                    GLSwingInternalFrame glFrame = new GLSwingInternalFrame(myHelper, (HUDJInternalFrame)frame);
                    myFrames.put(frame, glFrame);
                }
            }
        }
    }
}
