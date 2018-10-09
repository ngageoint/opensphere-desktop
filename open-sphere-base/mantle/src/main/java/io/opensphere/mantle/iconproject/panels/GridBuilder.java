package io.opensphere.mantle.iconproject.panels;

import java.awt.Window;
import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.view.IconCustomizerDialog;
import io.opensphere.mantle.iconproject.view.IconPopupMenu;

/** Creates the Icon Display Grid. */
public class GridBuilder extends TilePane
{
    /** The width used for icon buttons. */
    private final int myTileWidth;

    /** The icon record list. */
    private List<IconRecord> myRecordList;

    /** The model for the main icon panel. */
    private final PanelModel myPanelModel;

    /**
     * The GridBuilder constructor. Sets up the rows and columns for the icon
     * grid.
     *
     * @param panelModel the panel model
     */
    public GridBuilder(PanelModel panelModel)
    {
        myPanelModel = panelModel;
        myTileWidth = myPanelModel.getCurrentTileWidth().get();
        myRecordList = myPanelModel.getRecordList();

        for (IconRecord recordindex : myRecordList)
        {
            Button sample = buttonBuilder(recordindex);
            setMargin(sample, new Insets(5, 5, 5, 5));
            getChildren().add(sample);
        }
    }

    /**
     * Creates the image buttons to be placed in the grid.
     *
     * @param record the IconRecord
     * @return the made button
     */
    private Button buttonBuilder(IconRecord record)
    {
        Button generic = new Button();
        generic.setMinSize(myTileWidth, myTileWidth);
        generic.setMaxSize(myTileWidth, myTileWidth);
        String text = record.getName();
        generic.setPadding(new Insets(5, 5, 5, 5));
        generic.setTooltip(new Tooltip(record.getId() + record.getImageURL().toString()));

        generic.setText(text);
        generic.setContentDisplay(ContentDisplay.TOP);
        generic.setAlignment(Pos.BOTTOM_CENTER);

        ImageView iconView = new ImageView(record.getImageURL().toString());
        if (iconView.getImage().getWidth() + 20 > myTileWidth)
        {
            iconView.setFitHeight(myTileWidth - 40);
            iconView.setFitWidth(myTileWidth - 40);
        }
        generic.setGraphic(iconView);
        generic.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent e)
            {
                if (e.getButton() == MouseButton.SECONDARY)
                {
                    generic.setContextMenu(new IconPopupMenu(myPanelModel));
                }
                if (myPanelModel.getViewModel().getMultiSelectEnabled())
                {
                    generic.setStyle("-fx-effect: dropshadow(three-pass-box, purple, 20, 0, 0, 0);");
                    myPanelModel.getSelectedIcons().put(record, generic);
                }
                myPanelModel.getSelectedRecord().set(record);
                myPanelModel.getSelectedIconMap().clear();
                myPanelModel.getSelectedIconMap().put(record, generic);
            }
        });
        return generic;
    }

    /**
     * Shows the icon customizer.
     *
     * @param owner the current window pane.
     */
    public void showIconCustomizer(Window owner)
    {
    	if (myPanelModel.getSelectedRecord().get() != null)
    	{
    	    IconCustomizerDialog builderPane = new IconCustomizerDialog(owner, myPanelModel);
            builderPane.setVisible(true);
    	}
    }
}
