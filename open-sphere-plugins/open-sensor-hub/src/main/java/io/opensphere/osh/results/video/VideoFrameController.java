package io.opensphere.osh.results.video;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.osh.util.OSHQuerier;

/** Controls video frames. */
public class VideoFrameController extends EventListenerService
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(VideoFrameController.class);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The data registry querier. */
    private final OSHQuerier myQuerier;

    /** The active time listener. */
    private final PrimaryTimeSpanChangeListener myTimeListener;

    /** The map of data type to video window. */
    private final Map<DataTypeInfo, VideoWindow> myDataTypeToWindowMap = Collections.synchronizedMap(New.map());

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param querier The data registry querier
     */
    public VideoFrameController(Toolbox toolbox, OSHQuerier querier)
    {
        super(toolbox.getEventManager(), 1);
        myToolbox = toolbox;
        myQuerier = querier;
        myTimeListener = new PrimaryTimeSpanChangeListener()
        {
            @Override
            public void primaryTimeSpansChanged(TimeSpanList spans)
            {
                ThreadUtilities.runBackground(() -> handleTimeChange(spans));
            }

            @Override
            public void primaryTimeSpansCleared()
            {
            }
        };
        bindEvent(DataTypeVisibilityChangeEvent.class, this::handleDataTypeVisibilityChange);
    }

    @Override
    public void open()
    {
        super.open();
        myToolbox.getTimeManager().addPrimaryTimeSpanChangeListener(myTimeListener);
    }

    @Override
    public void close()
    {
        myToolbox.getTimeManager().removePrimaryTimeSpanChangeListener(myTimeListener);
        super.close();
    }

    /**
     * Adds the data type to the list of types with data.
     *
     * @param dataType the data type
     */
    public void addDataType(DataTypeInfo dataType)
    {
        myDataTypeToWindowMap.putIfAbsent(dataType, null);
    }

    /**
     * Shows the video window for the data type.
     *
     * @param dataType the data type
     */
    public void showWindow(DataTypeInfo dataType)
    {
        EventQueueUtilities.runOnEDT(() -> showWindowNow(dataType));
    }

    /**
     * Hides the video window for the data type.
     *
     * @param dataType the data type
     */
    public void hideWindow(DataTypeInfo dataType)
    {
        VideoWindow window = myDataTypeToWindowMap.get(dataType);
        if (window != null)
        {
            EventQueue.invokeLater(() -> window.setVisible(false));
        }
    }

    /**
     * Shows the video window for the data type on the calling thread.
     *
     * @param dataType the data type
     * @return the video window
     */
    private VideoWindow showWindowNow(DataTypeInfo dataType)
    {
        VideoWindow window = myDataTypeToWindowMap.get(dataType);
        if (window == null)
        {
            window = new VideoWindow(myToolbox.getUIRegistry().getMainFrameProvider().get(), dataType);
            window.setVisible(true);
            window.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    dataType.setVisible(false, this);
                }
            });
            myDataTypeToWindowMap.put(dataType, window);
        }
        else
        {
            ensureVisible(window);
        }
        dataType.setVisible(true, this);
        return window;
    }

    /**
     * Handles a change in the active time.
     *
     * @param spans the active spans
     */
    private void handleTimeChange(TimeSpanList spans)
    {
        // Have to query a proper time span instead of an instant for the data
        // registry to work
        TimeSpan timeSpan = TimeSpan.get(spans.get(0).getEnd(), Milliseconds.ONE);
        try
        {
            synchronized (myDataTypeToWindowMap)
            {
                for (Map.Entry<DataTypeInfo, VideoWindow> entry : myDataTypeToWindowMap.entrySet())
                {
                    DataTypeInfo dataType = entry.getKey();
                    VideoWindow window = entry.getValue();
                    if (dataType.isVisible())
                    {
                        byte[] bytes = myQuerier.queryImage(dataType.getTypeKey(), timeSpan);
                        if (bytes != null)
                        {
                            if (window != null)
                            {
                                EventQueue.invokeLater(() -> ensureVisible(window));
                                window.setImageBytes(bytes);
                            }
                            else
                            {
                                EventQueue.invokeLater(() -> showWindowNow(dataType).setImageBytes(bytes));
                            }
                        }
                    }
                }
            }
        }
        catch (QueryException e)
        {
            LOGGER.error(e, e);
            Notify.error(e.toString());
        }
    }

    /**
     * Handles a change in data type visibility.
     *
     * @param event the event
     */
    private void handleDataTypeVisibilityChange(DataTypeVisibilityChangeEvent event)
    {
        VideoWindow window = myDataTypeToWindowMap.get(event.getDataTypeInfo());
        if (window != null)
        {
            EventQueue.invokeLater(() -> window.setVisible(event.isVisible()));
        }
    }

    /**
     * Ensures the component is visible.
     *
     * @param c the components
     */
    private static void ensureVisible(Component c)
    {
        if (!c.isVisible())
        {
            c.setVisible(true);
        }
    }
}
