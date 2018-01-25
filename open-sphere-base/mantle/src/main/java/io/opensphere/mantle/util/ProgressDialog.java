package io.opensphere.mantle.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import org.jdesktop.swingx.JXBusyLabel;

import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The Class ProgressDialog.
 */
@SuppressWarnings("PMD.GodClass")
public class ProgressDialog
{
    /** The busy label. */
    private JXBusyLabel myBusyLabel;

    /** The busy label panel. */
    private JPanel myBusyLabelPanel;

    /** The cancel button. */
    private JButton myCancelButton;

    /** The cancelled. */
    private volatile boolean myCancelled;

    /** The dialog. */
    private JDialog myDialog;

    /** The done. */
    private volatile boolean myDone;

    /** The message area. */
    private JTextArea myMessageArea;

    /** The note. */
    private JLabel myNote;

    /** The progress bar. */
    private JProgressBar myProgressBar;

    /**
     * A ProgressDialog that can show either determinate ( progress bar ), or
     * indeterminate ( spinner ) along with a message and note, optional cancel
     * button, modality etc.
     *
     * @param parentProvider - the parent window provider
     * @param title - the title of the dialog
     * @param modal - true if modal, false if not
     * @param isIndeterminate - true if indeterminate, false if not
     * @param showCancelButton - true to show cancel button, false if not
     * @param message - the message to display
     * @param totalSteps - the total number of steps for a determinate dialog.
     */
    public ProgressDialog(final Supplier<? extends Window> parentProvider, final String title, final boolean modal,
            final boolean isIndeterminate, final boolean showCancelButton, final String message, final int totalSteps)
    {
        EventQueueUtilities
                .runOnEDT(() -> init(parentProvider.get(), title, modal, isIndeterminate, showCancelButton, message, totalSteps));
    }

    /**
     * A ProgressDialog that can show either determinate ( progress bar ), or
     * indeterminate ( spinner ) along with a message and note, optional cancel
     * button, modality etc.
     *
     * @param parent - the parent window
     * @param title - the title of the dialog
     * @param modal - true if modal, false if not
     * @param isIndeterminate - true if indeterminate, false if not
     * @param showCancelButton - true to show cancel button, false if not
     * @param message - the message to display
     * @param totalSteps - the total number of steps for a determinate dialog.
     */
    public ProgressDialog(final Window parent, final String title, final boolean modal, final boolean isIndeterminate,
            final boolean showCancelButton, final String message, final int totalSteps)
    {
        EventQueueUtilities.runOnEDT(() -> init(parent, title, modal, isIndeterminate, showCancelButton, message, totalSteps));
    }

    /**
     * Adds an {@link ActionListener} to the cancel button of this dialog.
     *
     * @param listener the listener
     */
    public void addCancelListener(final ActionListener listener)
    {
        if (listener != null)
        {
            EventQueueUtilities.runOnEDT(() -> myCancelButton.addActionListener(listener));
        }
    }

    /**
     * Dispose.
     */
    public void dispose()
    {
        EventQueueUtilities.runOnEDT(() ->
            {
                myDone = true;
                myDialog.dispose();
            }
        );
    }

    /**
     * Returns true if the cancel button has been pressed.
     */
    public void done()
    {
        if (!myDone)
        {
            EventQueueUtilities.runOnEDT(() ->
                {
                    if (myDialog != null)
                    {
                        myDialog.setVisible(false);
                        myDialog.dispose();
                    }
                    myDone = true;
                }
            );
        }
    }

    /**
     * Gets the maximum value for the progress bar.
     *
     * @return the max step
     */
    public int getMaxStep()
    {
        return myProgressBar.getMaximum();
    }

    /**
     * Gets the minimum step for the progress bar.
     *
     * @return the min step
     */
    public int getMinStep()
    {
        return myProgressBar.getMinimum();
    }

    /**
     * Gets a reference to the internal progress bar in case the user needs to
     * manipulate it more than the basic interfaces of this class allows.
     *
     * @return {@link JProgressBar}
     */
    public JProgressBar getProgressBar()
    {
        return myProgressBar;
    }

