package io.opensphere.core.pipeline;

import java.awt.Canvas;

import com.jogamp.newt.opengl.GLWindow;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.newt.NewtKeyEventConverter;
import io.opensphere.core.control.newt.NewtMouseEventConverter;

/** Helper for handling NEWT events. */
public class NewtEventHelper
{
    /** The OpenGL window. */
    private final GLWindow myGLWindow;

    /** The converter for handling changing NEWT events to AWT events. */
    private final NewtKeyEventConverter myKeyConverter;

    /** The converter for handling changing NEWT events to AWT events. */
    private final NewtMouseEventConverter myMouseConverter;

    //    /** The listener for window related controls. */
    //    private final KeyListener myWindowControls;

    /**
     * Constructor.
     *
     * @param window The OpenGL window.
     * @param toolbox The toolbox.
     * @param canvas The OpenGL canvas.
     */
    public NewtEventHelper(GLWindow window, Toolbox toolbox, Canvas canvas)
    {
        myGLWindow = window;
        myMouseConverter = new NewtMouseEventConverter(toolbox, canvas);
        myKeyConverter = new NewtKeyEventConverter(toolbox, canvas);
        // TODO if we move all of the menu/toolbars onto the canvas, we should
        // be able to have full screen mode again.
        //        myWindowControls = new KeyAdapter()
        //        {
        //            @Override
        //            public void keyPressed(KeyEvent e)
        //            {
        //                if (e.getKeyCode() == KeyEvent.VK_F11)
        //                {
        //                    myGLWindow.setFullscreen(!myGLWindow.isFullscreen());
        //                }
        //            }
        //        };

        myGLWindow.addMouseListener(myMouseConverter);
        myGLWindow.addKeyListener(myKeyConverter);
        //        myGLWindow.addKeyListener(myWindowControls);
    }

    /** Cleanup the event listeners. */
    public void close()
    {
        myGLWindow.removeMouseListener(myMouseConverter);
        myGLWindow.removeKeyListener(myKeyConverter);
        //        myGLWindow.removeKeyListener(myWindowControls);
    }
}
