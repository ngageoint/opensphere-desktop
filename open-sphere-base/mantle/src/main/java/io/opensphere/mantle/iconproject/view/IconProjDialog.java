package io.opensphere.mantle.iconproject.view;

import java.awt.Window;

import io.opensphere.core.util.fx.JFXDialog;

public class IconProjDialog extends JFXDialog
{

    public IconProjDialog(Window owner)
    {
        super(owner, "the title");
        setSize(1021, 520);
        setFxNode(new IconProjPanel());
    }
    
    /* public FinalStage(owner,"Test Case") { StackPane test = new StackPane();
     * test.getChildren().add(new Label("Hello World"));
     * 
     * Scene theScene = new Scene(test); setScene(theScene); }
     * 
     * 
     * public FinalStage(String string) { StackPane test = new StackPane();
     * test.getChildren().add(new Label(string)); Scene theScene = new
     * Scene(test); setScene(theScene); setTitle("rawr"); setSize(500.,500.); }
     * 
     * private void setSize(double d, double e) { setMinWidth(d);
     * setMinHeight(e); } */
}
