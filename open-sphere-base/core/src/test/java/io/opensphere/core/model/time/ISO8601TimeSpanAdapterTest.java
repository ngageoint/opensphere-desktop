package io.opensphere.core.model.time;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.units.duration.Hours;

/** Test for {@link ISO8601TimeSpanAdapter}. */
public class ISO8601TimeSpanAdapterTest
{
    /**
     * Test marshalling and unmarshalling using {@link ISO8601TimeSpanAdapter}.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void test() throws JAXBException
    {
        TestClass test = new TestClass();
        test.setTimeSpan(TimeSpan.get(new Date(1389980100000L), Hours.ONE));

        StringWriter writer = new StringWriter();
        JAXBContext.newInstance(TestClass.class).createMarshaller().marshal(test, writer);

        Object result = JAXBContext.newInstance(TestClass.class).createUnmarshaller()
                .unmarshal(new StringReader(writer.toString()));

        Assert.assertEquals(test, result);
    }

    /** Test class. */
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class TestClass
    {
        /** The timespan. */
        @XmlElement(name = "timespan")
        @XmlJavaTypeAdapter(value = ISO8601TimeSpanAdapter.class)
        private TimeSpan myTimeSpan;

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            TestClass other = (TestClass)obj;
            return Objects.equals(myTimeSpan, other.myTimeSpan);
        }

        /**
         * Get the timespan.
         *
         * @return The timespan.
         */
        public TimeSpan getTimeSpan()
        {
            return myTimeSpan;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myTimeSpan == null ? 0 : myTimeSpan.hashCode());
            return result;
        }

        /**
         * Set the timespan.
         *
         * @param timespan The timespan.
         */
        public void setTimeSpan(TimeSpan timespan)
        {
            myTimeSpan = timespan;
        }

        @Override
        public String toString()
        {
            return myTimeSpan.toString();
        }
    }
}
