package io.opensphere.core.util.fx;

import org.apache.log4j.Logger;

import io.opensphere.core.util.AwesomeIconSolid;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 */
public class OSTabDemo extends Application
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(OSTabDemo.class);

    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("Hello World!");
        ObservableList<OSTab> tabs = FXCollections.observableArrayList();

        tabs.add(new OSTab("One"));
        tabs.add(new OSTab("dos"));
        tabs.add(new OSTab("Set"));
        tabs.add(new OSTab("Shi"));

        OSTabBar bar = new OSTabBar(tabs);

        Tab tabone = new Tab("one");
        tabone.setGraphic(FxIcons.createClearIcon(Color.WHITESMOKE, 12, AwesomeIconSolid.ANCHOR));
        tabone.textProperty().addListener((obs, ov, nv) -> LOG.info("Tab one label changed from '" + ov + "' to '" + nv + "'"));
        Tab tabtwo = new Tab("dos");
        Tab tabthree = new Tab("Set");
        Tab tabfour = new Tab("Shi");
        Tab tabFive = new Tab();
        tabFive.setGraphic(FxIcons.createClearIcon(Color.WHITESMOKE, 12, AwesomeIconSolid.AMBULANCE));

        OSTabPane tabPaneSkinned = new OSTabPane(tabone, tabtwo, tabthree, tabfour, tabFive);
        tabPaneSkinned.tabDragPolicyProperty().set(TabDragPolicy.REORDER);

        Tab tabFiveB = new Tab();
        tabFiveB.setGraphic(FxIcons.createClearIcon(Color.WHITESMOKE, 12, AwesomeIconSolid.AMBULANCE));
        TabPane tabPaneStd = new TabPane(new Tab("one", FxIcons.createClearIcon(Color.WHITESMOKE, 12, AwesomeIconSolid.ANCHOR)),
                new Tab("dos"), new Tab("Set"), new Tab("Shi"), tabFiveB);
        tabPaneStd.tabDragPolicyProperty().set(TabDragPolicy.REORDER);

        VBox vbox = new VBox(20, bar, tabPaneSkinned, tabPaneStd);

        Scene value = new Scene(vbox, 250, 250);
        FXUtilities.addDesktopStyle(value);
        primaryStage.setScene(value);
        primaryStage.show();
    }
}
