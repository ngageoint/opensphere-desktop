package io.opensphere.osh.aerialimagery.results;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.Output;

/**
 * Unit test for {@link LocationParser}.
 */
public class LocationParserTest
{
    /**
     * Tests parsing the locations.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void test() throws IOException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        String testData = "2015-12-19T21:01:29.231Z,34.6905037,-86.5819168,183.99\n"
                + "2015-12-19T21:01:29.321Z,34.6905037,-86.5819167,183.98\n"
                + "2015-12-19T21:01:29.521Z,34.6905036,-86.5819167,183.99";
        Output output = createTestOutput();

        EasyMockSupport support = new EasyMockSupport();

        UIRegistry uiRegistry = createUIRegistry(support, false);

        support.replayAll();

        List<PlatformMetadata> metadatas = New.list();
        LocationParser parser = new LocationParser(uiRegistry);
        ByteArrayInputStream stream = new ByteArrayInputStream(testData.getBytes(StringUtilities.DEFAULT_CHARSET));
        parser.parse(output, new CancellableInputStream(stream, null), metadatas);

        assertEquals(3, metadatas.size());

        assertEquals("Sat Dec 19 21:01:29 GMT 2015", metadatas.get(0).getTime().toString());
        assertEquals(231, metadatas.get(0).getTime().getTime() % 1000);
        assertEquals(LatLonAlt.createFromDegreesMeters(34.6905037, -86.5819168, 183.99, ReferenceLevel.ELLIPSOID),
                metadatas.get(0).getLocation());

        assertEquals("Sat Dec 19 21:01:29 GMT 2015", metadatas.get(1).getTime().toString());
        assertEquals(321, metadatas.get(1).getTime().getTime() % 1000);
        assertEquals(LatLonAlt.createFromDegreesMeters(34.6905037, -86.5819167, 183.98, ReferenceLevel.ELLIPSOID),
                metadatas.get(1).getLocation());

        assertEquals("Sat Dec 19 21:01:29 GMT 2015", metadatas.get(2).getTime().toString());
        assertEquals(521, metadatas.get(2).getTime().getTime() % 1000);
        assertEquals(LatLonAlt.createFromDegreesMeters(34.6905036, -86.5819167, 183.99, ReferenceLevel.ELLIPSOID),
                metadatas.get(2).getLocation());

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
        String testData = "2015-12-19T21:01:29.231Z,34.6905037,-86.5819168,183.99\n"
                + "2015-12-19T21:01:29.321Z,34.6905037,-86.5819167,183.98\n"
                + "2015-12-19T21:01:29.521Z,34.6905036,-86.5819167,183.99";
        Output output = createTestOutput();

        EasyMockSupport support = new EasyMockSupport();

        UIRegistry uiRegistry = createUIRegistry(support, true);

        support.replayAll();

        List<PlatformMetadata> metadatas = New.list();
        LocationParser parser = new LocationParser(uiRegistry);
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
        Output output = new Output("platformLoc");

        Field timeField = new Field("time");
        timeField.setProperty("http://www.opengis.net/def/property/OGC/0/SamplingTime");

        Field latField = new Field("lat");
        Field lonField = new Field("lon");
        Field alt = new Field("alt");

        output.getFields().add(timeField);
        output.getFields().add(latField);
        output.getFields().add(lonField);
        output.getFields().add(alt);

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
