package io.opensphere.osh.aerialimagery.results;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.TimeZone;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.Output;

/**
 * Unit test for {@link PlatformOrientationParser}.
 */
public class PlatformOrientationParserTest
{
    /**
     * Tests parsing the locations.
     *
     * @throws IOException Bad IO.
     * @throws ParseException Bad parse.
     */
    @Test
    public void test() throws IOException, ParseException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        String testData = "2015-12-19T21:01:29.231Z,70.96,-58.82,0.1\n" + "2015-12-19T21:01:29.321Z,71.06,-59.8,1.2\n"
                + "2015-12-19T21:01:29.521Z,70.83,-56.27,2.2";
        Output output = createTestOutput();

        EasyMockSupport support = new EasyMockSupport();

        UIRegistry uiRegistry = createUIRegistry(support, false);

        support.replayAll();

        List<PlatformMetadata> metadatas = New.list();
        PlatformOrientationParser parser = new PlatformOrientationParser(uiRegistry);
        ByteArrayInputStream stream = new ByteArrayInputStream(testData.getBytes(StringUtilities.DEFAULT_CHARSET));
        parser.parse(output, new CancellableInputStream(stream, null), metadatas);

        assertEquals(3, metadatas.size());

        assertEquals(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.231Z").getTime(),
                metadatas.get(0).getTime().getTime());
        assertEquals(70.96, metadatas.get(0).getYawAngle(), 0d);
        assertEquals(-58.82, metadatas.get(0).getPitchAngle(), 0d);
        assertEquals(0.1, metadatas.get(0).getRollAngle(), 0d);

        assertEquals(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.321Z").getTime(),
                metadatas.get(1).getTime().getTime());
        assertEquals(71.06, metadatas.get(1).getYawAngle(), 0d);
        assertEquals(-59.8, metadatas.get(1).getPitchAngle(), 0d);
        assertEquals(1.2, metadatas.get(1).getRollAngle(), 0d);

        assertEquals(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.521Z").getTime(),
                metadatas.get(2).getTime().getTime());
        assertEquals(70.83, metadatas.get(2).getYawAngle(), 0d);
        assertEquals(-56.27, metadatas.get(2).getPitchAngle(), 0d);
        assertEquals(2.2, metadatas.get(2).getRollAngle(), 0d);

        support.verifyAll();
    }

    /**
     * Tests parsing the locations and getting cancelled.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testCancel() throws IOException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        String testData = "2015-12-19T21:01:29.231Z,70.96,-58.82,0.1\n" + "2015-12-19T21:01:29.321Z,71.06,-59.8,1.2\n"
                + "2015-12-19T21:01:29.521Z,70.83,-56.27,2.2";
        Output output = createTestOutput();

        EasyMockSupport support = new EasyMockSupport();

        UIRegistry uiRegistry = createUIRegistry(support, true);

        support.replayAll();

        List<PlatformMetadata> metadatas = New.list();
        PlatformOrientationParser parser = new PlatformOrientationParser(uiRegistry);
        ByteArrayInputStream stream = new ByteArrayInputStream(testData.getBytes(StringUtilities.DEFAULT_CHARSET));
        parser.parse(output, new CancellableInputStream(stream, null), metadatas);

        assertEquals(1, metadatas.size());

        support.verifyAll();
    }

    /**
     * Creates the test output object.
     *
     * @return The test {@link Output}.
     */
    private Output createTestOutput()
    {
        Output output = new Output("platformAtt");

        Field timeField = new Field("time");
        timeField.setProperty("http://www.opengis.net/def/property/OGC/0/SamplingTime");

        Field yaw = new Field("yaw");
        Field pitch = new Field("pitch");
        Field roll = new Field("roll");

        output.getFields().add(timeField);
        output.getFields().add(yaw);
        output.getFields().add(pitch);
        output.getFields().add(roll);

        return output;
    }

    /**
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @param isCancel True if we are to simulate the download being cancelled.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support, boolean isCancel)
    {
        MenuBarRegistry menuRegistry = support.createMock(MenuBarRegistry.class);
        menuRegistry.addTaskActivity(EasyMock.isA(CancellableTaskActivity.class));
        if (isCancel)
        {
            EasyMock.expectLastCall().andAnswer(() ->
            {
                ((CancellableTaskActivity)EasyMock.getCurrentArguments()[0]).setCancelled(true);
                return null;
            });
        }

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(menuRegistry);

        return uiRegistry;
    }
}
