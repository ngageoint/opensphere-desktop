package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.ValidatorSupport.ValidationStatusChangeListener;
import io.opensphere.core.util.collections.CollectionUtilities;

/**
 * <code>OptionDialog</code> makes it easy to pop up a standard dialog box that displays a particular component. It has some
 * similarities to <code>JOptionPane</code>, but it allows focus within the component, provides more control over the dialog
 * buttons, and provides validation support.
 *
 * <p>
 * Typical use:
 * </p>
 *
 * <pre>
 *     OptionDialog dialog = new OptionDialog(parentComponent, panel, title);
 *     dialog.buildAndShow();
 *     if (dialog.getSelection() == JOptionPane.OK_OPTION)
 *     {
 *         <i>// OK logic here</i>
 *     }
 * </pre>
 *
 * <p>
 * Non-modal use:
 * </p>
 *
 * <pre>
 *     OptionDialog dialog = new OptionDialog(parentComponent, panel, title);
 *     dialog.setModal(false);
 *     dialog.addWindowListener(new WindowAdapter()
 *     {
 *         public void windowClosed(WindowEvent e)
 *         {
 *             if (dialog.getSelection() == JOptionPane.OK_OPTION)
 *             {
 *                 <i>// OK logic here</i>
 *             }
 *         }
 *     }
 *     dialog.buildAndShow();
 * </pre>
 *
 * <p>
 * To reuse a dialog:
 * </p>
 *
 * <pre>
 *     <i>// Create it</i>
 *     myDialog = new OptionDialog(parentComponent, panel, title);
 *     myDialog.build();
 *
 *     <i>// Show it (multiple times if necessary)</i>
 *     myDialog.showDialog()
 *     if (myDialog.getSelection() == JOptionPane.OK_OPTION)
 *     {
 *         <i>// OK logic here</i>
 *     }
 * </pre>
 *
 * <p>
 * To enable validation, do one of the following:
 * </p>
 * <ul>
 * <li>Make the component implement {@link io.opensphere.core.util.Validatable}</li>
 * <li>Set the validator directly:
 *
 * <pre>
 * dialog.setValidator(validator);
 * </pre>
 *
 * </li>
 * </ul>
 */
