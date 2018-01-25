package io.opensphere.core.modulestate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.bitsys.fade.mist.state.v4.StateType;
import com.bitsys.fade.mist.state.v4.TagsType;

/** Tests for {@link StateV4ReaderWriter}. */
public class StateV4ReaderWriterTest
{
    /** Tests reading and writing. */
    @Test
    public void testReadWrite()
    {
        StateV4ReaderWriter readerWriter = new StateV4ReaderWriter();

        StateType state = newState();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try
        {
            readerWriter.write(state, outputStream);
        }
        catch (JAXBException e)
        {
            Assert.fail(e.getMessage());
        }
        byte[] bytes = outputStream.toByteArray();

        try
        {
            StateType readState = readerWriter.read(new ByteArrayInputStream(bytes));
            Assert.assertEquals(state.getTitle(), readState.getTitle());
            Assert.assertEquals(state.getDescription(), readState.getDescription());
            Assert.assertEquals(state.getSource(), readState.getSource());
            Assert.assertEquals(state.getVersion(), readState.getVersion());
            Assert.assertEquals(state.getTags().getTag(), readState.getTags().getTag());
        }
        catch (JAXBException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    /** Tests reading and writing to a file. */
//    @Test
    public void testReadWriteFile()
    {
        StateV4ReaderWriter readerWriter = new StateV4ReaderWriter();

        StateType state = newState();

        try
        {
            File file = File.createTempFile("state", ".xml");
            readerWriter.write(state, file);
            StateType readState = readerWriter.read(file);
            Assert.assertEquals(state.getTitle(), readState.getTitle());
            Assert.assertEquals(state.getDescription(), readState.getDescription());
            Assert.assertEquals(state.getSource(), readState.getSource());
            Assert.assertEquals(state.getVersion(), readState.getVersion());
            Assert.assertEquals(state.getTags().getTag(), readState.getTags().getTag());
            Assert.assertTrue(file.delete());
        }
        catch (IOException | JAXBException e)
        {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Creates a populated state object.
     *
     * @return the state object
     */
    private StateType newState()
    {
        StateType state = new StateType();
        state.setTitle("title");
        state.setDescription("description");
        state.setSource("source");
        state.setVersion("version");
        TagsType tags = new TagsType();
        tags.getTag().add("tag");
        state.setTags(tags);
        return state;
    }
}