    /**
     * Gets the current step for the progress bar.
     *
     * @return the step
     */
    public int getStep()
    {
        return myProgressBar == null ? 0 : myProgressBar.getValue();
    }

    /**
     * Increments the progress bar step.
     */
    public void incrementStep()
    {
        EventQueueUtilities.runOnEDT(() -> myProgressBar.setValue(myProgressBar.getValue() + 1));
    }

    /**
     * Returns true if the cancel button has been pressed.
     *
     * @return true, if is cancelled
     */
    public boolean isCancelled()
    {
        return myCancelled;
    }

    /**
     * Returns true if this ProgressDialog is in indeterminate mode ( i.e.
     * progress bar hidden, spinner showing and active. )
     *
     * @return true if in indeterminate mode.
     */
    public boolean isIndeterminate()
    {
        return myBusyLabel.isBusy();
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public boolean isVisible()
    {
        return myDialog.isVisible();
    }

    /**
     * Removes an {@link ActionListener} from the cancel button of this dialog.
     *
     * @param listener the listener
     */
    public void removeCancelListener(final ActionListener listener)
    {
        if (listener != null)
        {
            EventQueueUtilities.runOnEDT(() -> myCancelButton.removeActionListener(listener));
        }
    }

    /**
     * Sets this {@link ProgressDialog} to indeterminate mode which shows and
     * activates the spinner and hides the progress bar.
     *
     * @param indeterminate - true to be indeterminate, false if not
     */
    public void setIndeterminate(final boolean indeterminate)
    {
        EventQueueUtilities.runOnEDT(() ->
            {
                myBusyLabelPanel.setVisible(indeterminate);
                myBusyLabel.setBusy(indeterminate);
                myBusyLabel.setEnabled(indeterminate);
                myProgressBar.setVisible(!indeterminate);
            }
        );
    }

    /**
     * Sets the location relative to.
     *
     * @param c the new location relative to
     */
    public void setLocationRelativeTo(final Component c)
    {
        EventQueueUtilities.runOnEDT(() -> myDialog.setLocationRelativeTo(c));
    }

    /**
     * Sets the maximum step for the progress bar.
     *
     * @param max the new max step
     */
    public void setMaxStep(final int max)
    {
        EventQueueUtilities.runOnEDT(() -> myProgressBar.setMaximum(max));
    }

    /**
     * Sets the message for the {@link ProgressDialog}.
     *
     * @param messageText the new message
     */
    public void setMessage(final String messageText)
    {
        EventQueueUtilities.runOnEDT(() -> myMessageArea.setText(messageText));
    }

    /**
     * Sets the minimum step for the progress bar.
     *
     * @param min the new min step
     */
    public void setMinStep(final int min)
    {
        EventQueueUtilities.runOnEDT(() -> myProgressBar.setMinimum(min));
    }

    /**
     * Sets the note for the {@link ProgressDialog}.
     *
     * @param noteText the new note
     */
    public void setNote(final String noteText)
    {
        EventQueueUtilities.runOnEDT(() -> myNote.setText(noteText));
    }

    /**
     * Sets the resizable.
     *
     * @param resizable the new resizable
     */
    public void setResizable(final boolean resizable)
    {
        EventQueueUtilities.runOnEDT(() -> myDialog.setResizable(resizable));
    }

    /**
     * Sets the size.
     *
     * @param dimension the new size
     */
    public void setSize(final Dimension dimension)
    {
        EventQueueUtilities.runOnEDT(() -> myDialog.setSize(dimension));
    }

    /**
     * Sets the step of the progress bar to the specified value.
     *
     * @param step , step of total steps
     */
    public void setStep(final int step)
    {
        EventQueueUtilities.runOnEDT(() -> myProgressBar.setValue(step));
    }

    /**
     * Sets the visible.
     *
     * @param visible the new visible
     */
    public void setVisible(final boolean visible)
    {
        EventQueueUtilities.runOnEDT(() ->
            {
                if (visible)
                {
                    if (!myDone)
                    {
                        myDialog.setVisible(true);
                    }
                }
                else
                {
                    myDialog.setVisible(false);
                }
            }
        );
    }

    /**
     * Sets the visible after a delay.
     *
     * @param visible the new visible
     * @param milliseconds The delay in milliseconds.
     */
    public void setVisible(final boolean visible, final long milliseconds)
    {
        if (milliseconds <= 0)
        {
            setVisible(visible);
        }
        else
        {
            CommonTimer.schedule(() -> setVisible(true), milliseconds);
        }
    }

    /**
     * Initializes the progress dialog.
     *
     * @param parent the parent
     * @param title the title
     * @param modal the modal
     * @param isIndeterminate the is indeterminate
     * @param showCancelButton the show cancel button
     * @param message the message
     * @param totalSteps the total steps
     */
    protected void init(final Window parent, final String title, final boolean modal, final boolean isIndeterminate,
            final boolean showCancelButton, final String message, final int totalSteps)
    {
        myDialog = new JDialog(parent, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);

        myDialog.setSize(new Dimension(380, 200));

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        myBusyLabelPanel = new JPanel(new BorderLayout());
        myBusyLabelPanel.setBorder(BorderFactory.createLineBorder(Color.white));
        myBusyLabelPanel.setPreferredSize(new Dimension(80, 50));
        myBusyLabelPanel.setMinimumSize(new Dimension(80, 50));

        myBusyLabelPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        myBusyLabel = new JXBusyLabel(new Dimension(40, 40));
        myBusyLabel.setBusy(isIndeterminate);
        myBusyLabel.setEnabled(isIndeterminate);
        myBusyLabelPanel.add(myBusyLabel, BorderLayout.WEST);
        myBusyLabelPanel.setVisible(isIndeterminate);

        JPanel msgPanel = new JPanel(new BorderLayout());
        myMessageArea = new JTextArea(message);

        Font aFont = new Font(myMessageArea.getFont().getName(), Font.BOLD, myMessageArea.getFont().getSize() + 2);
        myMessageArea.setFont(aFont);

        myMessageArea.setEditable(false);
        myMessageArea.setBorder(BorderFactory.createEmptyBorder());
        myMessageArea.setBackground(msgPanel.getBackground());

        msgPanel.add(myMessageArea, BorderLayout.CENTER);

        JPanel noteAndBarPanel = new JPanel(new GridLayout(2, 1, 3, 3));

        myNote = new JLabel("");
        noteAndBarPanel.add(myNote);

        myProgressBar = new JProgressBar(0, totalSteps);
        noteAndBarPanel.add(myProgressBar);
        myProgressBar.setVisible(!isIndeterminate);

        msgPanel.add(noteAndBarPanel, BorderLayout.SOUTH);

        JPanel cancelButtonPanel = new JPanel(new BorderLayout());
        cancelButtonPanel.setBorder(BorderFactory.createEmptyBorder(15, 250, 0, 0));
        myCancelButton = new JButton("Cancel");
        myCancelButton.addActionListener(e -> cancelPerformed());

        cancelButtonPanel.add(myCancelButton, BorderLayout.CENTER);

        mainPanel.add(myBusyLabelPanel, BorderLayout.WEST);
        mainPanel.add(msgPanel, BorderLayout.CENTER);

        if (showCancelButton)
        {
            mainPanel.add(cancelButtonPanel, BorderLayout.SOUTH);
        }

        myDialog.setTitle(title);
        myDialog.setContentPane(mainPanel);
        myDialog.setLocationRelativeTo(parent);
    }

    /**
     * An event handler method invoked when a user clicks the cancel button.
     */
    protected void cancelPerformed()
    {
        myCancelled = true;
        setMessage("Cancelled By User...Please Wait");
        setNote("");
        setIndeterminate(true);
        myCancelButton.setVisible(false);
    }
}
