package io.opensphere.mantle.datasources;

/**
 * The listener interface for receiving sourceControllerChange events. The class
 * that is interested in processing a sourceControllerChange event implements
 * this interface, and the object created with that class is registered with a
 * component using the component's
 * <code>addSourceControllerChangeListener</code> method. When the
 * sourceControllerChange event occurs, that object's appropriate method is
 * invoked.
 *
 * @see SourceControllerChangeEvent
 */
@FunctionalInterface
public interface SourceControllerChangeListener
{
    /**
     * Fire source controller changed.
     *
     * @param evt the evt
     */
    void fireSourceControllerChanged(SourceControllerChangeEvent evt);
}
