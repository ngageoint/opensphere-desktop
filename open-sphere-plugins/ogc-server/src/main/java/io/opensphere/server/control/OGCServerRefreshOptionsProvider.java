package io.opensphere.server.control;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.swing.HorizontalSpacerForGridbag;
import io.opensphere.server.toolbox.ServerRefreshController;

/**
 * The Class OGCServerMainOptionsProvider.
 */
public class OGCServerRefreshOptionsProvider extends AbstractOptionsProvider
{
    /** Logging reference. */
    private static final Logger LOGGER = Logger.getLogger(OGCServerRefreshOptionsProvider.class);

    /** The main options configuration panel. */
    private final JPanel myOptionsPanel;

    /** The server refresh controller. */
    private final ServerRefreshController myRefreshController;

    /** Checkbox used to enable/disable server refreshes. */
    private JCheckBox myRefreshEnabledCheckBox;

    /** Text box used to enter the server refresh interval. */
    private JTextField myRefreshRateTB;

    /**
     * Instantiates an options provider that allows users to set configurable
     * parameters for OGC Servers.
     *
     * @param serverRefreshController The server refresh controller.
     */
    public OGCServerRefreshOptionsProvider(ServerRefreshController serverRefreshController)
    {
        super("Server Refresh");
        myRefreshController = serverRefreshController;
        myOptionsPanel = new JPanel();
        myOptionsPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
        myOptionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        myOptionsPanel.setLayout(new BoxLayout(myOptionsPanel, BoxLayout.Y_AXIS));

        // Add panel for Server refresh configuration
        myOptionsPanel.add(buildRefreshPanel());

        // Add blank panel to make spacing correct.
        myOptionsPanel.add(Box.createVerticalGlue());
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(TRANSPARENT_COLOR);
        myOptionsPanel.add(emptyPanel);
    }

    @Override
    public void applyChanges()
    {
        QuantifyToolboxUtils.collectMetric("mist3d.settings.servers.server-refresh.apply-button");

        LOGGER.warn("Saving main server options.");
        int interval = 0;
        String error = null;
        try
        {
            interval = Integer.parseInt(myRefreshRateTB.getText());
        }
        catch (NumberFormatException e)
        {
            error = "The specified interval is not a valid integer number of minutes.\nPlease enter a valid interval and re-save.";
        }
        if (interval < 1)
        {
            error = "The refresh interval must be greater than 0.\nPlease enter a valid interval and re-save.";
        }
        if (StringUtils.isNotEmpty(error))
        {
            JOptionPane.showMessageDialog(myOptionsPanel, error, "Unknown number format", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            myRefreshController.setRefreshEnabled(myRefreshEnabledCheckBox.isSelected());
            myRefreshController.setRefreshInterval(interval);
        }
    }

    @Override
    public JPanel getOptionsPanel()
    {
        populateRefreshPanelValues();
        return myOptionsPanel;
    }

    @Override
    public void restoreDefaults()
    {
        LOGGER.warn("Resetting main server options to defaults.");
        // Set the values in the controller, then update the UI
        myRefreshController.restoreDefaults();
        populateRefreshPanelValues();
    }

    /**
     * Builds the Server refresh configuration panel.
     *
     * @return the panel used to configure server refreshes
     */
    private JPanel buildRefreshPanel()
    {
        JPanel refreshPanel = new JPanel(new BorderLayout());
        refreshPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
        refreshPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Capabilities Refresh"));
        refreshPanel.setMinimumSize(new Dimension(200, 50));
        refreshPanel.setMaximumSize(new Dimension(1000, 50));

        // Add a brief description of what this preference is
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gridPanel.add(new JLabel("Allow servers to periodically check for new data and update the map."), gbc);
        HorizontalSpacerForGridbag hz = new HorizontalSpacerForGridbag(4, 0);
        gridPanel.add(hz, hz.getGbConst());

        // Add a checkbox to enable/disable auto-refresh
        gbc.gridx = 0;
        gbc.gridy = 1;
        myRefreshEnabledCheckBox = new JCheckBox("Auto-refresh Server layers");
        myRefreshEnabledCheckBox.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric("mist3d.settings.servers.server-refresh.auto-refresh-layers-checkbox");
            myRefreshRateTB.setEditable(myRefreshEnabledCheckBox.isSelected());
        });
        myRefreshEnabledCheckBox.setFocusPainted(false);
        gridPanel.add(myRefreshEnabledCheckBox, gbc);

        // Add a text field to enter the number of minutes between refreshes
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 30, 0, 0);
        gridPanel.add(new JLabel("Refresh interval: "), gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        myRefreshRateTB = new JTextField(3);
        gridPanel.add(myRefreshRateTB, gbc);
        gbc.gridx = 2;
        gridPanel.add(new JLabel(" minutes"), gbc);

        refreshPanel.add(gridPanel, BorderLayout.CENTER);
        populateRefreshPanelValues();
        return refreshPanel;
    }

    /**
     * Sets the values on the server refresh panel.
     */
    private void populateRefreshPanelValues()
    {
        myRefreshEnabledCheckBox.setSelected(myRefreshController.isRefreshEnabled());
        myRefreshRateTB.setText(Integer.toString(myRefreshController.getRefreshInterval()));
    }
}
