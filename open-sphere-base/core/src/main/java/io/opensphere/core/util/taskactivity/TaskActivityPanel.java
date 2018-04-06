package io.opensphere.core.util.taskactivity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import io.opensphere.core.util.concurrent.CatchingRunnable;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.swing.EventQueueUtilities;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 * A JPanel that displays a scrolling marquis (when necessary) or a combination
 * of different currently active busy tasks.
 */
public class TaskActivityPanel extends JPanel
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The busy icon. */
    private ImageIcon myBusyIcon;

    /** The dialog showing progress. */
    @GuardedBy("this")
    private ProgressManagerDialog myDialog;

    /** The button that brings up the dialog. */
    private JButton myDialogButton;

    /** The executor used to rebuild the label. */
    private final ScheduledExecutorService myExecutor = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("TaskActivityPanel"));

    /** The future tracking the task to rebuild the label. */
    @GuardedBy("this")
    private ScheduledFuture<?> myFuture;

    /** Flag indicating if the label is invalid and should be rebuilt. */
    @GuardedBy("this")
    private boolean myInvalid;

    /** The last label value. */
    private String myLastLabelValue;

    /** The last sub index of the label. */
    private int myLastSubIndex;

    /** Listener to be notified whenever the task activities change. */
    private final InvalidationListener myListener = new InvalidationListener()
    {
        @Override
        public void invalidated(Observable b)
        {
            ProgressManagerDialog dialog;
            synchronized (TaskActivityPanel.this)
            {
                myInvalid = true;

                if (myFuture == null)
                {
                    myFuture = myExecutor.scheduleAtFixedRate(new CatchingRunnable(TaskActivityPanel.this::rebuildLabel), 100,
                            300, TimeUnit.MILLISECONDS);
                }
                dialog = myDialog;
            }
            if (dialog != null)
            {
                dialog.refresh();
            }
        }
    };

    /** The list of TaskActivity. */
    private Deque<TaskActivity> myTaskActivities;

    /** The current total message being displayed. */
    private JLabel myTaskLabel;

    /**
     * Constructor.
     */
    public TaskActivityPanel()
    {
        super(new BorderLayout());
        buildPanel();
    }

    /**
     * Adds an activity to be added to the currently busy activities.
     *
     * @param ta - the {@link TaskActivity} to add.
     */
    public void addTaskActivity(TaskActivity ta)
    {
        synchronized (myTaskActivities)
        {
            myTaskActivities.add(ta);
        }

        ProgressManagerDialog dialog;
        synchronized (this)
        {
            dialog = myDialog;
        }
        if (dialog != null)
        {
            dialog.addTask(ta);
        }

        ta.activeProperty().addListener(myListener);
        ta.completeProperty().addListener(myListener);
        ta.labelProperty().addListener(myListener);

        Platform.startup(() -> myListener.invalidated(null));
    }

    /**
     * Removes a {@link TaskActivity} from the currently active list of
     * activities.
     *
     * @param ta - the {@link TaskActivity} to remove
     */
    public void removeTaskActivity(TaskActivity ta)
    {
        synchronized (myTaskActivities)
        {
            myTaskActivities.remove(ta);
            if (myDialog != null)
            {
                myDialog.removeTask(ta);
            }
        }
    }

    /**
     * Show the dialog.
     */
    protected final void showDialog()
    {
        synchronized (myTaskActivities)
        {
            if (myDialog == null)
            {
                Window window = SwingUtilities.getWindowAncestor(this);
                myDialog = new ProgressManagerDialog(window);
                myDialog.addTasks(myTaskActivities);
            }
            myDialog.setVisible(true);
        }
    }

    /**
     * True to show the label and spinner, false to not display.
     *
     * @param show - true to show, false to hide
     */
    protected void showStuff(boolean show)
    {
        assert EventQueue.isDispatchThread();
        myTaskLabel.setVisible(show);
        myDialogButton.setIcon(show ? myBusyIcon : null);
    }

    /**
     * Builds the main panel contents.
     */
    private void buildPanel()
    {
        setOpaque(false);
        myTaskActivities = new LinkedList<>();
        myTaskLabel = new JLabel("Downloads Queued 0");
        myTaskLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        myTaskLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        myBusyIcon = new ImageIcon(TaskActivityPanel.class.getResource("/images/busy.gif"));
        myDialogButton = new JButton();
        myBusyIcon.setImageObserver(myDialogButton);
        myDialogButton.setPreferredSize(new Dimension(myBusyIcon.getIconWidth() + 5, myBusyIcon.getIconHeight() + 5));
        myDialogButton.addActionListener(e -> showDialog());
        myDialogButton.setFocusPainted(false);
        myDialogButton.setBorderPainted(false);
        setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));

        add(myTaskLabel, BorderLayout.CENTER);
        add(myDialogButton, BorderLayout.EAST);
        myTaskLabel.setVisible(false);
    }

    /**
     * Ignore serialization.
     *
     * @param in in
     */
    private void readObject(java.io.ObjectInputStream in)
    {
    }

    /**
     * Rebuild the label text based on the current states of the task
     * activities.
     */
    private void rebuildLabel()
    {
        synchronized (this)
        {
            if (!myInvalid)
            {
                if (myFuture != null)
                {
                    myFuture.cancel(false);
                    myFuture = null;
                }
                return;
            }
            myInvalid = false;
        }

        int numActive = 0;
        StringBuilder sb = new StringBuilder();
        synchronized (myTaskActivities)
        {
            for (Iterator<TaskActivity> listItr = myTaskActivities.descendingIterator(); listItr.hasNext();)
            {
                TaskActivity curTask = listItr.next();
                if (curTask.isComplete())
                {
                    listItr.remove();
                    if (myDialog != null)
                    {
                        myDialog.removeTask(curTask);
                    }
                }
                else if (curTask.isActive())
                {
                    numActive++;
                    sb.append(curTask.getLabelValue());
                    sb.append(" : ");
                }
            }
        }
        // Remove the last " : "
        if (sb.length() > 0)
        {
            sb.setLength(sb.length() - 3);
        }

        setToolTipText(new StringBuilder(sb.length() + 13).append("<html>").append(sb.toString().replace(" : ", "<br>"))
                .append("</html>").toString());
        final int numActiveFinal = numActive;

        truncate(sb);

        final String label = sb.toString();

        if (!EqualsHelper.equals(label, myLastLabelValue))
        {
            myLastLabelValue = label;
            EventQueueUtilities.invokeLater(() ->
            {
                myTaskLabel.setText(label);
                showStuff(numActiveFinal > 0);
            });
        }
    }

    /**
     * Truncate the label to 80 columns.
     *
     * @param sb The buffer.
     */
    private void truncate(StringBuilder sb)
    {
        int length = sb.length();
        if (length > 80)
        {
            if (++myLastSubIndex > length)
            {
                myLastSubIndex = 0;
            }
            else
            {
                String begin = sb.substring(0, myLastSubIndex);
                sb.delete(0, myLastSubIndex);
                if (sb.length() < 80)
                {
                    sb.append(" : ").append(begin);
                }
            }
            sb.setLength(80);

            synchronized (this)
            {
                myInvalid = true;
            }
        }
    }

    /**
     * Ignore serialization.
     *
     * @param out out
     */
    private void writeObject(java.io.ObjectOutputStream out)
    {
    }
}
