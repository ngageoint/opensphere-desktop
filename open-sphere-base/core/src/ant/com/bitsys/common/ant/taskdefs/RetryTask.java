package com.bitsys.common.ant.taskdefs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * A task that retries some nested tasks until they succeed or the retry count is exhausted.
 * If the retry count is exhausted, either the failure property will be set to <code>true</code>,
 * if the failure property is set, or the build will be halted.
 */
public class RetryTask extends Task implements TaskContainer
{
    /** The property to set if the retry count is exhausted. */
    private String myFailureProperty;

    /** The nested tasks. */
    private final Collection<Task> myNestedTasks = new ArrayList<>();

    /** The retry count. */
    private int myRetryCount;

    /** The number of retries per failure message. */
    private int myRetryCountPerMessage;

    @Override
    public void addTask(Task task)
    {
        synchronized (myNestedTasks)
        {
            myNestedTasks.add(task);
        }
    }

    @Override
    public void execute()
    {
        super.execute();

        if (myRetryCount == 0 && myRetryCountPerMessage > 0)
        {
            myRetryCount = Integer.MAX_VALUE;
        }

        Collection<Task> tasks;
        synchronized (myNestedTasks)
        {
            tasks = new ArrayList<>(myNestedTasks);
        }

        Map<String, Integer> messageToCountMap = myRetryCountPerMessage > 0 ? new HashMap<String, Integer>() : null;

        BuildException lastException = null;
        Task task = null;
        for (int time = 0; time <= myRetryCount; ++time)
        {
            if (time > 0)
            {
                log("Retrying (" + time + (myRetryCount < Integer.MAX_VALUE ? "/" + myRetryCount : "") + ")");
            }
            try
            {
                for (Iterator<Task> iter = tasks.iterator(); iter.hasNext();)
                {
                    task = iter.next();
                    task.perform();
                }
                return;
            }
            catch (BuildException e)
            {
                lastException = e;
                if (task != null)
                {
                    log("Task '" + task.getTaskName() + "' in target '" + getOwningTarget() + "' failed with message '" + e.getMessage() + "'.", e, 2);
                }
                if (messageToCountMap != null)
                {
                    Integer count = messageToCountMap.get(e.getMessage());
                    int newCount = count == null ? 1 : count.intValue() + 1;
                    if (newCount > myRetryCountPerMessage)
                    {
                        log("Failure count for message '" + e.getMessage() + "' is " + newCount
                                + ", which is above the limit of " + myRetryCountPerMessage);
                        break;
                    }
                    messageToCountMap.put(e.getMessage(), Integer.valueOf(newCount));
                }
            }
        }
        if (myFailureProperty == null && lastException != null)
        {
            throw lastException;
        }
        else
        {
            getProject().setNewProperty(myFailureProperty, "true");
        }
    }

    /**
     * Accessor for the failureProperty.
     *
     * @return The failureProperty.
     */
    public String getFailureProperty()
    {
        return myFailureProperty;
    }

    /**
     * Accessor for the retryCount.
     *
     * @return The retryCount.
     */
    public int getRetryCount()
    {
        return myRetryCount;
    }

    /**
     * Accessor for the retryCountPerMessage.
     *
     * @return The retryCount.
     */
    public int getRetryCountPerMessage()
    {
        return myRetryCountPerMessage;
    }

    /**
     * Mutator for the failureProperty.
     *
     * @param failureProperty The failureProperty to set.
     */
    public void setFailureProperty(String failureProperty)
    {
        myFailureProperty = failureProperty;
    }

    /**
     * Mutator for the retryCount.
     *
     * @param retryCount The retryCount to set.
     */
    public void setRetryCount(int retryCount)
    {
        myRetryCount = retryCount;
    }

    /**
     * Mutator for the retryCountPerMessage.
     *
     * @param retryCount The retryCount to set.
     */
    public void setRetryCountPerMessage(int retryCount)
    {
        myRetryCountPerMessage = retryCount;
    }
}
