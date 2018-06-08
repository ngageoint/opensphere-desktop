package io.opensphere.wms.layer;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfigChangeListener.WMSLayerConfigChangeEvent;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.config.v1.WMSServerConfig;

/** WMS DataTypeInfoAssistant. */
public class WMSDataTypeInfoAssistant implements DataTypeInfoAssistant
{
    /** The WMS preferences. */
    private final Preferences myPrefs;

    /** The WMS Layer config. */
    @ThreadConfined("EDT")
    private WMSLayerConfigurationSet myWmsConfig;

    /** The layer labels. */
    private List<String> myLayerLabels;

    /**
     * Constructor.
     *
     * @param prefs the WMS preferences
     */
    public WMSDataTypeInfoAssistant(Preferences prefs)
    {
        myPrefs = prefs;
    }

    @Override
    public Component getLayerControlUIComponent(DataTypeInfo dataType)
    {
        myWmsConfig = ((WMSDataTypeInfo)dataType).getWmsConfig();

        if (myWmsConfig.getInheritedLayerConfig().getStyles().isEmpty())
        {
            return null;
        }

        GridBagPanel panel = new GridBagPanel();
        panel.add(new JLabel("Server Style "));
        panel.fillHorizontal().add(getStyleCombo());
        return panel;
    }

    @Override
    public List<Icon> getLayerIcons()
    {
        return List.of();
    }

    @Override
    public synchronized List<String> getLayerLabels()
    {
        if (myLayerLabels == null)
        {
            myLayerLabels = new CopyOnWriteArrayList<>();
        }
        return myLayerLabels;
    }

    /**
     * Creates the style combo box.
     *
     * @return the combo box
     */
    private Component getStyleCombo()
    {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setPreferredSize(comboBox.getPreferredSize());
        comboBox.setToolTipText("The server style to apply to the tiles");

        // Add an empty entry in case the user has no style
        comboBox.addItem("");
        for (String style : myWmsConfig.getInheritedLayerConfig().getStyles())
        {
            comboBox.addItem(style);
        }
        comboBox.setSelectedItem(myWmsConfig.getLayerConfig().getGetMapConfig().getStyle());

        comboBox.addActionListener(e -> saveStyle((String)comboBox.getSelectedItem()));

        return comboBox;
    }

    /**
     * Saves the style.
     *
     * @param selectedStyle the style selected by the user
     */
    private void saveStyle(String selectedStyle)
    {
        WMSLayerConfig layerConfig = myWmsConfig.getLayerConfig().clone();
        layerConfig.getGetMapConfig().setStyle(selectedStyle);

        WMSServerConfig serverConfig = myWmsConfig.getServerConfig();
        serverConfig.storeLayer(layerConfig);
        myPrefs.putJAXBObject(serverConfig.getServerId(), serverConfig, false, this);

        myWmsConfig.getLayerConfig().getGetMapConfig().setStyle(selectedStyle);

        serverConfig.notifyLayerConfigChanged(new WMSLayerConfigChangeEvent(myWmsConfig));
    }
}
