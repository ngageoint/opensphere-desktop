package io.opensphere.imagery;

import java.util.IdentityHashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;

import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * A worker class for creating the image overviews drives the status monitor for
 * the overview stage as it works off the queue of images.
 */
class CreateOverviewsWorker implements Runnable
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(CreateOverviewsWorker.class);

    /** The Console output ta. */
    private final JTextArea myConsoleOutputTA;

    /** The current file count. */
    private int myCurrentFileCount;

    /** The File ov gen pb. */
    private final JProgressBar myFileOVGenPB;

    /** The Gen ov note label. */
    private final JLabel myGenOvNoteLB;

    /** The Proc file note label. */
    private final JLabel myProcFileNoteLB;

    /** The Proc file pb. */
    private final JProgressBar myProcFilePB;

    /** The Progress callback. */
    private final OverviewProgressCallback myProgressCallback;

    /** The Sources. */
    private final List<ImageryFileSource> mySources;

    /** The Source to ds map. */
    private final IdentityHashMap<ImageryFileSource, Dataset> mySourceToDsMap;

    /** The total file count. */
    private int myTotalFileCount;

    /** The Wizard. */
    private final ImagerySourceWizardPanel myWizard;

    /**
     * Instantiates a new creates the overviews worker.
     *
     * @param wizard the wizard
     * @param procFilePB the proc file pb
     * @param fileOvGenPB the file ov gen pb
     * @param procFileNoteLb the proc file note lb
     * @param genOvNoteLb the gen ov note lb
     * @param consoleOutTa the console out ta
     * @param sources the sources
     * @param sourceToDsMap the source to ds map
     */
    public CreateOverviewsWorker(ImagerySourceWizardPanel wizard, JProgressBar procFilePB, JProgressBar fileOvGenPB,
            JLabel procFileNoteLb, JLabel genOvNoteLb, JTextArea consoleOutTa, List<ImageryFileSource> sources,
            IdentityHashMap<ImageryFileSource, Dataset> sourceToDsMap)
    {
        myWizard = wizard;
        myProcFilePB = procFilePB;
        myFileOVGenPB = fileOvGenPB;
        myProcFileNoteLB = procFileNoteLb;
        myGenOvNoteLB = genOvNoteLb;
        myConsoleOutputTA = consoleOutTa;
        mySources = sources;
        mySourceToDsMap = sourceToDsMap;
        myProgressCallback = new OverviewProgressCallback(myFileOVGenPB, myGenOvNoteLB, myConsoleOutputTA);
    }

    @Override
    public void run()
    {
        for (ImageryFileSource src : mySources)
        {
            if (src.isCreateOverviews() && mySourceToDsMap.get(src) != null)
            {
                myTotalFileCount++;
            }
        }

        myCurrentFileCount = 0;
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myProcFilePB.setMaximum(myTotalFileCount);
                myProcFilePB.setValue(0);
                myProcFileNoteLB.setText("Processing File 1 of " + myTotalFileCount);
                myFileOVGenPB.setMaximum(100);
                myFileOVGenPB.setValue(0);
                myConsoleOutputTA.setText(myConsoleOutputTA.getText() + "Beginning Overview Generation...\n");
            }
        });

        for (ImageryFileSource src : mySources)
        {
            if (src.isCreateOverviews() && mySourceToDsMap.get(src) != null)
            {
                final String fileName = src.getName();
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myProcFileNoteLB.setText("Processing File " + (myCurrentFileCount + 1) + " of " + myTotalFileCount);
                        myProcFilePB.setMaximum(myTotalFileCount);
                        myFileOVGenPB.setValue(0);
                        myGenOvNoteLB.setText("Generating Overview Files For: " + fileName);
                        myConsoleOutputTA.setText(myConsoleOutputTA.getText() + "Processing File: " + fileName + "\n");
                    }
                });

                // TODO: replace this place holder thread.sleep with the
                // real calls to generate the overviews. Don't forget that
                // if successful we need to set the the has overviews flag
                // on the source.

                Dataset ds = mySourceToDsMap.get(src);

                int[] levels = { 2, 4, 8, 16, 32, 64, 128 };
                try
                {
                    if (ds.BuildOverviews("NEAREST", levels, myProgressCallback) != gdalconst.CE_None)
                    {
                        EventQueueUtilities.runOnEDT(() -> myConsoleOutputTA
                                .setText(myConsoleOutputTA.getText() + "ERROR: Failed to create overview\n"));
                    }
                    else
                    {
                        src.setHasOverviews(true);
                    }
                }
                catch (RuntimeException e)
                {
                    final Exception fExc = e;
                    EventQueueUtilities.runOnEDT(() -> myConsoleOutputTA.setText(myConsoleOutputTA.getText()
                            + "ERROR: Exception while creating overview:\n" + fExc.getMessage() + "\n"));
                    LOGGER.error(e);
                }

                myCurrentFileCount++;

                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myProcFilePB.setValue(myCurrentFileCount);
                        myConsoleOutputTA.setText(myConsoleOutputTA.getText() + "Done With File: " + fileName + "\n");
                    }
                });
            }
        }

        finishStuff();
    }

    /**
     * Finish stuff.
     */
    private void finishStuff()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myFileOVGenPB.setIndeterminate(false);
                myProcFilePB.setMaximum(myTotalFileCount);
                myProcFilePB.setValue(myTotalFileCount);
                myProcFileNoteLB.setText("Completed Processing of " + myTotalFileCount + " files");
                myGenOvNoteLB.setText("Overview Generation Complete");
                myFileOVGenPB.setMaximum(1);
                myFileOVGenPB.setValue(1);
                myConsoleOutputTA.setText(myConsoleOutputTA.getText() + "Overview Generation Complete\n");
                myWizard.overViewGenerationComplete();
            }
        });
    }
}
