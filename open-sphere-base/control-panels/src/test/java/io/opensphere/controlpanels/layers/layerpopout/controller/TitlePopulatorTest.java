package io.opensphere.controlpanels.layers.layerpopout.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.controlpanels.layers.layerpopout.model.v1.PopoutModel;

/**
 * Tests the title populator.
 *
 */
public class TitlePopulatorTest
{
    /**
     * Tests the title populator.
     */
    @Test
    public void testPopulateTitle()
    {
        String nodeLabel = "Layer (35)";
        String nodeLabel2 = "Layer (35) Four";
        String nodeLabel3 = "Layer (Trunk)";
        String nodeLabel4 = "(35) (36)";
        String nodeLabel5 = "Layer";
        String nodeLabel6 = "(36)";
        String nodeLabel7 = "Layer )";

        PopoutModel model = new PopoutModel();
        TitlePopulator titlePopulator = new TitlePopulator();

        titlePopulator.populateTitle(model, nodeLabel);
        assertEquals("Layer", model.getTitle());

        titlePopulator.populateTitle(model, nodeLabel2);
        assertEquals("Layer (35) Four", model.getTitle());

        titlePopulator.populateTitle(model, nodeLabel3);
        assertEquals("Layer (Trunk)", model.getTitle());

        titlePopulator.populateTitle(model, nodeLabel4);
        assertEquals("(35)", model.getTitle());

        titlePopulator.populateTitle(model, nodeLabel5);
        assertEquals("Layer", model.getTitle());

        titlePopulator.populateTitle(model, nodeLabel6);
        assertEquals("(36)", model.getTitle());

        titlePopulator.populateTitle(model, nodeLabel7);
        assertEquals("Layer )", model.getTitle());
    }
}
