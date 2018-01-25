package com.bitsys.common.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.tools.ant.Task;

/**
 * A task that gets the svn version for a file.
 */
public class SvnversionTask extends Task
{
    /** The file to check. */
    private File myFile;

    /** The output property to set. */
    private String myOutputProperty;

    @Override
    public void execute()
    {
        super.execute();

        try
        {
            Process proc;
            if (myFile == null)
            {
                proc = Runtime.getRuntime().exec("svnversion -n");
            }
            else
            {
                proc = Runtime.getRuntime().exec("svnversion -n " + myFile.getAbsolutePath());
            }

            StringBuilder sb = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            try
            {
                char[] cbuf = new char[128];
                int len;
                while ((len = reader.read(cbuf)) >= 0)
                {
                    sb.append(cbuf, 0, len);
                }
            }
            finally
            {
                reader.close();
            }

            // If there is a colon in the version, change it to a dash.
            for (int index = 0; index < sb.length(); ++index)
            {
                if (sb.charAt(index) == ':')
                {
                    sb.setCharAt(index, '-');
                }
            }

            if (myOutputProperty != null)
            {
                getProject().setProperty(myOutputProperty, sb.toString());
            }
        }
        catch (IOException e1)
        {
            log(e1.getMessage(), e1, 2);
        }
    }

    /**
     * Accessor for the file.
     *
     * @return The file.
     */
    public File getFile()
    {
        return myFile;
    }

    /**
     * Accessor for the outputProperty.
     *
     * @return The outputProperty.
     */
    public String getOutputProperty()
    {
        return myOutputProperty;
    }

    /**
     * Set the file.
     *
     * @param f The file.
     */
    public void setFile(File f)
    {
        myFile = f;
    }

    /**
     * Mutator for the outputProperty.
     *
     * @param outputProperty The outputProperty to set.
     */
    public void setOutputProperty(String outputProperty)
    {
        myOutputProperty = outputProperty;
    }
}
