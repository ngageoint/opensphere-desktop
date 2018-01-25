package io.opensphere.controlpanels.layers.activedata;

import java.util.Set;

import io.opensphere.core.util.swing.QuadStateIconButton;
import io.opensphere.core.util.swing.tree.ButtonStateUpdater;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.PlayState;
import io.opensphere.mantle.data.StreamingSupport;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * ButtonStateUpdater for streaming support.
 */
class StreamingSupportUpdater implements ButtonStateUpdater
{
    /** The pause button name. */
    public static final String PAUSE_BUTTON_NAME = "Pause layer";

    /** The play button name. */
    public static final String PLAY_BUTTON_NAME = "Play layer";

    /** The play clock button name. */
    public static final String PLAYCLOCK_BUTTON_NAME = "Play layer synchronized";

    /** The stop button name. */
    public static final String STOP_BUTTON_NAME = "Stop layer";

    /**
     * Helper to get the streaming support out of the node.
     *
     * @param node the node
     * @return the streaming support
     */
    static StreamingSupport getStreamingSupport(TreeTableTreeNode node)
    {
        StreamingSupport streamingSupport = null;
        if (node.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject userObject = (GroupByNodeUserObject)node.getPayload().getPayloadData();
            DataTypeInfo dataType = userObject.getDataTypeInfo();
            if (dataType != null)
            {
                streamingSupport = dataType.getStreamingSupport();
            }
            else if (userObject.getDataGroupInfo() != null)
            {
                Set<DataTypeInfo> streamingDataTypes = userObject.getDataGroupInfo()
                        .findMembers(StreamingSupport.IS_STREAMING_ENABLED, false, false);
                if (streamingDataTypes.size() == 1)
                {
                    streamingSupport = streamingDataTypes.iterator().next().getStreamingSupport();
                }
            }
        }
        return streamingSupport;
    }

    /**
     * Checks if the play clock button should be visible.
     *
     * @param streamingSupport The streaming support.
     * @param buttonName The button name.
     * @param playState The play state.
     * @return if the play clock button should be visible
     */
    private static boolean isPlayClockButtonVisible(StreamingSupport streamingSupport, String buttonName, PlayState playState)
    {
        return PLAYCLOCK_BUTTON_NAME.equals(buttonName) && Boolean.FALSE.equals(streamingSupport.getSynchronized())
                && PlayState.STOP.equals(streamingSupport.getPlayState().get());
    }

    /**
     * Determines if the given button should be visible based on the streaming
     * support.
     *
     * @param button the button
     * @param streamingSupport the streaming support
     * @return the visibility of the button
     */
    private static boolean isVisible(QuadStateIconButton button, StreamingSupport streamingSupport)
    {
        boolean visible = false;
        if (streamingSupport != null && streamingSupport.isStreamingEnabled())
        {
            String buttonName = button.getName();
            PlayState playState = streamingSupport.getPlayState().get();
            visible = PLAY_BUTTON_NAME.equals(buttonName) && playState != PlayState.FORWARD
                    || PAUSE_BUTTON_NAME.equals(buttonName) && playState == PlayState.FORWARD
                    || STOP_BUTTON_NAME.equals(buttonName) && playState != PlayState.STOP
                    || isPlayClockButtonVisible(streamingSupport, buttonName, playState);
        }
        return visible;
    }

    @Override
    public void update(QuadStateIconButton button, TreeTableTreeNode node)
    {
        button.setHidden(!isVisible(button, getStreamingSupport(node)));
    }
}
