package io.opensphere.core.common.json.obj;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

public class JSONSaxArray extends LinkedList<JSONSaxValue> implements JSONSaxValue, JSONComposite
{
    private static final long serialVersionUID = 8856961105031306400L;

    public JSONSaxArray()
    {
        super();
    }

    public JSONSaxArray(Collection<? extends JSONSaxValue> elements)
    {
        super(elements);
    }

    @Override
    public void toJSON(PrintStream ps, int indent)
    {
        boolean pretty = indent >= 0;
        ps.append("[");
        if (pretty)
        {
            ps.append("\n");
        }
        boolean first = true;
        for (JSONSaxValue val : this)
        {
            if (!first)
            {
                ps.append(",");
                if (pretty)
                {
                    ps.append("\n");
                }
            }

            ps.append(StringUtils.leftPad("", pretty ? indent + 2 : 0));
            switch (val.getType())
            {
                case ARRAY:
                case OBJECT:
                    val.toJSON(ps, pretty ? indent + 2 : indent);
                    break;
                default:
                    val.toJSON(ps, indent + 2);
            }

            if (first)
            {
                first = false;
            }
        }
        if (pretty)
        {
            ps.append("\n");
            ps.append(StringUtils.leftPad("", pretty ? indent : 0));
        }
        ps.append("]");
    }

    @Override
    public Type getType()
    {
        return Type.ARRAY;
    }
}
