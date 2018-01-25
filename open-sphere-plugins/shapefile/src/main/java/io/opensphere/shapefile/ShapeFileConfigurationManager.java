package io.opensphere.shapefile;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.SupplierX;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.shapefile.config.v1.ShapeFileSource;
import io.opensphere.shapefile.config.v1.ShapeFilesConfig;
import io.opensphere.shapefile.config.v2.ShapeFileDataSource;
import io.opensphere.shapefile.config.v2.ShapeFileDataSources;

/**
 * Manages the configuration file.
 */
class ShapeFileConfigurationManager
{
    /**
     * Supplier for the JAXB context for marshalling/unmarshalling the
     * preferences.
     */
    private static final SupplierX<JAXBContext, JAXBException> CONTEXT_SUPPLIER = new SupplierX<JAXBContext, JAXBException>()
    {
        @Override
        public JAXBContext get() throws JAXBException
        {
            return JAXBContextHelper.getCachedContext(ShapeFileDataSources.class.getPackage());
        }
    };

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileConfigurationManager.class);

    /** Config preferences key (version 1). */
    private static final String PREFERENCES_KEY = "config";

    /** Config preferences key (version 2). */
    private static final String PREFERENCES_KEY_V2 = "config.v2";

    /** The shape file preferences. */
    private final Preferences myShapeFilePreferences;

    /**
     * Converts a v1 data source to a v2 data source.
     *
     * @param sourceV1 the v1 data source
     * @return the equivalent v2 data source
     */
    static ShapeFileDataSource convert1to2(final ShapeFileSource sourceV1)
    {
        // Data source fields
        ShapeFileDataSource sourceV2 = new ShapeFileDataSource(new File(sourceV1.getPath()).toURI());
        sourceV2.setActive(sourceV1.isActive());
        sourceV2.setVisible(sourceV1.isVisible());
        sourceV2.setFromStateSource(sourceV1.isFromStateSource());

        // Layer settings fields
        sourceV2.getLayerSettings().setName(sourceV1.getName());
        sourceV2.getLayerSettings().setLoadsTo(sourceV1.getLoadsTo());
        sourceV2.getLayerSettings().setColor(sourceV1.getShapeColor());

        // Parse parameter fields
        sourceV2.getParseParameters().setColumnNames(New.list(sourceV1.getColumnNames()));
        sourceV2.getParseParameters()
                .setColumnsToIgnore(sourceV1.getColumnFilter().stream()
                        .map(columnName -> Integer.valueOf(sourceV1.getColumnNames().indexOf(columnName))).sorted()
                        .collect(Collectors.toList()));

        // Special columns
        addSpecialColumns(sourceV2.getParseParameters().getSpecialColumns(), sourceV1);

        return sourceV2;
    }

    /**
     * Gets the data source from the given node and id.
     *
     * @param sourceNode the data source node
     * @return the data source, or null if it couldn't be read
     */
    static ShapeFileDataSource getDataSource(Node sourceNode)
    {
        ShapeFileDataSource dataSource = null;

        String nodeName = sourceNode.getNodeName();

        // New format
        if (ShapeFileDataSource.class.getSimpleName().equals(nodeName))
        {
            try
            {
                dataSource = XMLUtilities.readXMLObject(sourceNode, CONTEXT_SUPPLIER.get(), ShapeFileDataSource.class);
            }
            catch (JAXBException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
        // Old format
        else if (ShapeFileSource.class.getSimpleName().equals(nodeName))
        {
            try
            {
                ShapeFileSource oldSource = XMLUtilities.readXMLObject(sourceNode, ShapeFileSource.class);
                if (oldSource != null)
                {
                    dataSource = convert1to2(oldSource);
                }
            }
            catch (JAXBException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return dataSource;
    }

    /**
     * Adds special columns.
     *
     * @param specialColumns the special columns to which to add
     * @param sourceV1 the v1 data source
     */
    private static void addSpecialColumns(Collection<SpecialColumn> specialColumns, ShapeFileSource sourceV1)
    {
        if (sourceV1.getDateColumn() != -1)
        {
            ColumnType columnType = ColumnType.fromDateFormatType(sourceV1.getDateFormat().getType(), false);
            specialColumns.add(new SpecialColumn(sourceV1.getDateColumn(), columnType, sourceV1.getDateFormat().getSdf()));
        }
        if (sourceV1.getTimeColumn() != -1)
        {
            ColumnType columnType = ColumnType.fromDateFormatType(sourceV1.getTimeFormat().getType(), false);
            specialColumns.add(new SpecialColumn(sourceV1.getTimeColumn(), columnType, sourceV1.getTimeFormat().getSdf()));
        }
        if (sourceV1.getSmajColumn() != -1)
        {
            specialColumns.add(new SpecialColumn(sourceV1.getSmajColumn(), ColumnType.SEMIMAJOR, null));
        }
        if (sourceV1.getSminColumn() != -1)
        {
            specialColumns.add(new SpecialColumn(sourceV1.getSminColumn(), ColumnType.SEMIMINOR, null));
        }
        if (sourceV1.getOrientColumn() != -1)
        {
            specialColumns.add(new SpecialColumn(sourceV1.getOrientColumn(), ColumnType.ORIENTATION, null));
        }
        if (sourceV1.getLobColumn() != -1)
        {
            specialColumns.add(new SpecialColumn(sourceV1.getLobColumn(), ColumnType.LOB, null));
        }
    }

    /**
     * Constructor.
     *
     * @param shapeFilePreferences the shape file preferences
     */
    public ShapeFileConfigurationManager(Preferences shapeFilePreferences)
    {
        myShapeFilePreferences = shapeFilePreferences;
    }

    /**
     * Gets the configuration, migrating if necessary.
     *
     * @return The config object
     */
    public ShapeFileDataSources getConfig()
    {
        ShapeFileDataSources configV2;

        ShapeFilesConfig configV1 = myShapeFilePreferences.getJAXBObject(ShapeFilesConfig.class, PREFERENCES_KEY, null);
        if (configV1 != null && !configV1.isMigrated())
        {
            configV2 = migrate1to2(configV1);
        }
        else
        {
            configV2 = myShapeFilePreferences.getJAXBObject(ShapeFileDataSources.class, PREFERENCES_KEY_V2, CONTEXT_SUPPLIER,
                    new ShapeFileDataSources());
        }

        return configV2;
    }

    /**
     * Saves the given config to the preferences.
     *
     * @param config the config object
     */
    public void saveConfig(ShapeFileDataSources config)
    {
        myShapeFilePreferences.putJAXBObject(PREFERENCES_KEY_V2, config, false, CONTEXT_SUPPLIER, this);
    }

    /**
     * Migrates a v1 config to a v2 config.
     *
     * @param configV1 the v1 config
     * @return the equivalent v2 config
     */
    private ShapeFileDataSources migrate1to2(ShapeFilesConfig configV1)
    {
        LOGGER.info("Migrating configuration from v1 to v2");

        ShapeFileDataSources configV2 = new ShapeFileDataSources();
        for (ShapeFileSource sourceV1 : configV1.getShapeFileSources())
        {
            configV2.addSource(convert1to2(sourceV1));
        }
        myShapeFilePreferences.putJAXBObject(PREFERENCES_KEY_V2, configV2, false, CONTEXT_SUPPLIER, this);

        configV1.setMigrated(true);
        myShapeFilePreferences.putJAXBObject(PREFERENCES_KEY, configV1, false, this);

        LOGGER.info("Migrated " + configV1.getShapeFileSources().size() + " data sources");

        return configV2;
    }
}
