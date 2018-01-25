package io.opensphere.kml.export;

import java.util.Observable;
import java.util.Observer;

/**
 * Class that binds an {@link KMLExportOptionsView} to an
 * {@link KMLExportOptionsModel}.
 */
public class KMLExportOptionsViewBinder implements Observer
{
    /** The model. */
    private final KMLExportOptionsModel myModel;

    /** The view. */
    private final KMLExportOptionsView myView;

    /**
     * Constructs a new binder.
     *
     * @param view The view.
     * @param model The model.
     */
    public KMLExportOptionsViewBinder(KMLExportOptionsView view, KMLExportOptionsModel model)
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
        if (KMLExportOptionsModel.KML_TITLE.equals(arg))
        {
            kmlTitleToView();
        }
        else if (KMLExportOptionsModel.IS_METADATA_FIELD.equals(arg))
        {
            isMetadataFieldToView();
        }
        else if (KMLExportOptionsModel.RECORD_TITLE.equals(arg))
        {
            recordTitleToView();
        }
        else if (KMLExportOptionsModel.METADATA_FIELD.equals(arg))
        {
            metadataFieldToView();
        }
        else if (KMLExportOptionsModel.IS_DOT.equals(arg))
        {
            isDotToView();
        }
        else if (KMLExportOptionsModel.ICON_FILE.equals(arg))
        {
            iconFileToView();
        }
    }

    /**
     * Takes the values from the view and applies them to the model.
     */
    public void viewToModel()
    {
        titleTextToModel();
        recordTextToModel();
        isMetadataFieldToModel();
        metadataFieldToModel();
        iconDotToModel();
        iconFileFieldToModel();
    }

    /**
     * Subscribes to the view for changes.
     */
    private void subscribeToView()
    {
        myView.getTitleTextField().addActionListener(action -> titleTextToModel());
        myView.getRecordTextField().addActionListener(action -> recordTextToModel());
        myView.getMetadataFieldRadioButton().addActionListener(action -> isMetadataFieldToModel());
        myView.getMetadataFieldSelector().addActionListener(action -> metadataFieldToModel());
        myView.getIconDotRadioButton().addActionListener(action -> iconDotToModel());
        myView.getIconFileField().addActionListener(action -> iconFileFieldToModel());
    }

    /**
     * Takes the values from the model and applies them to the view.
     */
    private void modelToView()
    {
        kmlTitleToView();
        recordTextToView();
        isMetadataFieldToView();
        metadataFieldToView();
        iconDotToView();
        iconFileFieldToView();
    }

    /**
     * Takes the icon file field from the model and applies them to the view.
     */
    private void iconFileFieldToView()
    {
        myView.getIconFileField().setText(myModel.getIconFile());
    }

    /**
     * Takes the icon dot from the model and applies them to the view.
     */
    private void iconDotToView()
    {
        myView.getIconDotRadioButton().setSelected(myModel.isDot());
    }

    /**
     * Takes the record text from the model and applies them to the view.
     */
    private void recordTextToView()
    {
        myView.getRecordTextField().setText(myModel.getRecordText());
    }

    /**
     * Takes the kml title from the model and applies them to the view.
     */
    private void kmlTitleToView()
    {
        myView.getTitleTextField().setText(myModel.getTitleText());
    }

    /**
     * Takes the icon file from the model and applies them to the view.
     */
    private void iconFileToView()
    {
        myView.getIconFileField().setText(myModel.getIconFile());
    }

    /**
     * Takes the metadata field from the model and applies them to the view.
     */
    private void metadataFieldToView()
    {
        myView.getMetadataFieldSelector().setSelectedItem(myModel.getMetadataField());
    }

    /**
     * Takes the isDot parameter from the model and applies them to the view.
     */
    private void isDotToView()
    {
        myView.getIconDotRadioButton().setSelected(myModel.isDot());
    }

    /**
     * Takes the record title from the model and applies them to the view.
     */
    private void recordTitleToView()
    {
        myView.getRecordTextField().setText(myModel.getRecordText());
    }

    /**
     * Takes the isMetadata parameter from the model and applies them to the
     * view.
     */
    private void isMetadataFieldToView()
    {
        myView.getMetadataFieldRadioButton().setSelected(myModel.isMetadataField());
    }

    /**
     * Takes the icon file field from the view and applies them to the model.
     */
    private void iconFileFieldToModel()
    {
        myModel.setIconFile(myView.getIconFileField().getText());
    }

    /**
     * Takes the icon dot field from the view and applies them to the model.
     */
    private void iconDotToModel()
    {
        myModel.setIsDot(myView.getIconDotRadioButton().isSelected());
    }

    /**
     * Takes the metadata field from the view and applies them to the model.
     */
    private void metadataFieldToModel()
    {
        Object field = myView.getMetadataFieldSelector().getSelectedItem();
        if (field != null)
        {
            myModel.setMetadataField(field.toString());
        }
    }

    /**
     * Takes the metadata field field from the view and applies them to the
     * model.
     */
    private void isMetadataFieldToModel()
    {
        myModel.setIsMetadataField(myView.getMetadataFieldRadioButton().isSelected());
    }

    /**
     * Takes the record text from the view and applies them to the model.
     */
    private void recordTextToModel()
    {
        myModel.setRecordTitleTextField(myView.getRecordTextField().getText());
    }

    /**
     * Takes the title text from the view and applies them to the model.
     */
    private void titleTextToModel()
    {
        myModel.setKMLTitleText(myView.getTitleTextField().getText());
    }
}
