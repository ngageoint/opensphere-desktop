package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.TimeBudget;

/**
 * A dialog used to display a message to the user. The message may include
 * details on a second pane, and may automatically hide itself if desired by the
 * user.
 */
public class AutohideMessageDialog extends ChoiceMessageDialog
{
    /** The default delay. */
    private static final int DEFAULT_DELAY_MILLISECONDS = 5000;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The hide checkbox. */
    private JCheckBox myHideCheckbox;

    /** Used to save the auto-hide preference, if not {@code null}. */
    private Preferences myPrefs;

    /** The key for the preferences. */
    private String myPrefsKey;

    /** The timer used to hide the dialog. */
    private Timer myTimer;

    /** The remaining time to show the dialog. */
    private TimeBudget myTimeBudget;

    /** How many milliseconds to show the dialog. */
    private int myHideDelayMilliseconds = DEFAULT_DELAY_MILLISECONDS;

    /**
     * Get the window to use for the dialog parent.
     *
     * @param component A component in the window, or the window itself.
     * @return The window.
     */
    private static Window getWindow(Component component)
    {
        return component instanceof Window || component == null ? (Window)component : SwingUtilities.getWindowAncestor(component);
    }

    /**
     * Constructor.
     */
    public AutohideMessageDialog()
    {
        this((Window)null, ModalityType.MODELESS);
    }

    /**
     * Constructor.
     *
     * @param component The owner of the dialog.
     * @param modalityType The modality for the dialog.
     */
    public AutohideMessageDialog(Component component, ModalityType modalityType)
    {
        super(getWindow(component), modalityType);
    }

    /**
     * Constructor.
     *
     * @param modalityType The modality for the dialog.
     */
    public AutohideMessageDialog(ModalityType modalityType)
    {
        this((Window)null, modalityType);
    }

    /**
     * Initialize the components.
     *
     * @param summaryPanel The summary panel.
     * @param detailsPanel The details panel, may be {@code null}.
     * @param prefs The optional preferences.
     * @param prefsKey The key to be used with the preferences.
     * @param buttonLabels The labels for the buttons.
     */
    public void initialize(Component summaryPanel, Component detailsPanel, Preferences prefs, String prefsKey,
            String... buttonLabels)
    {
        setPreferences(prefs, prefsKey);
        super.initialize(summaryPanel, detailsPanel, buttonLabels);

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                pack();
                if (myHideCheckbox.isSelected())
                {
                    startTimer();
                }
            }
        });
    }

    /**
     * Initialize the components.
     *
     * @param summary The summary text (for a JLabel).
     * @param details The detail text (for a JTextArea), may be {@code null}.
     * @param prefs The optional preferences.
     * @param prefsKey The key to be used with the preferences.
     */
    public void initialize(String summary, String details, Preferences prefs, String prefsKey)
    {
        setPreferences(prefs, prefsKey);
        super.initialize(summary, details);
    }

    /**
     * Initialize the components.
     *
     * @param summary The summary text (for a JLabel).
     * @param details The detail text (for a JTextArea), may be {@code null}.
     * @param prefs The optional preferences.
     * @param prefsKey The key to be used with the preferences.
     * @param buttonLabels The labels for the buttons.
     */
    public void initialize(String summary, String details, Preferences prefs, String prefsKey, String... buttonLabels)
    {
        setPreferences(prefs, prefsKey);
        super.initialize(summary, details, buttonLabels);
    }

    /**
     * Set the delay before the dialog is hidden. The default is three seconds.
     *
     * @param delay The delay in milliseconds.
     */
    public void setHideDelayMilliseconds(int delay)
    {
        myHideDelayMilliseconds = delay;
    }

    @Override
    protected JPanel createButtonPanel(String... buttonLabels)
    {
        JPanel buttonPanel = super.createButtonPanel(buttonLabels);
        buttonPanel.add(createHideCheckbox(myPrefs, myPrefsKey));

        return buttonPanel;
    }

    /**
     * Create the "hide" checkbox.
     *
     * @param prefs The optional preferences.
     * @param prefsKey The preferences key.
     * @return The checkbox.
     */
    private JComponent createHideCheckbox(final Preferences prefs, final String prefsKey)
    {
        boolean initialValue = prefs != null && prefs.getBoolean(prefsKey, false);
        myHideCheckbox = new JCheckBox("", initialValue);
        myHideCheckbox.setHorizontalAlignment(SwingConstants.RIGHT);
        myHideCheckbox.setFocusPainted(false);
        setCheckboxText();
        myTimer = new Timer(0, e ->
        {
            if (myTimeBudget.isExpired())
            {
                myTimer.stop();
                dispatchEvent(new WindowEvent(AutohideMessageDialog.this, WindowEvent.WINDOW_CLOSING));
            }
            else
            {
                setCheckboxText();
                myTimer.setDelay(Math.min(myTimeBudget.getRemainingMilliseconds(), Constants.MILLI_PER_UNIT));
            }
        });
        myHideCheckbox.addChangeListener(e ->
        {
            if (prefs != null)
            {
                prefs.putBoolean(prefsKey, ((JCheckBox)e.getSource()).isSelected(), AutohideMessageDialog.this);
            }
            if (((JCheckBox)e.getSource()).isSelected())
            {
                startTimer();
            }
            else
            {
                myTimeBudget = null;
                setCheckboxText();
                myTimer.stop();
            }
        });
        return myHideCheckbox;
    }

    /**
     * Set the checkbox text based on the time remaining.
     */
    private void setCheckboxText()
    {
        if (myTimeBudget == null)
        {
            myHideCheckbox.setText("Hide message automatically          ");
        }
        else
        {
            myHideCheckbox.setText("Hide message automatically in "
                    + Math.round((float)myTimeBudget.getRemainingMilliseconds() / Constants.MILLI_PER_UNIT));
        }
    }

    /**
     * Set the preferences to be used for the auto-hide setting.
     *
     * @param prefs The preferences.
     * @param prefsKey The key to be used with the preferences.
     */
    private void setPreferences(Preferences prefs, String prefsKey)
    {
        myPrefs = prefs;
        myPrefsKey = prefsKey;
    }

    /** Start the timer and time budget. */
    private void startTimer()
    {
        myTimeBudget = TimeBudget.startMilliseconds(myHideDelayMilliseconds);
        myTimer.setInitialDelay(Math.min(myHideDelayMilliseconds, Constants.MILLI_PER_UNIT));
        myTimer.start();
        setCheckboxText();
    }
}
