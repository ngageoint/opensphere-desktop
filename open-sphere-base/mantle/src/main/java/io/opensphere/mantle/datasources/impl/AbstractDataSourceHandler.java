package io.opensphere.mantle.datasources.impl;

import java.io.FileNotFoundException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceHandler;
import io.opensphere.mantle.datasources.LoadEndDispositionEvent;
import io.opensphere.mantle.datasources.SingleFileDataSource;

/**
 * Base class for data source handlers.
 */
public abstract class AbstractDataSourceHandler implements IDataSourceHandler
{
    /** The my controller. */
    private final AbstractDataSourceController myController;

    /** The my event executor. */
    private ExecutorService myEventExecutor;

    /**
     * Instantiates a new default data source handler.
     *
     * @param controller the controller
     */
    public AbstractDataSourceHandler(AbstractDataSourceController controller)
    {
        myController = controller;
    }

    /**
     * Display a user message and optionally logs the message.
     *
     * @param logger the applicable handler logger
     * @param type the type
     * @param msg the message to display or log
     * @param e the exception
     * @param includeLoggerMsg the include logger msg
     */
    public void displayUserToastMessage(Logger logger, Type type, String msg, final Exception e, boolean includeLoggerMsg)
    {
        if (includeLoggerMsg)
        {
            switch (type)
            {
                case INFO:
                    logger.log(AbstractDataSource.class.getName(), Level.INFO, msg, e);
                    break;

                case WARNING:
                    logger.log(AbstractDataSource.class.getName(), Level.WARN, msg, e);
                    break;

                case ERROR:
                    logger.log(AbstractDataSource.class.getName(), Level.ERROR, msg, e);
                    break;

                default:
                    break;
            }
        }
        UserMessageEvent.message(getController().getToolbox().getEventManager(), type, msg, false, this, e, true);
    }

    /**
     * Gets the my controller.
     *
     * @return the my controller
     */
    public AbstractDataSourceController getController()
    {
        return myController;
    }

    /**
     * Gets the executor service.
     *
     * @return the executor service
     */
    public ExecutorService getExecutorService()
    {
        return myEventExecutor;
    }

    /**
     * Send error message to user.
     *
     * @param message the message
     * @param e the e
     */
    public void sendErrorMessageToUser(String message, Exception e)
    {
        addMessage(Type.ERROR, message, true, this, e);
    }

    /**
     * Process load end messages and send user messages to user message dialog.
     *
     * @param evt the {@link LoadEndDispositionEvent}
     */
    public void sendLoadEndMessages(LoadEndDispositionEvent evt)
    {
        if (!evt.wasSuccessful())
        {
            if (evt.getException() != null)
            {
                String typeName = evt.getController().getTypeName();
                String sourceName = evt.getDataSource() == null ? "" : "\"" + evt.getDataSource().getName() + "\"";
                if (evt.wasLoading())
                {
                    StringBuilder errorMessage = new StringBuilder();
                    if (evt.getException() instanceof FileNotFoundException)
                    {
                        handleFileNotFoundException(evt, typeName, errorMessage);
                    }
                    else if (evt.getException() instanceof SocketException)
                    {
                        errorMessage.append("A problem was encountered while trying to load the requested ").append(typeName)
                                .append(" source ").append(sourceName).append(":\n").append(evt.getException().getMessage());
                    }
                    else
                    {
                        // The interrupted exceptions happen when a user
                        // cancels
                        // a load during the event. In this case
                        // it's not really an error so don't show a
                        // message and
                        // set the load error to false.
                        if (evt.getException() instanceof InterruptedException
                                || evt.getException() instanceof InterruptedIOException
                                || evt.getException() instanceof CancellationException)
                        {
                            errorMessage = null;
                            evt.getDataSource().setLoadError(false, this);
                            evt.getController().updateSource(evt.getDataSource());
                        }
                        else
                        {
                            handleUnknownLoadEndException(evt, typeName, sourceName, errorMessage);
                        }
                    }
                    if (errorMessage != null)
                    {
                        sendErrorMessageToUser(errorMessage.toString(), null);
                    }
                }
                else
                {
                    sendErrorMessageToUser("SOURCE REMOVE ERROR\nA problem was encountered while trying to remove the requested "
                            + typeName + " source " + sourceName + ".\n" + evt.getException().getMessage(), null);
                }
            }
            else if (evt.getMessage() != null && !evt.getMessage().isEmpty())
            {
                sendWarningMessageToUser("SOURCE NOT ADDED\n" + evt.getMessage(), null);
            }
        }
        else if (evt.getMessage() != null && !evt.getMessage().isEmpty())
        {
            sendMessageToUser("SOURCE ADDED\n" + evt.getMessage());
        }
    }

    /**
     * Send message to user.
     *
     * @param message the message
     */
    public void sendMessageToUser(String message)
    {
        addMessage(Type.INFO, message, true, this, null);
    }

    /**
     * Send warning message to user.
     *
     * @param message the message
     * @param e the e
     */
    public void sendWarningMessageToUser(String message, Exception e)
    {
        addMessage(Type.WARNING, message, true, this, e);
    }

    /**
     * Sets the executor service.
     *
     * @param execService the new executor service
     */
    public void setExecutorService(ExecutorService execService)
    {
        myEventExecutor = execService;
    }

    @Override
    public boolean updateDataSource(IDataSource pSource)
    {
        removeDataSource(pSource);
        return addDataSource(pSource);
    }

    /**
     * Adds the message.
     *
     * @param type the type
     * @param msg the msg
     * @param makeVisible the make visible
     * @param source the source
     * @param e the e
     */
    private void addMessage(Type type, String msg, boolean makeVisible, Object source, final Exception e)
    {
        UserMessageEvent.message(getController().getToolbox().getEventManager(), type, msg, makeVisible, source, e, false);
    }

    /**
     * Handle file not found exception when processing load end disposition.
     *
     * @param event the LoadEndDispositionEvent
     * @param typeName the type name
     * @param errorMessage the error message
     */
    private void handleFileNotFoundException(final LoadEndDispositionEvent event, String typeName, StringBuilder errorMessage)
    {
        if (event.getDataSource() instanceof SingleFileDataSource)
        {
            errorMessage.append("Requested ").append(typeName)
                    .append(" file could not be loaded because it could not be found on the file system:\n")
                    .append(((SingleFileDataSource)event.getDataSource()).getPath());
        }
        else
        {
            errorMessage.append("Requested ").append(typeName)
                    .append(" could not be loaded because one or more files could not be found on the file system.");
        }
    }

    /**
     * Handle unknown load end exception.
     *
     * @param event the {@link LoadEndDispositionEvent}
     * @param typeName the type name
     * @param sourceName the source name
     * @param errorMessage the error message
     */
    private void handleUnknownLoadEndException(final LoadEndDispositionEvent event, String typeName, String sourceName,
            StringBuilder errorMessage)
    {
        String path = null;
        if (event.getDataSource() instanceof SingleFileDataSource)
        {
            path = ((SingleFileDataSource)event.getDataSource()).getPath();
        }
        errorMessage.append("A problem was encountered while trying to load the requested ").append(typeName).append(" source ")
                .append(sourceName);

        if (null != path)
        {
            errorMessage.append(":\n").append(path);
        }

        if (null != event.getException() && null != event.getException().getMessage())
        {
            errorMessage.append("\n\nError: ").append(event.getException().getMessage());
        }
    }
}
