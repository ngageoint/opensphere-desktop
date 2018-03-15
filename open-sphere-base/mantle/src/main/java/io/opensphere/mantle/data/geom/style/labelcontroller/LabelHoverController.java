package io.opensphere.mantle.data.geom.style.labelcontroller;

import java.awt.Font;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.event.DataElementHighlightChangeEvent;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.dialog.StyleManagerUtils;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractLocationFeatureVisualizationStyle;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * A controller used to react to a user hovering over a point, and displaying a
 * label for the item.
 */
public class LabelHoverController implements EventListener<DataElementHighlightChangeEvent>
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(LabelHoverController.class);

    /**
     * The default text to display when no label is configured.
     */
    private static final String DEFAULT_LABEL = "";

    /**
     * The toolbox through which application interaction occurs.
     */
    private final Toolbox myToolbox;

    /**
     * The previous label geometry rendered by the controller, may be null. This
     * handle is preserved to allow the controller to remove the label when the
     * user removes focus.
     */
    private volatile LabelGeometry myLastLabelGeometry;

    /**
     * Create a new controller to listen for mouse hover events, and react to
     * them.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     */
    public LabelHoverController(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
        getToolbox().getEventManager().subscribe(DataElementHighlightChangeEvent.class, this);
    }

    /**
     * Gets the value of the {@link #myToolbox} field.
     *
     * @return the value stored in the {@link #myToolbox} field.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.event.EventListener#notify(java.lang.Object)
     */
    @Override
    public void notify(DataElementHighlightChangeEvent event)
    {
        if (event.isHighlighted())
        {
            drawLabel(event);
        }
        else
        {
            eraseLabel();
        }
    }

    /**
     * Erases the last known label, if it isn't null, and sets it to null.
     */
    protected void eraseLabel()
    {
        LabelGeometry lastLabelGeometry = myLastLabelGeometry;
        if (lastLabelGeometry != null)
        {
            getToolbox().getGeometryRegistry().removeGeometriesForSource(this, Collections.singleton(lastLabelGeometry));
            myLastLabelGeometry = null;
        }
    }

    /**
     * Draws a label for the supplied event.
     *
     * @param pEvent the event for which to draw the label.
     */
    protected void drawLabel(DataElementHighlightChangeEvent pEvent)
    {
        DataElement element = MantleToolboxUtils.getDataElementLookupUtils(getToolbox()).getDataElement(pEvent.getRegistryId(),
                null, null);

        List<Class<? extends VisualizationSupport>> featureClasses = StyleManagerUtils
                .getDefaultFeatureClassesForType(element.getDataTypeInfo());
        VisualizationStyleController vsc = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleController();
        for (Class<? extends VisualizationSupport> featureClass : featureClasses)
        {
            Class<? extends VisualizationStyle> selectedStyleClass = vsc.getSelectedVisualizationStyleClass(featureClass,
                    element.getDataTypeInfo().getParent(), element.getDataTypeInfo());

            if (selectedStyleClass != null && ClassUtils.isAssignable(selectedStyleClass, AbstractLocationFeatureVisualizationStyle.class))
            {
                drawLabel(pEvent, element, vsc, featureClass,
                        selectedStyleClass.asSubclass(AbstractLocationFeatureVisualizationStyle.class));
            }
        }
    }

    /**
     * Draws the label on the map for the supplied element.
     *
     * @param pEvent the event for which to draw the label.
     * @param pElement the element for which to draw the label.
     * @param pStyleController the style controller from which the configuration
     *            of the label is loaded.
     * @param pFeatureClass the feature class associated with the supplied
     *            element.
     * @param pStyleClass the style class associated with the supplied element.
     */
    protected void drawLabel(DataElementHighlightChangeEvent pEvent, DataElement pElement,
            VisualizationStyleController pStyleController, Class<? extends VisualizationSupport> pFeatureClass,
            Class<? extends AbstractLocationFeatureVisualizationStyle> pStyleClass)
    {
        MapGeometrySupport geom = pElement instanceof MapDataElement ? ((MapDataElement)pElement).getMapGeometrySupport() : null;
        if (geom != null)
        {
            // check to see if a label is already drawn, and if so, erase it
            // (otherwise, the handle to the last label would be lost, and it
            // can never be erased)
            if (myLastLabelGeometry != null)
            {
                eraseLabel();
            }

            AbstractFeatureVisualizationStyle style = pStyleClass.cast(pStyleController.getStyleForEditorWithConfigValues(
                    pStyleClass, pFeatureClass, pElement.getDataTypeInfo().getParent(), pElement.getDataTypeInfo()));

            LabelGeometry lastLabelGeometry = buildLabelGeometry(pEvent.getRegistryId(), style, pElement, geom);
            getToolbox().getGeometryRegistry().addGeometriesForSource(this, Collections.singleton(lastLabelGeometry));
            myLastLabelGeometry = lastLabelGeometry;
        }
        else
        {
            LOG.warn("Unable to draw label for non-mapped element type: " + pElement.getClass().getName());
        }
    }

    /**
     * Creates a new {@link LabelGeometry} using the supplied data.
     *
     * @param pRegistryId the identifier of the data element for which to build
     *            the label.
     * @param pStyle the style object from which the display parameters of the
     *            label are extracted.
     * @param pDataElement the data element for which to construct the label.
     * @param pMapGeometrySupport the geometry support with which the location
     *            of the label is determined.
     * @return a {@link LabelGeometry} constructed for the identified element.
     */
    protected LabelGeometry buildLabelGeometry(long pRegistryId, AbstractFeatureVisualizationStyle pStyle,
            DataElement pDataElement, MapGeometrySupport pMapGeometrySupport)
    {
        LabelGeometry.Builder<GeographicPosition> builder = new LabelGeometry.Builder<>();
        builder.setDataModelId(pDataElement.getId());
        builder.setPosition(getLabelPosition(pMapGeometrySupport));
        builder.setHorizontalAlignment(.5f);
        builder.setVerticalAlignment(1f);
        builder.setText(getLabelContent(pRegistryId, pStyle, pDataElement.getMetaData()));
        builder.setFont(Font.SANS_SERIF + " " + pStyle.getLabelSize());
        builder.setOutlined(true);

        LabelRenderProperties crp = new DefaultLabelRenderProperties(0, true, false);
        crp.setColor(pStyle.getLabelColor());

        return new LabelGeometry(builder, crp, new Constraints(TimeConstraint.getTimeConstraint(TimeSpan.TIMELESS)));
    }

    /**
     * Extracts the position of the label from the supplied map geometry. This
     * allows for labels to be applied to different types of geometries.
     *
     * @param pMapGeometrySupport the support object from which a location is
     *            extracted.
     * @return the position at which to draw the label.
     */
    protected GeographicPosition getLabelPosition(MapGeometrySupport pMapGeometrySupport)
    {
        if (pMapGeometrySupport instanceof MapLocationGeometrySupport)
        {
            return new GeographicPosition(((MapLocationGeometrySupport)pMapGeometrySupport).getLocation());
        }
        else if (pMapGeometrySupport instanceof MapPathGeometrySupport)
        {
            return new GeographicPosition(((MapPathGeometrySupport)pMapGeometrySupport).getLocations().get(0));
        }
        LOG.warn("Unrecognized map geometry support: " + pMapGeometrySupport.getClass().getName());
        return null;
    }

    /**
     * Gets the label's text from the supplied style and data element. If no
     * content is found, the value of the {@link #DEFAULT_LABEL} is used.
     *
     * @param pRegistryId the identifier of the data element for which to get
     *            the column text.
     * @param pStyle the style from which to get the value.
     * @param pMetaData the data element's metadata from which to extract
     *            metadata from which to get the column text.
     * @return the text to display for the label.
     */
    protected String getLabelContent(long pRegistryId, AbstractFeatureVisualizationStyle pStyle, MetaDataProvider pMetaData)
    {
        Object columnValue = pStyle.getLabelColumnValue(pRegistryId, pMetaData);
        String columnText;
        if (columnValue != null)
        {
            columnText = columnValue.toString();
        }
        else
        {
            columnText = DEFAULT_LABEL;
        }
        return columnText;
    }
}
