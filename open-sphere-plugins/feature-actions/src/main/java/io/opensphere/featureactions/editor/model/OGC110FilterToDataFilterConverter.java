package io.opensphere.featureactions.editor.model;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.xml.bind.JAXBElement;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.filterbuilder.filter.v1.CommonFieldGroup;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.filterbuilder.filter.v1.Source;
import io.opensphere.filterbuilder.impl.WFSUtilities;
import net.opengis.ogc._110.BinaryComparisonOpType;
import net.opengis.ogc._110.BinaryLogicOpType;
import net.opengis.ogc._110.FilterType;
import net.opengis.ogc._110.LiteralType;
import net.opengis.ogc._110.PropertyIsLikeType;
import net.opengis.ogc._110.PropertyIsNullType;
import net.opengis.ogc._110.PropertyNameType;
import net.opengis.ogc._110.UnaryLogicOpType;

/**
 * Converts an OGC_110 filter to the application's internal 'Filter' structure.
 */
public class OGC110FilterToDataFilterConverter implements Function<FilterType, Filter>
{
    /** If in list should be ignored. */
    private boolean ignoreInList;

    /**
     * Create an instance that ignores calls to checkInList.
     *
     * @return the filter converter that ignores calls to checkInList
     */
    public static OGC110FilterToDataFilterConverter noInList()
    {
        OGC110FilterToDataFilterConverter w = new OGC110FilterToDataFilterConverter();
        w.ignoreInList = true;
        return w;
    }

    @Override
    public Filter apply(FilterType ogcFilter)
    {
        Filter filter = new Filter("Filter", new Source());
        filter.getOtherSources().add(filter.getSource());
        filter.setActive(false, this);
        filter.setFilterGroup(new Group());
        if (ogcFilter.getLogicOps() != null)
        {
            filter.setMatch(ogcFilter.getLogicOps().getName().getLocalPart().toUpperCase());
            convert(ogcFilter.getLogicOps(), filter, null, null);
        }
        else if (ogcFilter.getComparisonOps() != null)
        {
            filter.setMatch("AND");
            convert(ogcFilter.getComparisonOps(), filter, null, null);
        }
        else if (ogcFilter.getSpatialOps() != null)
        {
            filter.setMatch("AND");
            convert(ogcFilter.getSpatialOps(), filter, null, null);
        }
        return filter;
    }

    /**
     * Recursively iterates over an OGC filter and converts it into the
     * application type 'Filter'.
     *
     * @param comparisonOps the list of JAXBElements to be converted.
     * @param filter the filter
     * @param group the group
     * @param criteria the criteria
     */
    private void convert(Collection<JAXBElement<?>> comparisonOps, Filter filter, Group group, Criteria criteria)
    {
        for (JAXBElement<?> element : comparisonOps)
        {
            convert(element, filter, group, criteria);
        }
    }

    /**
     * Recursively iterates over an OGC filter and converts it into the
     * application type 'Filter'.
     *
     * @param element the JAXBElement to be converted.
     * @param filter the filter
     * @param group the group
     * @param criteria the criteria
     */
    protected void convert(JAXBElement<?> element, Filter filter, Group group, Criteria criteria)
    {
        Object val = element.getValue();
        if (val instanceof UnaryLogicOpType)
        {
            doUnaryLogicOpAction(filter, group, criteria, element);
        }
        else if (val instanceof BinaryLogicOpType)
        {
            doBinaryLogicOpAction(filter, group, criteria, element);
        }
        else if (val instanceof BinaryComparisonOpType)
        {
            doBinaryComparisonOpTypeAction(filter, group, (BinaryComparisonOpType)val, element.getName().getLocalPart());
        }
        else if (val instanceof PropertyNameType)
        {
            if (criteria != null)
            {
                criteria.setField(((PropertyNameType)val).getValue());
            }
        }
        else if (val instanceof LiteralType)
        {
            if (criteria != null)
            {
                criteria.setValue(literalStringVal((LiteralType)val));
            }
        }
        else if (val instanceof PropertyIsLikeType)
        {
            Criteria newCriteria = criteria;
            if (group != null)
            {
                PropertyIsLikeType pilt = (PropertyIsLikeType)val;
                if (newCriteria == null)
                {
                    newCriteria = new Criteria();
                }
                newCriteria.setComparisonOperator(Conditional.LIKE);
                for (Object obj : pilt.getLiteral().getContent())
                {
                    newCriteria.setValue(obj.toString());
                }
                newCriteria.setField(pilt.getPropertyName().getValue());
                group.addFilterCriteria(newCriteria);
            }
        }
        else if (val instanceof PropertyIsNullType)
        {
            Criteria newCriteria = new Criteria();
            newCriteria.setComparisonOperator(Conditional.EMPTY);
            newCriteria.setField(((PropertyIsNullType)val).getPropertyName().getValue());
            group.addFilterCriteria(newCriteria);
        }
    }

