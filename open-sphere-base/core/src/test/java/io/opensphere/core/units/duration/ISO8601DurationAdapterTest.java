package io.opensphere.core.units.duration;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
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

/** Test for {@link ISO8601DurationAdapter}. */
public class ISO8601DurationAdapterTest
{
    /**
     * Test marshalling and unmarshalling using {@link ISO8601DurationAdapter}.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void test() throws JAXBException
    {
        TestClass test = new TestClass();
        test.setDuration(new Months(new BigDecimal("2.5")));

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
        /** The duration. */
        @XmlElement(name = "duration")
        @XmlJavaTypeAdapter(value = ISO8601DurationAdapter.class)
        private Duration myDuration;

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
            return Objects.equals(myDuration, other.myDuration);
        }

        /**
         * Get the duration.
         *
         * @return The duration.
         */
        public Duration getDuration()
        {
            return myDuration;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myDuration == null ? 0 : myDuration.hashCode());
            return result;
        }

        /**
         * Set the duration.
         *
         * @param duration The duration.
         */
        public void setDuration(Duration duration)
        {
            myDuration = duration;
        }
    }
}
