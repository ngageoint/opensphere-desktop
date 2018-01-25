package io.opensphere.controlpanels.timeline;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingConstants;
import javax.swing.Timer;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.awt.AWTUtilities;

/**
 * A layer that displays a message for a few seconds.
 */
public class TemporaryMessageLayer extends ContextLabel
{
    /** How long to display messages. */
    private static final int DISPLAY_TIME_MILLIS = 3000;

    /** The message. */
    private final ObservableValue<String> myMessage;

    /** Message change listener. */
    private final ChangeListener<String> myMessageChangeListener = new ChangeListener<String>()
    {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
        {
            if (myTimer != null)
            {
                myTimer.stop();
            }

            if (myMessage.get() != null)
            {
                myStartTime = System.currentTimeMillis();
                startTimer();
            }
        }
    };

    /** The start time of showing the latest message. */
    private long myStartTime;

    /** The timer used to fade out the message. */
    private Timer myTimer;

    /**
     * Constructor.
     *
     * @param message The observable message.
     */
    public TemporaryMessageLayer(ObservableValue<String> message)
    {
        super(new Font(Font.DIALOG, Font.BOLD, 12));
        myMessage = message;
        myMessage.addListener(myMessageChangeListener);
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        String message = myMessage.get();
        if (message != null)
        {
            int labelX = AWTUtilities.getTextXLocation(message, (int)getUIModel().getTimelinePanelBounds().getCenterX(), 0,
                    SwingConstants.CENTER, g2d);
            int labelY = (int)getUIModel().getTimelinePanelBounds().getCenterY() - 2;
            float alpha = (myStartTime + DISPLAY_TIME_MILLIS - System.currentTimeMillis()) / (float)DISPLAY_TIME_MILLIS;
            if (alpha > 0f)
            {
                update(message, labelX, labelY, alpha);
                super.paint(g2d);
            }
        }
    }

    /**
     * Create the timer.
     */
    private void startTimer()
    {
        myTimer = new Timer(100, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (System.currentTimeMillis() > myStartTime + DISPLAY_TIME_MILLIS)
                {
                    myTimer.stop();
                    myTimer = null;
                    myMessage.set(null);
                }
                else
                {
                    getUIModel().repaint();
                }
            }
        });
        myTimer.start();
    }
}
