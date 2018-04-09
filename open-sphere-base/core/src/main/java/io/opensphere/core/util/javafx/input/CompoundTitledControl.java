package io.opensphere.core.util.javafx.input;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import io.opensphere.core.util.Visitable;
import io.opensphere.core.util.Visitor;

/**
 * Defines a control which wraps other controls. The set of child controls is
 * arranged according to the orientation supplied with the controls.If no
 * controls are supplied in the <code>pControls</code> variable argument array
 * during instantiation, then the caller may supply the controls one time to the
 * {@link #setControls(Orientation, boolean, IdentifiedControl...)} method. In
 * either case, the class is immutable, and controls may not be changed once
 * they have been set.
 */
public class CompoundTitledControl extends TitledControl
{
    /**
     * The controls contained within this compound control.
     */
    private List<IdentifiedControl<?>> myControls;

    /**
     * The node into which the controls are rendered.
     */
    private Node myNode;

    /**
     * Creates a new compound control, in which multiple child controls are
     * contained. The orientation of the child controls is determined from the
     * supplied orientation. If no controls are supplied in the
     * <code>pControls</code> variable argument array, then the caller may
     * supply the controls one time to the
     * {@link #setControls(Orientation, boolean, IdentifiedControl...)} method.
     *
     * @param pTitle the title of the compound control, which will be displayed
     *            in a center left position for vertical orientation.
     * @param pOrientation the layout of the child controls.
     * @param pShowSubtitles if true, the title of each child control will be
     *            displayed to the left of the child control, if false, the
     *            titles of the child controls will be omitted.
     * @param pControls the optional set of controls to encapsulate within the
     *            compound control.
     */
    public CompoundTitledControl(String pTitle, Orientation pOrientation, boolean pShowSubtitles,
            IdentifiedControl<?>... pControls)
    {
        super(pTitle);

        if (pControls != null && pControls.length > 0)
        {
            setControls(pOrientation, pShowSubtitles, pControls);
        }

        setStyle("-fx-border-width: 1; -fx-border-style: solid; -fx-border-radius: 5;");
    }

    /**
     * Sets the style of the internal node to the supplied value.
     *
     * @param pStyle The inline CSS style to use for this Node.
     *            <code>null</code> is implicitly converted to an empty String.
     */
    protected void setNodeStyle(String pStyle)
    {
        myNode.setStyle("-fx-alignment: center-right; " + pStyle);
    }

    /**
     * Sets the set of controls within the compound control. The orientation of
     * the child controls is determined from the supplied orientation. If the
     * controls have already been set (the {@link #myControls} field is not
     * null), then an {@link UnsupportedOperationException} is thrown.
     *
     * @param pOrientation the layout of the child controls.
     * @param pShowSubtitles if true, the title of each child control will be
     *            displayed to the left of the child control, if false, the
     *            titles of the child controls will be omitted.
     * @param pControls the optional set of controls to encapsulate within the
     *            compound control.
     * @throws UnsupportedOperationException if the controls have already been
     *             set.
     */
    protected void setControls(Orientation pOrientation, boolean pShowSubtitles, IdentifiedControl<?>... pControls)
    {
        if (myControls != null)
        {
            throw new UnsupportedOperationException(
                    "Reassignment of the child controls is not permitted once they have been set.");
        }
        myControls = Arrays.asList(pControls);

        switch (pOrientation)
        {
            case HORIZONTAL:
                myNode = createHorizontalBox(pShowSubtitles, pControls);
                break;
            default:
                myNode = createVerticalBox(pShowSubtitles, pControls);
                break;
        }
        setNodeStyle("-fx-padding: 5;");
    }

    /**
     * Creates a {@link Node} in which the child components are laid out. This
     * node does not include the compound control's title, as the title is
     * usually handled by the caller in a separate controller. If
     * <code>pShowSubtitles</code> is true, the box will be created as a grid,
     * with labels containing the title of each child component shown on the
     * left column of the row, and the corresponding child component shown in
     * the right column of the row. If <code>pShowSubtitles</code> is true, the
     * box is merely a vertical box containing each of the controls.
     *
     * @param pShowSubtitles if true, the title of each child control will be
     *            displayed to the left of the child control, if false, the
     *            titles of the child controls will be omitted.
     * @param pControls the set of controls to include in the vertical box.
     * @return a Node in which the child controls are rendered.
     */
    protected Node createVerticalBox(boolean pShowSubtitles, IdentifiedControl<?>... pControls)
    {
        if (pShowSubtitles)
        {
            int rowNumber = 0;
            GridPane grid = new GridPane();
            grid.setVgap(4);
            for (IdentifiedControl<?> wpsControl : pControls)
            {
                Label label = new Label(wpsControl.getTitle() + ":");
                grid.addRow(rowNumber++, label, wpsControl);
                GridPane.setHalignment(label, HPos.RIGHT);
                GridPane.setHalignment(wpsControl, HPos.RIGHT);
                GridPane.setFillWidth(wpsControl, true);
            }
            return grid;
        }
        return new VBox(pControls);
    }

    /**
     * Creates a {@link Node} in which the child components are laid out. This
     * node does not include the compound control's title, as the title is
     * usually handled by the caller in a separate controller. If
     * <code>pShowSubtitles</code> is true, the box will be created as a grid,
     * with labels containing the title of each child component shown on the top
     * row of the column, and the corresponding child component shown in the
     * bottom row of column. If <code>pShowSubtitles</code> is true, the box is
     * merely a horizontal box containing each of the controls.
     *
     * @param pShowSubtitles if true, the title of each child control will be
     *            displayed to above the child control, if false, the titles of
     *            the child controls will be omitted.
     * @param pControls the set of controls to include in the vertical box.
     * @return a Node in which the child controls are rendered.
     */
    protected Node createHorizontalBox(boolean pShowSubtitles, IdentifiedControl<?>... pControls)
    {
        if (pShowSubtitles)
        {
            int columnNumber = 0;
            GridPane grid = new GridPane();
            grid.setHgap(4);
            for (IdentifiedControl<?> wpsControl : pControls)
            {
                Label label = new Label(wpsControl.getTitle() + ":");
                grid.addColumn(columnNumber++, label, wpsControl);
                GridPane.setValignment(label, VPos.CENTER);
                GridPane.setValignment(wpsControl, VPos.CENTER);
            }
            return grid;
        }
        return new HBox(pControls);
    }

    /**
     * Gets the value of the {@link #myControls} field.
     *
     * @return the value stored in the {@link #myControls} field.
     */
    public List<IdentifiedControl<?>> getControls()
    {
        return myControls;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Control#createDefaultSkin()
     */
    @Override
    protected Skin<?> createDefaultSkin()
    {
        return new SimpleSkin(this, myNode);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitable#visit(io.opensphere.core.util.Visitor)
     */
    @Override
    public void visit(Visitor<?> pVisitor)
    {
        for (Visitable control : myControls)
        {
            control.visit(pVisitor);
        }
    }
}
