package io.opensphere.mantle.iconproject.view;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class IconProjPanel extends AnchorPane
{

    public IconProjPanel()
    {

        Button test = new Button("click me");
        setBottomAnchor(test, 10.);
        getChildren().add(test);

    }

}
