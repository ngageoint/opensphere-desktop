package io.opensphere.controlpanels.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.common.io.Files;

import io.opensphere.core.Notify;
import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Unit test for {@link StateVersionChecker}.
 */
public class StateVersionCheckerTest
{
    /**
     * The expected state string.
     */
    private static final String ourExpectedState = "<state source=\"OpenSphere\" xmlns=\"http://www.bit-sys.com/state/v2\"/>";

    /**
     * Tests a large binary file and verifies it can return.
     *
     * @throws IOException Bad IO.
     * @throws ParserConfigurationException Bad parse.
     * @throws SAXException Bad SAX.
     */
    @Test(expected = SAXException.class)
    public void testLargeBinaryFile() throws IOException, SAXException, ParserConfigurationException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        Notify.setToolbox(toolbox);
        File file = File.createTempFile("statetest", ".klv");
        try
        {
            byte[] largeBytes = new byte[1000000000];
            Files.write(largeBytes, file);
            FileInputStream fileStream = new FileInputStream(file);
            BufferedInputStream buffered = new BufferedInputStream(fileStream);
            StateVersionChecker checker = new StateVersionChecker();
            InputStream goodStream = checker.checkVersion(buffered, false).getFirstObject();

            assertEquals(1000000000, goodStream.available());
        }
        finally
        {
            file.delete();
            Notify.setToolbox(null);
        }

        support.verifyAll();
    }

    /**
     * Tests files that are not state files.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws SAXException Bad SAX.
     * @throws IOException Bad IO.
     */
    @Test
    public void testNonStateFile() throws IOException, SAXException, ParserConfigurationException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        Notify.setToolbox(toolbox);
        try
        {
            String nonState = "<filters xmlns:ogc=\"http://www.opengis.net/ogc\"/>";
            InputStream stateStream = new ByteArrayInputStream(nonState.getBytes(StringUtilities.DEFAULT_CHARSET));
            StateVersionChecker checker = new StateVersionChecker();
            InputStream goodStream = checker.checkVersion(stateStream, false).getFirstObject();

            StreamReader reader = new StreamReader(goodStream);
            String goodState = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

            assertEquals(nonState, goodState);
        }
        finally
        {
            Notify.setToolbox(null);
        }

        support.verifyAll();
    }

    /**
     * Tests v2 state files.
     *
     * @throws IOException Bad IO.
     * @throws ParserConfigurationException Bad parse.
     * @throws SAXException Bad sax.
     */
    @Test
    public void testV2() throws IOException, SAXException, ParserConfigurationException
    {
        InputStream stateStream = new ByteArrayInputStream(ourExpectedState.getBytes(StringUtilities.DEFAULT_CHARSET));
        StateVersionChecker checker = new StateVersionChecker();
        InputStream goodStream = checker.checkVersion(stateStream, true).getFirstObject();

        StreamReader reader = new StreamReader(goodStream);
        String goodState = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

        assertEquals(ourExpectedState, goodState);
    }

    /**
     * Tests v2 state files.
     *
     * @throws IOException Bad IO.
     * @throws ParserConfigurationException Bad parse.
     * @throws SAXException Bad sax.
     */
    @Test
    public void testV2FromFile() throws IOException, SAXException, ParserConfigurationException
    {
        File test = File.createTempFile("test", "state");
        try
        {
            StringBuilder builder = new StringBuilder(106);
            builder.append("<state source=\"OpenSphere\" xmlns=\"http://www.bit-sys.com/state/v2\">");
            for (int i = 0; i < 1000; i++)
            {
                builder.append("<fakeElement></fakeElement>");
            }
            builder.append("</state>");

            String stateString = builder.toString();

            byte[] stateBytes = stateString.getBytes(StringUtilities.DEFAULT_CHARSET);
            Files.write(stateBytes, test);

            InputStream stateStream = new BufferedInputStream(new FileInputStream(test));
            StateVersionChecker checker = new StateVersionChecker();
            InputStream goodStream = checker.checkVersion(stateStream, true).getFirstObject();

            StreamReader reader = new StreamReader(goodStream);
            String goodState = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

            assertEquals(stateString, goodState);
        }
        finally
        {
            test.delete();
        }
    }

    /**
     * Tests v3 state files.
     *
     * @throws IOException Bad IO.
     * @throws ParserConfigurationException Bad parse.
     * @throws SAXException Bad SAX.
     */
    @Test
    public void testV3() throws IOException, SAXException, ParserConfigurationException
    {
        String v3State = "<state source=\"OpenSphere\" xmlns=\"http://www.bit-sys.com/state/v3\"/>";
        InputStream stateStream = new ByteArrayInputStream(v3State.getBytes(StringUtilities.DEFAULT_CHARSET));
        StateVersionChecker checker = new StateVersionChecker();
        InputStream goodStream = checker.checkVersion(stateStream, true).getFirstObject();

        StreamReader reader = new StreamReader(goodStream);
        String goodState = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

        assertEquals(ourExpectedState, goodState);
    }

    /**
     * Tests v4 state files.
     *
     * @throws IOException Bad IO.
     * @throws ParserConfigurationException Bad parse.
     * @throws SAXException Bad Sax
     */
//    @Test
    public void testV4() throws IOException, SAXException, ParserConfigurationException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        Notify.setToolbox(toolbox);
        try
        {
            String v4State = "<state source=\"OpenSphere\" xmlns=\"http://www.bit-sys.com/state/v4\"/>";
            InputStream stateStream = new ByteArrayInputStream(v4State.getBytes(StringUtilities.DEFAULT_CHARSET));
            StateVersionChecker checker = new StateVersionChecker();
            InputStream goodStream = checker.checkVersion(stateStream, true).getFirstObject();

            StreamReader reader = new StreamReader(goodStream);
            String goodState = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

            assertEquals(ourExpectedState, goodState);
        }
        finally
        {
            Notify.setToolbox(null);
        }

        support.verifyAll();
    }

    /**
     * Tests v4 state files.
     *
     * @throws IOException Bad IO.
     * @throws SAXException Bad xml.
     * @throws ParserConfigurationException Bad parse.
     */
    public void testV4NoNotify() throws IOException, SAXException, ParserConfigurationException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        Notify.setToolbox(toolbox);
        try
        {
            String v4State = "<state source=\"OpenSphere\" xmlns=\"http://www.bit-sys.com/state/v4\"/>";
            InputStream stateStream = new ByteArrayInputStream(v4State.getBytes(StringUtilities.DEFAULT_CHARSET));
            StateVersionChecker checker = new StateVersionChecker();
            InputStream goodStream = checker.checkVersion(stateStream, false).getFirstObject();

            StreamReader reader = new StreamReader(goodStream);
            String goodState = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

            assertEquals(ourExpectedState, goodState);
        }
        finally
        {
            Notify.setToolbox(null);
        }

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.publishEvent(EasyMock.isA(UserMessageEvent.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            UserMessageEvent event = (UserMessageEvent)EasyMock.getCurrentArguments()[0];

            assertEquals("Unrecognized state file version, there may be issues loading the data from this file.",
                    event.getMessage());
            assertTrue(event.isShowToast());
            assertFalse(event.isMakeVisible());
            assertEquals(Type.WARNING, event.getType());

            return null;
        });

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);

        return toolbox;
    }
}
