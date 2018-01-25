package io.opensphere.core.modulestate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.bitsys.fade.mist.state.v4.ObjectFactory;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.util.XMLUtilities;

/** Writes version 4 state to a file. */
public class StateV4ReaderWriter
{
    /** The JAXB context classes. */
    private static final Class<?>[] CLASSES = new Class<?>[] { ObjectFactory.class,
        oasis.names.tc.ciq.xsdschema.xal._2.ObjectFactory.class };

    /**
     * Gets the classes.
     *
     * @return the classes
     */
    public static Class<?>[] getClasses()
    {
        return CLASSES.clone();
    }

    /**
     * Reads a state object from the file.
     *
     * @param file the file
     * @return the state object
     * @throws JAXBException if something bad happens
     */
    public StateType read(File file) throws JAXBException
    {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file)))
        {
            return read(inputStream);
        }
        catch (IOException e)
        {
            throw new JAXBException(e.getMessage(), e);
        }
    }

    /**
     * Reads a state object from the input stream.
     *
     * @param inputStream the input stream
     * @return the state object
     * @throws JAXBException if something bad happens
     */
    @SuppressWarnings("unchecked")
    public StateType read(InputStream inputStream) throws JAXBException
    {
        Object stateElement = XMLUtilities.readXMLObject(inputStream, StateType.class, Arrays.asList(CLASSES));
        return ((JAXBElement<StateType>)stateElement).getValue();
    }

    /**
     * Writes the state object to the file.
     *
     * @param state the state object
     * @param file the file
     * @throws JAXBException if something bad happens
     */
    public void write(StateType state, File file) throws JAXBException
    {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file)))
        {
            write(state, outputStream);
        }
        catch (IOException e)
        {
            throw new JAXBException(e.getMessage(), e);
        }
    }

    /**
     * Writes the state object to the output stream.
     *
     * @param state the state object
     * @param outputStream the output stream
     * @throws JAXBException if something bad happens
     */
    public void write(StateType state, OutputStream outputStream) throws JAXBException
    {
        JAXBElement<StateType> stateElement = new ObjectFactory().createState(state);
        XMLUtilities.writeXMLObject(stateElement, outputStream, CLASSES);
    }
}
