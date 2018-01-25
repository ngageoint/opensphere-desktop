package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.ClasspathPreferencesPersistenceManager;
import io.opensphere.core.preferences.InternalPreferencesIF;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.OneDayMultipleTimesDecider;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.detect.datetime.util.DateDataGenerator;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the OneDayMultipleTimesDecider class.
 *
 */
public class OneDayMultipleTimesDeciderTest
{
    /**
     * Tests both the calculate and compile functions.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void test() throws ParseException
    {
        List<DateFormat> dateFormats1 = getDateFormats(Type.DATE);
        List<DateFormat> timeFormats1 = getDateFormats(Type.TIME);
        List<DateFormat> timeFormats2 = getDateFormats(Type.TIME);

        OneDayMultipleTimesDecider decider = new OneDayMultipleTimesDecider();

        DateFormat additionalDateFormat = new DateFormat();
        additionalDateFormat.setSdf("HH:mm:ss");
        additionalDateFormat.setType(Type.TIME);

        PotentialColumn dateColumn1 = new PotentialColumn();
        dateColumn1.setColumnIndex(1);

        PotentialColumn timeColumn1 = new PotentialColumn();
        timeColumn1.setColumnIndex(2);

        PotentialColumn timeColumn2 = new PotentialColumn();
        timeColumn2.setColumnIndex(4);

        PotentialColumn timeColumn3 = new PotentialColumn();
        timeColumn3.setColumnIndex(3);

        List<PotentialColumn> potentials = New.list();
        potentials.add(dateColumn1);
        potentials.add(timeColumn1);
        potentials.add(timeColumn2);
        potentials.add(timeColumn3);

        for (DateFormat dateFormat1 : dateFormats1)
        {
            for (DateFormat timeFormat1 : timeFormats1)
            {
                for (DateFormat timeFormat2 : timeFormats2)
                {
                    List<List<String>> data = DateDataGenerator.generateDayTimeUpTimeDown(dateFormat1, timeFormat1, timeFormat2);

                    SuccessfulFormat successfulFormat = new SuccessfulFormat();
                    successfulFormat.setNumberOfSuccesses(data.size());
                    successfulFormat.setFormat(dateFormat1);

                    dateColumn1.getFormats().clear();
                    dateColumn1.getFormats().put(dateFormat1.getSdf(), successfulFormat);

                    successfulFormat = new SuccessfulFormat();
                    successfulFormat.setNumberOfSuccesses(data.size());
                    successfulFormat.setFormat(timeFormat1);

                    timeColumn1.getFormats().clear();
                    timeColumn1.getFormats().put(timeFormat1.getSdf(), successfulFormat);

                    successfulFormat = new SuccessfulFormat();
                    successfulFormat.setNumberOfSuccesses(data.size());
                    successfulFormat.setFormat(timeFormat2);

                    timeColumn2.getFormats().clear();
                    timeColumn2.getFormats().put(timeFormat2.getSdf(), successfulFormat);

                    successfulFormat = new SuccessfulFormat();
                    successfulFormat.setNumberOfSuccesses(data.size());
                    successfulFormat.setFormat(additionalDateFormat);

                    timeColumn3.getFormats().clear();
                    timeColumn3.getFormats().put(additionalDateFormat.getSdf(), successfulFormat);

                    List<Pair<PotentialColumn, Integer>> scores = decider.calculateConfidence(potentials, data);

                    assertEquals(3, scores.size());
                    assertEquals(1, scores.get(0).getFirstObject().getColumnIndex());
                    assertEquals(2, scores.get(1).getFirstObject().getColumnIndex());
                    assertEquals(4, scores.get(2).getFirstObject().getColumnIndex());

                    List<Pair<DateColumn, Integer>> results = decider.compileResults(scores);

                    assertEquals(Type.TIMESTAMP, results.get(0).getFirstObject().getDateColumnType());

                    assertEquals(dateFormat1.getSdf(), results.get(0).getFirstObject().getPrimaryColumnFormat());
                    assertEquals(1, results.get(0).getFirstObject().getPrimaryColumnIndex());
                    assertEquals(timeFormat1.getSdf(), results.get(0).getFirstObject().getSecondaryColumnFormat());
                    assertEquals(2, results.get(0).getFirstObject().getSecondaryColumnIndex());

                    assertEquals(Type.TIMESTAMP, results.get(1).getFirstObject().getDateColumnType());

                    assertEquals(dateFormat1.getSdf(), results.get(1).getFirstObject().getPrimaryColumnFormat());
                    assertEquals(1, results.get(1).getFirstObject().getPrimaryColumnIndex());
                    assertEquals(timeFormat2.getSdf(), results.get(1).getFirstObject().getSecondaryColumnFormat());
                    assertEquals(4, results.get(1).getFirstObject().getSecondaryColumnIndex());
                }
            }
        }
    }

    /**
     * Gets the date formats.
     *
     * @param dateType The type of formats to retrieve.
     * @return The list of known configured date formats.
     */
    private List<DateFormat> getDateFormats(Type dateType)
    {
        ClasspathPreferencesPersistenceManager manager = new ClasspathPreferencesPersistenceManager();
        InternalPreferencesIF preferences = manager.load(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC, null, false);

        DateFormatsConfig config = preferences.getJAXBObject(DateFormatsConfig.class, "DateFormatConfig", null);

        List<DateFormat> formats = New.list();

        for (DateFormat format : config.getFormats())
        {
            if (format.getType() == dateType)
            {
                formats.add(format);
            }
        }

        return formats;
    }
}
