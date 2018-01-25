package io.opensphere.wms;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.wms.layer.WMSLayer;

/**
 * Test for {@link WMSPlugin}.
 */
public class WMSPluginTest
{
    /**
     * Test the plug-in by adding an envoy and transformer and opening the envoy
     * and getting geometries from the transformer.
     */
    @Test
    public void testPlugin()
    {
        DataRegistry dataRegistry = EasyMock.createMock(DataRegistry.class);
        final String source = FileWMSEnvoy.class.getName() + ":";
        final String category = "";
        final Date expiration = CacheDeposit.SESSION_END;
        EasyMock.reportMatcher(new IArgumentMatcher()
        {
            @Override
            public void appendTo(StringBuffer buffer)
            {
                buffer.append("matches(\"").append(CacheDeposit.class.getSimpleName()).append("(\"").append(source)
                        .append("\",\"").append(category).append("\",\"").append(expiration).append("\",\"")
                        .append(UnserializableAccessor.class.getSimpleName()).append('<').append(WMSLayer.class.getSimpleName())
                        .append(">\",(not empty)\")");
            }

            @Override
            public boolean matches(Object obj)
            {
                if (obj instanceof CacheDeposit<?>)
                {
                    CacheDeposit<?> ins = (CacheDeposit<?>)obj;
                    if (ins.getCategory().getSource().startsWith(source) && ins.getCategory().getCategory().equals(category)
                            && CollectionUtilities.hasContent(ins.getInput()) && ins.getAccessors().size() == 1)
                    {
                        PropertyAccessor<?, ?> accessor = ins.getAccessors().iterator().next();
                        if (accessor instanceof UnserializableAccessor<?, ?> && ((UnserializableAccessor<?, ?>)accessor)
                                .getPropertyDescriptor().getType().equals(WMSLayer.class))
                        {
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        dataRegistry.addModels((CacheDeposit<WMSLayer>)null);
        final List<WMSLayer> layersFromEnvoy = new ArrayList<>();
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
        {
            @Override
            public Object answer()
            {
                @SuppressWarnings("unchecked")
                CacheDeposit<? extends WMSLayer> models = (CacheDeposit<? extends WMSLayer>)EasyMock.getCurrentArguments()[0];
                for (WMSLayer layer : models.getInput())
                {
                    layer.getTypeInfo().setVisible(true, this);
                    layersFromEnvoy.add(layer);
                }
                return null;
            }
        });
        dataRegistry.addChangeListener(EasyMock.<DataRegistryListener<WMSLayer>>anyObject(),
                EasyMock.<DataModelCategory>anyObject(), EasyMock.same(WMSLayer.PROPERTY_DESCRIPTOR));
        final DataRegistryListener<?>[] listener = new DataRegistryListener<?>[1];
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
        {
            @Override
            public Object answer()
            {
                listener[0] = (DataRegistryListener<?>)EasyMock.getCurrentArguments()[0];
                return null;
            }
        });
        EasyMock.replay(dataRegistry);

        Toolbox toolbox = WMSTestToolbox.getToolbox(false);
//        AnimationManager animateMgr = EasyMock.createMock(AnimationManager.class);
//        EventManager eventMgr = EasyMock.createMock(EventManager.class);
//        MantleToolbox mantleTb = EasyMock.createMock(MantleToolbox.class);
//        PluginToolboxRegistry pluginTb = EasyMock.createMock(PluginToolboxRegistry.class);
//        EasyMock.expect(pluginTb.getPluginToolbox(MantleToolbox.class)).andReturn(mantleTb).anyTimes();
//        Toolbox toolbox = EasyMock.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).anyTimes();
//        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginTb).anyTimes();
//        EasyMock.expect(toolbox.getAnimationManager()).andReturn(animateMgr).anyTimes();
//        EasyMock.expect(toolbox.getEventManager()).andReturn(eventMgr).anyTimes();
        EasyMock.replay(toolbox);
        WMSPlugin plugin = new WMSPlugin();
        plugin.createTransformer(toolbox);
        plugin.addFileEnvoy(toolbox, new File(""));

        Collection<? extends Envoy> envoys = plugin.getEnvoys();
        assertEquals(1, envoys.size());

        Collection<? extends Transformer> transformers = plugin.getTransformers();
        assertEquals(1, transformers.size());

        AbstractEnvoy envoy = (AbstractEnvoy)envoys.iterator().next();
        assertEquals(dataRegistry, envoy.getDataRegistry());
        Transformer transformer = transformers.iterator().next();

        final Collection<Geometry> receivedGeometries = new ArrayList<>();
        GenericSubscriber<Geometry> subscriber = new GenericSubscriber<Geometry>()
        {
            @Override
            public void receiveObjects(Object unused, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
            {
                receivedGeometries.addAll(adds);
            }
        };
        transformer.addSubscriber(subscriber);
        transformer.open();

        envoy.open();

        EasyMock.verify(dataRegistry);

        DataModelCategory add = new DataModelCategory("", WMSLayer.class.getName(), "");

        @SuppressWarnings("unchecked")
        DataRegistryListener<WMSLayer> dataRegistryListener = (DataRegistryListener<WMSLayer>)listener[0];
        dataRegistryListener.valuesAdded(add, null, layersFromEnvoy, null);

        assertEquals(32, receivedGeometries.size());
        EasyMock.verify(dataRegistry);
    }
}
