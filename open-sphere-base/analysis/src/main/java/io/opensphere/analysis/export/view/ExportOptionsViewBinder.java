package io.opensphere.analysis.export.view;

import java.util.Observable;
import java.util.Observer;

import io.opensphere.analysis.export.model.ColorFormat;
import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.export.model.LatLonFormat;
import io.opensphere.core.util.swing.ListComboBoxModel;

/**
 * Class that binds an {@link ExportOptionsView} to an
 * {@link ExportOptionsModel}.
 */
public class ExportOptionsViewBinder implements Observer
{
    /**
     * The model.
     */
    private final ExportOptionsModel myModel;

    /**
     * The view.
     */
    private final ExportOptionsView myView;

    /**
     * Constructs a new binder.
     *
     * @param view The view.
     * @param model The model.
     */
    public ExportOptionsViewBinder(ExportOptionsView view, ExportOptionsModel model)
    {
        myView = view;
        myModel = model;
        myModel.addObserver(this);
        subscribeToView();
        modelToView();
    }

    /**
     * Removes from listening to the model.
     */
    public void close()
    {
        myModel.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ExportOptionsModel.ADD_WKT_PROP.equals(arg))
        {
            wktModelToView();
        }
        else if (ExportOptionsModel.INCLUDE_META_COLUMNS_PROP.equals(arg))
        {
            includeMetaColumnsModelToView();
        }
        else if (ExportOptionsModel.SELECTED_COLOR_FORMAT_PROP.equals(arg))
        {
            selectedColorFormatModelToView();
        }
        else if (ExportOptionsModel.SELECTED_LAT_LON_FORMAT_PROP.equals(arg))
        {
            selectedLatLonModelToView();
        }
        else if (ExportOptionsModel.SELECTED_ROWS_ONLY_PROP.equals(arg))
        {
            selectedRowsModelToView();
        }
        else if (ExportOptionsModel.SEPERATE_DATE_TIME_COLUMNS_PROP.equals(arg))
        {
            separateDateTimeModelToView();
        }
    }

    /**
     * Applies the value of includeMetaColumns to the view.
     */
    private void includeMetaColumnsModelToView()
    {
        myView.getIncludeMetaColumns().setSelected(myModel.isIncludeMetaColumns());
        myView.getColorFormat().setVisible(myModel.isIncludeMetaColumns());
        myView.getColorFormatLabel().setVisible(myModel.isIncludeMetaColumns());
    }

    /**
     * Applies the includeMetaColumns value to the model.
     */
    private void includeMetaColumnsViewToModel()
    {
        myModel.setIncludeMetaColumns(myView.getIncludeMetaColumns().isSelected());
    }

    /**
     * Takes the values from the model and applies them to the view.
     */
    private void modelToView()
    {
        myView.getColorFormat().setModel(new ListComboBoxModel<>(ColorFormat.values()));
        myView.getLatLonFormat().setModel(new ListComboBoxModel<>(LatLonFormat.values()));
        includeMetaColumnsModelToView();
        selectedColorFormatModelToView();
        selectedLatLonModelToView();
        selectedRowsModelToView();
        separateDateTimeModelToView();
        wktModelToView();
    }

    /**
     * Applies the value of selected color format to the view.
     */
    private void selectedColorFormatModelToView()
    {
        myView.getColorFormat().setSelectedItem(myModel.getSelectedColorFormat());
    }

    /**
     * Applies the selected color format value to the model.
     */
    private void selectedColorFormatViewToModel()
    {
        myModel.setSelectedColorFormat((ColorFormat)myView.getColorFormat().getSelectedItem());
    }

    /**
     * Applies the value of selected lat lon format to the view.
     */
    private void selectedLatLonModelToView()
    {
        myView.getLatLonFormat().setSelectedItem(myModel.getSelectedLatLonFormat());
    }

    /**
     * Applies the value of selected lat lon format to the model.
     */
    private void selectedLatLonViewToModel()
    {
        myModel.setSelectedLatLonFormat((LatLonFormat)myView.getLatLonFormat().getSelectedItem());
    }

    /**
     * Applies the value of include selected rows only to the view.
     */
    private void selectedRowsModelToView()
    {
        myView.getSelectedRowsOnly().setSelected(myModel.isSelectedRowsOnly());
    }

    /**
     * Applies the include selected rows only value to the model.
     */
    private void selectedRowsViewToModel()
    {
        myModel.setSelectedRowsOnly(myView.getSelectedRowsOnly().isSelected());
    }

    /**
     * Applies the value of separate date time to the view.
     */
    private void separateDateTimeModelToView()
    {
        myView.getSeparateDateTimeColumns().setSelected(myModel.isSeparateDateTimeColumns());
    }

    /**
     * Applies the separate date time value to the model.
     */
    private void separateDateTimeViewToModel()
    {
        myModel.setSeparateDateTimeColumns(myView.getSeparateDateTimeColumns().isSelected());
    }

    /**
     * Subscribes to the view for changes.
     */
    private void subscribeToView()
    {
        myView.getAddWKT().addChangeListener((action) -> wktViewToModel());
        myView.getColorFormat().addActionListener((action) -> selectedColorFormatViewToModel());
        myView.getIncludeMetaColumns().addChangeListener((action) -> includeMetaColumnsViewToModel());
        myView.getLatLonFormat().addActionListener((action) -> selectedLatLonViewToModel());
        myView.getSelectedRowsOnly().addChangeListener((action) -> selectedRowsViewToModel());
        myView.getSeparateDateTimeColumns().addChangeListener((action) -> separateDateTimeViewToModel());
    }

    /**
     * Applies the value of add wkt column to the view.
     */
    private void wktModelToView()
    {
        myView.getAddWKT().setSelected(myModel.isAddWkt());
    }

    /**
     * Applies the add wkt column value to the model.
     */
    private void wktViewToModel()
    {
        myModel.setAddWkt(myView.getAddWKT().isSelected());
    }
}
