package io.opensphere.core.pipeline;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * A timer that repaints a component periodically.
 */
public class RepaintTimer extends Timer
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the repaint timer.
     *
     * @param periodMillis How often in milliseconds to repaint.
     * @param component The component to be repainted.
     */
    public RepaintTimer(int periodMillis, final Component component)
    {
        super(periodMillis, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                component.repaint();
            }
        });
    }
}
