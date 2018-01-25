package io.opensphere.kml.datasource.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;

import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.datasource.view.KMLDataSourcePanel;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant;

/**
 * KML Data Group Info Assistant.
 */
public class KMLDataGroupInfoAssistant extends DefaultDataGroupInfoAssistant
{
    /** The data source controller. */
    private final KMLDataSourceControllerImpl myController;

    /**
     * Constructor.
     *
     * @param controller The data source controller
     */
    public KMLDataGroupInfoAssistant(KMLDataSourceControllerImpl controller)
    {
        myController = controller;
    }

    @Override
    public boolean canDeleteGroup(DataGroupInfo dgi)
    {
        return true;
    }

    @Override
    public boolean canReImport(DataGroupInfo dgi)
    {
        return true;
    }

    @Override
    public void deleteGroup(DataGroupInfo dgi, Object source)
    {
        KMLDataSource dataSource = myController.getDataSource(dgi);
        if (dataSource != null)
        {
            myController.removeSource(dataSource);
        }
    }

    @Override
    public Component getSettingsUIComponent(Dimension preferredSize, DataGroupInfo dataGroup)
    {
        final KMLDataSource dataSource = myController.getDataSource(dataGroup);
        if (dataSource != null)
        {
            KMLDataSourcePanel panel = new KMLDataSourcePanel(dataSource, false, Collections.<String>emptyList());
            panel.getDataSourceModel().addListener((observable, oldValue, newValue) -> myController.updateSource(dataSource));
            return panel;
        }
        return null;
    }

    @Override
    public void reImport(DataGroupInfo dgi, Object source)
    {
        KMLDataSource dataSource = myController.getDataSource(dgi);
        if (dataSource != null)
        {
            myController.getImporter().importDataSource(dataSource, true, null, null, null);
        }
    }
}