    /**
     * Construct a comparison subexpression.
     *
     * @param filter the Filter
     * @param group the Group
     * @param bcot the comparison operands
     * @param opName the comparison operator
     */
    private void doBinaryComparisonOpTypeAction(Filter filter, Group group, BinaryComparisonOpType bcot, String opName)
    {
        // handle the case of not equal to "null" as a special case
        if (detectNeqNull(group, bcot, opName))
        {
            return;
        }

        Criteria newCriteria = null;
        if (group != null)
        {
            Conditional cond = WFSUtilities.getConditionalForProperty(opName);
            newCriteria = new Criteria();
            newCriteria.setComparisonOperator(cond);
            group.addFilterCriteria(newCriteria);
        }
        convert(bcot.getExpression(), filter, group, newCriteria);
    }

    /**
     * Detect and handle the case of a field compared not equal to "null".
     *
     * @param group a Group (to contain any created Criteria)
     * @param bcot comparison operands
     * @param opName comparison operator
     * @return true if and only if this method handled the subexpression
     */
    private static boolean detectNeqNull(Group group, BinaryComparisonOpType bcot, String opName)
    {
        // check for the "not equal" operator
        if (WFSUtilities.getConditionalForProperty(opName) != Conditional.NEQ)
        {
            return false;
        }
        // check the operands--one of them must be the literal empty string,
        // and the other must be the field
        LiteralType lit = getLiteralOperand(bcot);
        if (lit == null || !"".equals(literalStringVal(lit)))
        {
            return false;
        }
        PropertyNameType prop = getFieldOperand(bcot);
        if (prop == null)
        {
            return false;
        }

        Criteria crit = new Criteria();
        crit.setComparisonOperator(Conditional.NOT_EMPTY);
        crit.setField(prop.getValue());
        group.addFilterCriteria(crit);

        return true;
    }

    /**
     * Do binary logic op action.
     *
     * @param filter the filter
     * @param group the group
     * @param criteria the criteria
     * @param element the element
     */
    private void doBinaryLogicOpAction(Filter filter, Group group, Criteria criteria, JAXBElement<?> element)
    {
        BinaryLogicOpType blot = (BinaryLogicOpType)element.getValue();
        Group newGroup = null;

        Logical logicOp = Logical.valueOf(element.getName().getLocalPart().toUpperCase());
        if (logicOp == Logical.AND || logicOp == Logical.OR)
        {
            newGroup = new Group(logicOp);
            if (group == null)
            {
                newGroup.setName(filter.getName());
                filter.setFilterGroup(newGroup);
            }
            else
            {
                group.addFilterGroup(newGroup);
            }
        }
        convert(blot.getComparisonOpsOrSpatialOpsOrLogicOps(), filter, newGroup, criteria);
        if (group != null)
        {
            checkInList(group, newGroup);
            checkInRange(group, newGroup);
        }
    }

    /**
     * Do unary logic op action.
     *
     * @param filter the filter
     * @param group the group
     * @param criteria the criteria
     * @param element the element
     */
    private void doUnaryLogicOpAction(Filter filter, Group group, Criteria criteria, JAXBElement<?> element)
    {
        UnaryLogicOpType ulot = (UnaryLogicOpType)element.getValue();
        Group newGroup = null;

        Logical logicOp = Logical.valueOf(element.getName().getLocalPart().toUpperCase());
        if (logicOp == Logical.NOT)
        {
            newGroup = new Group(logicOp);
            if (group == null)
            {
                newGroup.setName(filter.getName());
                filter.setFilterGroup(newGroup);
            }
            else
            {
                group.addFilterGroup(newGroup);
            }
        }
        if (ulot.getComparisonOps() != null)
        {
            convert(ulot.getComparisonOps(), filter, newGroup, criteria);
        }
        else if (ulot.getLogicOps() != null)
        {
            convert(ulot.getLogicOps(), filter, newGroup, criteria);
        }
    }

