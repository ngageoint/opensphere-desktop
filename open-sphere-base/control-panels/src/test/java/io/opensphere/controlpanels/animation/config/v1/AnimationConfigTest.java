package io.opensphere.controlpanels.animation.config.v1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.controlpanels.timeline.chart.ChartType;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Test for {@link AnimationConfig}.
 */
public class AnimationConfigTest
{
    /**
     * Test marshalling and unmarshalling.
     *
     * @throws JAXBException If there is a JAXB error.
     */
    @Test
    public void testMarshalling() throws JAXBException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        AnimationModel animationModel = new AnimationModel();
        animationModel.getActiveSpanDuration().set(Minutes.ONE);
        animationModel.getLoopSpan().set(TimeSpan.get(new Date(), Days.ONE));
        animationModel.setAdvanceDuration(Minutes.ONE);
        animationModel.getFPS().set(Float.valueOf(2f));
        animationModel.setPlayState(PlayState.FORWARD);
        animationModel.getRememberTimes().set(Boolean.TRUE);
        EventQueueUtilities.runOnEDTAndWait(() ->
        {
            animationModel.getFadeUser().set(Integer.valueOf(75));
        });
        animationModel.getFade().set(Integer.valueOf(50));
        animationModel.getChartType().set(ChartType.BAR_OVERLAPPING);
        animationModel.getHeldIntervals().add(TimeSpan.get());
        animationModel.getHeldIntervalToLayersMap().put(TimeSpan.get(), Arrays.asList("a", "b"));

        AnimationConfig input = new AnimationConfig(animationModel);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(input, outputStream);
        AnimationConfig result = XMLUtilities.readXMLObject(new ByteArrayInputStream(outputStream.toByteArray()),
                AnimationConfig.class);

        AnimationModel resultModel = result.getAnimationModel();
        Assert.assertTrue(animationModel.getActiveSpanDuration().get().compareTo(resultModel.getActiveSpanDuration().get()) == 0);
        Assert.assertEquals(animationModel.getLoopSpan().get(), resultModel.getLoopSpan().get());
        Assert.assertTrue(animationModel.getAdvanceDuration().compareTo(resultModel.getAdvanceDuration()) == 0);
        Assert.assertEquals(animationModel.getFPS().get(), resultModel.getFPS().get());
        Assert.assertEquals(PlayState.STOP, resultModel.getPlayState());
        Assert.assertEquals(animationModel.getRememberTimes().get(), resultModel.getRememberTimes().get());
        Assert.assertEquals(50, resultModel.getFade().get().intValue());
        Assert.assertEquals(75, resultModel.getFadeUser().get().intValue());
        Assert.assertEquals(animationModel.getChartType().get(), resultModel.getChartType().get());
        Assert.assertEquals(animationModel.getHeldIntervals(), resultModel.getHeldIntervals());
        Assert.assertEquals(animationModel.getHeldIntervalToLayersMap(), resultModel.getHeldIntervalToLayersMap());
    }
}
