package io.opensphere.mantle.iconproject.model;

import io.opensphere.mantle.iconproject.panels.MainPanel;
import io.opensphere.mantle.iconproject.panels.TopMenuBar;
import javafx.scene.control.ScrollPane;

public class ViewModel
{

    
    private TopMenuBar myTopMenuBar;
    private MainPanel myMainPanel;

    public void setTopMenuBar(TopMenuBar theTopMenuBar)
    {
        myTopMenuBar = theTopMenuBar;
        
    }
    
    public TopMenuBar getTopMenuBar()
    {
        return myTopMenuBar;
    }
    
    public void setMainPanel(MainPanel theMainPanel)
    {
        myMainPanel = theMainPanel;
        
    }
    
    public MainPanel getMainPanel()
    {
        return myMainPanel;
    }
    
    
    
    
    
    
    
    
    
    
    
}
