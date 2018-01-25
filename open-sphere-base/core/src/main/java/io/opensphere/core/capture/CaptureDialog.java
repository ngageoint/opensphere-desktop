package io.opensphere.core.capture;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.hud.awt.HUDFrame;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.predicate.FalsePredicate;
import io.opensphere.core.util.swing.OkayCancelDialog;

/**
 * Class is a dialog for selecting HUD components to include in screen capture.
 */
public class CaptureDialog extends OkayCancelDialog
{
    /** Serial ID. */
    private static final long serialVersionUID = 1L;

    /**
     * The cb panel. This is the panel that will contain the hud check box
     * options.
     */
    private JPanel myCBPanel;

    /** List of HUD component checkboxes. */
    private List<JCheckBox> myHUDCheckboxes;

    /**
     * Create a JPanel that lists all the available internal frames.
     *
     * @param frames The frames.
     * @param selectedPredicate The predicate to determine whether the check box
     *            should be selected, based on the text
     * @return A panel.
     */
    public static JPanel showInternalFrames(List<HUDFrame> frames, Predicate<String> selectedPredicate)
    {
        JPanel framePanel = new JPanel(new GridBagLayout());
        addCheckboxesToPanel(getInternalFrameCheckboxes(frames, selectedPredicate), framePanel);
        return framePanel;
    }

    /**
     * Add some check boxes to the panel.
     *
     * @param checkboxes The check boxes.
     * @param panel The panel.
     */
    private static void addCheckboxesToPanel(List<JCheckBox> checkboxes, JPanel panel)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        for (JCheckBox cB : checkboxes)
        {
            gbc.insets = new Insets(5, 10, 0, 0);
            panel.add(cB, gbc);
        }
    }

    /**
     * Get a list of check boxes, one for each of the provided frames that are
     * visible and have titles.
     *
     * @param frames The input frames.
     * @param selectedPredicate The predicate to determine whether the check box
     *            should be selected, based on the text
     * @return The check boxes for the visible frames.
     */
    private static List<JCheckBox> getInternalFrameCheckboxes(List<? extends HUDFrame> frames,
            Predicate<? super String> selectedPredicate)
    {
        List<JCheckBox> frameCBList = New.list();
        for (HUDFrame frame : getVisibleFrames(frames))
        {
            JCheckBox newCB = new JCheckBox(frame.getTitle(), selectedPredicate.test(frame.getTitle()));
            newCB.setFocusPainted(false);
            frameCBList.add(newCB);
        }
        return frameCBList;
    }

    /**
     * Get a list of check boxes, one for each of the provided frames that are
     * visible and have titles.
     *
     * @param frames The input frames.
     * @return The check boxes for the visible frames.
     */
    public static List<HUDFrame> getVisibleFrames(List<? extends HUDFrame> frames)
    {
        List<HUDFrame> visible = New.list(frames.size());
        for (HUDFrame frame : frames)
        {
            if (frame.isVisible() && StringUtils.isNotEmpty(frame.getTitle()))
            {
                visible.add(frame);
            }
        }

        return visible;
    }

    /**
     * Default constructor.
     */
    public CaptureDialog()
    {
        init("Capture Options", "Select HUD Windows to remain visible during capture:");
    }

    /**
     * Construct that takes a parent window.
     *
     * @param parent The parent window.
     */
    public CaptureDialog(Window parent)
    {
        super(parent);
        init("Capture Options", "Select HUD Windows to remain visible during capture:");
    }

    /**
     * Returns names corresponding to the selected HUD check boxes.
     *
     * @return Returns a set of names.
     */
    public Set<String> getSelectedList()
    {
        Set<String> selectedCBSet = new HashSet<>();
        for (JCheckBox cB : myHUDCheckboxes)
        {
            if (cB.isSelected())
            {
                selectedCBSet.add(cB.getText());
            }
        }
        return selectedCBSet;
    }

    /**
     * Set the HUD frames that are to be represented in the dialog.
     *
     * @param frames The frames.
     */
    public void setVisibleFrames(List<? extends HUDFrame> frames)
    {
        myHUDCheckboxes = getInternalFrameCheckboxes(frames, new FalsePredicate());
        myCBPanel.removeAll();
        addCheckboxesToPanel(myHUDCheckboxes, myCBPanel);
    }

    /**
     * Initializes the dialog.
     *
     * @param title The title for the dialog.
     * @param displayText The text to display for dialog.
     */
    private void init(String title, String displayText)
    {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle(title);
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add the message text.
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.insets = new Insets(10, 20, 0, 20);
        getContentPane().add(new JLabel(displayText), gbc);

        myCBPanel = new JPanel(new GridBagLayout());
        myCBPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLoweredBevelBorder()));
        JScrollPane cpPane = new JScrollPane(myCBPanel);
        cpPane.setSize(new Dimension(300, 200));
        cpPane.setMinimumSize(cpPane.getSize());
        cpPane.setPreferredSize(cpPane.getSize());

        gbc.gridy++;
        gbc.insets = new Insets(3, 20, 3, 20);
        getContentPane().add(cpPane, gbc);

        // Add the OK/Cancel buttons.
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy++;
        getContentPane().add(getOkayCancelPanel(), gbc);
        pack();

        setLocationRelativeTo(getOwner());
    }
}
