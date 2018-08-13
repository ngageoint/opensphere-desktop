package io.opensphere.controlpanels.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;

/**
 * Test {@link StateControllerImpl}.
 */
public class StateControllerImplTest
{
    /**
     * Test
     * {@link StateControllerImpl#saveState(String, String, Collection, Collection, boolean, java.io.OutputStream)}
     * .
     *
     * @throws SAXException If the test fails.
     * @throws IOException If the test fails.
     * @throws ParserConfigurationException If the test fails.
     */
    @Test
    public void testSaveState() throws SAXException, IOException, ParserConfigurationException
    {
        ModuleStateManager moduleStateManager = EasyMock.createMock(ModuleStateManager.class);
        String id = "stateid";
        String description = "State description";
        Collection<? extends String> tags = Arrays.asList("tag1", "tag2", "tag3");
        @SuppressWarnings("unchecked")
        Collection<? extends String> modules = EasyMock.createMock(Collection.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final Capture<StateType> stateCapture = EasyMock.newCapture();
        moduleStateManager.saveState(EasyMock.eq(id), EasyMock.eq(description), EasyMock.same(tags), EasyMock.same(modules),
                EasyMock.isA(StateType.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                StateType state = (StateType)EasyMock.getCurrentArguments()[4];
                stateCapture.setValue(state);
                return null;
            }
        });
        moduleStateManager.registerState(EasyMock.eq(id), EasyMock.eq(description), EasyMock.same(tags), EasyMock.same(modules),
                EasyMockHelper.eq(stateCapture));
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, moduleStateManager);
        Collection<String> idCollection = New.collection();
        EasyMock.expect(moduleStateManager.getRegisteredStateIds()).andReturn(idCollection);
        EasyMock.replay(moduleStateManager, modules);
        support.replayAll();

        StateControllerImpl controller = new StateControllerImpl(toolbox);
        controller.saveState(id, description, tags, modules, true, outputStream);
        support.verifyAll();

        EasyMock.verify(moduleStateManager, modules);

        XMLUtilities.newDocumentBuilderNS().parse(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    /**
     * Creates a mocked {@link Toolbox}.
     *
     * @param support The mock support to create a toolbox.
     * @param moduleStateManager The ModuleStateManager used in creating the toolbox.
     * @return The newly mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, ModuleStateManager moduleStateManager)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getModuleStateManager()).andReturn(moduleStateManager);
        PluginToolboxRegistry pluginToolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginToolboxRegistry).atLeastOnce();
        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(pluginToolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantleToolbox).atLeastOnce();
        DataGroupController dataGroupController = support.createMock(DataGroupController.class);
        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(dataGroupController);
        DataTypeInfoPreferenceAssistant preferenceAssistant = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(mantleToolbox.getDataTypeInfoPreferenceAssistant()).andReturn(preferenceAssistant).atLeastOnce();
        EasyMock.expect(preferenceAssistant.isVisiblePreference("state:stateid")).andReturn(true).atLeastOnce();
        EasyMock.expect(preferenceAssistant.getColorPreference("state:stateid", -1)).andReturn(1);
        EasyMock.expect(preferenceAssistant.getOpacityPreference("state:stateid", 255)).andReturn(255);
        EasyMock.expect(dataGroupController.addRootDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(Object.class))).andReturn(true);
        OrderManagerRegistry orderManagerRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderManagerRegistry);
        OrderManager orderManager = support.createMock(OrderManager.class);
        EasyMock.expect(orderManagerRegistry.getOrderManager(EasyMock.anyString(), EasyMock.isA(OrderCategory.class))).andReturn(orderManager);
        orderManager.addParticipantChangeListener(EasyMock.isA(OrderChangeListener.class));
        EasyMock.expectLastCall();
        EventManager eventManager = support.createMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).atLeastOnce();
        eventManager.publishEvent(EasyMock.isA(AbstractDataGroupInfoChangeEvent.class));
        EasyMock.expectLastCall().atLeastOnce();
        DataRegistry dataRegistry = support.createNiceMock(DataRegistry.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).atLeastOnce();
        SimpleSessionOnlyCacheDeposit<StateType> cacheDeposit = support.createMock(SimpleSessionOnlyCacheDeposit.class);
        long[] longArray = new long[0];
        EasyMock.expect(dataRegistry.addModels(cacheDeposit)).andStubReturn(longArray);
        return toolbox;
    }
}
