
package io.opensphere.merge.layout;

import java.util.function.Consumer;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/**
 * This is a test facility for debugging layout code. Since the result is purely
 * visual, there is no real point in converting to JUnit.
 */
public class Chrysalis
{
    /**
     * Main method, called (indirectly) by the JavaFX framework. Creates the GUI
     * and shoves it into the Stage. Then it shows the Stage.
     *
     * @param st Stage thingy
     */
    private void go(Stage st)
    {
        st.setHeight(500.0);
        st.setWidth(500.0);
        st.setScene(new Scene(scenario3()));
        st.show();
    }

    /**
     * Test scenario.
     *
     * @return the root Pane of the GUI
     */
    protected Pane scenario1()
    {
        PaneX p0 = new PaneX();
        p0.root.setPrefSize(10.0, 20.0);
        PaneX p1 = new PaneX(Color.BLUE);
        PaneX p2 = new PaneX(Color.GREEN);
        p2.root.setPrefSize(20.0, 50.0);
        PaneX p3 = new PaneX(Color.BLACK);

        LinearLayout vLay = LinearLayout.col();
        vLay.add(p0.root, LayMode.MIN);
        vLay.addSpace(20.0);
        vLay.addStretch(p1.root, LayMode.CENTER, 30.0, 100.0, 1.0);
        vLay.add(p2.root, LayMode.MAX);
        vLay.addSpace(0.0, 3.0);
        vLay.addFixed(p3.root, LayMode.STRETCH, 30.0, 10.0);

        GenericLayout g = new GenericLayout(vLay);
        g.getRoot().setBorder(emptyBorder(10.0));
        return g.getRoot();
    }

    /**
     * Test scenario.
     *
     * @return the root Pane of the GUI
     */
    protected Pane scenario2()
    {
        PaneX p0 = new PaneX();
        p0.root.setPrefSize(10.0, 20.0);
        PaneX p1 = new PaneX(Color.BLUE);
        PaneX p2 = new PaneX(Color.GREEN);
        p2.root.setPrefSize(20.0, 50.0);
        PaneX p3 = new PaneX(Color.BLACK);

        LinearLayout hLay = LinearLayout.row();
        hLay.add(p0.root, LayMode.MIN);
        hLay.addSpace(20.0);
        hLay.addStretch(p1.root, LayMode.CENTER, 30.0, 100.0, 1.0);
        hLay.add(p2.root, LayMode.MAX);
        hLay.addSpace(0.0, 3.0);
        hLay.addFixed(p3.root, LayMode.STRETCH, 30.0, 10.0);

        GenericLayout g = new GenericLayout(hLay);
        g.getRoot().setBorder(emptyBorder(10.0));
        return g.getRoot();
    }

    /**
     * Test scenario.
     *
     * @return the root Pane of the GUI
     */
    protected Pane scenario3()
    {
        PaneX p0 = new PaneX();
        p0.root.setPrefSize(10.0, 20.0);
        PaneX p1 = new PaneX(Color.BLUE);
        PaneX p2 = new PaneX(Color.GREEN);
        p2.root.setPrefSize(50.0, 50.0);

        LinearLayout row = LinearLayout.row();
        row.addFixed(new PaneX(Color.BLACK).root, LayMode.STRETCH, 10.0, 10.0);
        row.addSpace(0.0, 3.0);
        row.addFixed(new PaneX(Color.BLACK).root, LayMode.STRETCH, 10.0, 10.0);
        row.addSpace(5.0, 1.0);
        row.addFixed(new PaneX(Color.BLACK).root, LayMode.STRETCH, 10.0, 10.0);

        LinearLayout vLay = LinearLayout.col();
        vLay.add(p0.root, LayMode.MIN);
        vLay.addSpace(20.0);
        vLay.addStretch(p1.root, LayMode.CENTER, 30.0, 100.0, 1.0);
        vLay.add(p2.root, LayMode.MAX);
        vLay.addSpace(0.0, 3.0);
        vLay.addAcross(row, 20.0);

        GenericLayout g = new GenericLayout(vLay);
        g.getRoot().setBorder(emptyBorder(10.0));
        return g.getRoot();
    }

    /**
     * Test scenario.
     *
     * @return the root Pane of the GUI
     */
    protected Pane scenario4()
    {
        PaneX p0 = new PaneX(Color.BLUE);
        PaneX p1 = new PaneX(Color.GREEN);
        ThreeZoneRow tzr = new ThreeZoneRow();
        tzr.addCenter(new Button("Add"));
        tzr.addCenter(new Button("Delete"));
        tzr.addRight(new Button("Cancel"));

        LinearLayout vLay = LinearLayout.col();
        vLay.addStretch(p0.root, 1.0);
        vLay.addStretch(p1.root, 2.0);
        vLay.addSpace(10.0);
        vLay.addAcross(tzr);

        GenericLayout g = new GenericLayout(vLay);
        // g.getRoot().setBorder(emptyBorder(10.0));
        return g.getRoot();
    }

