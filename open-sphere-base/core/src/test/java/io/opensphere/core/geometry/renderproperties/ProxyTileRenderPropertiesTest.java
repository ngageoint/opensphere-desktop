package io.opensphere.core.geometry.renderproperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

/**
 * Unit test for {@link ProxyTileRenderProperties}.
 */
public class ProxyTileRenderPropertiesTest
{
    /**
     * Tests opacitizing the color.
     */
    @Test
    public void testOpacitizeColor()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setOpacity(.9f);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(10, proxyProps.getZOrder());
        assertTrue(proxyProps.isDrawable());
        assertTrue(proxyProps.isPickable());

        assertEquals(229f, proxyProps.getOpacity(), 0f);
        proxyProps.opacitizeColor(.5f);
        assertEquals(128f, proxyProps.getOpacity(), 0f);
        assertEquals(229f, props.getOpacity(), 0f);

        support.verifyAll();
    }

    /**
     * Tests setting blending.
     */
    @Test
    public void testSetBlending()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setBlending(BlendingConfigGL.getDefaultBlending());
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(BlendingConfigGL.getDefaultBlending(), proxyProps.getBlending());
        proxyProps.setBlending(null);
        assertEquals(null, proxyProps.getBlending());
        assertEquals(BlendingConfigGL.getDefaultBlending(), props.getBlending());

        support.verifyAll();
    }

    /**
     * Tests setting color.
     */
    @Test
    public void testSetColor()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setColor(Color.RED);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(Color.RED, proxyProps.getColor());
        proxyProps.setColor(Color.BLUE);
        assertEquals(Color.BLUE, proxyProps.getColor());
        assertEquals(Color.RED, props.getColor());

        support.verifyAll();
    }

    /**
     * Tests setting color.
     */
    @Test
    public void testSetColorARGB()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setColorARGB(2);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(2, proxyProps.getColorARGB());
        proxyProps.setColorARGB(4);
        assertEquals(4, proxyProps.getColorARGB());
        assertEquals(2, props.getColorARGB());

        support.verifyAll();
    }

    /**
     * Tests setting hidden.
     */
    @Test
    public void testSetHidden()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        EasyMock.expectLastCall().times(2);

        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setHidden(true);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertTrue(proxyProps.isHidden());
        proxyProps.setHidden(false);
        assertFalse(proxyProps.isHidden());
        assertFalse(props.isHidden());

        props.setHidden(true);
        assertTrue(proxyProps.isHidden());

        support.verifyAll();
    }

    /**
     * Tests setting highlight color.
     */
    @Test
    public void testSetHighlightColor()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setHighlightColor(Color.RED);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(Color.RED, proxyProps.getHighlightColor());
        proxyProps.setHighlightColor(Color.BLUE);
        assertEquals(Color.BLUE, proxyProps.getHighlightColor());
        assertEquals(Color.RED, props.getHighlightColor());

        support.verifyAll();
    }

    /**
     * Tests setting highlight color.
     */
    @Test
    public void testSetHighlightColorARGB()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setHighlightColorARGB(2);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(2, proxyProps.getHighlightColorARGB());
        proxyProps.setHighlightColorARGB(4);
        assertEquals(4, proxyProps.getHighlightColorARGB());
        assertEquals(2, props.getHighlightColorARGB());

        support.verifyAll();
    }

    /**
     * Tests setting lighting.
     */
    @Test
    public void testSetLighting()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setLighting(LightingModelConfigGL.getDefaultLight());
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(LightingModelConfigGL.getDefaultLight(), proxyProps.getLighting());
        proxyProps.setLighting(null);
        assertEquals(null, proxyProps.getLighting());
        assertEquals(LightingModelConfigGL.getDefaultLight(), props.getLighting());

        support.verifyAll();
    }

    /**
     * Tests setting obscurant.
     */
    @Test
    public void testSetObscurant()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setObscurant(true);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertTrue(proxyProps.isObscurant());
        proxyProps.setObscurant(false);
        assertFalse(proxyProps.isObscurant());
        assertTrue(props.isObscurant());

        support.verifyAll();
    }

    /**
     * Tests setting opacity.
     */
    @Test
    public void testSetOpacity()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setOpacity(.9f);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(229f, proxyProps.getOpacity(), 0f);
        proxyProps.setOpacity(.5f);
        assertEquals(127f, proxyProps.getOpacity(), 0f);
        assertEquals(229f, props.getOpacity(), 0f);

        support.verifyAll();
    }

    /**
     * Tests setting rendering order.
     */
    @Test
    public void testSetRenderingOrder()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.setRenderingOrder(2);
        props.addListener(listener);

        support.replayAll();

        ProxyTileRenderProperties proxyProps = new ProxyTileRenderProperties(props);
        assertEquals(2, proxyProps.getRenderingOrder());
        proxyProps.setRenderingOrder(4);
        assertEquals(4, proxyProps.getRenderingOrder());
        assertEquals(2, props.getRenderingOrder());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link RenderPropertyChangeListener}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link RenderPropertyChangeListener}.
     */
    private RenderPropertyChangeListener createListener(EasyMockSupport support)
    {
        RenderPropertyChangeListener listener = support.createMock(RenderPropertyChangeListener.class);

        listener.propertyChanged(EasyMock.isA(RenderPropertyChangedEvent.class));

        return listener;
    }
}
