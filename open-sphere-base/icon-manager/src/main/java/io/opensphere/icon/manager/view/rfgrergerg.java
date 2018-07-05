package io.opensphere.icon.manager.view;

import javafx.application.Application;
import javafx.stage.Stage;

public class rfgrergerg extends Application

{


    @Override
    public void start(Stage defaultStageIgnored) {
        FinalStage stage = new FinalStage();
        stage.show();
        
        FinalStage stage2= new FinalStage("Hello my friend Eli");
        
        stage2.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

