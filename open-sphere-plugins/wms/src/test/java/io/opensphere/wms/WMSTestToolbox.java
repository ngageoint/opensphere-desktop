package io.opensphere.wms;

import java.util.Collection;
import java.util.HashMap;

import org.easymock.EasyMock;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.OrderManagerRegistryImpl;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.PlayState;

/**
 * The Class WFSTestToolbox.
 */
public final class WMSTestToolbox
{
    /**
     * Setup a test toolbox for testing purposes.
     *
     * @param replay flag indicating whether to replay the mocked toolbox before
     *            returning it
     * @return the toolbox
     */
    public static Toolbox getToolbox(boolean replay)
    {
        // Add Z-Order manager to mantle toolbox.
        MantleToolbox mantleTb = EasyMock.createMock(MantleToolbox.class);

        // Add DataGroupController to mantle toolbox.
        DataGroupController dgCtrl = EasyMock.createNiceMock(DataGroupController.class);
        EasyMock.expect(mantleTb.getDataGroupController()).andReturn(dgCtrl).anyTimes();

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
        EasyMock.replay(pluginTb);

        // Create Core toolbox with test mantle toolbox.
        Toolbox coreToolbox = EasyMock.createMock(Toolbox.class);
        EasyMock.expect(coreToolbox.getPluginToolboxRegistry()).andReturn(pluginTb).anyTimes();

        // Add animation manager
        AnimationManager animationMgr = EasyMock.createNiceMock(AnimationManager.class);
        EasyMock.expect(animationMgr.getCurrentPlan()).andReturn(null).anyTimes();
        EasyMock.replay(animationMgr);
        EasyMock.expect(coreToolbox.getAnimationManager()).andReturn(animationMgr).anyTimes();

        // Add event manager
        EventManager eventMgr = EasyMock.createNiceMock(EventManager.class);
        EasyMock.expect(coreToolbox.getEventManager()).andReturn(eventMgr).anyTimes();

        // Add mock time manager
        TimeManager timeMgr = EasyMock.createNiceMock(TimeManager.class);
        EasyMock.expect(timeMgr.getPrimaryActiveTimeSpans()).andReturn(TimeSpanList.singleton(TimeSpan.get())).anyTimes();
        EasyMock.expect(timeMgr.getSecondaryActiveTimeSpans()).andReturn(new HashMap<Object, Collection<? extends TimeSpan>>())
                .anyTimes();
        EasyMock.replay(timeMgr);
        EasyMock.expect(coreToolbox.getTimeManager()).andReturn(timeMgr).anyTimes();

        // Add control action manager
        ContextActionManager ctrlMgr = EasyMock.createNiceMock(ContextActionManager.class);
        UIRegistry uiRegistry = EasyMock.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getContextActionManager()).andReturn(ctrlMgr).anyTimes();
        EasyMock.replay(uiRegistry);
        EasyMock.expect(coreToolbox.getUIRegistry()).andReturn(uiRegistry).anyTimes();

        OrderManagerRegistry orderManagerReg = new OrderManagerRegistryImpl(null);
        EasyMock.expect(coreToolbox.getOrderManagerRegistry()).andReturn(orderManagerReg).anyTimes();

        if (replay)
        {
            EasyMock.replay(coreToolbox);
        }
        return coreToolbox;
    }

    /** Disallow instantiation of utility class. */
    private WMSTestToolbox()
    {
    }
}
