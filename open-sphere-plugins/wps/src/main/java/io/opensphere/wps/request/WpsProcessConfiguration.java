package io.opensphere.wps.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.wfs.layer.WFSDataType;
import net.opengis.wps._100.ProcessDescriptionType;

/**
 * A container in which the configuration of a given process is encapsulated.
 */
public class WpsProcessConfiguration implements Serializable
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(WpsProcessConfiguration.class);

    /**
     * The default serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The set of inputs defined by the server.
     */
    private final Map<String, String> myInputs;

    /**
     * The id of this instance of the wps configuration.
     */
    private final String myInstanceId;

    /**
     * The process configuration as described by the server.
     */
    private transient ProcessDescriptionType myProcessDescription;

    /**
     * The identifier of the process to execute.
     */
    private String myProcessIdentifier;

    /**
     * The title of the process to execute.
     */
    private String myProcessTitle;

    /**
     * The data type with which the process's results will be associated.
     */
    private transient WFSDataType myResultType;

    /**
     * The execution mode selected by the user.
     */
    private WpsExecutionMode myRunMode;

    /**
     * The process description serialized into bytes. This helps facilitate
     * saving of the wps processes.
     */
    private byte[] mySerializedProcessDescription;

    /**
     * The unique identifier of the server on which the configuration is based.
     */
    private final String myServerId;

    /**
     * Creates a new process configuration, configured for the supplied server.
     *
     * @param pServerId the unique identifier of the server for which the
     *            process is configured.
     * @param pProcessDescription the description of the process to be invoked.
     */
    public WpsProcessConfiguration(String pServerId, ProcessDescriptionType pProcessDescription)
    {
        myServerId = pServerId;
        myProcessDescription = pProcessDescription;
        myInstanceId = UUID.randomUUID().toString();

        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            XMLUtilities.writeXMLObject(myProcessDescription, output);
            mySerializedProcessDescription = output.toByteArray();
        }
        catch (JAXBException e)
        {
            LOGGER.error(e, e);
        }

        myInputs = New.map();
    }

    /**
     * Gets the value of the {@link #myInputs} field.
     *
     * @return the value stored in the {@link #myInputs} field.
     */
    public Map<String, String> getInputs()
    {
        return myInputs;
    }

    /**
     * Gets the id of this instance of the wps configuration.
     *
     * @return The id of this instance.
     */
    public String getInstanceId()
    {
        return myInstanceId;
    }

    /**
     * Gets the value of the {@link #myProcessDescription} field.
     *
     * @return the value stored in the {@link #myProcessDescription} field.
     */
    public ProcessDescriptionType getProcessDescription()
    {
        if (myProcessDescription == null && mySerializedProcessDescription != null && mySerializedProcessDescription.length > 0)
        {
            try
            {
                myProcessDescription = XMLUtilities.readXMLObject(new ByteArrayInputStream(mySerializedProcessDescription),
                        ProcessDescriptionType.class);
            }
            catch (JAXBException e)
            {
                LOGGER.error(e, e);
            }
        }

        return myProcessDescription;
    }

    /**
     * Gets the value of the {@link #myProcessIdentifier} field.
     *
     * @return the value stored in the {@link #myProcessIdentifier} field.
     */
    public String getProcessIdentifier()
    {
        return myProcessIdentifier;
    }

    /**
     * Gets the value of the {@link #myProcessTitle} field.
     *
     * @return the value stored in the {@link #myProcessTitle} field.
     */
    public String getProcessTitle()
    {
        return myProcessTitle;
    }

    /**
     * Gets the value of the {@link #myResultType} field.
     *
     * @return the value stored in the {@link #myResultType} field.
     */
    public WFSDataType getResultType()
    {
        return myResultType;
    }

    /**
     * Gets the value of the {@link #myRunMode} field.
     *
     * @return the value stored in the {@link #myRunMode} field.
     */
    public WpsExecutionMode getRunMode()
    {
        return myRunMode;
    }

    /**
     * Gets the value of the {@link #myServerId} field.
     *
     * @return the value stored in the {@link #myServerId} field.
     */
    public String getServerId()
    {
        return myServerId;
    }

    /**
     * Sets the value of the {@link #myProcessIdentifier} field.
     *
     * @param pProcessIdentifier the value to store in the
     *            {@link #myProcessIdentifier} field.
     */
    public void setProcessIdentifier(String pProcessIdentifier)
    {
        myProcessIdentifier = pProcessIdentifier;
    }

    /**
     * Sets the value of the {@link #myProcessTitle} field.
     *
     * @param pProcessTitle the value to store in the {@link #myProcessTitle}
     *            field.
     */
    public void setProcessTitle(String pProcessTitle)
    {
        myProcessTitle = pProcessTitle;
    }

    /**
     * Sets the value of the {@link #myResultType} field.
     *
     * @param pResultType the value to store in the {@link #myResultType} field.
     */
    public void setResultType(WFSDataType pResultType)
    {
        myResultType = pResultType;
    }

    /**
     * Sets the value of the {@link #myRunMode} field.
     *
     * @param pRunMode the value to store in the {@link #myRunMode} field.
     */
    public void setRunMode(WpsExecutionMode pRunMode)
    {
        myRunMode = pRunMode;
    }
}
