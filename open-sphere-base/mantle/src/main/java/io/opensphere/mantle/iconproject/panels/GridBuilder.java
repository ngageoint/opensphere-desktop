package io.opensphere.mantle.iconproject.panels;

import java.awt.Window;
import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.view.IconCustomizerDialog;
import io.opensphere.mantle.iconproject.view.IconPopupMenu;

/** Crates the Icon Display Grid. */
public class GridBuilder extends TilePane
{
    /** The icon options context menu. */
    ContextMenu cMenu = new ContextMenu();

    /** The width used for icon buttons. */
    private final int myTileWidth;

    /** The icon record list. */
    List<IconRecord> myRecordList;

    /** The model for the main icon panel. */
    private final PanelModel myPanelModel;

    /**
     * The GridBuilder constructor. sets up the rows and columns for the icon
     * grid.
     *
     * @param thePanelModel the panel model
     */
    public GridBuilder(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        myTileWidth = myPanelModel.getTileWidth().get();
        myRecordList = myPanelModel.getRecordList();

        for (IconRecord recordindex : myRecordList)
        {
            Button sample = buttonBuilder(recordindex);
            setMargin(sample, new Insets(5, 5, 5, 5));
            getChildren().add(sample);
        }
//        {
//            int borderSize = 6;
//            if (myTileWidth == 0)
//            {
//                myTileWidth += 100;
//            }
//            int iconWidth = (int)(myTileWidth - borderSize);
//            int width = myGridPanel.getWidth();
//            if (width < 0 || width > 5000)
//            {
//                width = 400;
//            }
//
//            int height = myGridPanel.getHeight();
//            if (height < 0 || height > 5000)
//            {
//                height = 400;
//            }
//            int numIconRowsInView = (int)Math.ceil((double)height / (double)myTileWidth);
//            GridPane grid;
//
//                    int numIconsPerRow = myTileWidth > width ? 1 : (int)Math.floor((double)width / (double)myTileWidth);
//                    int numRows = (int)Math.ceil((double)myRecordList.size() / (double)numIconsPerRow);
//                    grid = new GridPane();
//                    numRows < numIconRowsInView ? numIconRowsInView : numRows, numIconsPerRow
//                    for (int i = 0; i < myRecordList.size(); i++)
//                    {
//                        Button sample = buttonBuilder(recordindex);
//                        grid.getChildren().add(sample);
//                        grid.add(imageBT);
//                    }
//        }
    }

    /**
     * Creates the image buttons to be placed in the grid.
     *
     * @param record the IconRecord
     * @return generic the made button
     */
    private Button buttonBuilder(IconRecord record)
    {
        Button generic = new Button();
        generic.setMinSize(myTileWidth, myTileWidth);
        generic.setMaxSize(myTileWidth, myTileWidth);
        String text = record.getName();
        generic.setPadding(new Insets(5, 5, 5, 5));
        generic.setTooltip(new Tooltip(text));

        generic.setText(text);
        generic.setContentDisplay(ContentDisplay.TOP);
        generic.setAlignment(Pos.BOTTOM_CENTER);

        ImageView iconView = new ImageView(record.getImageURL().toString());
        if (iconView.getImage().getWidth() > myTileWidth)
        {
            iconView.setFitHeight(myTileWidth - 35);
            iconView.setFitWidth(myTileWidth - 35);
        }
        generic.setGraphic(iconView);
        generic.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent e)
            {
                if (e.getButton() == MouseButton.PRIMARY)
                {
                    myPanelModel.getSelectedRecord().set(record);
                    if (myPanelModel.getViewModel().getMulti())
                    {
                        generic.setStyle("-fx-border-style: solid inside;" + "-fx-border-width: 3;" + "-fx-border-radius: 5;"
                                + "-fx-border-color: red;");
                        myPanelModel.getSelectedIcons().put(record, generic);
                    }
                }
                if (e.getButton() == MouseButton.SECONDARY)
                {
                    myPanelModel.getSelectedRecord().set(record);
                    generic.setContextMenu(new IconPopupMenu(myPanelModel));
                }
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
        IconCustomizerDialog builderPane = new IconCustomizerDialog(owner, myPanelModel);
        builderPane.setVisible(true);
    }
}
