package io.opensphere.server.permalink.loaders;

/**
 * The class representing the response received when uploading a file.
 *
 */
public class UploadResponse
{
    /**
     * True if upload was a success, false otherwise.
     */
    private boolean myIsSuccess;

    /**
     * The url pointing to the uploaded file.
     */
    private String myUrl;

    /**
     * Gets the url pointing to the uploaded file.
     *
     * @return The uploaded file url.
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Indicates if the upload was successful or not.
     *
     * @return True if upload was successful, false otherwise.
     */
    public boolean isSuccess()
    {
        return myIsSuccess;
    }

    /**
     * Sets if the upload was successful or not.
     *
     * @param isSuccess True if upload was successful, false otherwise.
     */
    public void setSuccess(boolean isSuccess)
    {
        myIsSuccess = isSuccess;
    }

    /**
     * Sets the url pointing to the uploaded file.
     *
     * @param url The uploaded file url.
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }
}
