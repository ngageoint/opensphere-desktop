package io.opensphere.mantle.iconproject.view;

import java.awt.Window;

import io.opensphere.core.Toolbox;
import javafx.scene.layout.AnchorPane;

public class IconProjNewView extends AnchorPane
{

    final TopMenuBar myTopMenuBar = new TopMenuBar();

    final MainPanel myMainPanel;

    public IconProjNewView(Toolbox tb, Window owner)
    {

        myMainPanel = new MainPanel(tb, owner);
        setTopAnchor(myMainPanel, 30.);
        setBottomAnchor(myMainPanel, 0.0);
        setLeftAnchor(myMainPanel, -8.);
        setRightAnchor(myMainPanel, 0.);
        setLeftAnchor(myTopMenuBar, 0.);
        setRightAnchor(myTopMenuBar, 0.);
        getChildren().addAll(myMainPanel, myTopMenuBar);
    }

}