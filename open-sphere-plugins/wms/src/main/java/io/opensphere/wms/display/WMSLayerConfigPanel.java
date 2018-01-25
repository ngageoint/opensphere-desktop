package io.opensphere.wms.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.Logger;

import com.bric.swing.ColorPicker;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.ColorIcon;
import io.opensphere.core.util.swing.ExtendedComboBox;
import io.opensphere.core.util.swing.HorizontalSpacerForGridbag;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig.LayerType;
import io.opensphere.wms.config.v1.WMSLayerConfigChangeListener.WMSLayerConfigChangeEvent;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.config.v1.WMSLayerDisplayConfig;
import io.opensphere.wms.config.v1.WMSLayerGetMapConfig;
import io.opensphere.wms.config.v1.WMSLayerGetMapConfig.StyleType;
import io.opensphere.wms.config.v1.WMSServerConfig;
import io.opensphere.wms.sld.SldRegistry;
import io.opensphere.wms.sld.event.SldChangeEvent;
import io.opensphere.wms.sld.event.SldChangeListener;
import io.opensphere.wms.sld.ui.SldBuilderDialog;
import io.opensphere.wms.toolbox.WMSToolbox;
import net.opengis.sld._100.StyledLayerDescriptor;

/**
 * Configuration panel for getMap request parameters.
 */
@SuppressWarnings("PMD.GodClass")
public class WMSLayerConfigPanel extends JPanel implements SldChangeListener
{
    /**
     * The content type expected for lidar.
     */
    private static final String LIDAR_CONTENT_TYPE = MimeType.GEOTIFF.getMimeType() + "; " + MimeType.TIFF.getMimeType();

    /** Lager reference. */
    private static final Logger LOGGER = Logger.getLogger(WMSLayerConfigPanel.class);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * When this is false a save is being attempted, but the previously set
     * value is in valid. The value will be reset and the save will be skipped.
     */
    private boolean myAllowCommit = true;

    /** Tile background color. */
    private ColorIcon myBGColorIcon;

    /** Any required custom parameters. */
    private JTextField myCustomTextField;

    /**
     * The value of the currently focused text field if that field supports
     * validation. When validation fails for a new value, the field will be
     * reset to this value.
     */
    private String myFocusFieldValue;

    /** Tile image format. */
    private ExtendedComboBox<String> myFormatComboBox;

    /** Tile pixel height. */
    private JTextField myHeightTextField;

    /** The largest tile size for the top level of the layer. */
    private JTextField myLargestTileSizeTextField;

    /** Override for the GetMap URL. */
//    private JTextField myGetMapOverrideTextField;

    /** Configuration for the current layer. */
    private WMSLayerConfigurationSet myLayerConfigSet;

    /** General, SRTM, or PlaceNames. */
    private ExtendedComboBox<WMSLayerConfig.LayerType> myLayerTypeComboBox;

    /** The valid refresh times for tile layers. */
    private ExtendedComboBox<WMSRefreshTimes> myRefreshTimeComboBox;

    /** The maximum viewer elevation at which tiles will be displayed. */
    private JTextField myMaxDisplayElevationTextField;

    /** The number of levels of tiles allowed by splitting. */
    private JSpinner myMaxResolveLevelsSpinner;

    /** The minimum viewer elevation at which tiles will be displayed. */
    private JTextField myMinDisplayElevationTextField;

    /** The dialog which owns this panel. */
    private final JDialog myParent;

    /** Preferences used to persist the layer configuration. */
    private final Preferences myPrefs;

    /** The button which saves and applies configuration changes. */
    private JButton mySaveButton;

    /** Request bounding box SRS. */
    private ExtendedComboBox<String> mySrsComboBox;

    /** The Style type combobox. */
    private ExtendedComboBox<WMSLayerGetMapConfig.StyleType> myStyleTypeCombobox;

    /** Tile style. */
    private ExtendedComboBox<String> myServerStylesComboBox;

    /** Tile transparency. */
    private ExtendedComboBox<String> myTransparentComboBox;

    /** Tile pixel width. */
    private JTextField myWidthTextField;

    /** The sld combobox. */
    private JComboBox<String> myClientStylesCombobox;

    /** The Sld actions button. */
    private JButton mySldActionsButton;

    /** The sld controls popupmenu. */
    private JPopupMenu mySldControlsPopupmenu;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The Sld registry. */
    private final SldRegistry mySldRegistry;

    /** The Advanced panel. */
    private JPanel myAdvancedPanel;

    /** The Styles panel. */
    private JPanel myStylesPanel;

    /** The Style type panel. */
    private JPanel myStyleTypePanel;

    /** The Sever styles panel. */
    private JPanel mySeverStylesPanel;

    /** The Client styles panel. */
    private JPanel myClientStylesPanel;

    /** The BG color button. */
    private JButton myBGColorButton;

    /** This listener will fire an event when the parent dialog is disposed. */
    private ActionListener myCloseListener;

    /** The Sld dialog. */
    private SldBuilderDialog mySldDialog;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param parent The dialog which owns this panel.
     * @param config The current configuration for the layer.
     * @param prefs The preferences used to persist the configuration.
     */
    public WMSLayerConfigPanel(Toolbox toolbox, JDialog parent, WMSLayerConfigurationSet config, Preferences prefs)
    {
        myParent = parent;
        myToolbox = toolbox;
        mySldRegistry = myToolbox.getPluginToolboxRegistry().getPluginToolbox(WMSToolbox.class).getSldRegistry();
        mySldRegistry.addSldChangeListener(this);
        myLayerConfigSet = config;
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(450, 310));
        setMinimumSize(new Dimension(450, 310));

        JTabbedPane tp = new JTabbedPane();

        // build the advanced panel first so that the image type can be check by
        // the basic panel to ensure the image type is compatible with the layer
        // type.
        JPanel advPan = getAdvancedPanel();
        JPanel basicPan = getBasicPanel();

