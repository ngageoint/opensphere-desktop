package io.opensphere.shapefile.config.v2;

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
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.ImportParseParameters;
import io.opensphere.importer.config.LayerSettings;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.LoadsTo;

/** Tests for {@link ShapeFileDataSource}. */
public class ShapeFileDataSourceTest
{
    /**
     * Test marshalling and unmarshalling.
     *
     * @throws JAXBException If there is a JAXB error.
     */
    @Test
    public void testMarshalling() throws JAXBException
    {
        ShapeFileDataSource input = getTestObject();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(input, outputStream);
        ShapeFileDataSource result = XMLUtilities.readXMLObject(new ByteArrayInputStream(outputStream.toByteArray()),
                ShapeFileDataSource.class);

        Assert.assertEquals(input, result);
    }

    /**
     * Test clone.
     */
    @Test
    public void testClone()
    {
        ShapeFileDataSource input = getTestObject();
        ShapeFileDataSource clone = input.clone();
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
        ShapeFileDataSource dataSource = getTestObject();

        File file = new File("x:\\sample_files\\test.shp");
        URI uri = file.toURI();

        dataSource.setSourceUri(uri);

        String expectedTypeKey = "SHP::" + dataSource.getName() + "::" + file.getAbsolutePath();

        Assert.assertEquals(expectedTypeKey, dataSource.generateTypeKey());
    }

    /**
     * Creates a ShapeFileDataSource test object.
     *
     * @return the ShapeFileDataSource
     */
    private static ShapeFileDataSource getTestObject()
    {
        ShapeFileDataSource dataSource = new ShapeFileDataSource();
        dataSource.setParseParameters(getTestParseParameters());
        dataSource.setLayerSettings(getLayerSettingsTestObject());
        try
        {
            dataSource.setSourceUri(new URI("file:///tmp/test.shp"));
        }
        catch (URISyntaxException e)
        {
            Assert.fail(e.getMessage());
        }
        dataSource.setVisible(false);
        return dataSource;
    }

    /**
     * Creates a ImportParseParameters test object.
     *
     * @return the ImportParseParameters
     */
    private static ImportParseParameters getTestParseParameters()
    {
        ImportParseParameters parseParameters = new ImportParseParameters();
        SpecialColumn dateColumn = new SpecialColumn();
        dateColumn.setColumnIndex(5);
        dateColumn.setColumnType(ColumnType.DATE);
        dateColumn.setFormat("format");
        parseParameters.getSpecialColumns().add(dateColumn);
        parseParameters.setColumnNames(Arrays.asList("col1", "col2", "col3"));
        parseParameters.setColumnsToIgnore(Arrays.asList(Integer.valueOf(2)));
        return parseParameters;
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
}
