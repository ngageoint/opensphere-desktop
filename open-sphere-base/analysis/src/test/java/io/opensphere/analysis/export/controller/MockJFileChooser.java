package io.opensphere.analysis.export.controller;

import static org.junit.Assert.assertEquals;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * A mock {@link JFileChooser}.
 */
public class MockJFileChooser extends JFileChooser
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The expected parent to be passed in to showDialog.
     */
    private final Component myExpectedParent;

    /**
     * Indicates if the mock should simulate the user hitting cancel.
     */
    private final boolean myIsCancel;

    /**
     * The selected file to return.
     */
    private final File mySelectedFile;

    /**
     * Constructs a new {@link MockJFileChooser}.
     *
     * @param theSelectedFile The file to return in getSelectedFile call.
     * @param expectedParent The expected parent component in the showSaveDialog
     *            call.
     * @param isCancel Indicates if the mock should simulate the user hitting
     *            cancel.
     */
    public MockJFileChooser(File theSelectedFile, Component expectedParent, boolean isCancel)
    {
        mySelectedFile = theSelectedFile;
        myExpectedParent = expectedParent;
        myIsCancel = isCancel;
    }

    @Override
    public File getSelectedFile()
    {
        return mySelectedFile;
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException
    {
        assertEquals(myExpectedParent, parent);
        int option = JFileChooser.APPROVE_OPTION;
        if (myIsCancel)
        {
            option = JFileChooser.CANCEL_OPTION;
        }

        return option;
    }
}