    /**
     * Factory method for creating a JavaFX Border.
     *
     * @param thick thickness of the Border
     * @return the Border
     */
    protected static Border emptyBorder(double thick)
    {
        return insBorder(new Insets(thick));
    }

    /**
     * Factory method for creating a JavaFX Border.
     *
     * @param top thickness on the top side
     * @param right thickness on the right side
     * @param bottom thickness on the bottom side
     * @param left thickness on the left side
     * @return the Border
     */
    protected static Border emptyBorder(double top, double right, double bottom, double left)
    {
        return insBorder(new Insets(top, right, bottom, left));
    }

    /**
     * Factory method for creating a JavaFX Border.
     *
     * @param ins Insets defining the thickness of the Border
     * @return the Border
     */
    protected static Border insBorder(Insets ins)
    {
        return new Border(new BorderStroke(null, null, null, null, ins));
    }

    /**
     * Creates a Pane that displays a colored "X" touching its four corners.
     */
    private static class PaneX
    {
        /** The root Pane. */
        public Pane root = new Pane();

        /** One of the lines that form the "X". */
        private final Line ln1 = new Line();

        /** One of the lines that form the "X". */
        private final Line ln2 = new Line();
        {
            ln1.setStartX(0.0);
            ln1.setStartY(0.0);
            ln1.setStrokeWidth(3.0);
            ln1.setStroke(Color.RED);
            root.getChildren().add(ln1);

            ln2.setStartX(0.0);
            ln2.setEndY(0.0);
            ln2.setStrokeWidth(3.0);
            ln2.setStroke(Color.RED);
            root.getChildren().add(ln2);

            chListen(root.widthProperty(), v -> setWidth(v.doubleValue()));
            chListen(root.heightProperty(), v -> setHeight(v.doubleValue()));
        }

        /** Create. */
        public PaneX()
        {
        }

        /**
         * Create.
         *
         * @param c the Color of the "X"
         */
        public PaneX(Color c)
        {
            setColor(c);
        }

        /**
         * Set the Color.
         *
         * @param c the Color
         */
        public void setColor(Color c)
        {
            ln1.setStroke(c);
            ln2.setStroke(c);
        }

        /**
         * Adjust to changing width.
         *
         * @param w width
         */
        private void setWidth(double w)
        {
            ln1.setEndX(w - 3.0);
            ln2.setEndX(w - 3.0);
        }

        /**
         * Adjust to changing height.
         *
         * @param h height
         */
        private void setHeight(double h)
        {
            ln1.setEndY(h - 3.0);
            ln2.setStartY(h - 3.0);
        }
    }

    /**
     * Removes some of the stupidity inherent in the handling of ObservableValue
     * change events (cf. chEar), covering 99.5% of use cases.
     *
     * @param obs an ObservableValue of some kind.
     * @param ear a Consumer for values assumed by <i>obs</i>
     * @param<T> type of value in <i>obs</i>
     */
    private static <T> void chListen(ObservableValue<T> obs, Consumer<T> ear)
    {
        obs.addListener(chEar(ear));
    }

    /**
     * Converts a simple Consumer to a ChangeListener for use in handling events
     * from an ObservableValue.
     *
     * @param ear listener
     * @param<T> the type of value to be consumed
     * @return a ChangeListener derived from the simpler <i>ear</i>
     */
    private static <T> ChangeListener<T> chEar(Consumer<T> ear)
    {
        return (obs, old, val) -> ear.accept(val);
    }

    /**
     * Main.
     *
     * @param argv ignored
     */
    public static void main(String[] argv)
    {
        Application.launch(InnerApp.class, new String[0]);
    }

    /**
     * This nested class extends from Application because no JavaFX gui can be
     * launched without such a class. The top-level class is not used for that
     * purpose to so that its namespace should not be polluted with crap
     * inherited from Application. <br>
     * <br>
     * Note that the class must be declared public for this to work. Indeed, it
     * is not possible to instantiate one's Application subclass and pass it to
     * the JavaFX application launcher. The jackasses that designed JavaFX
     * committed one of the most egregious sins imaginable by implementing the
     * launcher such that it uses reflection to obtain a Constructor that it
     * then uses to create the Application instance. This removes all ability
     * for the developer to control how the object is constructed.
     */
    public static class InnerApp extends Application
    {
        @Override
        public void start(Stage st) throws Exception
        {
            new Chrysalis().go(st);
        }
    }
}
