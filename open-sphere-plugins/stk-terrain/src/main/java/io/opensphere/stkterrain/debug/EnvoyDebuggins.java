package io.opensphere.stkterrain.debug;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultPropertyValueReceiver;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.stkterrain.util.Constants;

/**
 * Executes stk terrain queries to verify envoys are all hooked up.
 */
public class EnvoyDebuggins
{
    /**
     * Used to log the results.
     */
    private static final Logger LOGGER = Logger.getLogger(EnvoyDebuggins.class);

    /**
     * Used to perform the queries and execute the envoys.
     */
    private final DataRegistry myRegistry;

    /**
     * Allows us to add a debug menu to the system.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Construct a new envoy debuggins.
     *
     * @param toolbox The system toolbox.
     */
    public EnvoyDebuggins(Toolbox toolbox)
    {
        myRegistry = toolbox.getDataRegistry();
        myUIRegistry = toolbox.getUIRegistry();
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myUIRegistry.getMenuBarRegistry()
                        .getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU, "STK Terrain").add(getDebugMenu());
            }
        });
    }

    /**
     * The the debug options for video playing.
     *
     * @return The menu which contains the debug options.
     */
    private JMenuItem getDebugMenu()
    {
        final JMenuItem videoHistoryQuery = new JMenuItem("Query STK Envoys");
        videoHistoryQuery.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ThreadUtilities.runBackground((Runnable)EnvoyDebuggins.this::performSTKQueries);
            }
        });

        return videoHistoryQuery;
    }

    /**
     * Performs the queries to test the envoys.
     */
    private void performSTKQueries()
    {
        SimpleQuery<TileSet> tileSetQuery = new SimpleQuery<>(new DataModelCategory(null, TileSet.class.getName(), null),
                Constants.TILESET_PROPERTY_DESCRIPTOR);
        myRegistry.performQuery(tileSetQuery);

        ObjectMapper mapper = new ObjectMapper();
        try
        {
            for (TileSet tileSet : tileSetQuery.getResults())
            {
                LOGGER.info(mapper.writeValueAsString(tileSet));
            }

            SimpleQuery<TileSetMetadata> metadataQuery = new SimpleQuery<>(
                    new DataModelCategory(null, TileSetMetadata.class.getName(), "world"),
                    Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR);
            myRegistry.performQuery(metadataQuery);
            TileSetMetadata metadata = metadataQuery.getResults().get(0);
            LOGGER.info(mapper.writeValueAsString(metadata));

            DefaultPropertyValueReceiver<QuantizedMesh> receiver = new DefaultPropertyValueReceiver<QuantizedMesh>(
                    Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR);
            DefaultQuery meshQuery = new DefaultQuery(new DataModelCategory(null, QuantizedMesh.class.getName(), "world"),
                    Collections.singletonList(receiver), Collections.singletonList(
                            new ZYXKeyPropertyMatcher(Constants.KEY_PROPERTY_DESCRIPTOR, new ZYXImageKey(1, 0, 0, null))),
                    null);
            myRegistry.performQuery(meshQuery);
            QuantizedMesh mesh = receiver.getValues().get(0);

            LOGGER.info("Quantized Mesh max height " + mesh.getHeader().getMaxHeight());
        }
        catch (IOException e)
        {
            LOGGER.error(e, e);
        }
    }
}
