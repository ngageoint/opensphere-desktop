package io.opensphere.core.datafilter.columns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.state.v2.ColumnMappings;

/** Unit tests for {@link ColumnMappingControllerImpl}. */
public class ColumnMappingControllerImplTest
{
    /**
     * Creates a test object.
     *
     * @return the test object
     */
    private static MutableColumnMappingController newTestObject()
    {
        ColumnMappingControllerImpl controller = new ColumnMappingControllerImpl(null)
        {
            @Override
            ColumnMappings loadColumnMappings()
            {
                return ColumnMappingUtilsTest.newColumnMappings();
            }

            @Override
            void saveColumnMappings(ColumnMappings columnMappings)
            {
            }
        };
        controller.initialize();
        return controller;
    }

    /**
     * Tests getting the shared defined columns.
     */
    @Test
    public void testDefinedColumns()
    {
        MutableColumnMappingController controller = newTestObject();

        final String layer1 = "server!!layer1";
        final String layer2 = "server!!layer2";
        final String layer3 = "server!!layer3";
        final String newCol = "New Field";
        final String newValue = "new value";

        List<Pair<String, List<String>>> layers = New.list();
        layers.add(new Pair<String, List<String>>(layer1, New.list("alt", "lat", "lon")));
        layers.add(new Pair<String, List<String>>(layer2, New.list(newValue, "lat", "lon")));
        layers.add(new Pair<String, List<String>>(layer3, New.list("value3", "lat", "lon")));

        controller.addMapping(newCol, layer2, "new value", true);
        controller.addMapping(newCol, layer3, "value3", true);

        Map<String, Map<String, String>> definedColumns = controller.getDefinedColumns(layers);
        Assert.assertEquals(2, definedColumns.size());
        Assert.assertEquals(1, definedColumns.get(layer2).size());
        Assert.assertEquals(1, definedColumns.get(layer3).size());
        Assert.assertEquals(newCol, definedColumns.get(layer2).get(newValue));
        Assert.assertEquals(newCol, definedColumns.get(layer3).get("value3"));
    }

    /**
     * Tests it.
     */
    @Test
    public void testIt()
    {
        MutableColumnMappingController controller = newTestObject();

        final String layer1 = "server!!layer1";
        final String layer2 = "server!!layer2";
        final String layer3 = "server!!layer3";
        final String altitudeCol = "Altitude";
        final String newCol = "New Field";

        Assert.assertEquals(Arrays.asList(altitudeCol), controller.getDefinedColumns());
        Assert.assertEquals(altitudeCol, controller.getDefinedColumn(layer1, "alt"));
        Assert.assertEquals(null, controller.getDefinedColumn(layer1, "alt2"));
        Assert.assertEquals("alt", controller.getLayerColumn(layer1, altitudeCol));
        Assert.assertEquals(null, controller.getLayerColumn(layer1, "Altitude2"));

        controller.addMapping(newCol, layer2, "old value", true);
        Assert.assertEquals("old value", controller.getLayerColumn(layer2, newCol));

        controller.addMapping(newCol, layer2, "new value", true);
        Assert.assertEquals("new value", controller.getLayerColumn(layer2, newCol));

        controller.addMapping(newCol, layer3, "value3", true);
        Assert.assertEquals(Arrays.asList(new ColumnMapping(altitudeCol, layer1, "alt")), controller.getMappings(altitudeCol));
        Assert.assertEquals(
                Arrays.asList(new ColumnMapping(newCol, layer2, "new value"), new ColumnMapping(newCol, layer3, "value3")),
                controller.getMappings(newCol));

        controller.setDescription(altitudeCol, "blah", true);
        Assert.assertEquals("blah", controller.getDescription(altitudeCol));

        controller.clearMappings(altitudeCol);
        Assert.assertTrue(controller.getMappings(altitudeCol).isEmpty());
        Assert.assertEquals("blah", controller.getDescription(altitudeCol));
    }

    /**
     * Tests marshalling and unmarshalling.
     *
     * @throws JAXBException If there is a JAXB error.
     */
    @Test
    public void testMarshalling() throws JAXBException
    {
        ColumnMappings input = ColumnMappingUtilsTest.newColumnMappings();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(input, outputStream);
        ColumnMappings result = XMLUtilities.readXMLObject(new ByteArrayInputStream(outputStream.toByteArray()),
                ColumnMappings.class);

        Assert.assertEquals(1, result.getColumnMapping().size());
        ColumnMappingUtilsTest.assertEquals(input, result);
    }
}
