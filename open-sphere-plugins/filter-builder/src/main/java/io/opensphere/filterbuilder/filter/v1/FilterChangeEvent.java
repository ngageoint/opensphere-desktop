package io.opensphere.filterbuilder.filter.v1;

/**
 * FilterChangeEvent.
 */
public class FilterChangeEvent
{
    /** The Constant ACTIVE_STATE. */
    public static final int ACTIVE_STATE = 0;

    /** The Constant NAME_CHANGE. */
    public static final int NAME_CHANGE = 2;

    /** The Constant STRUCTURE_CHANGED. */
    public static final int STRUCTURE_CHANGED = 4;

    /** The Constant SOURCE_CHANGED. */
    public static final int SOURCE_CHANGED = 8;

    /** The Constant SAVE_STATE. */
    public static final int SAVE_STATE = 16;

    /** The Constant FILTER_ADDED. */
    public static final int FILTER_ADDED = 32;

    /** The Constant FILTER_REMOVED. */
    public static final int FILTER_REMOVED = 64;

    /** The Constant FILTER_DESCRIPTION_CHANGE. */
    public static final int FILTER_DESCRIPTION_CHANGE = 128;

    /** The filter. */
    private final Filter myFilter;

    /** The source. */
    private final Object mySource;

    /** The change type. */
    private final int myChangeType;

    /** The Message. */
    private final String myMessage;

    /**
     * Constructor for the event.
     *
     * @param pFilter the filter that changed.
     * @param pSource the source of the event. What class generated the event.
     * @param pChangeType the change type selected from {@link #ACTIVE_STATE},
     *            {@link #NAME_CHANGE} or {@link #STRUCTURE_CHANGED}.
     */
    public FilterChangeEvent(Filter pFilter, Object pSource, int pChangeType)
    {
        this(pFilter, pSource, pChangeType, null);
    }

    /**
     * Instantiates a new filter change event.
     *
     * @param pFilter the filter
     * @param pSource the source
     * @param pChangeType the change type
     * @param pMessage the message
     */
    public FilterChangeEvent(Filter pFilter, Object pSource, int pChangeType, String pMessage)
    {
        super();
        myFilter = pFilter;
        mySource = pSource;
        myChangeType = pChangeType;
        myMessage = pMessage;
    }

    /**
     * Gets the change type.
     *
     * @return the changeType
     */
    public int getChangeType()
    {
        return myChangeType;
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public Filter getFilter()
    {
        return myFilter;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage()
    {
        return myMessage;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public Object getSource()
    {
        return mySource;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);

        sb.append("FilterChangeEvent: " + "[" + "changeType=");
        switch (myChangeType)
        {
            case ACTIVE_STATE:
                sb.append("ACTIVE_STATE");
                break;
            case NAME_CHANGE:
                sb.append("NAME_CHANGE");
                break;
            case FILTER_DESCRIPTION_CHANGE:
                sb.append("FILTER_DESCRIPTION_CHANGE");
                break;
            case STRUCTURE_CHANGED:
                sb.append("STRUCTURE_CHANGED");
                break;
            default:
                sb.append("UNKNOW_TYPE");
                break;
        }
        sb.append(";  " + "filter=");
        sb.append(myFilter.toString());
        sb.append(";  " + "source=");
        sb.append(mySource.toString());
        sb.append("message=");
        sb.append(myMessage);
        sb.append(']');

        return sb.toString();
    }
}
