package io.opensphere.shapefile;

import java.awt.Color;
import java.io.File;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.shapefile.config.v1.ShapeFileSource;
import io.opensphere.shapefile.config.v2.ShapeFileDataSource;

/** Test for {@link ShapeFileConfigurationManager}. */
public class ShapeFileConfigurationManagerTest
{
    /**
     * Test conversion.
     */
    @Test
    public void testConversion()
    {
        Assert.assertEquals(createDataSourceV2(), ShapeFileConfigurationManager.convert1to2(createDataSourceV1()));
    }

    /**
     * Creates a test ShapeFileSource object.
     *
     * @return the ShapeFileSource object
     */
    private ShapeFileSource createDataSourceV1()
    {
        ShapeFileSource sourceV1 = new ShapeFileSource();
        sourceV1.setPath(System.getProperty("java.io.tmpdir"));
        sourceV1.setName("Korben Dallas");
        sourceV1.setLoadsTo(LoadsTo.STATIC);
        sourceV1.setShapeColor(Color.YELLOW);
        sourceV1.setColumnNames(New.list("Earth", "Water", "Air", "Fire"));
        // There are no columns to ignore
        sourceV1.setTimeColumn(7);
        sourceV1.setTimeFormat(new DateFormat(DateFormat.Type.TIMESTAMP, DateTimeFormats.DATE_TIME_FORMAT, null));
        sourceV1.setSmajColumn(9);
        sourceV1.setSminColumn(10);
        sourceV1.setColumnFilter(New.set("Water", "Fire"));
        return sourceV1;
    }

    /**
     * Creates a test ShapeFileDataSource object.
     *
     * @return the ShapeFileDataSource object
     */
    private ShapeFileDataSource createDataSourceV2()
    {
        URI sourceUri = null;
        sourceUri = new File(System.getProperty("java.io.tmpdir")).toURI();
        ShapeFileDataSource sourceV2 = new ShapeFileDataSource(sourceUri);
        sourceV2.setActive(true);
        sourceV2.getLayerSettings().setName("Korben Dallas");
        sourceV2.getLayerSettings().setLoadsTo(LoadsTo.STATIC);
        sourceV2.getLayerSettings().setColor(Color.YELLOW);
        sourceV2.getParseParameters().setColumnNames(New.list("Earth", "Water", "Air", "Fire"));
        sourceV2.getParseParameters().getSpecialColumns()
                .add(new SpecialColumn(7, ColumnType.TIMESTAMP, DateTimeFormats.DATE_TIME_FORMAT));
        sourceV2.getParseParameters().getSpecialColumns().add(new SpecialColumn(9, ColumnType.SEMIMAJOR, null));
        sourceV2.getParseParameters().getSpecialColumns().add(new SpecialColumn(10, ColumnType.SEMIMINOR, null));
        sourceV2.getParseParameters().setColumnsToIgnore(New.list(Integer.valueOf(1), Integer.valueOf(3)));
        return sourceV2;
    }
}
