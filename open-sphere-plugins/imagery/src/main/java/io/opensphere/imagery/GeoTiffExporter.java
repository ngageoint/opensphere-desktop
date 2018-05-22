package io.opensphere.imagery;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;

import javax.swing.JComponent;
import javax.swing.JFrame;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.export.ExportUtilities;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.gdal.GdalIOUtilities;
import io.opensphere.core.util.javafx.WebPanel;
import io.opensphere.core.util.swing.AutohideMessageDialog;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * Implements the Exporter interface and is inadvisably installed into the
 * application through means not found in the source. Once installed, it
 * provides a menu item to activate its functionality, including a dialog and
 * supporting code. Its purpose is to allow a user to select some
 * server-generated imagery and save it as a GeoTiff that can eventually be
 * re-imported into the tool for future reference. It is also important to note
 * that a new instance of this class is created for each user request. <br>
 * <br>
 * Most of the technical work of retrieving and compositing image tiles is
 * handled by the helper class ExportHelper (q.v.). <br>
 * <br>
 * At present, the only supported geographical projection is WGS_84/EPSG:4326,
 * which is the default used by the application.
 */
public class GeoTiffExporter extends AbstractExporter
{
    /** Bla. */
    private MenuBarRegistry menuBarReg;

    /** Bla. */
    private MantleToolbox mantleTools;

    /** Bla. */
    private DataGroupController dataCtrl;

    /** The list of data types to be exported. */
    private Collection<DataTypeInfo> typeList;

    /** When false, an export will not be allowed. */
    private boolean valid;

    /** Query bounds for the current request. */
    private GeographicBoundingBox bounds;

    /** Main dialog component. */
    private ExportOptionsPanel myOptionsPanel;

    @Override
    public File export(File file) throws ExportException
    {
        if (myOptionsPanel == null)
        {
            return null;
        }
        int zoom = myOptionsPanel.getMaxZoomLevel();
        setObjects(myOptionsPanel.getSelections());
        myOptionsPanel = null;
        if (!valid)
        {
            return null;
        }

        File outFile = FileUtilities.ensureSuffix(file, "tiff");
        doExport(outFile, zoom);
        return outFile;
    }

    /**
     * Main workhorse method for exporting to file.
     *
     * @param outFile output file
     * @param zoom maximum zoom level
     */
    private void doExport(File outFile, int zoom)
    {
        // register as a task in progress
        TaskActivity act = new TaskActivity();
        act.setLabelValue("Composing data imagery...");
        act.setActive(true);
        menuBarReg.addTaskActivity(act);

        ExportHelper help = new ExportHelper();
        help.setTools(myToolbox);

        List<String> typeKeys = typeList.stream().map(t -> t.getTypeKey()).collect(Collectors.toList());
        BufferedImage img = help.stackComposites(typeKeys, bounds, zoom);
        if (img == null)
        {
            return;
        }

        GdalIOUtilities.exportTiff(img, bounds, outFile);

        // no longer is this task in progress
        act.setComplete(true);
        // remove activity indicator
        menuBarReg.removeTaskActivity(act);
        // inform the user
        EventQueue.invokeLater(() -> showCompletionDialog(outFile));
    }

    /**
     * Inform the user that the download is complete and where to find it.
     *
     * @param file the output file
     */
    private void showCompletionDialog(File file)
    {
        WebPanel fxPanel = new WebPanel();

        JFrame mainFrame = getToolbox().getUIRegistry().getMainFrameProvider().get();
        AutohideMessageDialog dialog = new AutohideMessageDialog(mainFrame, ModalityType.MODELESS);
        dialog.setTitle("Done");
        dialog.initialize(fxPanel, null, getToolbox().getPreferencesRegistry().getPreferences(GeoTiffExporter.class),
                "hideDoneDialog");
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setPreferredSize(new Dimension(700, 150));
        dialog.setVisible(true);
        dialog.setLocationRelativeTo(mainFrame);

        Platform.runLater(() -> fxPanel.loadContent("<html><body bgcolor='#535366' style='color: white;'>"
                + "GeoTIFF export is complete to <a style='color: white;' href='" + file.toURI() + "'>" + file
                + "</a>.<p><a style='color: white;' href='" + file.getParentFile().toURI() + "'>Parent directory</a>"
                + "<body></html>"));
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, Object key)
    {
        if (!(key instanceof GeometryContextKey))
        {
            return null;
        }
        Geometry geom = ((GeometryContextKey)key).getGeometry();
        if (!(geom instanceof PolygonGeometry))
        {
            return null;
        }
        bounds = findBounds((PolygonGeometry)geom);
        return Collections.singleton(SwingUtilities.newMenuItem(getMimeTypeString(), e -> export()));
    }

    @Override
    public void setToolbox(Toolbox toolbox)
    {
        myToolbox = toolbox;
        menuBarReg = myToolbox.getUIRegistry().getMenuBarRegistry();
        mantleTools = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        dataCtrl = mantleTools.getDataGroupController();
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        if (bounds == null)
        {
            return null;
        }
        myOptionsPanel = new ExportOptionsPanel(dataCtrl.findActiveMembers(GeoTiffExporter::isImageData));
        return myOptionsPanel;
    }

    /**
     * Respond to the menu item by launching the dialog (cf.
     * ExportUtilities::export).
     */
    private void export()
    {
        ExportUtilities.export(myToolbox.getUIRegistry().getMainFrameProvider().get(), myToolbox.getPreferencesRegistry(), this);
    }

    /**
     * Glibly create the GeographicBoundingBox for the specified polygon, which
     * must be composed of GeographicPosition vertices.
     *
     * @param poly bla
     * @return bla
     */
    @SuppressWarnings("unchecked")
    private static GeographicBoundingBox findBounds(PolygonGeometry poly)
    {
        return GeographicBoundingBox.getMinimumBoundingBox((Collection<? extends GeographicPosition>)poly.getVertices());
    }

    @Override
    public boolean canExport(Class<?> target)
    {
        /* False to prevent showing it in the layers menu. This is OK because it doesn't even get called when exporting from a
         * query area. */
        return false;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.TIFF;
    }

    @Override
    public AbstractExporter setObjects(Collection<?> objects)
    {
        valid = false;
        typeList = New.list();
        for (Object obj : objects)
        {
            if (!(obj instanceof DataTypeInfo))
            {
                return this;
            }
            DataTypeInfo type = (DataTypeInfo)obj;
            if (isImageData(type))
            {
                typeList.add(type);
            }
        }
        valid = !typeList.isEmpty();
        return super.setObjects(typeList);
    }

    /**
     * Decide whether or not to allow the given DataTypeInfo as an option for
     * export. Generally speaking, only the IMAGE_TILE visualization type is
     * allowed, but other special rules may be applied.
     *
     * @param dataType bla
     * @return bla
     */
    private static boolean isImageData(DataTypeInfo dataType)
    {
        if (!dataType.isVisible())
        {
            return false;
        }
        MapVisualizationInfo mapVisInfo = dataType.getMapVisualizationInfo();
        if (mapVisInfo == null)
        {
            return false;
        }
        MapVisualizationType visType = mapVisInfo.getVisualizationType();
        return visType == MapVisualizationType.IMAGE_TILE && mapVisInfo.getTileLevelController() != null;
        // GeoPackage export also allows: MapVisualizationType.TERRAIN_TILE
    }
}
