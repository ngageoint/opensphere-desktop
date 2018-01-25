package io.opensphere.imagery;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import org.gdal.gdal.ProgressCallback;

import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Callback class to help get the GDAL progress back into the overview stage
 * progress viewers.
 */
class OverviewProgressCallback extends ProgressCallback
{
    /** The Output area. */
    private final JTextArea myOutputArea;

    /** The Prog bar. */
    private final JProgressBar myProgBar;

    /**
     * Instantiates a new overview progress callback.
     *
     * @param progBar the prog bar
     * @param progNote the prog note
     * @param outputArea the output area
     */
    public OverviewProgressCallback(JProgressBar progBar, JLabel progNote, JTextArea outputArea)
    {
        super();
        myProgBar = progBar;
        myOutputArea = outputArea;
    }

    @Override
    public int run(double dfComplete, String message)
    {
        int retValue = super.run(dfComplete, message);
        final int fVal = (int)(100.0 * dfComplete);
        final String fMsg = message;
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myProgBar.setValue(fVal);
                if (fMsg != null)
                {
                    myOutputArea.setText(myOutputArea.getText() + fMsg + "\n");
                }
            }
        });
        return retValue;
    }
}
