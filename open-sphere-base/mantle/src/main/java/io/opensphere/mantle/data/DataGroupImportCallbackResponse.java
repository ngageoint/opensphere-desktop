package io.opensphere.mantle.data;

/**
 * The Interface DataGroupImportCallbackResponse.
 */
@FunctionalInterface
public interface DataGroupImportCallbackResponse
{
    /**
     * Gets the new or changed group.
     *
     * @return the new or changed group
     */
    DataGroupInfo getNewOrChangedGroup();
}
