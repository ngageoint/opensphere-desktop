package io.opensphere.merge.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.columns.gui.Constants;
import io.opensphere.merge.controller.ColumnAssociationsLauncher;
import io.opensphere.merge.layout.GenericLayout;
import io.opensphere.merge.layout.LayMode;
import io.opensphere.merge.layout.LinearLayout;
import io.opensphere.merge.model.JoinModel;
import io.opensphere.merge.model.MergePrefs;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * This class houses the GUI for allowing users to request dataset joins.
 */
public class JoinGui
{
    /** The main Toolbox. */
    private Toolbox tools;

    /** The data group controller. */
    private DataGroupController groupCtrl;

    /** For displaying status in the JFXDialog. */
    private final SimpleValidator jVal = new SimpleValidator(this);

    /** Group of radio buttons for selecting the primary layer. */
    private final ToggleGroup typeGroup = new ToggleGroup();

    /** Subcomponents that manage the layers. */
    private final List<LayerRow> rows = new LinkedList<>();

    /** Top layout component. */
    private final LinearLayout vLay = LinearLayout.col();

    /** GUI root. */
    private final GenericLayout mainPane = new GenericLayout(vLay);

    /** Entry field for the new layer name. */
    private final TextField nameField = new TextField();

    /** Shortcut to column associations GUI. */
    private final Button colAssoc;

    /** Group of radio buttons for method of matching. */
    private final ToggleGroup methGroup = new ToggleGroup();

    /** Exact matching selector. */
    private final RadioButton exactMeth = GuiUtil.radio("Exact Match", methGroup);

    /** Partial matching selector. */
    private final RadioButton partialMeth = GuiUtil.radio("Contains", methGroup);

    /**
     * Constructor used to create a new Join UI. Refactored to remove static
     * initialization blocks, per OpenSphere style guidelines, section 6.5.
     */
    public JoinGui()
    {
        colAssoc = new Button(Constants.COLUMN_MAPPING.pluralTitleCase());
        Image image = new Image(getClass().getResourceAsStream("/images/columnassociations.png"));
        colAssoc.setGraphic(new ImageView(image));

        mainPane.getRoot().setBorder(GuiUtil.emptyBorder(5.0));
        nameField.textProperty().addListener((o, v0, v1) -> checkValid());
        colAssoc.setOnAction(e -> ColumnAssociationsLauncher.go(tools));

        methGroup.selectToggle(exactMeth);
    }

    /**
     * Return the root JFX Node.
     *
     * @return Pane
     */
    public Pane getMainPane()
    {
        return mainPane.getRoot();
    }

    /**
     * Perform initialization that requires external resources.
     *
     * @param tb the system Toolbox
     * @param d the enclosing JFXDialog
     */
    public void setup(Toolbox tb, JFXDialog d)
    {
        tools = tb;
        MantleToolbox mtb = tools.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        groupCtrl = mtb.getDataGroupController();
        d.setValidatorSupport(jVal);
        checkValid();
    }

    /**
     * Receive the list of DataTypeInfo and setup to edit a new configuration.
     *
     * @param types the layers to join
     */
    public void setData(List<DataTypeInfo> types)
    {
        // sometimes (rarely) JavaFX complains if the setData method is called
        // on the current thread; shift it to the JFX thread to avoid the issue
        GuiUtil.invokeJfx(() ->
        {
            for (DataTypeInfo t : types)
            {
                rows.add(getLayerRow(t));
            }
            layoutGui();

            if (!typeGroup.getToggles().isEmpty())
            {
                typeGroup.selectToggle(typeGroup.getToggles().get(0));
            }
        });
    }

    /**
     * Setup to edit an existing configuration.
     *
     * @param j the "Join" config to be edited.
     */
    public void setData(MergePrefs.Join j)
    {
        GuiUtil.invokeJfx(() ->
        {
            // extract name and matching method from the config
            nameField.setText(j.name);
            if (j.useExact)
            {
                methGroup.selectToggle(exactMeth);
            }
            else
            {
                methGroup.selectToggle(partialMeth);
            }

            // create the layer parameter rows
            for (MergePrefs.LayerParam lp : j.params)
            {
                DataTypeInfo t = groupCtrl.findMemberById(lp.typeKey);
                // t ought not to be null
                LayerRow r = getLayerRow(t);
                r.colCombo.setValue(lp.column);
                rows.add(r);
                if (lp.primary)
                {
                    typeGroup.selectToggle(r.lbl);
                }
            }

            layoutGui();
        });
    }

