package io.opensphere.core.dialog.alertviewer;

import java.awt.Color;

import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.AlertNotificationButton;
import io.opensphere.core.util.swing.EventQueueUtilities;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/** The alert viewer button. */
class AlertViewerButton extends AlertNotificationButton
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The dialog. */
    @ThreadConfined("EDT")
    private AlertViewerDialog myDialog;

    /** The alerts. */
    @ThreadConfined("JavaFX")
    private final ObservableList<Alert> myAlerts;

    /** The maximum message level since the message count was reset. */
    private volatile Type myMaxLevel = Type.INFO;

    /**
     * Instantiates a new alert viewer alert button.
     *
     * @param toolbox the toolbox
     * @param alerts The alerts
     */
    public AlertViewerButton(Toolbox toolbox, ObservableList<Alert> alerts)
    {
        IconUtil.setIcons(this, IconType.NOTIFICATION);
        setFocusPainted(false);
        setToolTipText(AlertViewerDialog.TITLE);

        myToolbox = toolbox;
        myAlerts = alerts;

        addActionListener(e -> handleButtonClick());
        Platform.runLater(() -> myAlerts.addListener(this::handleAlertsChange));
    }

    @Override
    @ThreadConfined("EDT")
    public final void setAlertCount(int cnt)
    {
        setCount(cnt);
        setAlertCounterText(Integer.toString(cnt));
        repaint();
    }

    /**
     * Gets the dialog.
     *
     * @return the dialog
     */
    @ThreadConfined("EDT")
    final AlertViewerDialog getDialog()
    {
        if (myDialog == null)
        {
            myDialog = new AlertViewerDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), myAlerts);
        }
        return myDialog;
    }

    /** Handles a button click. */
    @ThreadConfined("EDT")
    private void handleButtonClick()
    {
        reset();

        AlertViewerDialog dialog = getDialog();
        dialog.setVisible(!dialog.isVisible());
    }

    /**
     * Handles a change in the list of alerts.
     *
     * @param change an object representing the change that was done
     */
    @ThreadConfined("JavaFX")
    private void handleAlertsChange(ListChangeListener.Change<? extends Alert> change)
    {
        if (change.next() && change.wasAdded())
        {
            boolean makeVisible = false;
            for (Alert alert : change.getAddedSubList())
            {
                if (alert.getLevel().ordinal() > myMaxLevel.ordinal())
                {
                    myMaxLevel = alert.getLevel();
                }
                makeVisible |= alert.isMakeVisible();
            }

            final int newMessageCount = change.getAddedSize();
            final int alertCount = myAlerts.size();
            final boolean finalMakeVisible = makeVisible;
            EventQueueUtilities.invokeLater(() -> update(newMessageCount, alertCount, finalMakeVisible));
        }
    }

    /**
     * Updates the button.
     *
     * @param newMessageCount the new message count
     * @param alertCount the total alert count
     * @param makeVisible whether to make the dialog visible
     */
    @ThreadConfined("EDT")
    private void update(int newMessageCount, int alertCount, boolean makeVisible)
    {
        if (!getDialog().isVisible())
        {
            setAlertCount(Math.min(getCount() + newMessageCount, alertCount));
        }
        else
        {
            reset();
        }
        setAlertColor(getColor(myMaxLevel));

        if (makeVisible)
        {
            reset();
            getDialog().setVisible(true);
        }
    }

    /** Resets. */
    @ThreadConfined("EDT")
    private void reset()
    {
        setAlertCount(0);
        myMaxLevel = Type.INFO;
    }

    /**
     * Gets the text from the results.
     *
     * @param level the level
     * @return the text
     */
    private static Color getColor(Type level)
    {
        Color color;
        switch (level)
        {
            case ERROR:
                color = Color.RED;
                break;
            case WARNING:
                color = Color.YELLOW;
                break;
            default:
                color = Color.WHITE;
                break;
        }
        return color;
    }
}
