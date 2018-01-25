package io.opensphere.stkterrain.transformer;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.MapManager;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.LabelGeometry.Builder;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer.Observer;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.util.Constants;

/**
 * Displays copyright information about active STK terrain layers on the screen.
 */
public class AttributionTransformer extends DefaultTransformer implements DataRegistryListener<TileSetMetadata>
{
    /**
     * The label geometries displaying the attribution of each active STK layer.
     */
    private final Map<String, LabelGeometry> myGeometries = Collections.synchronizedMap(New.map());

    /**
     * Used to get the screens height.
     */
    private final MapManager myMapManager;

    /** The viewer observer. */
    private final Observer myViewerObserver = this::handleViewChange;

    /**
     * Constructs a new {@link AttributionTransformer}.
     *
     * @param dataRegistry The system data registry.
     * @param mapManager Used to get the screen's height.
     */
    public AttributionTransformer(DataRegistry dataRegistry, MapManager mapManager)
    {
        super(dataRegistry);
        myMapManager = mapManager;
    }

    @Override
    public void open()
    {
        super.open();
        getDataRegistry().addChangeListener(this, new DataModelCategory(null, TileSetMetadata.class.getName(), null),
                Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR);
        myMapManager.getStandardViewer().addObserver(myViewerObserver);
    }

    @Override
    public void close()
    {
        myMapManager.getStandardViewer().removeObserver(myViewerObserver);
        getDataRegistry().removeChangeListener(this);
        super.close();
    }

    @Override
    public void allValuesRemoved(Object source)
    {
        List<LabelGeometry> removals = New.list(myGeometries.values());
        myGeometries.clear();
        publishGeometries(Collections.emptyList(), removals);
    }

    @Override
    public boolean isIdArrayNeeded()
    {
        return false;
    }

    @Override
    public boolean isWantingRemovedObjects()
    {
        return false;
    }

    @Override
    public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends TileSetMetadata> newValues,
            Object source)
    {
        LabelGeometry.Builder<ScreenPosition> builder = new Builder<>();
        builder.setText("Terrain provided by " + newValues.iterator().next().getAttribution());
        builder.setOutlined(true);
        builder.setPosition(getPosition());
        builder.setFont("");

        LabelRenderProperties props = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        props.setColor(Color.white);

        LabelGeometry label = new LabelGeometry(builder, props, null);
        myGeometries.put(dataModelCategory.getSource() + dataModelCategory.getCategory(), label);
        publishGeometries(Collections.singletonList(label), Collections.emptyList());
    }

    @Override
    public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends TileSetMetadata> removedValues,
            Object source)
    {
        LabelGeometry geometry = myGeometries.get(dataModelCategory.getSource() + dataModelCategory.getCategory());
        if (geometry != null)
        {
            publishGeometries(Collections.emptyList(), Collections.singletonList(geometry));
        }
    }

    @Override
    public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Object source)
    {
        LabelGeometry geometry = myGeometries.get(dataModelCategory.getSource() + dataModelCategory.getCategory());
        if (geometry != null)
        {
            publishGeometries(Collections.emptyList(), Collections.singletonList(geometry));
        }
    }

    @Override
    public void valuesUpdated(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends TileSetMetadata> newValues,
            Object source)
    {
    }

    /**
     * Handles a view change, republishing the label(s) in the right place.
     *
     * @param type the change type
     */
    private void handleViewChange(ViewChangeSupport.ViewChangeType type)
    {
        if (type == ViewChangeType.WINDOW_RESIZE && !myGeometries.isEmpty())
        {
            ThreadUtilities.runCpu(() ->
            {
                synchronized (myGeometries)
                {
                    List<LabelGeometry> removes = New.list(myGeometries.values());
                    for (Map.Entry<String, LabelGeometry> entry : myGeometries.entrySet())
                    {
                        LabelGeometry geom = entry.getValue();
                        @SuppressWarnings("unchecked")
                        LabelGeometry.Builder<ScreenPosition> builder = (LabelGeometry.Builder<ScreenPosition>)geom
                                .createBuilder();
                        builder.setPosition(getPosition());
                        entry.setValue(new LabelGeometry(builder, geom.getRenderProperties(), geom.getConstraints()));
                    }
                    publishGeometries(New.list(myGeometries.values()), removes);
                }
            });
        }
    }

    /**
     * Gets the current position to place the label.
     *
     * @return the position
     */
    private ScreenPosition getPosition()
    {
        return new ScreenPosition(2, myMapManager.getStandardViewer().getViewportHeight() - 2);
    }
}
