package io.opensphere.kml.envoy;

import java.io.PrintWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/** Listener for XML transform exceptions. */
public class TransformErrorListener implements ErrorListener
{
    /** The output printer. */
    private final PrintWriter fOut;
    
    /**
     * Constructs an error handler that prints error messages to
     * <code>System.err</code>.
     */
    public TransformErrorListener() {
        this(new PrintWriter(System.err));
    }

    /**
     * Constructs an error handler that prints error messages to the
     * specified <code>PrintWriter</code.
     * 
     * @param out the PrintWriter
     */
    public TransformErrorListener(PrintWriter out) {
        fOut = out;
    }
    
    @Override
    public void warning(TransformerException exception) throws TransformerException
    {
        printError("Warning", exception);
    }

    @Override
    public void error(TransformerException exception) throws TransformerException
    {
        printError("Error", exception);
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException
    {
        printError("Fatal Error", exception);
    }
    
    /**
     * Prints an exception to {@link #fOut}.
     * 
     * @param type the exception type
     * @param ex the exception to print
     */
    private void printError(String type, TransformerException ex) {

        fOut.print("[");
        fOut.print(type);
        fOut.print("] ");
        fOut.print(ex.getMessageAndLocation());
        fOut.println();
        fOut.flush();

    }

}
