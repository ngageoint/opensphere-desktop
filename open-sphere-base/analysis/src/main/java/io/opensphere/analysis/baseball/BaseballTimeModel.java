package io.opensphere.analysis.baseball;

import java.util.HashMap;
import java.util.List;

import io.opensphere.core.util.swing.table.AbstractColumnTableModel;
import io.opensphere.mantle.data.element.DataElement;

public class BaseballTimeModel extends AbstractColumnTableModel
{
	/** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private final List<DataElement> myDataElements;

//    private final HashMap<String, DataElement> myElementTimes;

    public BaseballTimeModel(List<DataElement> dataElements)
    {
        super();
        myDataElements = dataElements;
//        myElementTimes = new HashMap<>();
//        myDataElements.forEach(e -> myElementTimes.put(e.getTimeSpan().toDisplayString(), e));
    }

	@Override
    public int getRowCount() {
        return myDataElements.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return myDataElements.get(rowIndex);
    }
}
