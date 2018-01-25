package com.bitsys.common.ant.taskdefs;

import java.io.File;
import java.util.Collection;
import java.util.ArrayList;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;

/**
 * Generates the resources list for run.jnlp.
 */
public class GenerateJNLPResourcesTask extends Task
{
    /** The name of the property to return. */
    private String myProperty;

    /** The paths for the run.jnlp resources. */
    private final Collection<String> myPaths = new ArrayList<>();

    @Override
    public void execute()
    {
        super.execute();

        // Build the resources string
        @SuppressWarnings("PMD.InsufficientStringBufferDeclaration")
        StringBuilder resourcesBuilder = new StringBuilder(40 * myPaths.size());
        for (String path : myPaths)
        {
            File file = new File(path);
            resourcesBuilder.append("\t\t<jar href=\"").append(file.getName()).append("\"/>\n");
        }

        // Set the property so the ant file can access the results
        getProject().setNewProperty(myProperty, resourcesBuilder.toString());
    }

    /**
     * Sets the property.
     *
     * @param property The property
     */
    public void setProperty(String property)
    {
        myProperty = property;
    }

    /**
     * Adds a configured ResourceCollection.
     *
     * @param rc The ResourceCollection
     */
    public void addConfigured(ResourceCollection rc)
    {
        if (rc instanceof Path)
        {
            Path path = (Path)rc;
            for (String pathElement : path.list())
            {
                myPaths.add(pathElement);
            }
        }
    }
}
