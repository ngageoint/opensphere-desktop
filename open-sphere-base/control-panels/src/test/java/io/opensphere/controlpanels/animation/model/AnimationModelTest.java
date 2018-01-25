package io.opensphere.controlpanels.animation.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Unit test for {@link AnimationModel}.
 */
public class AnimationModelTest
{
    /**
     * Tests the fade property and verifies the fade and user fade or disjoint.
     */
    @Test
    public void testFade()
    {
        AnimationModel model = new AnimationModel();

        EventQueueUtilities.runOnEDTAndWait(() ->
        {
            model.getFadeUser().set(Integer.valueOf(100));
        });

        assertEquals(100, model.getFade().get().intValue());
        assertEquals(100, model.getFadeUser().get().intValue());

        model.getFade().set(Integer.valueOf(0));

        assertEquals(0, model.getFade().get().intValue());
        assertEquals(100, model.getFadeUser().get().intValue());
    }
}
