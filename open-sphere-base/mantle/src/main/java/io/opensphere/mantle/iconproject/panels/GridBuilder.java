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
import io.opensphere.mantle.iconproject.model.ViewStyle;
import io.opensphere.mantle.iconproject.view.IconCustomizerDialog;
import io.opensphere.mantle.iconproject.view.IconPopupMenu;

/** Creates the Icon Display Grid. */
public class GridBuilder extends TilePane
{
    /** The width used for icon buttons. */
    private final int myTileWidth;

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

        if (myPanelModel.getUseFilteredList())
        {
            buildIconButtons(myPanelModel.getFilteredRecordList());
        }
        else
        {
            buildIconButtons(myPanelModel.getRecordList());
        }
    }

    /**
     * Creates the image buttons to be placed in the grid.
     *
     * @param record the IconRecord
     * @return the button of the icon
     */
    private Button buttonBuilder(IconRecord record)
    {
        Button iconButton = new Button();
        iconButton.setMinSize(myTileWidth, myTileWidth);
        iconButton.setMaxSize(myTileWidth, myTileWidth);
        String text = record.getName();
        iconButton.setPadding(new Insets(5, 5, 5, 5));
        iconButton.setTooltip(new Tooltip(record.getId() + record.getImageURL().toString()));

        iconButton.setText(text);
        iconButton.setContentDisplay(ContentDisplay.TOP);
        iconButton.setAlignment(Pos.BOTTOM_CENTER);

        ImageView iconView = new ImageView(record.getImageURL().toString());
        if (iconView.getImage().getWidth() + 20 > myTileWidth)
        {
            iconView.setFitHeight(myTileWidth - 40);
            iconView.setFitWidth(myTileWidth - 40);
        }
        iconButton.setGraphic(iconView);
        iconButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent e)
            {
                if (e.getButton() == MouseButton.SECONDARY)
                {
                    iconButton.setContextMenu(new IconPopupMenu(myPanelModel));
                }
                if (myPanelModel.getViewModel().getMultiSelectEnabled())
                {
                    iconButton.setStyle("-fx-effect: dropshadow(three-pass-box, purple, 20, 0, 0, 0);");
                    myPanelModel.getSelectedIcons().put(record, iconButton);
                }
                myPanelModel.getSelectedRecord().set(record);
                myPanelModel.getSelectedIconMap().clear();
                myPanelModel.getSelectedIconMap().put(record, iconButton);
            }
        });
        return iconButton;
    }

    private Button buttonBuilderListStyle(IconRecord record)
    {
        Button iconButton = new Button();
        iconButton.setMinHeight(30);
        iconButton.setMaxHeight(30);
        iconButton.setPadding(new Insets(5, 5, 5, 5));
        iconButton.setText(record.getName());
        iconButton.setAlignment(Pos.CENTER);
        iconButton.setTooltip(new Tooltip(record.getId() + record.getImageURL().toString()));
        iconButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent e)
            {
                if (e.getButton() == MouseButton.SECONDARY)
                {
                    iconButton.setContextMenu(new IconPopupMenu(myPanelModel));
                }
                if (myPanelModel.getViewModel().getMultiSelectEnabled())
                {
                    iconButton.setStyle("-fx-effect: dropshadow(three-pass-box, purple, 20, 0, 0, 0);");
                    myPanelModel.getSelectedIcons().put(record, iconButton);
                }
                myPanelModel.getSelectedRecord().set(record);
                myPanelModel.getSelectedIconMap().clear();
                myPanelModel.getSelectedIconMap().put(record, iconButton);
            }
        });
        return iconButton;
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

    /**
     * Adds the icons in the list to the visible panel.
     *
     * @param iconRecordList the list of icon records to add
     */
    private void buildIconButtons(List<IconRecord> iconRecordList)
    {
        for (IconRecord recordindex : iconRecordList)
        {
            Button iconButton;
            if (myPanelModel.getViewStyle().get() == ViewStyle.GRID)
            {
                iconButton = buttonBuilder(recordindex);
            }
            else
            {
                iconButton = buttonBuilderListStyle(recordindex);
            }
            setMargin(iconButton, new Insets(5, 5, 5, 5));
            getChildren().add(iconButton);
        }
    }
}
