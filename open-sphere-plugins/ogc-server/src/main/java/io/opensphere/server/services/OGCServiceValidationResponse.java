package io.opensphere.server.services;

/**
 * The Class OGCServiceValidationResponse used to relay server validation
 * information back to a master server controller.
 */
public class OGCServiceValidationResponse
{
    /** Message describing why validation failed (if applicable). */
    private String myErrorMessage;

    /** Valid flag: true if valid, false otherwise. */
    private boolean myIsValid;

    /** The server's unique id. */
    private final String myServerId;

    /** The title as retrieved from the server. */
    private String myServerTitle;

    /**
     * Constructor that takes a required serverId.
     *
     * @param serverId the server id
     */
    public OGCServiceValidationResponse(String serverId)
    {
        myServerId = serverId;
    }

    /**
     * Gets the error message, if validation failed.
     *
     * @return the error message
     */
    public String getErrorMessage()
    {
        return myErrorMessage;
    }

    /**
     * Gets the server id.
     *
     * @return the server id
     */
    public String getServerId()
    {
        return myServerId;
    }

    /**
     * Gets the server title.
     *
     * @return the server title
     */
    public String getServerTitle()
    {
        return myServerTitle;
    }

    /**
     * Checks if this server validated successfully.
     *
     * @return true, if is valid
     */
    public boolean isValid()
    {
        return myIsValid;
    }

    /**
     * If there was a specific error, set it here so it can be displayed to a
     * user or properly logged.
     *
     * @param error the validation error
     */
    public void setErrorMessage(String error)
    {
        myErrorMessage = error;
    }

    /**
     * Sets the server title.
     *
     * @param serverTitle the new server title
     */
    public void setServerTitle(String serverTitle)
    {
        myServerTitle = serverTitle;
    }

    /**
     * Sets the valid flag.
     *
     * @param isValid true, if the server validated successfully
     */
    public void setValid(boolean isValid)
    {
        myIsValid = isValid;
    }
}
