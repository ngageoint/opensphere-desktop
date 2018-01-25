package io.opensphere.build.maven.install.descriptor;

import org.apache.maven.shared.model.fileset.FileSet;

/**
 * An extension to the generic {@link FileSet} in which a target directory is
 * specified.
 */
public class DirectedFileSet extends FileSet
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = 8098491745726650126L;

    /**
     * The target directory into which the included files will be placed.
     * Specified as a relative path.
     */
    private String target;

    /**
     * Gets the value of the {@link #target} field.
     *
     * @return the value stored in the {@link #target} field.
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * Sets the value of the {@link #target} field.
     *
     * @param target
     *            the value to store in the {@link #target} field.
     */
    public void setTarget(String target)
    {
        this.target = target;
    }
}
