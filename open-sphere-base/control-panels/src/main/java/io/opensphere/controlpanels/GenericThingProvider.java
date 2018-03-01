package io.opensphere.controlpanels;

import io.opensphere.mantle.data.DataGroupInfo;

/**
 * An interface defining a detail panel provider. A provider is used to abstract
 * construction away from the core, so that plugins may define additional
 * providers without tightly integrating into core.
 */
public interface GenericThingProvider
{
    /**
     * Tests to determine if the provider supports the supplied data group. If
     * this method returns true for the supplied value, a call to
     * {@link #getDetailPanel(DataGroupInfo)} with the same parameter should
     * never return null.
     *
     * @param dataGroupInfo the data group to test for support in the provider
     *            implementation.
     * @return true if the provider implementation supports panel generation for
     *         the supplied data type, false otherwise.
     */
    boolean supports(DataGroupInfo dataGroupInfo);

    /**
     * Gets the detail panel implementation for the supplied data group. If the
     * provider does not support detail panels for the supplied data group, a
     * null value should be returned. This method should not return null if a
     * call to {@link #supports(DataGroupInfo)} with the same parameter returns
     * true.
     *
     * @param dataGroupInfo the data group for which to get the detail panel.
     * @return a detail panel implementation for the supplied data group.
     */
    DetailPane getDetailPanel(DataGroupInfo dataGroupInfo);
}
