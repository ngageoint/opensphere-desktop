package io.opensphere.wps.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import net.opengis.ows._110.CodeType;
import net.opengis.wps._100.InputDescriptionType;
import net.opengis.wps._100.ProcessDescriptionType;
import net.opengis.wps._100.ProcessDescriptionType.DataInputs;

/**
 * Unit test for {@link WpsProcessConfiguration}.
 */
public class WpsProcessConfigurationTest
{
    /**
     * Tests serializing the configuration.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        String input1 = "column1";
        String input3 = "column3";

        ProcessDescriptionType description = new ProcessDescriptionType();
        DataInputs dataInputs = new DataInputs();
        description.setDataInputs(dataInputs);

        InputDescriptionType descriptionType = new InputDescriptionType();
        CodeType codeType = new CodeType();
        codeType.setValue(input1);
        descriptionType.setIdentifier(codeType);
        dataInputs.getInput().add(descriptionType);

        descriptionType = new InputDescriptionType();
        codeType = new CodeType();
        codeType.setValue(input3);
        descriptionType.setIdentifier(codeType);
        dataInputs.getInput().add(descriptionType);

        WpsProcessConfiguration configuration = new WpsProcessConfiguration("serverId", description);
        configuration.setProcessIdentifier("processId");
        configuration.setProcessTitle("processTitle");
        configuration.setRunMode(WpsExecutionMode.SAVE_AND_RUN);

        configuration.getInputs().put(input1, "value1");
        configuration.getInputs().put(input3, "value3");

        WpsDataTypeInfo layer = new WpsDataTypeInfo(toolbox, "wps", "typeKey", "typeName", "displayName",
                new DefaultMetaDataInfo());
        configuration.setResultType(layer);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);
        objectOut.writeObject(configuration);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);
        WpsProcessConfiguration actual = (WpsProcessConfiguration)objectIn.readObject();

        assertEquals("processId", actual.getProcessIdentifier());
        assertEquals("processTitle", actual.getProcessTitle());
        assertEquals(WpsExecutionMode.SAVE_AND_RUN, actual.getRunMode());
        assertNull(actual.getResultType());

        assertEquals("value1", actual.getInputs().get(input1));
        assertEquals("value3", actual.getInputs().get(input3));

        assertEquals(input1, actual.getProcessDescription().getDataInputs().getInput().get(0).getIdentifier().getValue());
        assertEquals(input3, actual.getProcessDescription().getDataInputs().getInput().get(1).getIdentifier().getValue());

        assertNotNull(configuration.getInstanceId());
        assertEquals(configuration.getInstanceId(), actual.getInstanceId());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the toolbox.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        DataTypeInfoPreferenceAssistant prefAssist = support.createNiceMock(DataTypeInfoPreferenceAssistant.class);

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getDataTypeInfoPreferenceAssistant()).andReturn(prefAssist).anyTimes();

        ServerToolbox serverToolbox = support.createMock(ServerToolbox.class);

        WFSLayerConfigurationManager configurationManager = new WFSLayerConfigurationManager();
        EasyMock.expect(serverToolbox.getLayerConfigurationManager()).andReturn(configurationManager).anyTimes();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantle).anyTimes();
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(ServerToolbox.class))).andReturn(serverToolbox).anyTimes();

        OrderManagerRegistry orderRegistry = support.createNiceMock(OrderManagerRegistry.class);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).anyTimes();

        return toolbox;
    }
}
