package io.opensphere.mantle.plugin.selection;

import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ContextMenuSelectionAdapter;
import io.opensphere.core.control.action.ActionContext;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import net.jcip.annotations.NotThreadSafe;

/**
 * Creates a buffer region.
 *
 */
@NotThreadSafe
public class BufferRegionCreator
{
    /** The default distance of a new buffer. */
    public static final Length DEFAULT_BUFFER_DISTANCE = Length.create(Kilometers.class, 5.0);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The selection geometry. */
    private Geometry myGeometry;

    /** The Preview geometry. */
    private Geometry myPreviewGeometry;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox.
     */
    public BufferRegionCreator(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Gets the value of the {@link #myToolbox} field.
     *
     * @return the value stored in the {@link #myToolbox} field.
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Creates the buffer region for last selection geometry.
     *
     * @param geometry the geometry for which to create the buffer.
     */
    public void createBuffer(Geometry geometry)
    {
        myGeometry = geometry;
        UnitsProvider<Length> uProv = myToolbox.getUnitsRegistry().getUnitsProvider(Length.class);
        Length defaultBuffer = createDefaultBuffer(uProv);
        // show the buffer when the dialog pops up:
        handleBufferEdit(defaultBuffer);
        BufferRegionInputPanel inputPanel = new BufferRegionInputPanel(defaultBuffer, uProv, getBufferRangeMessage(),
                this::handleBufferEdit);
        Length val = getBufferUserInput(inputPanel);
        if (val != null)
        {
            handleCreateBuffer(MouseInfo.getPointerInfo().getLocation(), val.inMeters());
        }
    }

    /**
     * Gets the default buffer size from the supplied unit provider, using the
     * default buffer distance.
     *
     * @param provider the provider from which units are provided.
     * @return the default length extracted from the supplied units provider.
     */
    private static Length createDefaultBuffer(UnitsProvider<Length> provider)
    {
        return provider.convert(provider.getPreferredFixedScaleUnits(DEFAULT_BUFFER_DISTANCE), DEFAULT_BUFFER_DISTANCE);
    }

    /**
     * Respond to a change in the buffer distance editor. If the distance is
     * well-formed and valid, show a preview; otherwise kill the preview.
     *
     * @param pLength the buffer distance supplied by the editor.
     */
    public void handleBufferEdit(Length pLength)
    {
        if (isLengthValid(pLength))
        {
            handlePreviewBuffer(pLength.inMeters());
        }
        else
        {
            destroyPreview();
        }
    }

    /**
     * Creates a preview of the supplied distance, expressed in meters.
     *
     * @param pDistanceInMeters the distance from the origin, expressed in
     *            meters.
     */
    private void handlePreviewBuffer(double pDistanceInMeters)
    {
        if (myGeometry != null)
        {
            if (myPreviewGeometry != null)
            {
                unregisterGeometry(myPreviewGeometry);
            }
            myPreviewGeometry = bufferOrSame(myGeometry, pDistanceInMeters);
            if (myPreviewGeometry != null)
            {
                registerGeometry(myPreviewGeometry);
            }
        }
    }

    /**
     * Removes the supplied geometry from display.
     *
     * @param geometry the geometry to remove from display.
     */
    private void unregisterGeometry(Geometry geometry)
    {
        myToolbox.getGeometryRegistry().removeGeometriesForSource(this, Collections.singletonList(geometry));
    }

    /**
     * May not be necessary, but this is the way I found it. Create a "buffer"
     * Geometry for the argument, if it is of a supported type. If it is not,
     * then the argument is returned unchanged.
     *
     * @param geometry the geometry
     * @param distance buffer distance in meters
     * @return the buffer geometry if supported, or <i>g</i>
     */
    protected Geometry bufferOrSame(Geometry geometry, double distance)
    {
        Geometry newG = JTSCoreGeometryUtilities.getBufferGeom(geometry, distance);
        if (newG != null)
        {
            return newG;
        }
        return geometry;
    }

    /**
     * Registers the supplied geometry for display.
     *
     * @param geometry the geometry to register.
     */
    protected void registerGeometry(Geometry geometry)
    {
        myToolbox.getGeometryRegistry().addGeometriesForSource(this, Collections.singletonList(geometry));
    }

    /**
     * Destroy preview.
     */
    protected void destroyPreview()
    {
        if (myPreviewGeometry != null)
        {
            unregisterGeometry(myPreviewGeometry);
        }
        myPreviewGeometry = null;
    }

    /**
     * Validate the buffer distance.
     *
     * @param pLength the buffer distance to validate.
     * @return true if and only if the given distance is acceptable
     */
    private boolean isLengthValid(Length pLength)
    {
        if (myGeometry instanceof PolygonGeometry)
        {
            return pLength.getMagnitude() != 0.0;
        }
        return pLength.getMagnitude() > 0.0;
    }

    /**
     * Gathers input from the user to construct the buffer. The user may cancel
     * the dialog, indicating that no buffer should be drawn. As part of the
     * dialog, the buffer is drawn on the screen as the user changes the input
     * values, as a preview. The preview is a transient geometry, and should be
     * destroyed when the dialog closes (regardless of the user's choice). If
     * the user elects to accept the input, then a new, permanent buffer is
     * drawn for later use. The {@link Length} return reflects the user's input,
     * and is used to draw the permanent buffer. If the user cancels the dialog,
     * a null value is returned.
     *
     * @param inputPanel the panel in which the user enters data.
     * @return the {@link Length} from the user's input, or null if the dialog
     *         has been canceled.
     */
    protected Length getBufferUserInput(BufferRegionInputPanel inputPanel)
    {
        while (true)
        {
            // if the user cancels, then forget it
            if (!showPopup("Input Buffer Distance", inputPanel))
            {
                destroyPreview();
                return null;
            }
            try
            {
                Length returnValue = inputPanel.getDistance();
                if (isLengthValid(returnValue))
                {
                    destroyPreview();
                    return returnValue;
                }
                errorPopup("Invalid Distance Error", "The distance must be greater than zero.");
            }
            catch (NumberFormatException e)
            {
                errorPopup("Invalid Distance Error", "The distance must be a valid number.");
            }
        }
    }

    /**
     * Sets the value of the {@link #myGeometry} field.
     *
     * @param geometry the value to store in the {@link #myGeometry} field.
     */
    protected void setGeometry(Geometry geometry)
    {
        myGeometry = geometry;
    }

    /**
     * Gets the value of the {@link #myGeometry} field.
     *
     * @return the value stored in the {@link #myGeometry} field.
     */
    protected Geometry getGeometry()
    {
        return myGeometry;
    }

    /**
     * Sets the value of the {@link #myPreviewGeometry} field.
     *
     * @param previewGeometry the value to store in the
     *            {@link #myPreviewGeometry} field.
     */
    protected void setPreviewGeometry(Geometry previewGeometry)
    {
        myPreviewGeometry = previewGeometry;
    }

    /**
     * Gets the value of the {@link #myPreviewGeometry} field.
     *
     * @return the value stored in the {@link #myPreviewGeometry} field.
     */
    protected Geometry getPreviewGeometry()
    {
        return myPreviewGeometry;
    }

    /**
     * Handle create buffer.
     *
     * @param point the screen location at which to show the popup.
     * @param distance the buffer distance
     */
    protected void handleCreateBuffer(Point point, double distance)
    {
        myGeometry = bufferOrSame(myGeometry, distance);
        if (myGeometry == null)
        {
            errorPopup("Buffer Region Failure", "Failed to create buffer region for this item.");
            return;
        }
        myPreviewGeometry = myGeometry;
        registerGeometry(myPreviewGeometry);
        ActionContext<GeometryContextKey> context = myToolbox.getUIRegistry().getContextActionManager()
                .getActionContext(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class);
        Frame mainFrame = myToolbox.getUIRegistry().getMainFrameProvider().get();
        SwingUtilities.convertPointFromScreen(point, mainFrame);
        context.doAction(new GeometryContextKey(myPreviewGeometry), mainFrame, point.x, point.y, new PreviewKiller());
    }

    /**
     * Gets a message in which the user is informed of the constraints of buffer
     * region creation.
     *
     * @return a message in which the user is informed of the constraints of
     *         buffer region creation.
     */
    private String getBufferRangeMessage()
    {
        if (myGeometry instanceof PolygonGeometry)
        {
            return "Distance may be positive or negative, but not zero";
        }
        return "Distance must be greater than zero";
    }

    /**
     * Show a modal dialog with the given "message". In the only current use,
     * the so-called "message" is actually a GUI.
     *
     * @param title the popup title
     * @param msg the "message"
     * @return true if and only if the user dismissed by selecting "Okay"
     */
    private boolean showPopup(String title, Object msg)
    {
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), msg,
                title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Show an error popup. Who would have guessed?
     *
     * @param title title
     * @param msg message
     */
    protected void errorPopup(String title, String msg)
    {
        JOptionPane.showMessageDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), msg, title,
                JOptionPane.ERROR_MESSAGE);
    }

    /** Kills the preview, sometimes. */
    class PreviewKiller extends ContextMenuSelectionAdapter
    {
        @Override
        public void popupMenuCanceled(PopupMenuEvent e)
        {
            destroyPreview();
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
        {
            destroyPreview();
        }
    }
}
