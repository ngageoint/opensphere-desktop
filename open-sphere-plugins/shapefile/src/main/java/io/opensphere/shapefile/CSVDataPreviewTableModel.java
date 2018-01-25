package io.opensphere.shapefile;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * The Class CSVDataPreviewTableModel.
 */
class CSVDataPreviewTableModel extends DefaultTableModel
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The import config. */
    private final ShapeFileSource myImportConfig;

    /** The values set. */
    private final List<List<String>> myValuesSet;

    /**
     * Instantiates a new cSV data preview table model.
     *
     * @param source the source
     * @param valuesSet the values set
     */
    public CSVDataPreviewTableModel(ShapeFileSource source, List<List<String>> valuesSet)
    {
        super();
        myImportConfig = source;
        myValuesSet = valuesSet;
    }

    @Override
    public int getColumnCount()
    {
        return myImportConfig.getColumnNames().size();
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return myImportConfig.getColumnNames().get(columnIndex);
    }

    @Override
    public int getRowCount()
    {
        if (myValuesSet != null)
        {
            if (myValuesSet.size() > 100)
            {
                return 100;
            }
            else
            {
                return myValuesSet.size();
            }
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        try
        {
            if (myValuesSet != null)
            {
                List<String> rowData = myValuesSet.get(row);
                if (col < rowData.size())
                {
                    return rowData.get(col);
                }
                else
                {
                    return "";
                }
            }
            else
            {
                return "";
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            return "";
        }
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column)
    {
        // Intentionally not implemented as cell should not be edited.
    }
}
