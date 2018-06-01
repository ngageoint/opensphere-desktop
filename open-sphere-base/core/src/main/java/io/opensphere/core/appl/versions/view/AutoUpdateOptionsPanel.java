package io.opensphere.core.appl.versions.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.appl.versions.AutoUpdateToolboxUtils;
import io.opensphere.core.appl.versions.controller.AutoUpdateController;
import io.opensphere.core.appl.versions.model.AutoUpdatePreferenceKeys;
import io.opensphere.core.appl.versions.model.AutoUpdatePreferences;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.binding.CheckBox;
import io.opensphere.core.util.swing.binding.TextField;
import io.opensphere.core.util.swing.input.ViewPanel;
import io.opensphere.core.util.taskactivity.TaskActivity;

/** The options panel for auto-update. */
public class AutoUpdateOptionsPanel extends ViewPanel
{
    /** The {@link Logger} instance used to capture output. */
    private static final Logger LOG = Logger.getLogger(AutoUpdateOptionsPanel.class);

    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = -6092206441451732898L;

    /** Versions to button model dictionary to allow selection detection. */
    private final Map<String, ButtonModel> myPreferredVersionDictionary;

    /** The controller used for application state changes. */
    private final AutoUpdateController myController;

    /** The model in which values are contained. */
    private final AutoUpdatePreferences myPreferences;

    /** The button group used to enforce unique version selection. */
    private final ButtonGroup myPreferredVersionButtonGroup;

    /**
     * Maps delete buttons to versions. If a version is marked as preferred, we
     * can easily hide the button.
     */
    private final Map<String, JButton> myVersionToDeleteMap = New.map();

    /** The container in which version rows are displayed. */
    private final GridBagPanel myVersionContainer;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The preferred version of the application. */
    private String myPreferredVersion;

    /**
     * Creates a new options panel for configuring auto update.
     *
     * @param toolbox The toolbox through which application state is accessed.
     * @param controller the controller used for application state changes.
     */
    public AutoUpdateOptionsPanel(Toolbox toolbox, AutoUpdateController controller)
    {
        myToolbox = toolbox;
        myController = controller;
        myPreferences = AutoUpdateToolboxUtils.getAutoUpdateToolboxToolbox(myToolbox).getPreferences();
        addHeading("Auto-Update Options");

        JButton checkForUpdates = new JButton("Check for Updates");
        checkForUpdates.addActionListener(e -> myController.checkForUpdates(true));
        addRow(new CheckBox(myPreferences.autoUpdateProperty(), myPreferences.autoUpdateProperty().getName()), checkForUpdates);

        addComponent(
                new CheckBox(myPreferences.updateWithoutPromptProperty(), myPreferences.updateWithoutPromptProperty().getName()));
        addLabelComponent(myPreferences.autoUpdateHostnameProperty().getName(),
                new TextField(myPreferences.autoUpdateHostnameProperty(), myPreferences.autoUpdateHostnameProperty().getName()));
        addLabelComponent(myPreferences.latestVersionUrlProperty().getName(),
                new TextField(myPreferences.latestVersionUrlProperty(), myPreferences.latestVersionUrlProperty().getName()));
        addLabelComponent(myPreferences.updateUrlProperty().getName(),
                new TextField(myPreferences.updateUrlProperty(), myPreferences.updateUrlProperty().getName()));

        addHeading("Installed Versions");

        myVersionContainer = new GridBagPanel();
        myVersionContainer.setWeightx(1);
        myVersionContainer.setWeighty(1);
        myVersionContainer.setFill(GridBagConstraints.BOTH);
        myVersionContainer.setAnchor(GridBagConstraints.NORTHWEST);

        List<String> versions = myController.getVersionOptions();

        myPreferredVersionDictionary = New.map();
        myPreferredVersion = myController.loadLaunchConfiguration().getProperty(AutoUpdatePreferenceKeys.PREFERRED_VERSION_KEY);
        myPreferredVersionButtonGroup = new ButtonGroup();
        for (String version : versions)
        {
            myVersionContainer.addRow(createVersionComponent(version, myPreferredVersionButtonGroup,
                    StringUtils.equals(myPreferredVersion, version)));
        }

        JScrollPane versionsPane = new JScrollPane(myVersionContainer);
        fillBoth();
        setGridwidth(2);
        addRow(versionsPane);
    }

