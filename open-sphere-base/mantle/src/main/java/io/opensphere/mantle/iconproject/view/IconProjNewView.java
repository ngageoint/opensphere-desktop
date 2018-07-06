package io.opensphere.mantle.iconproject.view;

import javafx.scene.layout.AnchorPane;

public class IconProjNewView extends AnchorPane
{

    final TopMenuBar myTopMenuBar = new TopMenuBar();

    final MainPanel myMainPanel = new MainPanel();

    public IconProjNewView()
    {
        setTopAnchor(myMainPanel, 45.);
        setBottomAnchor(myMainPanel, 30.0);
        setLeftAnchor(myMainPanel, 10.);
        setRightAnchor(myMainPanel, 10.);
        getChildren().addAll(myMainPanel, myTopMenuBar);
        setLeftAnchor(myTopMenuBar, 5.);
        setRightAnchor(myTopMenuBar, 5.);
    }

}