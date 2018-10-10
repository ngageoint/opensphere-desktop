package io.opensphere.core.dialog.alertviewer.toast;

import java.awt.Color;
import java.awt.Frame;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import javax.swing.Timer;

import org.apache.log4j.Logger;

import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** The controller for the toast message view. */
public class ToastController implements Runnable
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(ToastController.class);

    /**
     * The amount of time to show the toast message.
     */
    private static final int SHOW_LENGTH = 5000;

    /**
     * The amount to change the opacity.
     */
    private static final float OPACITY_DELTA = 0.05f;

    /**
     * The maximum opacity value.
     */
    private static final float MAX_OPACITY = .5f;

    /**
     * The parent frame provider.
     */
    private final Supplier<? extends Frame> myParentProvider;

    /**
     * Contains the pending toast messages to show.
     */
    private final LinkedBlockingQueue<ToastModel> myPendingMessages = new LinkedBlockingQueue<>(5);

    /**
     * Indicates if the toast message thread is running.
     */
    private boolean myIsRunning = true;

    /**
     * The toast message thread.
     */
    private final Thread myShowThread;

    /**
     * Constructs a new toast controller.
     *
     * @param parentProvider The parent frame provider.
     */
    public ToastController(Supplier<? extends Frame> parentProvider)
    {
        myParentProvider = parentProvider;
        myShowThread = new Thread(this, "Toast Thread");
        myShowThread.setDaemon(true);
        myShowThread.start();
    }

    @Override
    public void run()
    {
        while (myIsRunning)
        {
            try
            {
                final ToastModel model = myPendingMessages.take();
                if (model != null && myIsRunning)
                {
                    EventQueueUtilities.invokeLater(() -> showAndAnimate(new ToastView(myParentProvider.get(), model)));
                    Thread.sleep(SHOW_LENGTH + 1000);
                }
            }
            catch (InterruptedException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Shows the specified toast message,.
     *
     * @param message The message to show.
     * @param messageType The message type.
     */
    public void showToastMessage(String message, Type messageType)
    {
        ToastModel model = new ToastModel();
        model.setMessage(message);

        Color color = Color.blue;
        if (messageType == Type.ERROR)
        {
            color = Color.red;
        }
        else if (messageType == Type.WARNING)
        {
            color = Color.yellow;
        }

        model.setColor(color);

        myPendingMessages.offer(model);
    }

    /**
     * Stops the toast thread.
     */
    public void stop()
    {
        myIsRunning = false;
        ToastModel shutdownModel = new ToastModel();
        myPendingMessages.offer(shutdownModel);
    }

    /**
     * Shows the toast view with the message, fades the view in and out.
     *
     * @param view The toast view to show.
     */
    private void showAndAnimate(final ToastView view)
    {
        view.setVisible(true);
        final Timer startTimer = new Timer(100, e ->
        {
            float currentOpacity = view.getTheOpacity();
            if (currentOpacity < MAX_OPACITY)
            {
                currentOpacity += OPACITY_DELTA;

                if (currentOpacity > MAX_OPACITY)
                {
                    currentOpacity = MAX_OPACITY;
                }

                view.setTheOpacity(currentOpacity);
            }
        });
        startTimer.setRepeats(true);
        startTimer.start();

        Timer shutdownTimer = new Timer(SHOW_LENGTH, e ->
        {
            view.setVisible(false);
            view.dispose();
        });
        shutdownTimer.setRepeats(false);
        shutdownTimer.start();
    }
}
