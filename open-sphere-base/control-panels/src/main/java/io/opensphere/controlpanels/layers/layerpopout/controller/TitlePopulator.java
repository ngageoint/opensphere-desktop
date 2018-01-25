package io.opensphere.controlpanels.layers.layerpopout.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.opensphere.controlpanels.layers.layerpopout.model.v1.PopoutModel;

/**
 * Populates the popout window title derived from the tree node being popped
 * out.
 */
public class TitlePopulator
{
    /**
     * Populates the title based on the node label.
     *
     * @param model The model to set the title for.
     * @param nodeLabel The node label to derive the title from.
     */
    public void populateTitle(PopoutModel model, String nodeLabel)
    {
        Matcher matcher = Pattern.compile("(.+?)\\s*\\(\\d+\\)$").matcher(nodeLabel);
        model.setTitle(matcher.matches() ? matcher.group(1) : nodeLabel);
    }
}
