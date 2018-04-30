package io.opensphere.core.common.json.obj;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

public class JSONSaxObject extends LinkedList<JSONSaxPair> implements JSONSaxValue, JSONComposite
{
    private static final long serialVersionUID = 2458676636511397209L;

    public JSONSaxObject()
    {
        super();
    }

    public JSONSaxObject(Collection<JSONSaxPair> elements)
    {
        super(elements);
    }

    @Override
    public void toJSON(PrintStream ps, int indent)
    {
        boolean pretty = indent >= 0;
        ps.append("{");
        if (pretty)
        {
            ps.append("\n");
        }
        boolean first = true;
        for (JSONSaxPair val : this)
        {
            if (!first)
            {

                if (pretty)
                {
                    ps.append(",");
                    ps.append("\n");
                }
                else
                {
                    ps.append(",");
                }
            }

            ps.append(StringUtils.leftPad("", pretty ? indent + 2 : 0));
            val.toJSON(ps, pretty ? indent + 2 : indent);

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
        ps.append("}");
    }

    @Override
    public Type getType()
    {
        return Type.OBJECT;
    }
}