        tp.addTab("Basic Configuration", basicPan);
        tp.addTab("Advanced Configuration", advPan);
        add(tp, BorderLayout.CENTER);

        add(getActiveSavePanel(), BorderLayout.SOUTH);
        myPrefs = prefs;
    }

    /**
     * Adds the dialog closed listener.
     *
     * @param closeListener the close listener
     */
    public void addDialogClosedListener(ActionListener closeListener)
    {
        myCloseListener = closeListener;
    }

    /**
     * Hide sld builder.
     */
    public void hideSldBuilder()
    {
        if (mySldDialog == null)
        {
            LOGGER.error("******************************");
            LOGGER.error("******************************");
            LOGGER.error("dialog was null");
            LOGGER.error("******************************");
            LOGGER.error("******************************");
        }
        else
        {
            mySldDialog.setVisible(false);
        }
    }

    @Override
    public void sldCreated(SldChangeEvent evt)
    {
        if (myLayerConfigSet.getLayerConfig().getLayerKey().equals(evt.getLayerKey()))
        {
            getClientStylesCombobox().removeAllItems();
            List<String> sldList = mySldRegistry.getSldNamesForLayer(evt.getLayerKey());
            for (String s : sldList)
            {
                getClientStylesCombobox().addItem(s);
            }
            getClientStylesCombobox().setSelectedItem(evt.getSldName());
            hideSldBuilder();
        }
    }

    @Override
    public void sldDeleted(SldChangeEvent evt)
    {
        if (myLayerConfigSet.getLayerConfig().getLayerKey().equals(evt.getLayerKey()))
        {
            getClientStylesCombobox().removeAllItems();
            List<String> sldList = mySldRegistry.getSldNamesForLayer(evt.getLayerKey());
            for (String s : sldList)
            {
                getClientStylesCombobox().addItem(s);
            }
        }
    }

    /**
     * Adds the sld actions.
     */
    private void addSldActions()
    {
        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionevent)
            {
                showSldBuilder(myLayerConfigSet, null);
            }
        });
        getControlsViewPopupmenu().add(newItem);

        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String key = myLayerConfigSet.getLayerConfig().getLayerKey();
                String sldName = getClientStylesCombobox().getSelectedItem().toString();
                editSld(mySldRegistry.getSldByLayerAndName(key, sldName));
            }
        });
        getControlsViewPopupmenu().add(editItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (getClientStylesCombobox().getSelectedItem() == null)
                {
                    JOptionPane.showMessageDialog(WMSLayerConfigPanel.this, "Failed to remove style:\n\nNo style selected.",
                            "Style delete failed", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // TODO: revisit this SLD delete logic
                String key = myLayerConfigSet.getLayerConfig().getLayerKey();
                String sldName = getClientStylesCombobox().getSelectedItem().toString();
                String currentSld = myLayerConfigSet.getLayerConfig().getGetMapConfig().getStyle();
                if (currentSld != null && currentSld.equals(sldName))
                {
                    JOptionPane.showMessageDialog(WMSLayerConfigPanel.this,
                            "Failed to remove selected style:\n\nStyle is currently in use.", "Style delete failed",
                            JOptionPane.WARNING_MESSAGE);
                }
                else
                {
                    mySldRegistry.removeSld(key, sldName);
                }
            }
        });
        getControlsViewPopupmenu().add(deleteItem);
    }

    /**
     * Brings up the sld dialog for editing.
     *
     * @param sld the sld to edit
     */
    private void editSld(StyledLayerDescriptor sld)
    {
        mySldDialog.editSld(sld);
    }

    /**
     * Get the panel which has the control buttons.
     *
     * @return The newly created panel.
     */
    private JPanel getActiveSavePanel()
    {
        JPanel pan = new JPanel(new FlowLayout());
        pan.setMinimumSize(new Dimension(300, 35));
        pan.setPreferredSize(new Dimension(300, 35));

        pan.add(getSaveButton());
        pan.add(getCancelButton());

        return pan;
    }

    /**
     * Gets the advanced panel.
     *
     * @return the advanced panel
     */
    private JPanel getAdvancedPanel()
    {
        if (myAdvancedPanel == null)
        {
            myAdvancedPanel = new JPanel(new GridBagLayout());
            myAdvancedPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 8, 0);
            gbc.weightx = 1.0;
            myAdvancedPanel.add(getHeightWidthPanel(), gbc);

            gbc.gridy++;
            myAdvancedPanel.add(getColorPanel(), gbc);

            gbc.gridy++;
            myAdvancedPanel.add(getFormatSRSPanel(), gbc);

            gbc.gridy++;
            myAdvancedPanel.add(getCustomPanel(), gbc);

            gbc.gridy++;
            myAdvancedPanel.add(getStylePanel(), gbc);
        }
        return myAdvancedPanel;
    }

    /**
     * Get the basic settings panel.
     *
     * @return The panel for "basic" settings.
     */
    private JPanel getBasicPanel()
    {
        JPanel pan = new JPanel(new GridBagLayout());
        pan.setPreferredSize(new Dimension(450, 250));
        pan.setMinimumSize(new Dimension(450, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        WMSGridBagUtil.addLabel(pan, gbc, "Layer Type");

        gbc.gridx = 1;
        WMSGridBagUtil.addComponent(pan, gbc, getLayerTypeComboBox());

        gbc.gridx = 2;
        WMSGridBagUtil.addLabel(pan, gbc, "Largest Tile Size");

        gbc.gridx = 3;
        WMSGridBagUtil.addComponent(pan, gbc, getLargestTileSizeTextField());

        // Next row
        gbc.gridy++;
        gbc.gridx = 0;
        WMSGridBagUtil.addLabel(pan, gbc, "Min Elevation");

        gbc.gridx = 1;
        WMSGridBagUtil.addComponent(pan, gbc, getMinDisplayElevationTextField());

        gbc.gridx = 2;
        WMSGridBagUtil.addLabel(pan, gbc, "Max Elevation");

        gbc.gridx = 3;
        WMSGridBagUtil.addComponent(pan, gbc, getMaxDisplayElevationTextField());

        // Next row
        gbc.gridy++;
        gbc.gridx = 0;
        WMSGridBagUtil.addLabel(pan, gbc, "Max Split Levels");

        gbc.gridx = 1;
        WMSGridBagUtil.addComponent(pan, gbc, getMaxResolveLevelsSpinner());

        gbc.gridx = 2;
        WMSGridBagUtil.addLabel(pan, gbc, "Auto Refresh:");

        gbc.gridx = 3;
        WMSGridBagUtil.addComponent(pan, gbc, getRefreshTimeComboBox());

        return pan;
    }

    /**
     * Gets the bg color button.
     *
     * @return the bg color button
     */
    private JButton getBgColorButton()
    {
        if (myBGColorButton == null)
        {
            myBGColorIcon = new ColorIcon();
            myBGColorIcon.setIconWidth(16);
            myBGColorIcon.setIconHeight(12);
            String clrStr = myLayerConfigSet.getLayerConfig().getGetMapConfig().getBGColor();
            if (clrStr == null)
            {
                myBGColorIcon.setColor(new Color(0, 0, 0, 0));
            }
            else
            {
                Color aColor = Color.decode(clrStr);
                myBGColorIcon.setColor(aColor);
            }

            myBGColorButton = new JButton(myBGColorIcon);
            myBGColorButton.setMargin(new Insets(3, 6, 3, 6));
            myBGColorButton.setText("BG Color");
            myBGColorButton.setFocusPainted(false);
            myBGColorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    Color selectColor = ColorPicker.showDialog((Window)getTopLevelAncestor(), "Choose Background Color",
                            myBGColorIcon.getColor(), true);
                    if (selectColor != null)
                    {
                        myBGColorIcon.setColor(selectColor);
                    }
                }
            });
        }
        return myBGColorButton;
    }

    /**
     * Get the cancel button.
     *
     * @return The button that closes the configuration panel without saving.
     */
    private JButton getCancelButton()
    {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                myCloseListener.actionPerformed(new ActionEvent(WMSLayerConfigPanel.this, 0, "CLOSED"));
                myParent.dispose();
            }
        });

        return cancelButton;
    }

    /**
     * Gets the client styles combobox.
     *
     * @return the client styles combobox
     */
    private JComboBox<String> getClientStylesCombobox()
    {
        if (myClientStylesCombobox == null)
        {
            myClientStylesCombobox = new JComboBox<>();
            myClientStylesCombobox.setSize(new Dimension(120, 20));
            myClientStylesCombobox.setMinimumSize(myClientStylesCombobox.getSize());
            myClientStylesCombobox.setPreferredSize(myClientStylesCombobox.getSize());
            List<String> sldNames = mySldRegistry.getSldNamesForLayer(myLayerConfigSet.getLayerConfig().getLayerKey());
            if (CollectionUtilities.hasContent(sldNames))
            {
                for (String s : sldNames)
                {
                    getClientStylesCombobox().addItem(s);
                }
            }
        }
        return myClientStylesCombobox;
    }

    /**
     * Gets the client styles panel.
     *
     * @return the client styles panel
     */
    private JPanel getClientStylesPanel()
    {
        if (myClientStylesPanel == null)
        {
            myClientStylesPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            myClientStylesPanel.add(new JLabel("Style:"));

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.weightx = 1;
            myClientStylesPanel.add(getClientStylesCombobox(), gbc);

            gbc.fill = GridBagConstraints.NONE;
            gbc.gridx = 2;
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.weightx = 0;
            myClientStylesPanel.add(getSldActionsButton(), gbc);
        }
        return myClientStylesPanel;
    }

    /**
     * Gets the color panel.
     *
     * @return the color panel
     */
    private JPanel getColorPanel()
    {
        JPanel colorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        colorPanel.add(new JLabel("Transparent:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 3, 0, 0);
        colorPanel.add(getTransparentComboBox(), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 65, 0, 0);
        colorPanel.add(getBgColorButton(), gbc);

        HorizontalSpacerForGridbag hs = new HorizontalSpacerForGridbag(3, 0);
        colorPanel.add(hs, hs.getGbConst());

        return colorPanel;
    }

    /**
     * Gets the controls view popupmenu.
     *
     * @return the controls view popupmenu
     */
    private JPopupMenu getControlsViewPopupmenu()
    {
        if (mySldControlsPopupmenu == null)
        {
            mySldControlsPopupmenu = new JPopupMenu();
        }
        return mySldControlsPopupmenu;
    }

    /**
     * Gets the custom panel.
     *
     * @return the custom panel
     */
    private JPanel getCustomPanel()
    {
        JPanel customPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 20, 0, 0);
        customPanel.add(new JLabel("Custom:"), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 3, 0, 20);
        gbc.weightx = 1.0;
        customPanel.add(getCustomTextField(), gbc);

        return customPanel;
    }

    /**
     * This method initializes text field for custom parameters.
     *
     * @return JTextField custom text field.
     */
    private JTextField getCustomTextField()
    {
        myCustomTextField = new JTextField();
        myCustomTextField.setSize(new Dimension(40, 20));
        myCustomTextField.setMinimumSize(myCustomTextField.getSize());
        myCustomTextField.setPreferredSize(myCustomTextField.getSize());
        myCustomTextField.setToolTipText(
                "<html>Enter custom parameters in the form :<br>" + "param1=param1Value&param2=param2Value</html>");

        String tf = myLayerConfigSet.getLayerConfig().getGetMapConfig().getCustomParams();
        if (tf != null)
        {
            myCustomTextField.setText(tf);
        }

        return myCustomTextField;
    }

    /**
     * This method initializes formatComboBox.
     *
     * @return CustomComboBox format combo box.
     */
    private ExtendedComboBox<String> getFormatComboBox()
    {
        if (myFormatComboBox == null)
        {
            myFormatComboBox = new ExtendedComboBox<>(200);
            myFormatComboBox.setSize(new Dimension(100, 20));
            myFormatComboBox.setMinimumSize(myFormatComboBox.getSize());
            myFormatComboBox.setPreferredSize(myFormatComboBox.getSize());

            String curFmt = myLayerConfigSet.getLayerConfig().getGetMapConfig().getImageFormat();

            int selIndex = 0;
            int idx = 0;
            Set<String> addedItems = New.set();
            for (String fmt : myLayerConfigSet.getInheritedLayerConfig().getGetMapFormats())
            {
                if (fmt.equals(curFmt))
                {
                    selIndex = idx;
                }
                myFormatComboBox.addItem(fmt);
                addedItems.add(fmt);
                ++idx;
            }

            String[] lidarFormats = new String[] { LIDAR_CONTENT_TYPE, MimeType.GEOTIFF.getMimeType(),
                MimeType.TIFF.getMimeType() };

            for (String lidarFormat : lidarFormats)
            {
                if (!addedItems.contains(lidarFormat))
                {
                    myFormatComboBox.addItem(lidarFormat);
                }
            }

            myFormatComboBox.setSelectedIndex(selIndex);
        }
        return myFormatComboBox;
    }

    /**
     * Gets the format srs panel.
     *
     * @return the format srs panel
     */
    private JPanel getFormatSRSPanel()
    {
        JPanel formatSRSPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 28, 0, 0);
        formatSRSPanel.add(new JLabel("Format:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 3, 0, 0);
        gbc.weightx = 0;
        formatSRSPanel.add(getFormatComboBox(), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 35, 0, 0);
        gbc.weightx = 0;
        formatSRSPanel.add(new JLabel("SRS:"), gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 3, 0, 0);
        gbc.weightx = 0;
        formatSRSPanel.add(getSrsComboBox(), gbc);

        HorizontalSpacerForGridbag hs = new HorizontalSpacerForGridbag(4, 0);
        formatSRSPanel.add(hs, hs.getGbConst());

        return formatSRSPanel;
    }

    /**
     * This method initializes myHeightTextField.
     *
     * @return JTextField height text field.
     */
    private JTextField getHeightTextField()
    {
        myHeightTextField = new JTextField();
        myHeightTextField.setSize(new Dimension(100, 20));
        myHeightTextField.setMinimumSize(myHeightTextField.getSize());
        myHeightTextField.setPreferredSize(myHeightTextField.getSize());

        Integer fixedHeight = myLayerConfigSet.getLayerConfig().getFixedHeight();
        if (fixedHeight != null && fixedHeight.intValue() != 0)
        {
            myHeightTextField.setText(fixedHeight.toString());
            myLayerConfigSet.getLayerConfig().getGetMapConfig().setTextureHeight(fixedHeight);
            myHeightTextField.setEditable(false);
        }
        else
        {
            myHeightTextField.setText(myLayerConfigSet.getLayerConfig().getGetMapConfig().getTextureHeight().toString());
            myHeightTextField.setEditable(true);
        }

        return myHeightTextField;
    }

    /**
     * Gets the height width panel.
     *
     * @return the height width panel
     */
    private JPanel getHeightWidthPanel()
    {
        JPanel heightWidthPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 27, 0, 0);
        heightWidthPanel.add(new JLabel("Height:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 3, 0, 0);
        heightWidthPanel.add(getHeightTextField(), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 30, 0, 0);
        heightWidthPanel.add(new JLabel("Width:"), gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 3, 0, 0);
        heightWidthPanel.add(getWidthTextField(), gbc);

        HorizontalSpacerForGridbag hs = new HorizontalSpacerForGridbag(4, 0);
        heightWidthPanel.add(hs, hs.getGbConst());

        return heightWidthPanel;
    }

    /**
     * A utility method to parse the integer or return null when unparsable.
     *
     * @param intStr integer to parse.
     * @return null or the parsed integer
     */
    private Integer getInteger(String intStr)
    {
        Integer retInt = null;
        try
        {
            retInt = Integer.valueOf(intStr);
        }
        catch (NumberFormatException ex)
        {
            LOGGER.warn("Could not parse value : " + intStr, ex);
        }
        return retInt;
    }

    /**
     * Create the text field for the largest tile size.
     *
     * @return The text field for the largest tile size.
     */
    private JTextField getLargestTileSizeTextField()
    {
        myLargestTileSizeTextField = new JTextField();
        myLargestTileSizeTextField.setSize(new Dimension(40, 20));
        myLargestTileSizeTextField.setMinimumSize(myLargestTileSizeTextField.getSize());
        myLargestTileSizeTextField.setPreferredSize(myLargestTileSizeTextField.getSize());
        myLargestTileSizeTextField.setToolTipText("<html>The largest tile size in degrees.<br>"
                + "This value should evenly divide both<br>the width and height of the layer.</html>");

        myLargestTileSizeTextField.addActionListener(new DoubleTextActionListener(myLargestTileSizeTextField, false));
        myLargestTileSizeTextField.addFocusListener(new DoubleTextFocusListener(myLargestTileSizeTextField, false));

        double tileSize = myLayerConfigSet.getLayerConfig().getDisplayConfig().getLargestTileSize();
        myLargestTileSizeTextField.setText(Double.toString(tileSize));

        return myLargestTileSizeTextField;
    }

    /**
     * This method initializes formatComboBox.
     *
     * @return CustomComboBox format combo box.
     */
    private ExtendedComboBox<WMSLayerConfig.LayerType> getLayerTypeComboBox()
    {
        myLayerTypeComboBox = new ExtendedComboBox<>(100);
        myLayerTypeComboBox.setSize(new Dimension(40, 20));
        myLayerTypeComboBox.setMinimumSize(myLayerTypeComboBox.getSize());
        myLayerTypeComboBox.setPreferredSize(myLayerTypeComboBox.getSize());
        myLayerTypeComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                WMSLayerConfig.LayerType layerType = (WMSLayerConfig.LayerType)myLayerTypeComboBox.getSelectedItem();
                JComboBox<String> formatComboBox = getFormatComboBox();

                if (layerType.equals(WMSLayerConfig.LayerType.SRTM))
                {
                    formatComboBox.setSelectedItem(MimeType.BIL.getMimeType());
                }
                else if (layerType.equals(WMSLayerConfig.LayerType.LIDAR))
                {
                    formatComboBox.setSelectedItem(LIDAR_CONTENT_TYPE);
                }
                else
                {
                    String format = (String)formatComboBox.getSelectedItem();
                    if (format.equals(MimeType.BIL.getMimeType()) || format.equals(LIDAR_CONTENT_TYPE))
                    {
                        formatComboBox.setSelectedItem(MimeType.PNG.getMimeType());
                    }
                }

                formatComboBox
                        .setEnabled(layerType == WMSLayerConfig.LayerType.General || layerType == WMSLayerConfig.LayerType.LIDAR);
            }
        });
        for (WMSLayerConfig.LayerType type : WMSLayerConfig.LayerType.values())
        {
            myLayerTypeComboBox.addItem(type);
            if (type.equals(myLayerConfigSet.getLayerConfig().getLayerType()))
            {
                myLayerTypeComboBox.setSelectedItem(type);
            }
        }

        return myLayerTypeComboBox;
    }

    /**
     * This method initializes formatComboBox.
     *
     * @return CustomComboBox format combo box.
     */
    private ExtendedComboBox<WMSRefreshTimes> getRefreshTimeComboBox()
    {
        myRefreshTimeComboBox = new ExtendedComboBox<>(100);
        myRefreshTimeComboBox.setSize(new Dimension(40, 20));
        myRefreshTimeComboBox.setMinimumSize(myRefreshTimeComboBox.getSize());
        myRefreshTimeComboBox.setPreferredSize(myRefreshTimeComboBox.getSize());

        for (WMSRefreshTimes refreshTime : WMSRefreshTimes.values())
        {
            myRefreshTimeComboBox.addItem(refreshTime);
            if (refreshTime.getMilliseconds() == myLayerConfigSet.getLayerConfig().getDisplayConfig().getRefreshTime())
            {
                myRefreshTimeComboBox.setSelectedItem(refreshTime);
            }
        }

        return myRefreshTimeComboBox;
    }

    /**
     * Create the text field for the maximum viewer elevation at which the layer
     * will be displayed.
     *
     * @return The maximum viewer elevation at which the layer will be
     *         displayed.
     */
    private JTextField getMaxDisplayElevationTextField()
    {
        myMaxDisplayElevationTextField = new JTextField();
        myMaxDisplayElevationTextField.setSize(new Dimension(40, 20));
        myMaxDisplayElevationTextField.setMinimumSize(myMaxDisplayElevationTextField.getSize());
        myMaxDisplayElevationTextField.setPreferredSize(myMaxDisplayElevationTextField.getSize());
        myMaxDisplayElevationTextField
                .setToolTipText("<html>The maximum viewer elevation<br>" + "at which tiles will display..</html>");
        myMaxDisplayElevationTextField.addActionListener(new DoubleTextActionListener(myMaxDisplayElevationTextField, true));
        myMaxDisplayElevationTextField.addFocusListener(new DoubleTextFocusListener(myMaxDisplayElevationTextField, true));

        Double elevation = myLayerConfigSet.getLayerConfig().getDisplayConfig().getMaxDisplayElevation();
        if (elevation != null)
        {
            myMaxDisplayElevationTextField.setText(elevation.toString());
        }

        return myMaxDisplayElevationTextField;
    }

    /**
     * This method initializes text field for the GetMap override.
     *
     * @return JTextField get map override text field.
     */
