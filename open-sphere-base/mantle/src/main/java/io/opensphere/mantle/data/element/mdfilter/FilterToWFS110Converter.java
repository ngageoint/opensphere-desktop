package io.opensphere.mantle.data.element.mdfilter;

import java.util.List;

import javax.xml.bind.JAXBElement;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterCriteria;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import net.opengis.ogc._110.BinaryComparisonOpType;
import net.opengis.ogc._110.BinaryLogicOpType;
import net.opengis.ogc._110.ComparisonOpsType;
import net.opengis.ogc._110.LiteralType;
import net.opengis.ogc._110.LogicOpsType;
import net.opengis.ogc._110.ObjectFactory;
import net.opengis.ogc._110.PropertyIsLikeType;
import net.opengis.ogc._110.PropertyIsNullType;
import net.opengis.ogc._110.PropertyNameType;
import net.opengis.ogc._110.UnaryLogicOpType;

/**
 * FilterToOGCConverter utility to convert application filters into JAXB-formatted OGC Filter classes.
 */
public final class FilterToWFS110Converter
{
    /** We need exactly one of these. */
    private static final ObjectFactory OBJ_FACT = new ObjectFactory();

    /**
     * Convert the application filter into an OGC WFS 1.1.0 JAXB class.
     *
     * @param filter the filter to convert
     * @return the JAXB-formatted filter for use with OGC WFS 1.1.0 servers
     * @throws FilterException thrown for any Filter processing anomalies
     */
    public static JAXBElement<? extends LogicOpsType> convert(DataFilter filter) throws FilterException
    {
        DataFilterGroup group = filter.getFilterGroup();
        return convert(group);
    }

    /**
     * Convert an application filter criteria into an OGC WFS 1.1.0 JAXB class.
     *
     * @param crit the criteria to convert
     * @return the JAXB-formatted criteria for use with OGC WFS 1.1.0 servers
     * @throws FilterException thrown for any Filter processing anomalies
     */
    private static GenericOp convert(DataFilterCriteria crit) throws FilterException
    {
        return convert(propOf(crit.getField()), crit.getComparisonOperator(), crit.getValue());
    }

    /**
     * Operate on the components of the filter criteria.
     * @param field the field property
     * @param op the conditional operator
     * @param val the literal value for comparison
     * @return a JAXB thing
     * @throws FilterException in case of illegal comparison operator
     */
    private static GenericOp convert(PropertyNameType field, Conditional op, String val)
            throws FilterException
    {
        if (op == Conditional.LIKE)
        {
            return GenericOp.genComp(cvtLike(field, val));
        }
        else if (op == Conditional.NOT_LIKE)
        {
            return GenericOp.genLogic(cvtNot(GenericOp.genComp(cvtLike(field, val))));
        }
        else if (op == Conditional.CONTAINS)
        {
            return GenericOp.genComp(cvtLike(field, modContains(val)));
        }
        else if (op == Conditional.NOT_EMPTY)
        {
            return GenericOp.genLogic(cvtNot(GenericOp.genComp(cvtNull(field))));
        }
        else if (op == Conditional.EMPTY || val.isEmpty())
        {
            return GenericOp.genComp(cvtNull(field));
        }
        return GenericOp.genComp(cvtCompare(field, op, val));
    }

    /**
     * Create a unary check for "null".
     * @param field the field property
     * @return a JAXB thing
     */
    private static JAXBElement<? extends ComparisonOpsType> cvtNull(PropertyNameType field)
    {
        PropertyIsNullType isNull = new PropertyIsNullType();
        isNull.setPropertyName(field);
        return OBJ_FACT.createPropertyIsNull(isNull);
    }

    /**
     * Construct a pattern-matching comparison.
     * @param field the field property
     * @param val pattern
     * @return a JAXB thing
     */
    private static JAXBElement<? extends ComparisonOpsType> cvtLike(PropertyNameType field, String val)
    {
        PropertyIsLikeType isLike = new PropertyIsLikeType();
        isLike.setPropertyName(field);
        isLike.setWildCard("*");
        isLike.setEscapeChar("\\");
        isLike.setSingleChar(".");

        // Remove single and double quotes
        String noQuotes = val;
        if (noQuotes.contains("\"") || noQuotes.matches(".*?(\').*(\').*?"))
        {
            noQuotes = noQuotes.replaceAll("[\"\']", "");
        }
        isLike.setLiteral(literalOf(noQuotes));
        return OBJ_FACT.createPropertyIsLike(isLike);
    }

    /**
     * Construct a comparison of a field to a literal.
     * @param field the field property
     * @param op the conditional operator
     * @param val the literal value for comparison
     * @return a JAXB thing
     * @throws FilterException in case of illegal comparison operator
     */
    private static JAXBElement<? extends ComparisonOpsType> cvtCompare(
            PropertyNameType field, Conditional op, String val)
            throws FilterException
    {
        BinaryComparisonOpType binaryComp = new BinaryComparisonOpType();
        binaryComp.getExpression().add(OBJ_FACT.createPropertyName(field));
        binaryComp.getExpression().add(OBJ_FACT.createLiteral(literalOf(val)));

        if (op == Conditional.EQ)
        {
            return OBJ_FACT.createPropertyIsEqualTo(binaryComp);
        }
        if (op == Conditional.LT)
        {
            return OBJ_FACT.createPropertyIsLessThan(binaryComp);
        }
        if (op == Conditional.GT)
        {
            return OBJ_FACT.createPropertyIsGreaterThan(binaryComp);
        }
        if (op == Conditional.LTE)
        {
            return OBJ_FACT.createPropertyIsLessThanOrEqualTo(binaryComp);
        }
        if (op == Conditional.GTE)
        {
            return OBJ_FACT.createPropertyIsGreaterThanOrEqualTo(binaryComp);
        }
        if (op == Conditional.NEQ)
        {
            return OBJ_FACT.createPropertyIsNotEqualTo(binaryComp);
        }
        throw new FilterException("Unrecognized conditional operator: ", null);
    }

