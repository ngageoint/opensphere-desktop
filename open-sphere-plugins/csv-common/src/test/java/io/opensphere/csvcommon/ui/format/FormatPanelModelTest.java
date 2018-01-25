package io.opensphere.csvcommon.ui.format;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.ValidationStatus;
import io.opensphere.csvcommon.common.ColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.ListLineSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.DelimitedColumnFormatParameters;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.csvcommon.util.CsvTestUtils;

/** Test for {@link FormatPanelModel}. */
public class FormatPanelModelTest
{
    /**
     * Test the behavior.
     */
    @Test
    public void testBehavior()
    {
        doAwt(() ->
        {
            FormatPanelModel model = new FormatPanelModel(null, new CSVParseParameters(), new DetectedParameters(),
                    new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",")));

            model.getHeaderRow().set(Integer.valueOf(5));
            Assert.assertEquals(6, model.getFirstDataRow().getMin());
            Assert.assertEquals(6, model.getFirstDataRow().get().intValue());

            model.getHeaderRow().set(Integer.valueOf(0));
            Assert.assertEquals(1, model.getFirstDataRow().getMin());
            Assert.assertEquals(6, model.getFirstDataRow().get().intValue());

            model.getColumnFormat().set(ColumnFormat.FIXED_WIDTH);
            Assert.assertFalse(model.getColumnDelimiter().isEnabled());
            Assert.assertFalse(model.getQuoteCharacter().isEnabled());

            model.getColumnFormat().set(ColumnFormat.DELIMITED);
            Assert.assertTrue(model.getColumnDelimiter().isEnabled());
            Assert.assertTrue(model.getQuoteCharacter().isEnabled());
        });
    }

    /**
     * Test the parameter values (default values).
     */
    @Test
    public void testParameterValuesDefault()
    {
        doAwt(() ->
        {
            FormatPanelModel model = new FormatPanelModel(null, new CSVParseParameters(), new DetectedParameters(),
                    new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",")));

            Assert.assertEquals(1, model.getHeaderRow().get().intValue());
            Assert.assertEquals(2, model.getFirstDataRow().get().intValue());
            Assert.assertFalse(model.getHasHeader().get().booleanValue());
            Assert.assertEquals('#', model.getCommentCharacter().get().charValue());
            Assert.assertNull(model.getColumnFormat().get());
            Assert.assertEquals(',', model.getColumnDelimiter().get().charValue());
            Assert.assertEquals(",", model.getCustomDelimiter().get());
            Assert.assertEquals('"', model.getQuoteCharacter().get().charValue());
            Assert.assertTrue(model.getColumnDivisions().length == 0);
        });
    }

    /**
     * Test the parameter values (delimited happy path).
     */
    @Test
    public void testParameterValuesDelimitedHappy()
    {
        doAwt(() ->
        {
            CSVParseParameters parameters = new CSVParseParameters();
            parameters.setHeaderLine(Integer.valueOf(2));
            parameters.setDataStartLine(Integer.valueOf(4));
            parameters.setCommentIndicator("!");
            parameters.setColumnFormat(new CSVDelimitedColumnFormat("|", "'", 10));

            FormatPanelModel model = new FormatPanelModel(null, parameters, new DetectedParameters(),
                    new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",")));

            if (model.getValidationStatus() != ValidationStatus.VALID)
            {
                Assert.fail(model.getErrorMessage());
            }

            Assert.assertEquals(3, model.getHeaderRow().get().intValue());
            Assert.assertEquals(5, model.getFirstDataRow().get().intValue());
            Assert.assertTrue(model.getHasHeader().get().booleanValue());
            Assert.assertEquals('!', model.getCommentCharacter().get().charValue());
            Assert.assertEquals(ColumnFormat.DELIMITED, model.getColumnFormat().get());
            Assert.assertEquals('|', model.getColumnDelimiter().get().charValue());
            Assert.assertEquals(",", model.getCustomDelimiter().get());
            Assert.assertEquals('\'', model.getQuoteCharacter().get().charValue());
            Assert.assertTrue(model.getColumnDivisions().length == 0);
        });
    }

    /**
     * Test the parameter values (fixed width happy path).
     */
    @Test
    public void testParameterValuesFixedHappy()
    {
        doAwt(() ->
        {
            CSVParseParameters parameters = new CSVParseParameters();
            parameters.setColumnFormat(new CSVFixedWidthColumnFormat(new int[] { 5, 10 }));

            FormatPanelModel model = new FormatPanelModel(null, parameters, new DetectedParameters(),
                    new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",")));

            if (model.getValidationStatus() != ValidationStatus.VALID)
            {
                Assert.fail(model.getErrorMessage());
            }

            Assert.assertEquals(ColumnFormat.FIXED_WIDTH, model.getColumnFormat().get());
            Assert.assertTrue(model.getColumnDivisions().length == 2);
        });
    }

    /**
     * Test the parameter values (delimited tricky path).
     */
    @Test
    public void testParameterValuesDelimitedTricky()
    {
        doAwt(() ->
        {
            CSVParseParameters parameters = new CSVParseParameters();
            parameters.setCommentIndicator(",");
            parameters.setColumnFormat(new CSVDelimitedColumnFormat("#", "^", 10));

            DetectedParameters detectedParameters = new DetectedParameters();
            detectedParameters.setCommentParameter(new ValuesWithConfidence<Character>(Character.valueOf(','), 0f));
            detectedParameters.setColumnFormatParameter(new ValuesWithConfidence<ColumnFormatParameters>(
                    new DelimitedColumnFormatParameters(Character.valueOf('#'), Character.valueOf('^'), 10), 0f));

            FormatPanelModel model = new FormatPanelModel(null, parameters, detectedParameters,
                    new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",")));

            if (model.getValidationStatus() != ValidationStatus.VALID)
            {
                Assert.fail(model.getErrorMessage());
            }

            Assert.assertEquals(',', model.getCommentCharacter().get().charValue());
            Assert.assertEquals('#', model.getColumnDelimiter().get().charValue());
            Assert.assertEquals(",", model.getCustomDelimiter().get());
            Assert.assertEquals('^', model.getQuoteCharacter().get().charValue());
        });
    }

    /**
     * Test going both ways.
     */
    @Test
    public void testBothWays()
    {
        doAwt(() ->
        {
            CSVParseParameters parameters = new CSVParseParameters();
            parameters.setHeaderLine(Integer.valueOf(2));
            parameters.setDataStartLine(Integer.valueOf(4));
            parameters.setCommentIndicator("!");
            parameters.setColumnFormat(new CSVDelimitedColumnFormat("|", "'", 10));

            FormatPanelModel model = new FormatPanelModel(null, parameters, new DetectedParameters(),
                    new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",")));
            model.applyChanges();

            Assert.assertEquals(2, parameters.getHeaderLine().intValue());
            Assert.assertEquals(4, parameters.getDataStartLine().intValue());
            Assert.assertEquals("!", parameters.getCommentIndicator());
            Assert.assertEquals(new CSVDelimitedColumnFormat("|", "'", 1), parameters.getColumnFormat());
        });
    }

    /**
     * Do a thing on AWT dispatch thread.
     * @param r the thing to do
     */
    private static void doAwt(Runnable r)
    {
        try
        {
            SwingUtilities.invokeAndWait(r);
        }
        catch (Exception eek)
        {
            throw new RuntimeException(eek);
        }
    }
}