//    private JTextField getMapOverrideTextField()
//    {
//        myGetMapOverrideTextField = new JTextField();
//        myGetMapOverrideTextField.setSize(new Dimension(40, 20));
//        myGetMapOverrideTextField.setMinimumSize(myGetMapOverrideTextField.getSize());
//        myGetMapOverrideTextField.setPreferredSize(myGetMapOverrideTextField.getSize());
//        myGetMapOverrideTextField.setToolTipText("<html>If required, override the URL<br>for GetMap requests.</html>");
//
//        String ovr = myLayerConfig.getLayerConfig().getGetMapConfig().getGetMapURLOverride();
//        if (ovr != null)
//        {
//            myGetMapOverrideTextField.setText(ovr);
//        }
//
//        return myGetMapOverrideTextField;
//    }

    /**
     * Create the spinner for the maximum number of tile splits.
     *
     * @return The maximum number of tile splits.
     */
    private JSpinner getMaxResolveLevelsSpinner()
    {
        Integer levels = myLayerConfigSet.getLayerConfig().getDisplayConfig().getResolveLevels();
        SpinnerModel model = new SpinnerNumberModel(levels, Integer.valueOf(1), null, Integer.valueOf(1));
        myMaxResolveLevelsSpinner = new JSpinner(model);
        myMaxResolveLevelsSpinner.setSize(new Dimension(40, 20));
        myMaxResolveLevelsSpinner.setMinimumSize(myMaxResolveLevelsSpinner.getSize());
        myMaxResolveLevelsSpinner.setPreferredSize(myMaxResolveLevelsSpinner.getSize());

        return myMaxResolveLevelsSpinner;
    }

    /**
     * Create the text field for the minimum viewer elevation at which the layer
     * will be displayed.
     *
     * @return The text field for the minimum viewer elevation at which the
     *         layer will be displayed.
     */
    private JTextField getMinDisplayElevationTextField()
    {
        myMinDisplayElevationTextField = new JTextField();
        myMinDisplayElevationTextField.setSize(new Dimension(40, 20));
        myMinDisplayElevationTextField.setMinimumSize(myMinDisplayElevationTextField.getSize());
        myMinDisplayElevationTextField.setPreferredSize(myMinDisplayElevationTextField.getSize());
        myMinDisplayElevationTextField
                .setToolTipText("<html>The minimum viewer elevation<br>" + "at which tiles will display..</html>");
        myMinDisplayElevationTextField.addActionListener(new DoubleTextActionListener(myMinDisplayElevationTextField, true));
        myMinDisplayElevationTextField.addFocusListener(new DoubleTextFocusListener(myMinDisplayElevationTextField, true));

        Double elevation = myLayerConfigSet.getLayerConfig().getDisplayConfig().getMinDisplayElevation();
        if (elevation != null)
        {
            myMinDisplayElevationTextField.setText(elevation.toString());
        }

        return myMinDisplayElevationTextField;
    }

    /**
     * Get the modified configuration.
     *
     * @return The unsaved configuration as set on the GUI.
     */
    private WMSLayerConfig getModifiedConfig()
    {
        WMSLayerConfig modConf = myLayerConfigSet.getLayerConfig().clone();

        modConf.setLayerType((LayerType)myLayerTypeComboBox.getSelectedItem());

        WMSLayerGetMapConfig gmConf = modConf.getGetMapConfig();

        // Copy in the GetMap settings from the display
        int bgColor = myBGColorIcon.getColor().getRGB();
        if ((bgColor & 0xff000000) == 0)
        {
            gmConf.setBGColor(null);
        }
        else
        {
            String colorStr = "0x" + Integer.toHexString(bgColor & 0x00ffffff);
            gmConf.setBGColor(colorStr);
        }
        gmConf.setCustomParams(myCustomTextField.getText());
        gmConf.setImageFormat(getFormatComboBox().getSelectedItem().toString());
//        gmConf.setGetMapURLOverride(myGetMapOverrideTextField.getText());
        gmConf.setTextureHeight(getInteger(myHeightTextField.getText()));
        gmConf.setTextureWidth(getInteger(myWidthTextField.getText()));
        gmConf.setSRS(mySrsComboBox.getSelectedItem().toString());

        // Distinguish between client or server styles
        if (getStyleTypeCombobox().getSelectedItem().equals(WMSLayerGetMapConfig.StyleType.SERVER))
        {
            gmConf.setStyleType(WMSLayerGetMapConfig.StyleType.SERVER);
            gmConf.setStyle(getServerStylesComboBox().getSelectedItem().toString());
        }
        else if (getStyleTypeCombobox().getSelectedItem().equals(WMSLayerGetMapConfig.StyleType.CLIENT))
        {
            gmConf.setStyleType(WMSLayerGetMapConfig.StyleType.CLIENT);
            gmConf.setStyle(getClientStylesCombobox().getSelectedItem().toString());
        }
        gmConf.setTransparent(Boolean.valueOf("TRUE".equals(myTransparentComboBox.getSelectedItem().toString())));

        // Copy in the display settings from the display
        WMSLayerDisplayConfig dispConf = modConf.getDisplayConfig();
        dispConf.setLargestTileSize(Double.parseDouble(myLargestTileSizeTextField.getText()));
        if (myMinDisplayElevationTextField.getText() != null && !myMinDisplayElevationTextField.getText().isEmpty())
        {
            dispConf.setMinDisplayElevation(Double.valueOf(myMinDisplayElevationTextField.getText()));
        }
        else
        {
            dispConf.setMinDisplayElevation(null);
        }
        if (myMaxDisplayElevationTextField.getText() != null && !myMaxDisplayElevationTextField.getText().isEmpty())
        {
            dispConf.setMaxDisplayElevation(Double.valueOf(myMaxDisplayElevationTextField.getText()));
        }
        else
        {
            dispConf.setMaxDisplayElevation(null);
        }
        dispConf.setResolveLevels((Integer)myMaxResolveLevelsSpinner.getValue());
        dispConf.setRefreshTime(((WMSRefreshTimes)myRefreshTimeComboBox.getSelectedItem()).getMilliseconds());

        return modConf;
    }

    /**
     * Get the save button.
     *
     * @return The button for saving configuration
     */
    private JButton getSaveButton()
    {
        mySaveButton = new JButton("Clear Cache and Save");
        mySaveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                if (!myAllowCommit)
                {
                    myAllowCommit = true;
                    return;
                }
                WMSLayerConfig cfg = getModifiedConfig();

                WMSServerConfig serverConfig = myLayerConfigSet.getServerConfig();
                serverConfig.storeLayer(cfg);
                myLayerConfigSet = new WMSLayerConfigurationSet(serverConfig, cfg, myLayerConfigSet.getInheritedLayerConfig());
                myPrefs.putJAXBObject(myLayerConfigSet.getServerConfig().getServerId(), serverConfig, false, this);

                WMSLayerConfigChangeEvent event = new WMSLayerConfigChangeEvent(myLayerConfigSet);
                serverConfig.notifyLayerConfigChanged(event);
                myCloseListener.actionPerformed(new ActionEvent(WMSLayerConfigPanel.this, 0, "CLOSED"));
                myParent.dispose();
            }
        });

        return mySaveButton;
    }

    /**
     * Gets the server style panel.
     *
     * @return the server style panel
     */
    private JPanel getServerStylePanel()
    {
        if (mySeverStylesPanel == null)
        {
            mySeverStylesPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            mySeverStylesPanel.add(new JLabel("Style:"));

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.weightx = 1.0;
            mySeverStylesPanel.add(getServerStylesComboBox(), gbc);
        }
        return mySeverStylesPanel;
    }

    /**
     * This method initializes stylesTextField.
     *
     * @return JTextField styles text field.
     */
    private ExtendedComboBox<String> getServerStylesComboBox()
    {
        if (myServerStylesComboBox == null)
        {
            myServerStylesComboBox = new ExtendedComboBox<>(225);
            myServerStylesComboBox.setSize(new Dimension(100, 20));
            myServerStylesComboBox.setMinimumSize(myServerStylesComboBox.getSize());
            myServerStylesComboBox.setPreferredSize(myServerStylesComboBox.getSize());

            // Add an empty entry in case the user has no style (the empty entry
            // should be selected by default):
            myServerStylesComboBox.addItem("");
            for (String style : myLayerConfigSet.getInheritedLayerConfig().getStyles())
            {
                myServerStylesComboBox.addItem(style);
            }
            myServerStylesComboBox.setSelectedItem(myLayerConfigSet.getLayerConfig().getGetMapConfig().getStyle());
        }

        return myServerStylesComboBox;
    }

    /**
     * Gets the sld actions button.
     *
     * @return the sld actions button
     */
    private JButton getSldActionsButton()
    {
        if (mySldActionsButton == null)
        {
            mySldActionsButton = new JButton();
            mySldActionsButton.setFocusPainted(false);
            mySldActionsButton.setContentAreaFilled(false);
            mySldActionsButton.setBorder(null);
            mySldActionsButton.setSize(12, 12);
            mySldActionsButton.setMinimumSize(mySldActionsButton.getSize());
            mySldActionsButton.setPreferredSize(mySldActionsButton.getSize());
            mySldActionsButton.setIcon(new ImageIcon(WMSLayerConfigPanel.class.getResource("/images/column_control_button.png")));
            mySldActionsButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getSource().equals(mySldActionsButton))
                    {
                        getControlsViewPopupmenu().show(mySldActionsButton, 0, mySldActionsButton.getHeight());
                    }
                }
            });

            addSldActions();
        }
        return mySldActionsButton;
    }

    /**
     * This method initializes srsComboBox.
     *
     * @return CustomComboBox srs combo box.
     */
    private ExtendedComboBox<String> getSrsComboBox()
    {
        mySrsComboBox = new ExtendedComboBox<>(100);
        mySrsComboBox.setSize(new Dimension(100, 20));
        mySrsComboBox.setMinimumSize(mySrsComboBox.getSize());
        mySrsComboBox.setPreferredSize(mySrsComboBox.getSize());
        mySrsComboBox.setToolTipText(
                "<html>Only EPSG:4326 is supported<br>" + "Please pick a format compatible with the EPSG:4326 format</html>");

        Collection<String> srsOptions = myLayerConfigSet.getInheritedLayerConfig().getSRSOptions();
        if (srsOptions.isEmpty())
        {
            mySrsComboBox.addItem("EPSG:4326");
        }

        String curSrs = myLayerConfigSet.getLayerConfig().getGetMapConfig().getSRS();
        int selIdx = 0;
        int idx = 0;
        for (String srs : srsOptions)
        {
            if (srs.equals(curSrs))
            {
                selIdx = idx;
            }
            mySrsComboBox.addItem(srs);
            ++idx;
        }
        mySrsComboBox.setSelectedIndex(selIdx);

        return mySrsComboBox;
    }

    /**
     * Gets the style panel.
     *
     * @return the style panel
     */
    private JPanel getStylePanel()
    {
        if (myStylesPanel == null)
        {
            myStylesPanel = new JPanel(new GridBagLayout());
            myStylesPanel.setSize(150, 24);
            myStylesPanel.setMinimumSize(myStylesPanel.getSize());
            myStylesPanel.setPreferredSize(myStylesPanel.getSize());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;

            if (myLayerConfigSet.getServerConfig() != null
                    && myLayerConfigSet.getServerConfig().getUserDefinedSymbolization() != null)
            {
                myStylesPanel.add(getStyleTypePanel(), gbc);
            }
            else
            {
                gbc.insets = new Insets(0, 40, 0, 0);
                myStylesPanel.add(getServerStylePanel(), gbc);
            }
        }
        return myStylesPanel;
    }

    /**
     * Gets the style type combobox.
     *
     * @return the style type combobox
     */
    private ExtendedComboBox<WMSLayerGetMapConfig.StyleType> getStyleTypeCombobox()
    {
        if (myStyleTypeCombobox == null)
        {
            WMSLayerGetMapConfig.StyleType[] types = WMSLayerGetMapConfig.StyleType.values();
            myStyleTypeCombobox = new ExtendedComboBox<>(types, 200);
            myStyleTypeCombobox.setSize(new Dimension(100, 20));
            myStyleTypeCombobox.setMinimumSize(myStyleTypeCombobox.getSize());
            myStyleTypeCombobox.setPreferredSize(myStyleTypeCombobox.getSize());
            myStyleTypeCombobox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getSource().equals(myStyleTypeCombobox))
                    {
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.fill = GridBagConstraints.HORIZONTAL;
                        gbc.gridx = 2;
                        gbc.gridy = 0;
                        gbc.weightx = 1.0;
                        String type = myStyleTypeCombobox.getSelectedItem().toString();
                        if (WMSLayerGetMapConfig.StyleType.SERVER.toString().equals(type))
                        {
                            getStyleTypePanel().remove(getClientStylesPanel());
                            gbc.insets = new Insets(0, 35, 0, 0);
                            getStyleTypePanel().add(getServerStylePanel(), gbc);
                        }
                        else if (WMSLayerGetMapConfig.StyleType.CLIENT.toString().equals(type))
                        {
                            getStyleTypePanel().remove(getServerStylePanel());
                            gbc.insets = new Insets(0, 35, 0, 0);
                            getStyleTypePanel().add(getClientStylesPanel(), gbc);
                        }
                        getStyleTypePanel().revalidate();
                    }
                }
            });
            myStyleTypeCombobox.setSelectedItem(WMSLayerGetMapConfig.StyleType.SERVER);
        }
        return myStyleTypeCombobox;
    }

    /**
     * Gets the style type panel.
     *
     * @return the style type panel
     */
    private JPanel getStyleTypePanel()
    {
        if (myStyleTypePanel == null)
        {
            myStyleTypePanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 12, 0, 0);
            myStyleTypePanel.add(new JLabel("Style Type:"), gbc);

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.weightx = 1.0;
            myStyleTypePanel.add(getStyleTypeCombobox(), gbc);

            StyleType style = myLayerConfigSet.getLayerConfig().getGetMapConfig().getStyleType();
            if (style != null)
            {
                getStyleTypeCombobox().setSelectedItem(style);
                getClientStylesCombobox().setSelectedItem(myLayerConfigSet.getLayerConfig().getGetMapConfig().getStyle());
            }
        }
        return myStyleTypePanel;
    }

    /**
     * This method initializes myTransparentComboBox.
     *
     * @return CustomComboBox transparent combo box.
     */
    private ExtendedComboBox<String> getTransparentComboBox()
    {
        myTransparentComboBox = new ExtendedComboBox<>(100);
        myTransparentComboBox.setSize(new Dimension(100, 20));
        myTransparentComboBox.setMinimumSize(myTransparentComboBox.getSize());
        myTransparentComboBox.setPreferredSize(myTransparentComboBox.getSize());
        myTransparentComboBox.addItem("TRUE");
        myTransparentComboBox.addItem("FALSE");

        Boolean curTrans = myLayerConfigSet.getLayerConfig().getGetMapConfig().getTransparent();
        if (curTrans != null && !curTrans.booleanValue())
        {
            myTransparentComboBox.setSelectedIndex(1);
        }

        return myTransparentComboBox;
    }

    /**
     * This method initializes myWidthTextField.
     *
     * @return JTextField width text field.
     */
    private JTextField getWidthTextField()
    {
        myWidthTextField = new JTextField();
        myWidthTextField.setSize(new Dimension(100, 20));
        myWidthTextField.setMinimumSize(myWidthTextField.getSize());
        myWidthTextField.setPreferredSize(myWidthTextField.getSize());

        Integer fixedWidth = myLayerConfigSet.getLayerConfig().getFixedWidth();
        if (fixedWidth != null && fixedWidth.intValue() != 0)
        {
            myWidthTextField.setText(fixedWidth.toString());
            myLayerConfigSet.getLayerConfig().getGetMapConfig().setTextureHeight(fixedWidth);
            myWidthTextField.setEditable(false);
        }
        else
        {
            myWidthTextField.setText(myLayerConfigSet.getLayerConfig().getGetMapConfig().getTextureHeight().toString());
            myWidthTextField.setEditable(true);
        }

        return myWidthTextField;
    }

    /**
     * Show sld builder.
     *
     * @param layerConfigSet the layer config set
     * @param object the object
     */
    private void showSldBuilder(WMSLayerConfigurationSet layerConfigSet, Object object)
    {
        mySldDialog = new SldBuilderDialog(mySldRegistry, layerConfigSet, null);
        mySldDialog.setVisible(true);
    }

    /**
     * Validate the contents of the string to confirm that it contains a value
     * which can be converted to a double or that it is empty when empty values
     * are allowed.
     *
     * @param text The text to validate.
     * @param allowEmpty When true, null or empty values will be allowed.
     * @return true when the text contains a valid value.
     */
    private boolean validateDouble(String text, boolean allowEmpty)
    {
        if (text == null || text.isEmpty())
        {
            return allowEmpty;
        }

        try
        {
            Double.parseDouble(text);
        }
        catch (NumberFormatException e)
        {
            return false;
        }

        return true;
    }

    /**
     * Action listener class for text fields which required a floating point
     * value.
     */
    private class DoubleTextActionListener implements ActionListener
    {
        /**
         * When true, this listener will allow null or empty values in the
         * field.
         */
        private final boolean myAllowEmpty;

        /** The text field to which this listener is attached. */
        private final JTextField myTextField;

        /**
         * Constructor.
         *
         * @param field The text field to which this listener is attached.
         * @param allowEmpty When true, this listener will allow null or empty
         *            values in the field.
         */
        public DoubleTextActionListener(JTextField field, boolean allowEmpty)
        {
            myTextField = field;
            myAllowEmpty = allowEmpty;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (!validateDouble(myTextField.getText(), myAllowEmpty))
            {
                myTextField.setText(myFocusFieldValue);
            }
            else
            {
                myFocusFieldValue = myTextField.getText();
            }
        }
    }

    /**
     * Focus listener class for text fields which required a floating point
     * value.
     */
    private class DoubleTextFocusListener implements FocusListener
    {
        /**
         * When true, this listener will allow null or empty values in the
         * field.
         */
        private final boolean myAllowEmpty;

        /** The text field to which this listener is attached. */
        private final JTextField myTextField;

        /**
         * Constructor.
         *
         * @param field The text field to which this listener is attached.
         * @param allowEmpty When true, this listener will allow null or empty
         *            values in the field.
         */
        public DoubleTextFocusListener(JTextField field, boolean allowEmpty)
        {
            myTextField = field;
            myAllowEmpty = allowEmpty;
        }

        @Override
        public void focusGained(FocusEvent e)
        {
            myFocusFieldValue = myTextField.getText();
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            if (!validateDouble(myTextField.getText(), myAllowEmpty))
            {
                myTextField.setText(myFocusFieldValue);
                if (Utilities.sameInstance(e.getOppositeComponent(), getSaveButton()))
                {
                    myAllowCommit = false;
                }
            }
        }
    }
}
