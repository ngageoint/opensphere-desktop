package io.opensphere.wms;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.Date;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.wms.layer.WMSLayer;

/**
 * Test for {@link FileWMSEnvoy}.
 */
public class FileWMSEnvoyTest
{
    /**
     * Test for {@link FileWMSEnvoy#getImageURL(LevelRowCol)}.
     *
     * @throws MalformedURLException If the URL cannot be constructed.
     */
    @Test
    public void testGetPathToImage() throws MalformedURLException
    {
        LevelRowCol coords = new LevelRowCol(1, 5, 10);
        String path = "/path";
        String ext = ".extension";
        String pathToImage = new FileWMSEnvoy(null, path, ext).getImageURL(coords).toString();
        String expected = path + "/1/5/5_10" + ext;
        assertTrue(pathToImage + " does not end with " + expected, pathToImage.endsWith(expected));
    }

    /**
     * Test opening the envoy and verifying that the data registry is called.
     */
    @Test
    public void testOpen()
    {
        final FileWMSEnvoy[] envoy = new FileWMSEnvoy[1];
        DataRegistry dataRegistry = EasyMock.createNiceMock(DataRegistry.class);
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
            public boolean matches(Object arg0)
            {
                if (arg0 instanceof CacheDeposit<?>)
                {
                    CacheDeposit<?> ins = (CacheDeposit<?>)arg0;
                    if (ins.getCategory().getSource().equals(source) && ins.getCategory().getCategory().equals(category)
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
        EasyMock.expect(dataRegistry.addModels((CacheDeposit<WMSLayer>)null)).andReturn(new long[0]);
        EasyMock.replay(dataRegistry);
//        Toolbox toolbox = EasyMock.createMock(Toolbox.class);
        Toolbox toolbox = WMSTestToolbox.getToolbox(false);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);
        EasyMock.replay(toolbox);
        envoy[0] = new FileWMSEnvoy(toolbox, "", "");
        envoy[0].open(null);
        EasyMock.verify(dataRegistry);
    }
}
