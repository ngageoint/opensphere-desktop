package io.opensphere.mantle.iconproject.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import io.opensphere.mantle.icon.IconRegistry;

/** Crates the Icon Display Grid */
public class GridBuilder extends GridPane
{
    /** the width used for icon buttons. */
    private final int myTileWidth;

    /** the icon registry used for the pane */
    private final IconRegistry myIconRegistry;

    // /** the record of icons to be used */
    // private IconRecord myIconRecord;

    public GridBuilder(int TileWidth, IconRegistry iconRegistry)
    {
        myTileWidth = TileWidth;
        myIconRegistry = iconRegistry;
        System.out.println(myIconRegistry.getSubCategoiresForCollection("*************User Added"));

        setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
                + "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: purple;");
        int counter = 1;
        for (int row = 0; row <= 50; row++)
        {
            RowConstraints rc = new RowConstraints();
            rc.setFillHeight(true);
            getRowConstraints().add(rc);
            for (int col = 0; col <= 4; col++)
            {
                ColumnConstraints cc = new ColumnConstraints();
                //   cc.setFillWidth(true);
                cc.setPercentWidth(100/5);
                getColumnConstraints().add(cc);

                Button sample = GridButtonBuilder(counter);
                add(sample,col,row);
                counter = counter + 1;
            }
        }

        /*
        int numR = 5;
        int numC = 4;
        for (int row = 0; row <= numR; row++)
        {
            RowConstraints rc = new RowConstraints();
            rc.setFillHeight(true);
            getRowConstraints().add(rc);
        }
        for (int col = 0; col <= numC; col++)
        {
            ColumnConstraints cc = new ColumnConstraints();
         //   cc.setFillWidth(true);
            cc.setPercentWidth(100/5);
            getColumnConstraints().add(cc);
        }
        int topIndex = numR * numC;
        for (int i = 0; i <= 9; i++) {
            Button sample = GridButtonBuilder(i + 1);
            add(sample, i % numC, i / numC);
        }
         */
    }

    public Button GridButtonBuilder(int count)
    {
        Button generic = new Button();
        generic.setMinSize(myTileWidth, myTileWidth);
        generic.setMaxSize(myTileWidth, myTileWidth);
        generic.setText(myIconRegistry.getIconRecordByIconId(count).getName());
        generic.setContentDisplay(ContentDisplay.TOP);
        generic.setAlignment(Pos.BOTTOM_CENTER);
        ImageView iconView = new ImageView(myIconRegistry.getIconRecordByIconId(count).getImageURL().toString());

        if (iconView.getImage().getWidth() > myTileWidth) {
            iconView.setFitHeight(myTileWidth - 10);
            iconView.setFitWidth(myTileWidth - 10);
        }

        generic.setGraphic(iconView);
        return generic;
    }

}