    /**
     * Creates a single row with which the version can be selected or deleted.
     *
     * @param version the version to display in the row.
     * @param preferredVersionButtonGroup the group to which to add the button.
     * @param isPreferred flag to force selection of the row.
     * @return a component in which the version UI is encapsulated.
     */
    protected Component createVersionComponent(String version, ButtonGroup preferredVersionButtonGroup, boolean isPreferred)
    {
        Box box = Box.createHorizontalBox();

        JRadioButton toggleButton = new JRadioButton(version, isPreferred);
        toggleButton.setToolTipText("Click to select " + version + "as your preferred version.");
        toggleButton.addActionListener(e -> updatePreferredVersion(version));
        preferredVersionButtonGroup.add(toggleButton);

        box.add(toggleButton);

        myPreferredVersionDictionary.put(version, toggleButton.getModel());

        box.add(Box.createHorizontalGlue());

        JButton deleteButton = new JButton(new GenericFontIcon(AwesomeIconSolid.TRASH_ALT, Color.WHITE));
        deleteButton.setBackground(Color.RED);
        deleteButton.addActionListener(e -> deleteVersion(version, box, deleteButton));
        if (isPreferred)
        {
            deleteButton.setVisible(false);
            deleteButton.setEnabled(false);
        }

        box.add(deleteButton);

        myVersionToDeleteMap.put(version, deleteButton);

        return box;
    }

    /**
     * Updates the launch configuration to utilize the selected version as the
     * preferred.
     *
     * @param version the version to use
     */
    private void updatePreferredVersion(String version)
    {
        String chooseVersionMessage = "Are you sure you want to use version " + version + " as your default?"
                + System.lineSeparator() + "You will have to restart the application for this to take effect.";

        int yn = JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), chooseVersionMessage,
                "Confirm Change", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (yn == JOptionPane.YES_OPTION)
        {
            Properties launchConfig = myController.loadLaunchConfiguration();
            launchConfig.setProperty(AutoUpdatePreferenceKeys.PREFERRED_VERSION_KEY, version);
            myController.storeLaunchConfiguration(launchConfig);

            // Enable/Disable deletion buttons
            JButton deleteButton = myVersionToDeleteMap.get(version);
            deleteButton.setVisible(false);
            deleteButton.setEnabled(false);

            deleteButton = myVersionToDeleteMap.get(myPreferredVersion);
            deleteButton.setVisible(true);
            deleteButton.setEnabled(true);

            // Swap preferred versions finally
            myPreferredVersion = version;
        }
        else
        {
            myPreferredVersionButtonGroup.setSelected(myPreferredVersionDictionary.get(myPreferredVersion), true);
        }
    }

    /**
     * Deletes the named version from the filesystem, after prompting the user.
     *
     * @param version the version to remove from the filesystem.
     * @param row the row in which the version is displayed, will be removed
     *            from the UI.
     * @param deleteButton the button that triggered the delete, will be used
     *            for listener deregistration.
     */
    private void deleteVersion(String version, Component row, JButton deleteButton)
    {
        String chooseVersionMessage = "Are you sure you want to delete version " + version + "?" + System.lineSeparator()
                + "This action cannot be undone.";

        int response = JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), chooseVersionMessage,
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (response == JOptionPane.YES_OPTION)
        {
            LOG.info("Deleting version '" + version + "' from filesystem.");
            Path versionDirectory = Paths.get(myPreferences.getInstallDirectory().getAbsolutePath(), version).normalize();

            try (TaskActivity ta = TaskActivity.createActive("Deleting " + version))
            {
                myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);
                myController.deleteVersionFromFilesystem(versionDirectory);

                Arrays.stream(deleteButton.getActionListeners()).forEach(a -> deleteButton.removeActionListener(a));

                ButtonModel targetModel = myPreferredVersionDictionary.get(version);
                Enumeration<AbstractButton> buttons = myPreferredVersionButtonGroup.getElements();
                while (buttons.hasMoreElements())
                {
                    AbstractButton button = buttons.nextElement();

                    if (button.getModel().equals(targetModel))
                    {
                        myPreferredVersionButtonGroup.remove(button);
                        break;
                    }
                }

                myVersionContainer.remove(row);
                myVersionContainer.revalidate();
                myVersionContainer.repaint();
                ta.setActive(false);
            }
            catch (IOException e)
            {
                LOG.error("Unable to cleanup '" + versionDirectory.toString() + "' after requested cancel", e);
            }
        }
    }
}
