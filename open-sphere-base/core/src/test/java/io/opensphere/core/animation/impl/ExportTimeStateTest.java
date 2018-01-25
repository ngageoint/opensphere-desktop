package io.opensphere.core.animation.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.collections.New;

/** Test for {@link ExportTimeState}. */
public class ExportTimeStateTest
{
    /**
     * Test for marshalling/unmarshalling {@link ExportTimeState}.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void test() throws JAXBException
    {
        ExportTimeState test = new ExportTimeState();
        test.setAdvanceDuration(new Seconds(2));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        List<TimeSpan> sequence = New.list();
        sequence.add(TimeSpan.get(cal.getTime(), Hours.ONE));
        cal.add(Calendar.HOUR_OF_DAY, 1);
        sequence.add(TimeSpan.get(cal.getTime(), Hours.ONE));
        cal.add(Calendar.HOUR_OF_DAY, 1);
        sequence.add(TimeSpan.get(cal.getTime(), Hours.ONE));
        test.setSequence(sequence);

        test.setCurrent(sequence.get(1));

        StringWriter writer = new StringWriter();
        JAXBContext.newInstance(ExportTimeState.class).createMarshaller().marshal(test, writer);

        Object result = JAXBContext.newInstance(ExportTimeState.class).createUnmarshaller()
                .unmarshal(new StringReader(writer.toString()));

        Assert.assertEquals(test.getCurrent(), ((ExportTimeState)result).getCurrent());
        Assert.assertEquals(test.getAdvanceDuration(), ((ExportTimeState)result).getAdvanceDuration());
        Assert.assertEquals(test.getSequence(), ((ExportTimeState)result).getSequence());
    }
}
