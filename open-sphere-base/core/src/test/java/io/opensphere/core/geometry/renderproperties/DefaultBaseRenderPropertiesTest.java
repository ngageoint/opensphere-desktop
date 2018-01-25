package io.opensphere.core.geometry.renderproperties;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.ColorMaterialModeParameterType;

/**
 * Test for {@link DefaultBaseRenderProperties}.
 */
public class DefaultBaseRenderPropertiesTest
{
    /**
     * Test for {@link DefaultBaseRenderProperties#clone()}.
     */
    @Test
    public void testClone()
    {
        final BaseRenderProperties props = new DefaultBaseRenderProperties(0, true, true, true);
        boolean hidden = true;
        props.setHidden(hidden);
        LightingModelConfigGL.Builder builder = new LightingModelConfigGL.Builder();
        ColorMaterialModeParameterType colorMaterialMode = ColorMaterialModeParameterType.AMBIENT_AND_DIFFUSE;
        builder.setColorMaterialMode(colorMaterialMode);
        LightingModelConfigGL lighting = new LightingModelConfigGL(builder);
        props.setLighting(lighting);
        RenderPropertyChangeListener listener = EasyMock.createMock(RenderPropertyChangeListener.class);
        EasyMock.replay(listener);
        props.addListener(listener);

        BaseRenderProperties clone = props.clone();
        Assert.assertNotSame(props, clone);
        clone.setHidden(false);

        // The listener should not have been called.
        EasyMock.verify(listener);
    }
}
