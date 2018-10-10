package io.opensphere.core.util.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * A dialog used to display a message to the user. The message may include
 * details on a second pane, and may provide multiple buttons for the user to
 * select from.
 */
public class ChoiceMessageDialog extends JDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The selection that was made. */
    private volatile String mySelection;

    /**
     * Constructor.
     */
    public ChoiceMessageDialog()
    {
        this((Window)null, ModalityType.APPLICATION_MODAL);
    }

    /**
     * Constructor.
     *
     * @param modalityType The modality for the dialog.
     */
    public ChoiceMessageDialog(ModalityType modalityType)
    {
        this((Window)null, modalityType);
    }

    /**
     * Constructor.
     *
     * @param owner The owner of the dialog.
     * @param modalityType The modality for the dialog.
     */
    public ChoiceMessageDialog(Window owner, ModalityType modalityType)
    {
        super(owner, modalityType);
    }

    /**
     * Get the selection made by the user.
     *
     * @return The selection, or {@code null} if no selection was made.
     */
    public String getSelection()
    {
        return mySelection;
    }

    /**
     * Initialize the components.
     *
     * @param summaryPanel The summary panel.
     * @param detailsPanel The details panel, may be {@code null}.
     * @param buttonLabels The labels for the buttons.
     */
    public void initialize(Component summaryPanel, final Component detailsPanel, String... buttonLabels)
    {
        setContentPane(new JPanel(new BorderLayout()));

        getContentPane().add(createButtonPanel(buttonLabels), BorderLayout.SOUTH);

        if (detailsPanel == null)
        {
            getContentPane().add(summaryPanel, BorderLayout.CENTER);
            pack();
        }
        else
        {
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Summary", summaryPanel);
            getContentPane().add(tabbedPane, BorderLayout.CENTER);
            tabbedPane.addTab("Details", detailsPanel);
            pack();
            tabbedPane.addChangeListener(e ->
            {
                if (((JTabbedPane)e.getSource()).getSelectedComponent() == detailsPanel)
                {
                    pack();
                }
            });
        }
    }

    /**
     * Initialize the components.
     *
     * @param summary The summary text (for a JLabel).
     * @param details The detail text (for a JTextArea), may be {@code null}.
     */
    public void initialize(String summary, String details)
    {
        initialize(summary, details, "OK");
    }

    /**
     * Initialize the components.
     *
     * @param summary The summary text (for a JLabel).
     * @param details The detail text (for a JTextArea), may be {@code null}.
     * @param buttonLabels The labels for the buttons.
     */
    public void initialize(final String summary, String details, String... buttonLabels)
    {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel summaryLabel = new JLabel(summary);
        summaryPanel.add(summaryLabel);

        JPanel detailsPanel;
        if (details == null)
        {
            detailsPanel = null;
        }
        else
        {
            detailsPanel = new JPanel(new BorderLayout());

            JTextArea detailsText = new JTextArea(details);
            detailsText.setBackground(detailsPanel.getBackground());
            detailsText.setBorder(BorderFactory.createEmptyBorder());
            detailsText.setEditable(false);
            detailsPanel.add(new JScrollPane(detailsText));
        }

        initialize(summaryPanel, detailsPanel, buttonLabels);
    }

    /**
     * Create a panel containing buttons for each of the provided labels.
     *
     * @param buttonLabels The labels.
     * @return The panel.
     */
    protected JPanel createButtonPanel(String... buttonLabels)
    {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.add(new JLabel());
        buttonPanel.add(createButtons(buttonLabels));
        return buttonPanel;
    }

    /**
     * Create the push buttons.
     *
     * @param buttonLabels The labels for the buttons.
     *
     * @return The component containing the OK button.
     */
    private JComponent createButtons(String[] buttonLabels)
    {
        JPanel panel = new JPanel();
        for (String label : buttonLabels)
        {
            JButton button = new JButton(label);
            button.addActionListener(e ->
            {
                mySelection = e.getActionCommand();
                dispatchEvent(new WindowEvent(ChoiceMessageDialog.this, WindowEvent.WINDOW_CLOSING));
            });
            panel.add(button);
        }
        return panel;
    }
}