    /**
     * Create the logical inverse of the specified generic operand.
     * @param op operand (either a comparison or a logical operation)
     * @return JAXBElement
     */
    private static JAXBElement<? extends LogicOpsType> cvtNot(GenericOp op)
    {
        UnaryLogicOpType notOp = new UnaryLogicOpType();
        if (op.comp != null)
        {
            notOp.setComparisonOps(op.comp);
        }
        else
        {
            notOp.setLogicOps(op.logic);
        }
        return OBJ_FACT.createNot(notOp);
    }

    /**
     * Convert an application filter group into an OGC WFS 1.1.0 JAXB class.
     *
     * @param group the group to convert
     * @return the JAXB-formatted group for use with OGC WFS 1.1.0 servers
     * @throws FilterException thrown for any Filter processing anomalies
     */
    private static JAXBElement<? extends LogicOpsType> convert(DataFilterGroup group) throws FilterException
    {
        Logical op = group.getLogicOperator();
        List<? extends DataFilterCriteria> critList = group.getCriteria();
        List<? extends DataFilterGroup> grpList = group.getGroups();
        if (op == Logical.NOT)
        {
            if (critList.size() + grpList.size() != 1)
            {
                throw new FilterException("DataFilter Error: number of items in a \"NOT\" operator group must be 1.", null);
            }
            if (!group.getGroups().isEmpty())
            {
                return cvtNot(GenericOp.genLogic(convert(group.getGroups().get(0))));
            }
            return cvtNot(convert(group.getCriteria().get(0)));
        }

        if (critList.isEmpty() && grpList.isEmpty())
        {
            throw new FilterException("DataFilter Error: A filter group must contain at least one item", null);
        }

        BinaryLogicOpType logicOp = new BinaryLogicOpType();
        for (DataFilterCriteria criteria : critList)
        {
            logicOp.getComparisonOpsOrSpatialOpsOrLogicOps().add(convert(criteria).getWhatever());
        }
        for (DataFilterGroup child : grpList)
        {
            logicOp.getComparisonOpsOrSpatialOpsOrLogicOps().add(convert(child));
        }

        JAXBElement<? extends LogicOpsType> jaxbOutput = null;
        if (group.getLogicOperator().equals(Logical.AND))
        {
            jaxbOutput = OBJ_FACT.createAnd(logicOp);
        }
        else if (group.getLogicOperator().equals(Logical.OR))
        {
            jaxbOutput = OBJ_FACT.createOr(logicOp);
        }
        return jaxbOutput;
    }

    /**
     * Modify the specified value to match any String containing it.
     * @param val the desired value
     * @return the match String
     */
    private static String modContains(String val)
    {
        StringBuilder buf = new StringBuilder();
        buf.append('*');
        buf.append(val);
        buf.append('*');
        return buf.toString();
    }

    /**
     * Construct a PropertyNameType.
     * @param field name thereof
     * @return PropertyNameType
     */
    private static PropertyNameType propOf(String field)
    {
        PropertyNameType propName = new PropertyNameType();
        propName.setValue(field);
        return propName;
    }

    /**
     * Construct a LiteralType.
     * @param val value thereof
     * @return LiteralType
     */
    private static LiteralType literalOf(String val)
    {
        LiteralType literal = new LiteralType();
        literal.getContent().add(val);
        return literal;
    }

    /** Comparison or logical op.  Stupid design leads to stupid code. */
    private static class GenericOp
    {
        /** Reference used for a comparison op. */
        public JAXBElement<? extends ComparisonOpsType> comp;
        /** Reference used for a logic op. */
        public JAXBElement<? extends LogicOpsType> logic;

        /**
         * Create an instance with a comparison op.
         * @param op the op
         * @return GenericOp
         */
        public static GenericOp genComp(JAXBElement<? extends ComparisonOpsType> op)
        {
            GenericOp g = new GenericOp();
            g.comp = op;
            return g;
        }

        /**
         * Create an instance with a logical op.
         * @param op the op
         * @return GenericOp
         */
        public static GenericOp genLogic(JAXBElement<? extends LogicOpsType> op)
        {
            GenericOp g = new GenericOp();
            g.logic = op;
            return g;
        }

        /**
         * Get the contents as an undifferentiated JAXBElement.
         * @return JAXBElement
         */
        public JAXBElement<?> getWhatever()
        {
            if (comp != null)
            {
                return comp;
            }
            return logic;
        }
    }

    /** Disallow Instantiation. */
    private FilterToWFS110Converter()
    {
    }
}
