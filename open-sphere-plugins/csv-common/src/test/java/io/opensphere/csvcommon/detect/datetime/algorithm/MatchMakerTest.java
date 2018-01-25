package io.opensphere.csvcommon.detect.datetime.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.ClasspathPreferencesPersistenceManager;
import io.opensphere.core.preferences.InternalPreferencesIF;
import io.opensphere.csvcommon.common.datetime.ConfigurationProvider;
import io.opensphere.csvcommon.detect.datetime.algorithm.MatchMaker;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.util.CsvTestUtils;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the MatchMaker class.
 *
 */
public class MatchMakerTest
{
    /**
     * Tests the match maker and verifies that it gets the correct time columns
     * and successful counts.
     */
    @Test
    public void testFindPotentialDates()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig config = createDateFormats();

        DateFormat emptyPattern = new DateFormat();
        emptyPattern.setType(Type.TIMESTAMP);
        emptyPattern.setSdf("yyyy M d'T'HH:mm:ss'Z'");

        config.addFormat(emptyPattern);

        ConfigurationProvider provider = support.createMock(ConfigurationProvider.class);
        provider.getDateFormats();
        EasyMock.expectLastCall().andReturn(config);

        List<List<String>> multipleTimesData = CsvTestUtils.createMultipleTimesData();

        support.replayAll();

        MatchMaker matchMaker = new MatchMaker();
        Map<Integer, PotentialColumn> potentialDates = matchMaker.findPotentialDates(multipleTimesData, provider);

        PotentialColumn potential = potentialDates.get(0);

        assertNotNull(potential);
        assertEquals(0, potential.getColumnIndex());
        SuccessfulFormat format = potential.getFormats().get("yyyy-M-d'T'HH:mm:ss'Z'");

        assertNotNull(format);
        assertEquals("yyyy-M-d'T'HH:mm:ss'Z'", format.getFormat().getSdf());
        assertEquals(multipleTimesData.size(), format.getNumberOfSuccesses());
        assertTrue(potential.getFormats().containsKey("yyyy M d'T'HH:mm:ss'Z'"));

        potential = potentialDates.get(3);

        assertNotNull(potential);
        assertEquals(3, potential.getColumnIndex());
        format = potential.getFormats().get("HH:mm:ss");

        assertNotNull(format);
        assertEquals("HH:mm:ss", format.getFormat().getSdf());
        assertEquals(multipleTimesData.size() - multipleTimesData.size() / 5, format.getNumberOfSuccesses());

        potential = potentialDates.get(6);

        assertNotNull(potential);
        assertEquals(6, potential.getColumnIndex());
        format = potential.getFormats().get("MM-dd-yyyy");

        assertNotNull(format);
        assertEquals("MM-dd-yyyy", format.getFormat().getSdf());
        assertEquals(multipleTimesData.size() - multipleTimesData.size() / 10, format.getNumberOfSuccesses());

        support.verifyAll();
    }

    /**
     * Creates the date formats config.
     *
     * @return the date formats config.
     */
    private DateFormatsConfig createDateFormats()
    {
        ClasspathPreferencesPersistenceManager manager = new ClasspathPreferencesPersistenceManager();
        InternalPreferencesIF preferences = manager.load(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC, null, false);

        return preferences.getJAXBObject(DateFormatsConfig.class, "DateFormatConfig", null);
    }
}