@SuppressWarnings("PMD.GodClass")
public class OptionDialog extends JDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The button labels. */
    private Collection<String> myButtonLabels = ButtonPanel.OK_CANCEL;

    /** The labels for buttons that cancel the dialog. */
    private Collection<String> myCancelButtonLabels = CollectionUtilities.listView(ButtonPanel.CANCEL, ButtonPanel.NO);

    /** The component. */
    private Component myComponent;

    /** The content button panel. */
    private JPanel myContentButtonPanel;

    /** The dialog button panel. */
    private ButtonPanel myDialogButtonPanel;

    /** The labels for buttons that will dispose the dialog. */
    private Collection<String> myDisposingButtonLabels = CollectionUtilities.listView(ButtonPanel.OK, ButtonPanel.YES,
            ButtonPanel.CANCEL, ButtonPanel.NO, ButtonPanel.CLOSE);

    /** The error label. */
    private JTextField myErrorLabel;

    /** The error handling strategy. */
    private ErrorStrategy myErrorStrategy = ErrorStrategy.PRE_ACCEPT;

    /** The parent component. */
    private final Component myParent;

    /** The selection. */
    private volatile int mySelection = JOptionPane.CANCEL_OPTION;

    /** The labels for buttons that will validate the dialog. */
    private Collection<String> myValidatingButtonLabels = CollectionUtilities.listView(ButtonPanel.OK, ButtonPanel.YES);

    /** The optional validator support from the main component. */
    private ValidatorSupport myValidatorSupport;

    /**
     * Gets the first window ancestor, or the component itself if it's a Window.
     *
     * @param c the component
     * @return the window ancestor
     */
    private static Window getFirstWindow(Component c)
    {
        Window window = null;
        if (c instanceof Window)
        {
            window = (Window)c;
        }
        else if (c != null)
        {
            window = SwingUtilities.getWindowAncestor(c);
        }
        return window;
    }

    /**
     * Constructor.
     *
     * @param parent The parent
     */
    public OptionDialog(Component parent)
    {
        this(parent, null, null);
    }

    /**
     * Constructor.
     *
     * @param parent The parent
     * @param component The component
     */
    public OptionDialog(Component parent, Component component)
    {
        this(parent, component, null);
    }

    /**
     * Constructor.
     *
     * @param parent The parent
     * @param component The component
     * @param title The title
     */
    public OptionDialog(Component parent, Component component, String title)
    {
        super(getFirstWindow(parent), title, JDialog.DEFAULT_MODALITY_TYPE);
        myParent = parent;
        setComponentInternal(component);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Builds the dialog with the default size.
     */
    public void build()
    {
        buildInternal();
        pack();
        setMinimumSize(getPreferredSize());
    }

    /**
     * Builds the dialog with the given size.
     *
     * @param width the width
     * @param height the height
     */
    public void build(int width, int height)
    {
        buildInternal();
        setSize(width, height);
        setMinimumSize(new Dimension(width, height));
    }

    /**
     * Builds and shows the dialog.
     */
    public void buildAndShow()
    {
        build();
        showDialog();
    }

    /**
     * Accessor for the content button panel.
     *
     * @return The button panel.
     */
    public JPanel getContentButtonPanel()
    {
        if (myContentButtonPanel == null)
        {
            myContentButtonPanel = new JPanel(new GridLayout(1, 0, 6, 0));
        }
        return myContentButtonPanel;
    }

    /**
     * Accessor for the dialog button panel.
     *
     * @return The button panel.
     */
    public ButtonPanel getDialogButtonPanel()
    {
        if (myDialogButtonPanel == null)
        {
            myDialogButtonPanel = new ButtonPanel(myButtonLabels);
            myDialogButtonPanel.addActionListener(new ButtonEar());
        }
        return myDialogButtonPanel;
    }

    /**
     * A listener class ("ear", because it listens, get it?) configured to react to button events.
     */
    private class ButtonEar implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            boolean allowClose = myDisposingButtonLabels.contains(e.getActionCommand());

            if (myValidatingButtonLabels.contains(e.getActionCommand()) && myErrorStrategy == ErrorStrategy.POST_ACCEPT
                    && myValidatorSupport != null && myValidatorSupport.getValidationStatus() == ValidationStatus.ERROR)
            {
                JOptionPane.showMessageDialog(OptionDialog.this, myValidatorSupport.getValidationMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                allowClose = false;
            }

            if (allowClose)
            {
                boolean okay = !myCancelButtonLabels.contains(e.getActionCommand());
                mySelection = okay ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION;

                if (okay)
                {
                    allowClose = (!(myComponent instanceof DialogPanel) || ((DialogPanel)myComponent).accept()) && mayClose();
                }
                else if (myComponent instanceof DialogPanel)
                {
                    ((DialogPanel)myComponent).cancel();
                }

                if (allowClose)
                {
                    dispose();
                }
            }
        }
    }

    /**
     * Before closing the OptionDialog, check to see if the operation should be blocked. By default, the check succeeds
     * automatically, but subclasses may override this method to provide implementation-specific behavior.
     *
     * @return true if and only if the dialog should be allowed to close
     */
    protected boolean mayClose()
    {
        return true;
    }

    /**
     * Gets the selection.
     *
     * @return the selection
     */
    public int getSelection()
    {
        return mySelection;
    }

    /**
     * Accessor for the validator.
     *
     * @return The validator.
     */
    public ValidatorSupport getValidator()
    {
        return myValidatorSupport;
    }

    /**
     * Request focus for the button with the given button label.
     *
     * @param buttonLabel the button label
     */
    public void requestFocus(final String buttonLabel)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JButton button = getDialogButtonPanel().getButton(buttonLabel);
                if (button != null)
                {
                    button.requestFocusInWindow();
                }
            }
        });
    }

    /**
     * Sets the button labels. This defaults to {@link ButtonPanel#OK_CANCEL}.
     *
     * @param buttonLabels the new button labels
     */
    public void setButtonLabels(Collection<String> buttonLabels)
    {
        myButtonLabels = Utilities.checkNull(buttonLabels, "buttonLabels");
    }

    /**
     * Sets the button labels. This defaults to {@link ButtonPanel#OK_CANCEL}.
     *
     * @param buttonLabels the new button labels
     */
    public void setButtonLabels(String... buttonLabels)
    {
        setButtonLabels(Arrays.asList(buttonLabels));
    }

    /**
     * Set the labels for the buttons that will cancel the dialog. This defaults to {@link ButtonPanel#NO} and
     * {@link ButtonPanel#CANCEL}.
     *
     * @param buttonLabels The button labels.
     */
    public void setCancelButtonLabels(Collection<String> buttonLabels)
    {
        myCancelButtonLabels = Utilities.checkNull(buttonLabels, "buttonLabels");
    }

    /**
     * Set the labels for the buttons that will cancel the dialog. This defaults to {@link ButtonPanel#NO} and
     * {@link ButtonPanel#CANCEL}.
     *
     * @param buttonLabels The button labels.
     */
    public void setCancelButtonLabels(String... buttonLabels)
    {
        setCancelButtonLabels(Arrays.asList(buttonLabels));
    }

    /**
     * Sets the component.
     *
     * @param component The component
     */
    public void setComponent(Component component)
    {
        setComponentInternal(component);
    }

    /**
     * Set the labels for buttons that will dispose the dialog. This defaults to {@link ButtonPanel#OK},
     * {@link ButtonPanel#CANCEL}, {@link ButtonPanel#YES}, {@link ButtonPanel#NO}, and {@link ButtonPanel#CLOSE}.
     *
     * @param buttonLabels The button labels.
     */
    public void setDisposingButtonLabels(Collection<String> buttonLabels)
    {
        myDisposingButtonLabels = Utilities.checkNull(buttonLabels, "buttonLabels");
    }

    /**
     * Sets the error strategy.
     *
     * @param errorStrategy the error strategy
     */
    public void setErrorStrategy(ErrorStrategy errorStrategy)
    {
        myErrorStrategy = errorStrategy;
    }

    /**
     * Set the labels for buttons that will validate the dialog. This defaults to {@link ButtonPanel#OK} and
     * {@link ButtonPanel#YES}.
     *
     * @param buttonLabels The button labels.
     */
    public void setValidatingButtonLabels(Collection<String> buttonLabels)
    {
        myValidatingButtonLabels = Utilities.checkNull(buttonLabels, "buttonLabels");
    }

    /**
     * Sets the validator support.
     *
     * @param validator the validator support
     */
    public void setValidator(ValidatorSupport validator)
    {
        myValidatorSupport = validator;
    }

    /**
     * Shows the dialog.
     */
    public void showDialog()
    {
        setLocationRelativeTo(myParent);
        setVisible(true);
    }

    /**
     * Builds the bottom panel.
     *
     * @return The bottom panel
     */
    private JPanel buildBottomPanel()
    {
        GridBagPanel panel = new GridBagPanel();
        panel.add(getContentButtonPanel());
        panel.fillHorizontalSpace();
        panel.fillNone();
        panel.add(getDialogButtonPanel());
        return panel;
    }

    /**
     * Builds the error label.
     *
     * @return The error label
     */
    private JTextField buildErrorLabel()
    {
        myErrorLabel = new JTextField();
        myErrorLabel.setEditable(false);
        myErrorLabel.setBorder(null);
        myErrorLabel.setBackground(null);
        myErrorLabel.setForeground(Color.RED);
        return myErrorLabel;
    }

    /**
     * Builds the GUI.
     */
    private void buildInternal()
    {
        add(buildMainPanel(myComponent));
    }

    /**
     * Builds the main panel.
     *
     * @param comp The component
     * @return The main panel
     */
    private JPanel buildMainPanel(Component comp)
    {
        GridBagPanel panel = new GridBagPanel();
        int pad = 12;
        panel.init0();
        panel.fillBoth().setInsets(pad, pad, pad, pad);
        panel.addRow(comp);
        panel.fillHorizontal().setInsets(0, pad, pad, pad);
        if (myErrorStrategy == ErrorStrategy.PRE_ACCEPT && myValidatorSupport != null)
        {
            panel.addRow(buildErrorLabel());

            myValidatorSupport.addAndNotifyValidationListener(new ValidationStatusChangeListener()
            {
                @Override
                public void statusChanged(Object object, ValidationStatus valid, String message)
                {
                    updateErrorState(valid, message);
                }
            });
        }
        panel.addRow(buildBottomPanel());
        return panel;
    }

    /**
     * Sets the component.
     *
     * @param component The component
     */
    private void setComponentInternal(Component component)
    {
        myComponent = component;
        if (component instanceof Validatable)
        {
            myValidatorSupport = ((Validatable)component).getValidatorSupport();
        }
        if (component instanceof DialogPanel)
        {
            DialogPanel dialogPanel = (DialogPanel)component;
            setTitle(dialogPanel.getTitle());
            Collection<String> dialogButtonLabels = dialogPanel.getDialogButtonLabels();
            if (dialogButtonLabels != null)
            {
                myButtonLabels = dialogButtonLabels;
            }
            for (Component contentButton : dialogPanel.getContentButtons())
            {
                getContentButtonPanel().add(contentButton);
            }
        }
    }

    /**
     * Updates the error state.
     *
     * @param status whether it's valid
     * @param message the message
     */
    private void updateErrorState(ValidationStatus status, String message)
    {
        boolean isValid = status == ValidationStatus.VALID;
        boolean showError = !isValid && message != null;
        myErrorLabel.setText(showError ? message : null);
        myErrorLabel.setToolTipText(showError ? message : null);
        myErrorLabel.setCaretPosition(0);

        if (status == ValidationStatus.WARNING)
        {
            myErrorLabel.setForeground(Color.YELLOW);
        }
        else
        {
            myErrorLabel.setForeground(Color.RED);
        }

        JButton okButton = getDialogButtonPanel().getButton(ButtonPanel.OK);
        if (okButton != null)
        {
            okButton.setEnabled(status != null && status != ValidationStatus.ERROR);
        }
    }

    /** The error handling strategy enum. */
    public enum ErrorStrategy
    {
        /** Show errors after the user has clicked OK. */
        POST_ACCEPT,

        /** Show errors before the user has clicked OK. */
        PRE_ACCEPT;
    }
}
