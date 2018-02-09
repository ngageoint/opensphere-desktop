package io.opensphere.csvcommon.detect.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.model.IntegerRange;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.config.v2.CSVColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.basic.CommentDetector;
import io.opensphere.csvcommon.detect.basic.DataDetector;
import io.opensphere.csvcommon.detect.basic.HeaderDetector;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatDetector;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatFactoryImpl;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.DelimitedColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.FixedWidthColumnFormatParameters;
import io.opensphere.csvcommon.detect.datetime.DateTimeDetector;
import io.opensphere.csvcommon.detect.lob.LineOfBearingDetector;
import io.opensphere.csvcommon.detect.lob.model.LobColumnResults;
import io.opensphere.csvcommon.detect.location.AltitudeDetector;
import io.opensphere.csvcommon.detect.location.LocationDetector;
import io.opensphere.csvcommon.detect.location.LocationMatchMakerDetector;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.ColumnTypeDetector;
import io.opensphere.mantle.data.SpecialKey;

/** Controller for CSV format detectors. */
public class DetectionControllerImpl
{
    /** Used to log messages. */
    private static final Logger LOGGER = Logger.getLogger(DetectionControllerImpl.class);

    /** The system preferences registry. */
    protected final PreferencesRegistry myPrefsRegistry;

    /** The mantle column type detector. */
    private final ColumnTypeDetector myColumnTypeDetector;

    /**
     * Constructor.
     *
     * @param prefsRegistry The system preferences registry.
     * @param columnTypeDetector The mantle column type detector.
     */
    public DetectionControllerImpl(PreferencesRegistry prefsRegistry, ColumnTypeDetector columnTypeDetector)
    {
        myPrefsRegistry = prefsRegistry;
        myColumnTypeDetector = columnTypeDetector;
    }

    /**
     * Detect the possible parameters for the given sample of lines.
     *
     * @param truthParameters The parameters to be used as truth when detecting
     *            other parameters.
     * @param lineSampler The line sampler.
     * @param samplerFactory The factory that creates new samplers with the
     *            detected quote character.
     * @param changed the changed parameter
     * @return The detected parameters.
     * @throws FileNotFoundException Thrown if the specified file does not
     *             exist.
     */
    public DetectedParameters detectParameters(CSVParseParameters truthParameters, LineSampler lineSampler,
            LineSamplerFactory samplerFactory, String changed) throws FileNotFoundException
    {
        DetectedParameters result = new DetectedParameters();

        ValuesWithConfidence<? extends ColumnFormatParameters> columnFormat = getColumnFormatResult(truthParameters, lineSampler,
                changed);
        result.setColumnFormatParameter(columnFormat);

        Character tokenDelimiter = getTokenDelimiter(truthParameters.getColumnFormat(), columnFormat.getBestValue());
        Character textDelimiter = getTextDelimiter(truthParameters.getColumnFormat(), columnFormat.getBestValue());
        StringTokenizer tokenizer = new TokenizerFactoryImpl().getTokenizer(truthParameters.getColumnFormat(),
                columnFormat.getBestValue());
        int columnCount = getColumnCount(truthParameters.getColumnFormat(), columnFormat.getBestValue());

        result.setCommentParameter(getCommentResult(truthParameters, lineSampler, changed, tokenDelimiter, textDelimiter));
        result.setHeaderLineParameter(getHeaderLineResult(truthParameters, lineSampler, changed, tokenizer, columnCount));

        char[] quote = null;
        if (textDelimiter != null)
        {
            // create a line sampler with the correct quote type.
            quote = new char[] { textDelimiter.charValue() };
        }

        detectWithTextDelimiter(samplerFactory, quote, truthParameters, result, tokenizer, changed);

        return result;
    }

