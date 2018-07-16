package io.opensphere.mantle.iconproject.view;

import java.awt.Window;

import io.opensphere.core.Toolbox;
import javafx.scene.layout.AnchorPane;

/** Packages UI elements into one pane. */
public class IconProjNewView extends AnchorPane
{
    /** The top bar consisting of view,sizing, and filter. */
    final TopMenuBar myTopMenuBar = new TopMenuBar();

    /** Panel comprised of Tree and icon display. */
    final MainPanel myMainPanel;

    /**
     * Creates subpannels for UI.
     *
     * @param tb the toolbox used for registry.
     */
    
    public IconProjNewView(Toolbox tb, Window owner)
    {
        myMainPanel = new MainPanel(tb,owner);
        setTopAnchor(myMainPanel, 30.);
        setBottomAnchor(myMainPanel, 0.0);
        setLeftAnchor(myMainPanel, -8.);
        setRightAnchor(myMainPanel, 0.);
        setLeftAnchor(myTopMenuBar, 0.);
        setRightAnchor(myTopMenuBar, 0.);
        getChildren().addAll(myMainPanel, myTopMenuBar);
    }
}
