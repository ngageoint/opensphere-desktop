package io.opensphere.wfs.util;

import org.easymock.EasyMock;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.OrderManagerRegistryImpl;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.PlayState;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;

/**
 * The Class WFSTestToolbox.
 */
public final class WFSTestToolbox
{
    /**
     * Setup a test toolbox for testing purposes.
     *
     * @return the toolbox
     */
    public static Toolbox getToolbox()
    {
        ServerToolbox serverToolbox = EasyMock.createNiceMock(ServerToolbox.class);

        WFSLayerConfigurationManager configurationManager = new WFSLayerConfigurationManager();

        MantleToolbox mantleTb = EasyMock.createMock(MantleToolbox.class);

        // Add DataTypeInfo Preference Assistant to Mantle toolbox.
        DataTypeInfoPreferenceAssistant prefAsst = new DataTypeInfoPreferenceAssistant()
        {
            @Override
            public int getColorPreference(String dtiKey, int def)
            {
                return 0;
            }

            @Override
            public int getOpacityPreference(String dtiKey, int def)
            {
                return ColorUtilities.COLOR_COMPONENT_MAX_VALUE;
            }

            @Override
            public PlayState getPlayStatePreference(String dtiKey)
            {
                return PlayState.STOP;
            }

            @Override
            public boolean isVisiblePreference(String dtiKey)
            {
                return true;
            }

            @Override
            public boolean isVisiblePreference(String dtiKey, boolean def)
            {
                return true;
            }

            @Override
            public void removePreferences(String dtiKey)
            {
            }

            @Override
            public void removePreferences(String dtiKey, PreferenceType... typeToRemove)
            {
            }

            @Override
            public void removePreferencesForPrefix(String dtiKeyPrefix)
            {
            }

            @Override
            public void removePreferencesForPrefix(String dtiKeyPrefix, PreferenceType... typeToRemove)
            {
            }

            @Override
            public void setPlayStatePreference(String dtiKey, PlayState playState)
            {
            }

            @Override
            public void getBooleanPreference(BooleanModel property, String dtiKey)
            {
            }

            @Override
            public void setBooleanPreference(BooleanModel property, String dtiKey)
            {
            }
        };
        EasyMock.expect(mantleTb.getDataTypeInfoPreferenceAssistant()).andReturn(prefAsst).anyTimes();
        EasyMock.replay(mantleTb);

        // Add mantle toolbox to plugin toolbox registry.
        PluginToolboxRegistry pluginTb = EasyMock.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(pluginTb.getPluginToolbox(MantleToolbox.class)).andReturn(mantleTb).anyTimes();
        EasyMock.expect(pluginTb.getPluginToolbox(EasyMock.eq(ServerToolbox.class))).andReturn(serverToolbox).anyTimes();
        EasyMock.replay(pluginTb);

        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(configurationManager).anyTimes();
        EasyMock.replay(serverToolbox);

        ContextActionManager ctrlMgr = EasyMock.createNiceMock(ContextActionManager.class);
        UIRegistry uiRegistry = EasyMock.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getContextActionManager()).andReturn(ctrlMgr).anyTimes();
        EasyMock.replay(uiRegistry);

        // Create Core toolbox with test mantle toolbox.
        Toolbox coreToolbox = EasyMock.createMock(Toolbox.class);
        EasyMock.expect(coreToolbox.getPluginToolboxRegistry()).andReturn(pluginTb).anyTimes();
        EasyMock.expect(coreToolbox.getUIRegistry()).andReturn(uiRegistry).anyTimes();
        OrderManagerRegistry orderManagerReg = new OrderManagerRegistryImpl(null);
        EasyMock.expect(coreToolbox.getOrderManagerRegistry()).andReturn(orderManagerReg).anyTimes();
        EasyMock.replay(coreToolbox);

        return coreToolbox;
    }

    /** Disallow instantiation of utility class. */
    private WFSTestToolbox()
    {
    }
}
