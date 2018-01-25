package io.opensphere.mantle.data.impl;

import java.util.Collection;
import java.util.stream.Stream;

import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Helper class that wraps data group activation to provide a consistent message
 * reporting mechanism to the user.
 */
public interface DataGroupActivator
{
    /**
     * Deactivates and reactivates a group and reports errors via
     * {@link UserMessageEvent}s.
     *
     * @param group The group.
     * @return {@code true} if the process completed successfully.
     * @throws InterruptedException If the thread was interrupted.
     */
    boolean reactivateGroup(DataGroupInfo group) throws InterruptedException;

    /**
     * Deactivates and reactivates the groups and reports errors via
     * {@link UserMessageEvent}s. This will fork for each group, but block until
     * the process is complete.
     *
     * @param groups The groups.
     * @return {@code true} if the process completed successfully.
     * @throws InterruptedException If the thread was interrupted.
     */
    boolean reactivateGroups(Collection<DataGroupInfo> groups) throws InterruptedException;

    /**
     * Sets the group active (or not) and reports errors via
     * {@link UserMessageEvent}s. This will block until the process is complete.
     *
     * @param property The property.
     * @param active {@code true} if the group should become active,
     *            {@code false} if the group should become inactive.
     * @return {@code true} if the process completed successfully.
     * @throws InterruptedException If the thread was interrupted.
     */
    boolean setGroupActive(DataGroupActivationProperty property, boolean active) throws InterruptedException;

    /**
     * Sets the group active (or not) and reports errors via
     * {@link UserMessageEvent}s. This will block until the process is complete.
     *
     * @param group The group.
     * @param active {@code true} if the group should become active,
     * @return {@code true} if the process completed successfully.
     *         {@link UserMessageEvent}s. This will block until the process is
     *         complete. {@code false} if the group should become inactive.
     * @throws InterruptedException If the thread was interrupted.
     */
    boolean setGroupActive(DataGroupInfo group, boolean active) throws InterruptedException;

    /**
     * Sets the groups active (or not) and reports errors via
     * {@link UserMessageEvent}s. This will fork for each group, but block until
     * the process is complete.
     *
     * @param groups The groups.
     * @param active {@code true} if the groups should become active,
     *            {@code false} if the groups should become inactive.
     * @return {@code true} if the process completed successfully.
     * @throws InterruptedException If the thread was interrupted.
     */
    boolean setGroupsActive(Collection<DataGroupInfo> groups, boolean active) throws InterruptedException;

    /**
     * Sets the groups active (or not) and reports errors via
     * {@link UserMessageEvent}s. If the stream is parallel, this will fork for
     * each group, but block until the process is complete.
     *
     * @param groupStream The stream of groups.
     * @param active {@code true} if the groups should become active,
     *            {@code false} if the groups should become inactive.
     * @return {@code true} if the process completed successfully.
     * @throws InterruptedException If the thread was interrupted.
     */
    boolean setGroupsActive(Stream<DataGroupInfo> groupStream, boolean active) throws InterruptedException;
}