    /**
     * Runs the detectors that require the text delimiter to already be
     * identified.
     *
     * @param samplerFactory The sampler factory.
     * @param quote The quote or text delimiter.
     * @param truthParameters The truth parameters.
     * @param result The results to populate.
     * @param tokenizer The tokenizer.
     * @param changed the changed parameter
     * @throws FileNotFoundException the file not found exception
     */
    private void detectWithTextDelimiter(LineSamplerFactory samplerFactory, char[] quote, CSVParseParameters truthParameters,
            DetectedParameters result, StringTokenizer tokenizer, String changed) throws FileNotFoundException
    {
        LineSampler quotedSampler = samplerFactory.createSampler(quote);
        try
        {
            int headerLineNumber = getHeaderLineNumber(truthParameters, result);
            CellSampler cellSampler = new CellSamplerImpl(quotedSampler, tokenizer, headerLineNumber);
            if (headerLineNumber != -1)
            {
                runDetectors(result, cellSampler);
            }
            else
            {
                // Attempt to match location data.
                LocationMatchMakerDetector lmmd = new LocationMatchMakerDetector();
                ValuesWithConfidence<LocationResults> matchResults = lmmd.detect(cellSampler);
                result.setLocationParameter(matchResults);
            }

            result.setDataLinesParameter(getDataLinesResult(truthParameters, changed, cellSampler));
            result.setDateColumnParameter(new DateTimeDetector(myPrefsRegistry).detect(cellSampler));
        }
        finally
        {
            try
            {
                quotedSampler.close();
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Gets the data lines result.
     *
     * @param truthParameters the truth parameters
     * @param changed the changed
     * @param cellSampler the cell sampler
     * @return the data lines result
     */
    protected ValuesWithConfidence<IntegerRange> getDataLinesResult(CSVParseParameters truthParameters, String changed,
            CellSampler cellSampler)
    {
        ValuesWithConfidence<IntegerRange> dataLines = new DataDetector().detect(cellSampler);
        if (CSVParseParameters.DATASTARTLINE.equals(changed))
        {
            int sampleLine = cellSampler.absoluteLineToSampleLine(truthParameters.getDataStartLine().intValue());
            dataLines = new ValuesWithConfidence<IntegerRange>(
                    new IntegerRange(sampleLine, dataLines.getBestValue().getMax().intValue()), dataLines.getBestConfidence());
        }
        return dataLines;
    }

    /**
     * Gets the header line result.
     *
     * @param truthParameters the truth parameters
     * @param lineSampler the line sampler
     * @param changed the changed
     * @param tokenizer the tokenizer
     * @param columnCount the column count
     * @return the header line result
     */
    protected ValuesWithConfidence<Integer> getHeaderLineResult(CSVParseParameters truthParameters, LineSampler lineSampler,
            String changed, StringTokenizer tokenizer, int columnCount)
    {
        ValuesWithConfidence<Integer> headerLineParameter = null;
        if (!CSVParseParameters.HEADERLINE.equals(changed) && !CSVParseParameters.DATASTARTLINE.equals(changed))
        {
            headerLineParameter = new HeaderDetector(columnCount).detect(new CellSamplerImpl(lineSampler, tokenizer, -1));
        }
        else
        {
            float conf = truthParameters.getHeaderLine().intValue() == -1 ? 0.0f : 1.0f;
            headerLineParameter = new ValuesWithConfidence<>(truthParameters.getHeaderLine(), conf);
        }
        return headerLineParameter;
    }

    /**
     * Gets the comment result.
     *
     * @param truthParameters the truth parameters
     * @param lineSampler the line sampler
     * @param changed the changed
     * @param tokenDelimiter the token delimiter
     * @param textDelimiter the text delimiter
     * @return the comment result
     */
    protected ValuesWithConfidence<Character> getCommentResult(CSVParseParameters truthParameters, LineSampler lineSampler,
            String changed, Character tokenDelimiter, Character textDelimiter)
    {
        ValuesWithConfidence<Character> commentResult = null;
        if (!CSVParseParameters.COMMENTINDICATOR.equals(changed))
        {
            commentResult = new CommentDetector(tokenDelimiter, textDelimiter).detect(lineSampler);
        }
        else
        {
            ValueWithConfidence<Character> commentValue = new ValueWithConfidence<>();
            Character commentChar = truthParameters.getCommentIndicator() == null ? null
                    : Character.valueOf(truthParameters.getCommentIndicator().charAt(0));
            commentValue.setValue(commentChar);
            commentValue.setConfidence(1.0f);
            commentResult = new ValuesWithConfidence<>(commentValue);
        }
        return commentResult;
    }

    /**
     * Gets the column format result.
     *
     * @param params the CSVParseParameters
     * @param lineSampler the line sampler
     * @param changed the changed
     * @return the column format
     */
    protected ValuesWithConfidence<? extends ColumnFormatParameters> getColumnFormatResult(CSVParseParameters params,
            LineSampler lineSampler, String changed)
    {
        ValuesWithConfidence<? extends ColumnFormatParameters> columnFormat = null;
        if (changed == null)
        {
            columnFormat = new ColumnFormatDetector().detect(lineSampler);
        }
        else
        {
            ColumnFormatFactoryImpl factory = new ColumnFormatFactoryImpl();
            columnFormat = factory.createColumnFormat(params);
        }
        return columnFormat;
    }

    /**
     * Runs selected detectors.
     *
     * @param result the result
     * @param cellSampler the cell sampler
     */
    protected void runDetectors(DetectedParameters result, CellSampler cellSampler)
    {
        // These detectors require a header line.
        LocationDetector latlonDetector = new LocationDetector(myPrefsRegistry);
        ValuesWithConfidence<LocationResults> locationResults = latlonDetector.detect(cellSampler);
        result.setLocationParameter(locationResults);

        AltitudeDetector altDetector = new AltitudeDetector();
        ValuesWithConfidence<SpecialColumn> altResult = altDetector.detect(cellSampler);
        result.setAltitudeParameter(altResult);

        LineOfBearingDetector lobDetector = new LineOfBearingDetector(myPrefsRegistry);
        ValuesWithConfidence<LobColumnResults> lobResult = lobDetector.detect(cellSampler);
        result.setLOBParameter(lobResult);

        // Auto-detect other columns using Mantle's ColumnTypeDetector
        autoDetect(result, cellSampler);
    }

    /**
     * Auto-detect other columns using Mantle's ColumnTypeDetector.
     *
     * @param result the result
     * @param cellSampler the cell sampler
     */
    private void autoDetect(DetectedParameters result, CellSampler cellSampler)
    {
        List<? extends String> headerCells = cellSampler.getHeaderCells();
        if (CollectionUtilities.hasContent(headerCells) && myColumnTypeDetector != null)
        {
            Set<ColumnType> usedColumnTypes = New.set();
            for (int headerIndex = 0; headerIndex < headerCells.size(); headerIndex++)
            {
                String column = headerCells.get(headerIndex);
                SpecialKey specialKey = myColumnTypeDetector.detectColumn(column);
                if (specialKey != null)
                {
                    ColumnType columnType = ColumnType.fromSpecialKey(specialKey);
                    if (columnType != null && !usedColumnTypes.contains(columnType))
                    {
                        SpecialColumn specialColumn = new SpecialColumn(headerIndex, columnType, null);
                        result.getOtherColumns().add(specialColumn);
                        usedColumnTypes.add(columnType);
                    }
                }
            }
        }
    }

    /**
     * Get the column count, preferably from the truth format, or from the
     * detected format if available.
     *
     * @param truthFormat The truth parameters.
     * @param detectedFormat The detected parameters.
     * @return The column count.
     */
    protected int getColumnCount(CSVColumnFormat truthFormat, ColumnFormatParameters detectedFormat)
    {
        int columnCount;
        if (truthFormat instanceof CSVDelimitedColumnFormat)
        {
            columnCount = ((CSVDelimitedColumnFormat)truthFormat).getColumnCount();
        }
        else if (truthFormat instanceof CSVFixedWidthColumnFormat)
        {
            columnCount = ((CSVFixedWidthColumnFormat)truthFormat).getColumnDivisions().length;
        }
        else if (detectedFormat instanceof DelimitedColumnFormatParameters)
        {
            columnCount = ((DelimitedColumnFormatParameters)detectedFormat).getColumnCount();
        }
        else if (detectedFormat instanceof FixedWidthColumnFormatParameters)
        {
            int[] columnWidths = ((FixedWidthColumnFormatParameters)detectedFormat).getColumnDivisions();
            columnCount = columnWidths.length;
        }
        else
        {
            columnCount = 0;
        }
        return columnCount;
    }

    /**
     * Get the header line number, preferably from the truth parameters, or from
     * the detected parameters if a truth value is not available.
     *
     * @param truth The truth parameters.
     * @param detected The detected parameters.
     * @return The header line number, or -1 for no header.
     */
    protected int getHeaderLineNumber(CSVParseParameters truth, DetectedParameters detected)
    {
        int headerLineNumber = -1;
        if (truth.getHeaderLine() == null)
        {
            if (detected.getHeaderLineParameter() != null)
            {
                headerLineNumber = detected.getHeaderLineParameter().getBestConfidence() > .5
                        ? detected.getHeaderLineParameter().getBestValue().intValue() : -1;
            }
        }
        else
        {
            headerLineNumber = truth.getHeaderLine().intValue();
        }
        return headerLineNumber;
    }

    /**
     * Get the text delimiter, preferably from the truth format, or from the
     * detected format if available.
     *
     * @param truthFormat The truth parameters.
     * @param detectedFormat The detected parameters.
     * @return The text delimiter.
     */
    protected Character getTextDelimiter(CSVColumnFormat truthFormat, ColumnFormatParameters detectedFormat)
    {
        Character textDelimiter;
        if (truthFormat instanceof CSVDelimitedColumnFormat)
        {
            String delimiter = ((CSVDelimitedColumnFormat)truthFormat).getTextDelimiter();
            textDelimiter = delimiter != null ? Character.valueOf(delimiter.charAt(0)) : null;
        }
        else if (truthFormat instanceof CSVFixedWidthColumnFormat)
        {
            textDelimiter = null;
        }
        else if (detectedFormat instanceof DelimitedColumnFormatParameters)
        {
            textDelimiter = ((DelimitedColumnFormatParameters)detectedFormat).getTextDelimiter();
        }
        else
        {
            textDelimiter = null;
        }
        return textDelimiter;
    }

    /**
     * Get the token delimiter, preferably from the truth format, or from the
     * detected format if available.
     *
     * @param truthFormat The truth parameters.
     * @param detectedFormat The detected parameters.
     * @return The token delimiter.
     */
    protected Character getTokenDelimiter(CSVColumnFormat truthFormat, ColumnFormatParameters detectedFormat)
    {
        Character tokenDelimiter;
        if (truthFormat instanceof CSVDelimitedColumnFormat)
        {
            String delimiter = ((CSVDelimitedColumnFormat)truthFormat).getTokenDelimiter();
            tokenDelimiter = delimiter != null ? Character.valueOf(delimiter.charAt(0)) : null;
        }
        else if (truthFormat instanceof CSVFixedWidthColumnFormat)
        {
            tokenDelimiter = null;
        }
        else if (detectedFormat instanceof DelimitedColumnFormatParameters)
        {
            tokenDelimiter = ((DelimitedColumnFormatParameters)detectedFormat).getTokenDelimiter();
        }
        else
        {
            tokenDelimiter = null;
        }
        return tokenDelimiter;
    }
}
