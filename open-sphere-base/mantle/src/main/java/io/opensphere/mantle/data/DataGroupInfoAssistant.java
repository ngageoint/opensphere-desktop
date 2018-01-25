package io.opensphere.mantle.data;

import java.awt.Component;
import java.awt.Dimension;

/**
 * The Interface DataGroupInfoAssistant.
 *
 * The assistant provides a mechanism to make requests to the group
 * builder/provider to obtain certain information about what the provider can do
 * and to initiate the provider to do certain tasks. It also offers the provider
 * the opportunity to provide user interface components to change some of the
 * settings for the group, and to provide a debug user interface for the group.
 */
public interface DataGroupInfoAssistant
{
    /**
     * True if the assistant can delete the group.
     *
     * @param dgi the {@link DataGroupInfo} to be deleted.
     * @return true, if the assistant can delete the group.
     */
    boolean canDeleteGroup(DataGroupInfo dgi);

    /**
     * True if the assistant can initiate a re-import of the group.
     *
     * @param dgi the {@link DataGroupInfo} to be re-imported.
     * @return true, if the assistant can initiate a re-import of the group.
     */
    boolean canReImport(DataGroupInfo dgi);

    /**
     * Closes the assistant.
     */
    void close();

    /**
     * Requests the assistant to delete the group.
     *
     * @param dgi the {@link DataGroupInfo} to delete.
     * @param source the source of the delete request.
     */
    void deleteGroup(DataGroupInfo dgi, Object source);

    /**
     * Gets the debug user interface component.
     *
     * @param preferredSize the preferred size of the UI component.
     * @param dgi the {@link DataGroupInfo}
     * @return the debug component
     */
    Component getDebugUIComponent(Dimension preferredSize, DataGroupInfo dgi);

    /**
     * Gets the layer control user interface component.
     *
     * @param preferredSize the preferred size of the UI component.
     * @param dataGroup the {@link DataGroupInfo}
     * @param dataType the {@link DataTypeInfo}
     * @return the layer control component
     */
    Component getLayerControlUIComponent(Dimension preferredSize, DataGroupInfo dataGroup, DataTypeInfo dataType);

    /**
     * Gets the settings UI preferred size.
     *
     * @return The preferred size of the settings UI, or null if it does not
     *         matter.
     */
    Dimension getSettingsPreferredSize();

    /**
     * Gets the settings user interface component.
     *
     * @param preferredSize the preferred size of the UI component.
     * @param dataGroup the {@link DataGroupInfo}
     * @return the settings component
     */
    Component getSettingsUIComponent(Dimension preferredSize, DataGroupInfo dataGroup);

    /**
     * Request the assistant to re-import/setup the group.
     *
     * @param dgi the {@link DataGroupInfo} to be re-imported.
     * @param source the source of the re-import request.
     */
    void reImport(DataGroupInfo dgi, Object source);
}
