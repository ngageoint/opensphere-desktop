package io.opensphere.myplaces.importer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.MemoizingSupplier;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.input.ViewPanel;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.mp.MapAnnotationPointRegistry;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.mantle.mp.event.impl.MapAnnotationCreatedEvent;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPoint;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.TimeSpanUtility;

/**
 * An exporter that creates points from {@link DataElement}s.
 */
public class DataElementPointExporter extends AbstractDataElementExporter
{
    private static final String SAVE_TO_PLACES_COMMAND = "Save to Places...";

    /** The none string. */
    private static final String NONE = "NONE";

    /** The map annotation point registry. */
    private MapAnnotationPointRegistry myMapAnnotationPointRegistry;

    @Override
    public MenuOption getMenuOption()
    {
        return new MenuOption(SAVE_TO_PLACES_COMMAND, SAVE_TO_PLACES_COMMAND, "Creates new places from the selected features.",
                new GenericFontIcon(AwesomeIconSolid.MAP_MARKER_ALT, Color.WHITE));
    }

    @Override
    public void setToolbox(Toolbox toolbox)
    {
        super.setToolbox(toolbox);
        myMapAnnotationPointRegistry = MantleToolboxUtils.getMantleToolbox(toolbox).getMapAnnotationPointRegistry();
    }

    @Override
    protected void export(DataTypeInfo dataType, Collection<? extends MapDataElement> elements) throws ExportException
    {
        if (!acceptSize(elements.size()))
        {
            return;
        }

        Supplier<Pair<String, String>> titleAndDesc = new MemoizingSupplier<>(
            () -> EventQueueUtilities.happyOnEdt(() -> getTitleAndDescription(dataType)));

        List<MutableMapAnnotationPoint> points = elements.stream()
                .filter(e -> e.getMapGeometrySupport() instanceof MapLocationGeometrySupport)
                .map(e -> createPoint(e, titleAndDesc)).collect(Collectors.toList());

        if (!points.isEmpty())
        {
            if (points.get(0) != null)
            {
                getToolbox().getEventManager().publishEvent(new MapAnnotationCreatedEvent(this, points));
            }
            // else the user cancelled
        }
        else
        {
            throw new ExportException("The selected data do not contain any points.");
        }
    }

    /**
     * Creates a map point from the data element.
     *
     * @param dataElement the data element
     * @param titleAndDesc the title/description supplier
     * @return the map point
     */
    private MutableMapAnnotationPoint createPoint(MapDataElement dataElement, Supplier<Pair<String, String>> titleAndDesc)
    {
        Pair<String, String> pair = titleAndDesc.get();
        if (pair == null)
        {
            return null;
        }

        String title = getValue(dataElement, pair.getFirstObject());
        if (StringUtils.isBlank(title))
        {
            title = "Untitled";
        }
        String desc = getValue(dataElement, pair.getSecondObject());
        if (NONE.equals(desc))
        {
            desc = null;
        }

        return createDefaultMapAnnotationPoint(dataElement, title, desc);
    }

    /**
     * Creates a DefaultMapAnnotationPoint.
     *
     * @param mapElement the map data element
     * @param title the title
     * @param desc the description
     * @return the DefaultMapAnnotationPoint
     */
    private DefaultMapAnnotationPoint createDefaultMapAnnotationPoint(MapDataElement mapElement, String title, String desc)
    {
        DefaultMapAnnotationPoint mp = new DefaultMapAnnotationPoint(myMapAnnotationPointRegistry.getUserDefaultPoint());
        MapGeometrySupport mgs = mapElement.getMapGeometrySupport();
        if (mgs instanceof MapLocationGeometrySupport)
        {
            MapLocationGeometrySupport mlgs = (MapLocationGeometrySupport)mgs;
            mp.setLat(mlgs.getLocation().getLatD(), this);
            mp.setLon(mlgs.getLocation().getLonD(), this);
            mp.setTitle(title, this);
            mp.getAnnoSettings().setDotOn(true, this);
            mp.setVisible(true, this);
            if (desc != null)
            {
                mp.setDescription(desc, this);
            }

            if (mlgs.getLocation().getAltitudeReference() == ReferenceLevel.ELLIPSOID)
            {
                mp.setAltitude(mlgs.getLocation().getAltM(), null);
            }
        }
        return mp;
    }

    /**
     * Gets the title and description from the user.
     *
     * @param dataType the data type
     * @return the title and description pair
     */
    private Pair<String, String> getTitleAndDescription(DataTypeInfo dataType)
    {
        assert EventQueue.isDispatchThread();

        List<String> columns = New.list(dataType.getMetaDataInfo().getKeyNames());

        JComboBox<String> titleChoiceCB = new JComboBox<>(columns.toArray(new String[columns.size()]));
        if (columns.contains("TITLE"))
        {
            titleChoiceCB.setSelectedItem("TITLE");
        }

        columns.add(0, NONE);
        JComboBox<String> descChoiceCB = new JComboBox<>(columns.toArray(new String[columns.size()]));

        ViewPanel cfgPanel = new ViewPanel();
        cfgPanel.addLabelComponent("Title Column:", titleChoiceCB);
        cfgPanel.addLabelComponent("Description Column:", descChoiceCB);
        cfgPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.add(new JLabel("Please choose title and description columns:"), BorderLayout.NORTH);
        questionPanel.add(cfgPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(), questionPanel,
                "Map Point Creation Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION)
        {
            String titleColumn = (String)titleChoiceCB.getSelectedItem();
            String descColumn = (String)descChoiceCB.getSelectedItem();
            return new Pair<>(titleColumn, descColumn);
        }
        return null;
    }

    /**
     * Gets the value.
     *
     * @param dataElement the map data element
     * @param column the column
     * @return the value
     */
    private String getValue(MapDataElement dataElement, String column)
    {
        String timeKey = dataElement.getDataTypeInfo().getMetaDataInfo().getTimeKey();
        Object value = Objects.equals(timeKey, column) ? dataElement.getTimeSpan() : dataElement.getMetaData().getValue(column);
        return convertToString(value);
    }

    /**
     * Convert to string.
     *
     * @param value the value
     * @return the string
     */
    private String convertToString(Object value)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof TimeSpan || value instanceof Date)
        {
            int timePrecision = getToolbox().getPreferencesRegistry().getPreferences(ListToolPreferences.class)
                    .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0);
            SimpleDateFormat format = ListToolPreferences.getSimpleDateFormatForPrecision(timePrecision);
            return value instanceof TimeSpan ? TimeSpanUtility.formatTimeSpan(format, (TimeSpan)value) : format.format(value);
        }
        else
        {
            return value.toString();
        }
    }
}