    /** Like, lay out the GUI, ya know? */
    private void layoutGui()
    {
        vLay.clear();
        vLay.add(new Label("Layer Name:"), LayMode.MIN);
        vLay.addSpace(1.0);
        vLay.add(nameField, LayMode.STRETCH);
        vLay.addSpace(15.0);
        vLay.add(new Label("Choose primary layer and join columns:"), LayMode.MIN);
        vLay.addSpace(5.0);
        for (LayerRow r : rows)
        {
            vLay.addAcross(r.row);
            vLay.addSpace(1.0);
        }
        vLay.addSpace(14.0);

        vLay.add(colAssoc, LayMode.MIN);
        vLay.addSpace(15.0);

        vLay.add(new Label("Join Method:"), LayMode.MIN);
        vLay.addSpace(5.0);
        vLay.add(exactMeth, LayMode.MIN);
        vLay.addSpace(5.0);
        vLay.add(partialMeth, LayMode.MIN);
    }

    /**
     * Create a new row in the GUI to represent the specified <i>layer</i>.
     *
     * @param layer DataTypeInfo
     * @return the created LayerRow
     */
    private LayerRow getLayerRow(DataTypeInfo layer)
    {
        LayerRow row = new LayerRow();
        row.layer = layer;
        row.lbl = GuiUtil.radio(row.layer.getDisplayName(), typeGroup);
        // populate the combobox
        row.colCombo.getItems().clear();
        MetaDataInfo meta = row.layer.getMetaDataInfo();
        if (meta != null)
        {
            row.colCombo.getItems().addAll(meta.getKeyNames());
        }
        selectFirst(row.colCombo);
        // set a tooltip for the ComboBox
        row.colCombo.setTooltip(new Tooltip("Join column for " + row.layer.getDisplayName()));
        row.layout();
        return row;
    }

    /**
     * Simple utility to select the first item in a ComboBox. This method is
     * tolerant of the absence of items.
     *
     * @param cb ComboBox
     */
    private static <T> void selectFirst(ComboBox<T> cb)
    {
        List<T> items = cb.getItems();
        if (!items.isEmpty())
        {
            cb.setValue(items.get(0));
        }
    }

    /** Holder of the stuff relevant to one layer. */
    private static class LayerRow
    {
        /** The layer. */
        public DataTypeInfo layer;

        /** RadioButton selector as the primary; also shows the label. */
        public RadioButton lbl;

        /** Join column selector. */
        public ComboBox<String> colCombo = new ComboBox<>();
        {
            colCombo.setEditable(false);
        }

        /** Layout mechanism for this row. */
        public LinearLayout row;

        /** Place subcomponents within the layout. */
        public void layout()
        {
            row = LinearLayout.row();
            row.add(lbl, LayMode.STRETCH, 200.0);
            row.addSpace(5.0);
            row.add(colCombo, LayMode.STRETCH, 190.0);
        }
    }

    /**
     * Package the user selections for export to the caller.
     *
     * @return the data model
     */
    public JoinModel getModel()
    {
        JoinModel jm = new JoinModel();
        jm.setJoinName(nameField.getText());
        jm.setUseExact(exactMeth.isSelected());
        for (LayerRow r : rows)
        {
            jm.addRow(r.lbl.isSelected(), r.layer, r.colCombo.getValue());
        }
        return jm;
    }

    /** Check the validity of the current data and show status. */
    private void checkValid()
    {
        if (nameField.getText().isEmpty())
        {
            jVal.setStatus(ValidationStatus.ERROR, "A name must be provided.");
        }
        else
        {
            jVal.setStatus(ValidationStatus.VALID, "");
        }
    }
}
