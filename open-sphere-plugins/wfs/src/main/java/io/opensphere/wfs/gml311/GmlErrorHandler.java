package io.opensphere.wfs.gml311;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;

/**
 * A handler class in which a GML ExceptionReport is processed.
 */
public class GmlErrorHandler
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(GmlErrorHandler.class);

    /**
     * The GML tag in which an exception is wrapped.
     */
    public static final String EXCEPTION_TAG = "Exception";

    /**
     * The GML tag in which an exception is wrapped.
     */
    public static final String SERVICE_EXCEPTION_TAG = "ServiceException";

    /**
     * The GML tag in which exception text is wrapped.
     */
    public static final String EXCEPTION_TEXT_TAG = "ExceptionText";

    /**
     * The GML tag in which all content processed by this handler is wrapped.
     */
    public static final String EXCEPTION_REPORT_TAG = "ExceptionReport";

    /**
     * The GML tag in which all content processed by this handler is wrapped.
     */
    public static final String SERVICE_EXCEPTION_REPORT_TAG = "ServiceExceptionReport";

    /**
     * The set of wrapper tags processed by this handler.
     */
    public static final Set<String> RECOGNIZED_EXCEPTION_TEXT_TAGS = New.set(EXCEPTION_TEXT_TAG, SERVICE_EXCEPTION_TAG);

    /**
     * The set of wrapper tags processed by this handler.
     */
    public static final Set<String> RECOGNIZED_EXCEPTION_TAGS = New.set(EXCEPTION_TAG);

    /**
     * The set of wrapper tags processed by this handler.
     */
    public static final Set<String> RECOGNIZED_REPORT_TAGS = New.set(EXCEPTION_REPORT_TAG, SERVICE_EXCEPTION_REPORT_TAG);

    /**
     * The current state of the handler. The handler will track its state as it progresses through the XML snippet. Initialized to
     * {@link State#WAITING}.
     */
    private State myCollectState = State.WAITING;

    /**
     * The report currently being processed by the handler.
     */
    private GmlExceptionReport myCurrentExceptionReport;

    /**
     * The exceptions processed by the parser.
     */
    private List<GmlExceptionReport> myExceptionReports;

    /**
     * Creates a new GML Error handler, ready to process all content wrapped within the supplied tag.
     */
    public GmlErrorHandler()
    {
        /* intentionally blank */
    }

    /**
     * Gets the value of the {@link #myExceptionReports} field.
     *
     * @return the value stored in the {@link #myExceptionReports} field.
     */
    public List<GmlExceptionReport> getExceptionReports()
    {
        return myExceptionReports;
    }

    /**
     * Tests to determine if this handler will process the supplied tag.
     *
     * @param pTag the tag to inspect.
     * @return true if the handler will process the supplied tag, false otherwise.
     */
    public boolean handlesTag(String pTag)
    {
        return RECOGNIZED_REPORT_TAGS.contains(pTag);
    }

    /**
     * Handle a GML Geometry-related opening tag.
     *
     * @param pTag the name of the GML tag to handle
     * @param pValue the value of the XML tag
     */
    public void handleClosingTag(String pTag, String pValue)
    {
        switch (myCollectState)
        {
            case COLLECT_EXCEPTION_TEXT:
                if (StringUtils.equals(pTag, EXCEPTION_TEXT_TAG))
                {
                    myCurrentExceptionReport.setText(pValue);
                    myCollectState = State.COLLECT_EXCEPTION;
                }
                else if (StringUtils.equals(pTag, SERVICE_EXCEPTION_TAG))
                {
                    myCurrentExceptionReport.setText(pValue);
                    myExceptionReports.add(myCurrentExceptionReport);
                    myCurrentExceptionReport = null;
                    myCollectState = State.SEEK_EXCEPTION;
                }
                break;
            case COLLECT_EXCEPTION:
                if (RECOGNIZED_EXCEPTION_TAGS.contains(pTag))
                {
                    myExceptionReports.add(myCurrentExceptionReport);
                    myCurrentExceptionReport = null;
                    myCollectState = State.SEEK_EXCEPTION;
                }
                break;
            case SEEK_EXCEPTION:
                if (RECOGNIZED_REPORT_TAGS.contains(pTag))
                {
                    myCollectState = State.WAITING;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Handles the supplied attribute.
     *
     * @param pName the name of the attribute to process.
     * @param pValue the value of the attribute to process.
     */
    public void handleAttribute(String pName, String pValue)
    {
        if (myCollectState == State.SEEK_EXCEPTION_TEXT && StringUtils.equals(pName, "exceptionCode"))
        {
            myCurrentExceptionReport.setCode(pValue);
        }
    }

    /**
     * Handle a GML exception report opening tag. The tag is handled according to the current value of the {@link #myCollectState}
     * , to avoid extra parsing.
     *
     * @param pTag the name of the GML tag to handle
     */
    public void handleOpeningTag(String pTag)
    {
        LOG.info(pTag);
        switch (myCollectState)
        {
            case WAITING:
                if (RECOGNIZED_REPORT_TAGS.contains(pTag))
                {
                    myCollectState = State.SEEK_EXCEPTION;
                    myCurrentExceptionReport = new GmlExceptionReport();
                    myExceptionReports = new ArrayList<>();
                }
                break;
            case SEEK_EXCEPTION:
                if (StringUtils.equals(pTag, EXCEPTION_TAG))
                {
                    myCollectState = State.SEEK_EXCEPTION_TEXT;
                }
                else if (StringUtils.equals(pTag, SERVICE_EXCEPTION_TAG))
                {
                    myCollectState = State.COLLECT_EXCEPTION_TEXT;
                }
                break;
            case SEEK_EXCEPTION_TEXT:
                if (RECOGNIZED_EXCEPTION_TEXT_TAGS.contains(pTag))
                {
                    myCollectState = State.COLLECT_EXCEPTION_TEXT;
                }
                break;
            default:
                LOG.error("Unexpected open tag encountered: '" + pTag + "'");
                break;
        }
    }

    /** Enum used to track the current GML Exception Report parse State. */
    private enum State
    {
        /** The state definition used when waiting for a closing 'ExceptionText' tag. */
        COLLECT_EXCEPTION_TEXT,

        /** The state definition used when waiting for a closing 'Exception' tag. */
        COLLECT_EXCEPTION,

        /** The state definition used when waiting for an opening 'ExceptionText' tag. */
        SEEK_EXCEPTION_TEXT,

        /** The state definition used when waiting for an opening 'Exception' tag. */
        SEEK_EXCEPTION,

        /** State while waiting for a new 'ExceptionReport' tag. */
        WAITING
    }
}
