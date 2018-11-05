package io.opensphere.mantle.icons.view;

import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 *
 */
public class IconDetailPanel extends FlowPane
{
    private final Pane myPreviewPane;

    private final ImageView myIconPreview;

    private final Button myModifyButton;

    private final Button myCreateButton;

    private final Pane myDetailPane;

    private final Label myNameField;

    private final Label mySourceField;

    private final Label myTagField;

    private final Button myCancelButton;

    private final Button mySaveButton;

    /**
     *
     */
    public IconDetailPanel()
    {
        super(Orientation.VERTICAL);

        myIconPreview = new ImageView();
        myModifyButton = new Button("Modify");
        myCreateButton = new Button("Create");

        HBox buttonBox = new HBox(myModifyButton, myCreateButton);

        myPreviewPane = new VBox(myIconPreview, buttonBox);

        myDetailPane = new VBox();

        myNameField = new Label();
        mySourceField = new Label();
        myTagField = new Label();

        myDetailPane.getChildren().add(new HBox(new Label("Name:"), myNameField));
        myDetailPane.getChildren().add(new HBox(new Label("Source:"), mySourceField));
        myDetailPane.getChildren().add(new HBox(new Label("Tags:"), myTagField));

        myCancelButton = new Button("Cancel");
        mySaveButton = new Button("Save");

        HBox dialogControlBox = new HBox(myCancelButton, mySaveButton);

        getChildren().add(myPreviewPane);
        getChildren().add(myDetailPane);
        getChildren().add(dialogControlBox);
    }
}
