package io.opensphere.controlpanels.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.util.XMLUtilities;

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
        EasyMock.replay(moduleStateManager, modules);

        Toolbox toolbox = EasyMock.createMock(Toolbox.class);
        StateControllerImpl controller = new StateControllerImpl(toolbox);
        controller.saveState(id, description, tags, modules, true, outputStream);

        EasyMock.verify(moduleStateManager, modules);

        XMLUtilities.newDocumentBuilderNS().parse(new ByteArrayInputStream(outputStream.toByteArray()));
    }
}