    /**
     * Checks the group criteria to see if they constitute an 'In List'
     * operator. If so, the group is removed and a CommonFieldGroup is inserted.
     *
     * @param parent the group that contains the group we're checking
     * @param group the new group to check
     */
    private void checkInList(Group parent, Group group)
    {
        if (ignoreInList)
        {
            return;
        }

        if (group.getGroups().size() == 0 && group.getLogicOperator().equals(Logical.OR))
        {
            boolean inList = false;

            StringBuilder sb = new StringBuilder();
            CommonFieldGroup cfg = new CommonFieldGroup();
            cfg.setLogicOperator(group.getLogicOperator());
            String field = null;

            List<Criteria> criterion = group.getCriteria();
            for (int i = 0; i < criterion.size(); i++)
            {
                Criteria c = criterion.get(i);
                if (c.getComparisonOperator().equals(Conditional.EQ))
                {
                    inList = true;
                    cfg.addFilterCriteria(c);

                    if (field == null)
                    {
                        field = c.getField();
                    }
                    sb.append(c.getValue());
                    if (i < criterion.size())
                    {
                        sb.append(',');
                    }
                }
                else
                {
                    inList = false;
                    break;
                }
            }

            if (inList)
            {
                cfg.setName(sb.toString());
                cfg.setField(field);
                cfg.setFieldComparisonOperator(Conditional.EQ);
                parent.removeFilterGroup(group);
                parent.addCommonFieldFilterGroup(cfg);
            }
        }
    }

    /**
     * Checks the group criteria to see if there are 2 criteria that constitute
     * a range. If so, the group is removed and a CommonFieldGroup is inserted.
     *
     * @param parent the group that contains the group we're checking
     * @param group the new group to check
     */
    private void checkInRange(Group parent, Group group)
    {
        List<Criteria> criterion = group.getCriteria();
        if (group.getLogicOperator().equals(Logical.AND) && criterion.size() == 2)
        {
            Criteria c1 = criterion.get(0);
            Criteria c2 = criterion.get(1);
            if (c1.getComparisonOperator() == Conditional.GTE && c2.getComparisonOperator() == Conditional.LTE)
            {
                CommonFieldGroup cfg = new CommonFieldGroup();
                cfg.addFilterCriteria(c1);
                cfg.addFilterCriteria(c2);
                cfg.setLogicOperator(group.getLogicOperator());
                cfg.setField(c1.getField());
                cfg.setName(c1.getField() + " " + c1.getValue() + " - " + c2.getValue());
                parent.removeFilterGroup(group);
                parent.addCommonFieldFilterGroup(cfg);
            }
        }
    }

    /**
     * Utility method to find the String value of a Literal.
     *
     * @param lit the literal
     * @return the String value of <i>lit</i>
     */
    private static String literalStringVal(LiteralType lit)
    {
        return lit.getContent().stream().filter(v -> v != null).map(v -> v.toString()).findAny().orElse("");
    }

    /**
     * Find the LiteralType operand, if any.
     *
     * @param bcot a comparison
     * @return the LiteralType
     */
    private static LiteralType getLiteralOperand(BinaryComparisonOpType bcot)
    {
        return (LiteralType)bcot.getExpression().stream().map(op -> op.getValue()).filter(v -> v instanceof LiteralType).findAny()
                .orElse(null);
    }

    /**
     * Find the PropertyNameType operand, if any.
     *
     * @param bcot a comparison
     * @return the PropertyNameType
     */
    private static PropertyNameType getFieldOperand(BinaryComparisonOpType bcot)
    {
        return (PropertyNameType)bcot.getExpression().stream().map(op -> op.getValue()).filter(v -> v instanceof PropertyNameType)
                .findAny().orElse(null);
    }
}
