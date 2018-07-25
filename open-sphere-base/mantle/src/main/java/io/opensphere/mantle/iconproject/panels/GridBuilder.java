package io.opensphere.mantle.iconproject.panels;

import java.awt.Window;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.view.IconProjBuilderNewDialog;

/** Crates the Icon Display Grid. */
public class GridBuilder extends GridPane
{
    /** The width used for icon buttons. */
    private final int myTileWidth;

    /** The icon registry used for the pane. */
    private final IconRegistry myIconRegistry;

    /** the selected icon to be used for the builder. */
    private IconRecord mySelectedIcon;

    /** The chosen icon collection. */
    private String theChosen = "";

    /**Get theChosen.
     *
     * @return theChosen the collection selected from the tree
     */
    public String getTheChosen()
    {
        return theChosen;
    }

    /** Sets theChosen.
     *
     * @param theChosen he collection selected from the tree
     */
    public void setTheChosen(String theChosen)
    {
        this.theChosen = theChosen;
    }

    /**
     * The GridBuilder constructor. sets up the rows and columns for the icon
     * grid.
     *
     * @param tileWidth the width of each tile(button).
     * @param iconRegistry the icon registry
     * @param category the category the icons belong to on the tree.
     */

    public GridBuilder(int tileWidth, IconRegistry iconRegistry)//, String category)
    {
        myTileWidth = tileWidth;
        myIconRegistry = iconRegistry;
        //theChosen = category;
        // Temporary getting to replace the ugly setStyle and make all buttons
        // use one file.
        getStyleClass().add("IconManagerStyle.css");
        setId("BoxStyle");

        //setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
        //        + "-fx-border-radius: 5;" + "-fx-border-color: purple;");
        int counter = 3000    ;
        int numcols = 4;
        for (int row = 0; row <= 10; row++)
        {
            for (int col = 0; col <= numcols; col++)
            {
                Button sample = gridButtonBuilder(counter);
                add(sample, col, row);
                counter = counter + 1;
            }
        }
        for (int col = 0; col <= numcols; col++)
        {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setFillWidth(true);
            cc.setPercentWidth(100 / numcols);
            getColumnConstraints().add(cc);
        }
        System.out.println("the chosen in construct is:   " + theChosen);
    }

    /**
     * Build buttons with Images for the grid.
     *
     * @param count the index of the icon
     * @return the built button
     */
    public Button gridButtonBuilder(int count)
    {
        Button generic = new Button();
        generic.setMinSize(myTileWidth, myTileWidth);
        generic.setMaxSize(myTileWidth, myTileWidth);
        generic.setPadding(new Insets(5, 5, 5, 5));
        String text = myIconRegistry.getIconRecordByIconId(count).getName();
        generic.setText(text);
        generic.setContentDisplay(ContentDisplay.TOP);
        generic.setAlignment(Pos.BOTTOM_CENTER);

        ImageView iconView = new ImageView(myIconRegistry.getIconRecordByIconId(count).getImageURL().toString());

        if (iconView.getImage().getWidth() > myTileWidth)
        {
            iconView.setFitHeight(myTileWidth - 25);
            iconView.setFitWidth(myTileWidth - 25);
        }

        generic.setGraphic(iconView);
        generic.setOnAction(e ->
        {
            mySelectedIcon = myIconRegistry.getIconRecordByIconId(count);
        });

        return generic;
    }

    /**
     * Shows the icon customizer.
     *
     * @param tb the toolbox.
     * @param owner the current window pane.
     */
    public void showIconCustomizer(Toolbox tb, Window owner)
    {
        IconProjBuilderNewDialog builderPane = new IconProjBuilderNewDialog(owner, myIconRegistry, mySelectedIcon);
        builderPane.setVisible(true);
    }
}