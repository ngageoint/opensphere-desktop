package io.opensphere.csv.config.v2;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.csvcommon.config.v1.CSVColumnInfo;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.LayerSettings;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.LoadsTo;

/** Tests for {@link CSVDataSource}. */
public class CSVDataSourceTest
{
    /** Column for testing. */
    private static final String COL1 = "col1";

    /** Column for testing. */
    private static final String COL2 = "col2";

    /** Column for testing. */
    private static final String COL3 = "col3";

    /**
     * Test marshalling and unmarshalling.
     *
     * @throws JAXBException If there is a JAXB error.
     */
    @Test
    public void testMarshalling() throws JAXBException
    {
        CSVDataSource input = getTestObject();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(input, outputStream, CSVDataSource.class.getPackage());
        CSVDataSource result = XMLUtilities.readXMLObject(new ByteArrayInputStream(outputStream.toByteArray()),
                CSVDataSource.class, CSVDataSource.class.getPackage());

        Assert.assertEquals(input, result);
    }

    /**
     * Test clone.
     */
    @Test
    public void testClone()
    {
        CSVDataSource input = getTestObject();
        CSVDataSource clone = input.clone();
        Assert.assertTrue(Utilities.notSameInstance(input, clone));
        Assert.assertTrue(Utilities.sameInstance(input.getClass(), clone.getClass()));
        Assert.assertEquals(input, clone);
    }

    /**
     * Tests the generate type key.
     */
    @Test
    public void testGenerateTypeKey()
    {
        CSVDataSource dataSource = getTestObject();

        File file = new File("x:\\sample_files\\test.csv");
        URI uri = file.toURI();

        dataSource.setSourceUri(uri);

        String expectedTypeKey = "CSV::" + dataSource.getName() + "::" + file.getAbsolutePath();

        Assert.assertEquals(expectedTypeKey, dataSource.generateTypeKey());
    }

    /**
     * Creates a CSVDataSource test object.
     *
     * @return the CSVDataSource
     */
    public static CSVDataSource getTestObject()
    {
        CSVDataSource dataSource = new CSVDataSource();
        dataSource.setParseParameters(getParamsObject());
        dataSource.setLayerSettings(getLayerSettingsTestObject());
        try
        {
            dataSource.setSourceUri(new URI("file:///tmp/test.csv"));
        }
        catch (URISyntaxException e)
        {
            Assert.fail(e.getMessage());
        }
        dataSource.setVisible(false);
        return dataSource;
    }

    /**
     * Creates a CSVLayerSettings test object.
     *
     * @return the CSVLayerSettings
     */
    public static LayerSettings getLayerSettingsTestObject()
    {
        LayerSettings layerSettings = new LayerSettings("Tommy");
        layerSettings.setLoadsTo(LoadsTo.STATIC);
        layerSettings.setColor(Color.YELLOW);
        layerSettings.setActive(true);
        return layerSettings;
    }

    /**
     * Creates a CSVParseParameters test object.
     *
     * @return the CSVParseParameters
     */
    private static CSVParseParameters getParamsObject()
    {
        CSVParseParameters parseParameters = new CSVParseParameters();
        SpecialColumn dateColumn = new SpecialColumn();
        dateColumn.setColumnIndex(5);
        dateColumn.setColumnType(ColumnType.DATE);
        dateColumn.setFormat("format");
        parseParameters.getSpecialColumns().add(dateColumn);
        parseParameters.setColumnFormat(new CSVDelimitedColumnFormat(":", "'", 4));
        parseParameters.setColumnNames(Arrays.asList(COL1, COL2, COL3));
        parseParameters.setCommentIndicator("!");
        parseParameters.setDataStartLine(Integer.valueOf(5));
        parseParameters.setHeaderLine(Integer.valueOf(2));
        parseParameters.getColumnClasses().add(new CSVColumnInfo("java.lang.Float", 1, 2, true));
        return parseParameters;
    }
}
