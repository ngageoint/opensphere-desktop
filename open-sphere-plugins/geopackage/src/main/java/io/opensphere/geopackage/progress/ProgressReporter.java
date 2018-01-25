package io.opensphere.geopackage.progress;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.ProgressModel;

/**
 * Maintains the current progress of a geopackage import/export and reports that
 * to the user.
 */
public class ProgressReporter implements Observer
{
    /**
     * The geopackage file.
     */
    private String myFileName;

    /**
     * The verb to use when reporting progress to the user.
     */
    private final String myImportExportVerb;

    /**
     * The import model the total import/export count changes in here.
     */
    private final ProgressModel myModel;

    /**
     * Used to report the progress to the user.
     */
    private final CancellableTaskActivity myTa;

    /**
     * The total number of elements to total import/export.
     */
    private float myTotalCount;

    /**
     * Constructs a new progress reporter for importing.
     *
     * @param model The model whose count gets updated as things get total
     *            import/export.
     * @param layers The layers to total import, used to calculate the
     *            percentage complete.
     * @param ta Used to report the progress to the user.
     */
    public ProgressReporter(ProgressModel model, List<GeoPackageLayer> layers, CancellableTaskActivity ta)
    {
        myModel = model;
        myTa = ta;
        for (GeoPackageLayer layer : layers)
        {
            if (StringUtils.isEmpty(myFileName))
            {
                myFileName = layer.getPackageFile();
            }
            myTotalCount += layer.getRecordCount();
        }
        myImportExportVerb = "importing ";
        myModel.addObserver(this);
    }

    /**
     * Constructs a new progress reporter for exporting.
     *
     * @param model The model whose count gets updated as things get total
     *            import/export.
     * @param fileName The name of the geopackage file we are exporting to.
     * @param totalCountToExport The total number of things we need to export.
     * @param ta Used to report the progress to the user.
     */
    public ProgressReporter(ProgressModel model, String fileName, int totalCountToExport, CancellableTaskActivity ta)
    {
        myModel = model;
        myTa = ta;
        myFileName = fileName;
        myTotalCount = totalCountToExport;
        myImportExportVerb = "exporting ";
        myModel.addObserver(this);
    }

    /**
     * Stops listening for changes to the model.
     */
    public void close()
    {
        myModel.deleteObserver(this);
        myTa.close();
    }

    /**
     * Gets the progress model used by the reporter.
     *
     * @return The progress model.
     */
    public ProgressModel getModel()
    {
        return myModel;
    }

    /**
     * Gets the task activity.
     *
     * @return the task activity
     */
    public CancellableTaskActivity getTaskActivity()
    {
        return myTa;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ProgressModel.COMPLETED_COUNT_PROP.equals(arg))
        {
            float percentageComplete = myModel.getCompletedCount() / myTotalCount;
            myTa.setProgress(percentageComplete);
            myTa.setLabelValue((int)(percentageComplete * 100) + "% complete " + myImportExportVerb + myFileName);
        }
    }
}
