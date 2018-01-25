package io.opensphere.core.animation.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.animation.impl.ExportAnimationState.LoopBehavior;
import io.opensphere.core.animation.impl.ExportAnimationState.PlayState;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.util.MathUtil;

/** Test for {@link ExportAnimationState}. */
public class ExportAnimationStateTest
{
    /**
     * Test for marshalling/unmarshalling {@link ExportTimeState}.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void test() throws JAXBException
    {
        ExportAnimationState test = new ExportAnimationState();
        test.setLoopBehavior(LoopBehavior.TAPER_END_SNAP_START);
        test.setLoopInterval(TimeSpan.get(new Date(1389980100000L), Hours.ONE));
        test.setMillisPerFrame(20);
        test.setPlayState(PlayState.FORWARD);

        StringWriter writer = new StringWriter();
        JAXBContext.newInstance(ExportAnimationState.class).createMarshaller().marshal(test, writer);

        Object result = JAXBContext.newInstance(ExportAnimationState.class).createUnmarshaller()
                .unmarshal(new StringReader(writer.toString()));

        Assert.assertEquals(test.getLoopBehavior(), ((ExportAnimationState)result).getLoopBehavior());
        Assert.assertEquals(test.getLoopInterval(), ((ExportAnimationState)result).getLoopInterval());
        Assert.assertTrue(MathUtil.isZero(test.getMillisPerFrame() - ((ExportAnimationState)result).getMillisPerFrame()));
        Assert.assertEquals(test.getPlayState(), ((ExportAnimationState)result).getPlayState());
    }
}
