package io.opensphere.core.common.ogctools.conversion;

import javax.xml.bind.JAXBElement;

import com.google.common.base.Preconditions;

import io.opensphere.core.common.filter.Filter;
import io.opensphere.core.common.filter.expression.BinaryOperationEx;
import io.opensphere.core.common.filter.expression.Expression;
import io.opensphere.core.common.filter.expression.FunctionEx;
import io.opensphere.core.common.filter.expression.LiteralEx;
import io.opensphere.core.common.filter.expression.PropertyNameEx;
import io.opensphere.core.common.filter.operator.BinaryComparisonOp;
import io.opensphere.core.common.filter.operator.BinaryLogicalOp;
import io.opensphere.core.common.filter.operator.ComparisonOp;
import io.opensphere.core.common.filter.operator.IdEqualsOp;
import io.opensphere.core.common.filter.operator.LogicalNotOp;
import io.opensphere.core.common.filter.operator.LogicalOp;
import io.opensphere.core.common.filter.operator.Operator;
import io.opensphere.core.common.filter.operator.PropertyIsBetweenOp;
import io.opensphere.core.common.filter.operator.PropertyIsLikeOp;
import io.opensphere.core.common.filter.operator.PropertyIsNullOp;
import io.opensphere.core.common.filter.operator.SpatialOp;
import io.opensphere.core.common.filter.operator.UnaryLogicalOp;
import io.opensphere.core.common.filter.operator.SpatialOp.SpatialOperatorType;
import net.opengis.gml._311.AbstractGeometryType;
import net.opengis.ogc._110.AbstractIdType;
import net.opengis.ogc._110.BinaryComparisonOpType;
import net.opengis.ogc._110.BinaryLogicOpType;
import net.opengis.ogc._110.BinaryOperatorType;
import net.opengis.ogc._110.BinarySpatialOpType;
import net.opengis.ogc._110.ComparisonOpsType;
import net.opengis.ogc._110.DistanceBufferType;
import net.opengis.ogc._110.DistanceType;
import net.opengis.ogc._110.FeatureIdType;
import net.opengis.ogc._110.FilterType;
import net.opengis.ogc._110.FunctionType;
import net.opengis.ogc._110.GmlObjectIdType;
import net.opengis.ogc._110.LiteralType;
import net.opengis.ogc._110.LogicOpsType;
import net.opengis.ogc._110.LowerBoundaryType;
import net.opengis.ogc._110.ObjectFactory;
import net.opengis.ogc._110.PropertyIsBetweenType;
import net.opengis.ogc._110.PropertyIsLikeType;
import net.opengis.ogc._110.PropertyIsNullType;
import net.opengis.ogc._110.PropertyNameType;
import net.opengis.ogc._110.SpatialOpsType;
import net.opengis.ogc._110.UnaryLogicOpType;
import net.opengis.ogc._110.UpperBoundaryType;

/**
 * <code>CommonFilterToFilter110</code> transforms a common {@link Filter} to a
 * OGC 1.1.0 {@link FilterType}.
 * <p>
 * <b>WARNING:</b> This class is <i>not</i> thread-safe when the common filter
 * contains {@link IdEqualsOp}s.
 */
public class CommonFilterToFilter110
{
    /** The OGC Object Factory. */
    private static final ObjectFactory OGC_OBJECT_FACTORY = new ObjectFactory();

    /** The OGC filter instance. */
    private FilterType ogcFilter = null;

    /** Converts a JTS geometry to a GML geometry. */
    private JtsToGml311 jtsToGmlConverter = new JtsToGml311();

    /**
     * Sets the JTS to GML 3.1.1 geometries converter instance.
     *
     * @param jtsToGmlConverter the converter for JTS to GML 3.1.1 geometries.
     */
    public void setJtsToGmlConverter(final JtsToGml311 jtsToGmlConverter)
    {
        Preconditions.checkArgument(jtsToGmlConverter != null, "The JtsToGml311 conversion class cannot be null");
        this.jtsToGmlConverter = jtsToGmlConverter;
    }

    /**
     * Returns the JTS to GML 3.1.1 geometries converter instance.
     *
     * @return the converter for JTS to GML 3.1.1 geometries.
     */
    public JtsToGml311 getJtsToGmlConverter()
    {
        return jtsToGmlConverter;
    }

    /**
     * Transforms a common <code>Filter</code> an OGC <code>FilterType</code>.
     *
     * @param filter the filter to transform.
     * @return the OGC <code>FilterType</code>.
     */
    public FilterType transform(final Filter filter)
    {
        ogcFilter = null;
        if (filter != null)
        {
            ogcFilter = new FilterType();
            final Operator operator = filter.getOperator();
            if (operator instanceof ComparisonOp)
            {
                final JAXBElement<? extends ComparisonOpsType> element = transform((ComparisonOp)operator);
                if (element != null)
                {
                    ogcFilter.setComparisonOps(element);
                }
            }
            else if (operator instanceof LogicalOp)
            {
                final JAXBElement<? extends LogicOpsType> element = transform((LogicalOp)operator);
                ogcFilter.setLogicOps(element);
            }
            else if (operator instanceof SpatialOp)
            {
                final JAXBElement<? extends SpatialOpsType> element = transform((SpatialOp)operator);
                ogcFilter.setSpatialOps(element);
            }
        }

        // Reset the internal filter to null prior to returning.
        final FilterType tmp = ogcFilter;
        ogcFilter = null;
        return tmp;
    }

    /**
     * Transforms a common <code>Operator</code> an OGC operator.
     *
     * @param operator the operator to transform.
     * @return the OGC operator or <code>null</code> if the given operator was
     *         <code>null</code>.
     */
    public JAXBElement<?> transform(final Operator operator)
    {
        JAXBElement<?> element = null;
        if (operator != null)
        {
            if (operator instanceof ComparisonOp)
            {
                element = transform((ComparisonOp)operator);
            }
            else if (operator instanceof LogicalOp)
            {
                element = transform((LogicalOp)operator);
            }
            else if (operator instanceof SpatialOp)
            {
                element = transform((SpatialOp)operator);
            }
            else
            {
                throw new IllegalArgumentException("Unexpected operator type: " + operator.getClass().getCanonicalName());
            }
        }
        return element;
    }

    /**
     * Transforms a common <code>ComparisonOp</code> to an OGC
     * <code>ComparisonOpsType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>ComparisonOpsType</code>.
     */
    public JAXBElement<? extends ComparisonOpsType> transform(final ComparisonOp operator)
    {
        Preconditions.checkArgument(operator != null, "The comparison operator cannot be null");
        final JAXBElement<? extends ComparisonOpsType> element;
        if (operator instanceof BinaryComparisonOp)
        {
            element = transform((BinaryComparisonOp)operator);
        }
        else if (operator instanceof IdEqualsOp)
        {
            final JAXBElement<? extends AbstractIdType> idElement = transform((IdEqualsOp)operator);
            // XXX: This is the thread-unsafe portion of the class.
            if (ogcFilter != null)
            {
                ogcFilter.getId().add(idElement);
            }
            element = null;
        }
        else if (operator instanceof PropertyIsBetweenOp)
        {
            element = transform((PropertyIsBetweenOp)operator);
        }
        else if (operator instanceof PropertyIsLikeOp)
        {
            element = transform((PropertyIsLikeOp)operator);
        }
        else if (operator instanceof PropertyIsNullOp)
        {
            element = transform((PropertyIsNullOp)operator);
        }
        else
        {
            throw new IllegalArgumentException("Unexpected comparison operator type: " + operator.getClass().getCanonicalName());
        }
        return element;
    }

    /**
     * Transforms a common <code>BinaryComparisonOp</code> to an OGC
     * <code>BinaryComparisonOpType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>BinaryComparisonOpType</code>.
     */
    public JAXBElement<BinaryComparisonOpType> transform(final BinaryComparisonOp operator)
    {
        Preconditions.checkArgument(operator != null, "The binary comparison operator cannot be null");
        final BinaryComparisonOpType ogcOperator = new BinaryComparisonOpType();
        ogcOperator.setMatchCase(operator.isMatchCase());
        ogcOperator.getExpression().add(transform(operator.getLeftExpression()));
        ogcOperator.getExpression().add(transform(operator.getRightExpression()));

        final JAXBElement<BinaryComparisonOpType> element;
        switch (operator.getType())
        {
            case EQUAL_TO:
                element = OGC_OBJECT_FACTORY.createPropertyIsEqualTo(ogcOperator);
                break;
            case GREATER_THAN:
                element = OGC_OBJECT_FACTORY.createPropertyIsGreaterThan(ogcOperator);
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                element = OGC_OBJECT_FACTORY.createPropertyIsGreaterThanOrEqualTo(ogcOperator);
                break;
            case LESS_THAN:
                element = OGC_OBJECT_FACTORY.createPropertyIsLessThan(ogcOperator);
                break;
            case LESS_THAN_OR_EQUAL_TO:
                element = OGC_OBJECT_FACTORY.createPropertyIsLessThanOrEqualTo(ogcOperator);
                break;
            case NOT_EQUAL_TO:
                element = OGC_OBJECT_FACTORY.createPropertyIsNotEqualTo(ogcOperator);
                break;
            default:
                throw new IllegalArgumentException("Unexpected binary comparison operator type: " + operator.getType());
        }
        return element;
    }

    /**
     * Transforms a common <code>IdEqualsOp</code> to the OGC
     * <code>FeatureIdType</code>. Subclasses may change the implementation to
     * use {@link GmlObjectIdType} instead.
     *
     * @param operator the operator to transform.
     */
    public JAXBElement<? extends AbstractIdType> transform(final IdEqualsOp operator)
    {
        Preconditions.checkArgument(operator != null, "The ID equals operator cannot be null");
        final FeatureIdType featureId = new FeatureIdType();
        featureId.setFid(operator.getValue());
        final JAXBElement<? extends AbstractIdType> element = OGC_OBJECT_FACTORY.createFeatureId(featureId);
        return element;
    }

    /**
     * Transforms a common <code>PropertyIsBetweenOp</code> to an OGC
     * <code>PropertyIsBetweenType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>PropertyIsBetweenType</code>.
     */
    public JAXBElement<PropertyIsBetweenType> transform(final PropertyIsBetweenOp operator)
    {
        Preconditions.checkArgument(operator != null, "The property is between operator cannot be null");
        final JAXBElement<?> ogcExpression = transform(operator.getExpression());

        final LowerBoundaryType lowerBoundary = new LowerBoundaryType();
        final JAXBElement<?> lowerBoundaryExpression = transform(operator.getLowerBound());
        lowerBoundary.setExpression(lowerBoundaryExpression);

        final UpperBoundaryType upperBoundary = new UpperBoundaryType();
        final JAXBElement<?> upperBoundaryExpression = transform(operator.getUpperBound());
        upperBoundary.setExpression(upperBoundaryExpression);

        final PropertyIsBetweenType ogcOperator = new PropertyIsBetweenType();
        ogcOperator.setExpression(ogcExpression);
        ogcOperator.setLowerBoundary(lowerBoundary);
        ogcOperator.setUpperBoundary(upperBoundary);
        return OGC_OBJECT_FACTORY.createPropertyIsBetween(ogcOperator);
    }

    /**
     * Transforms a common <code>PropertyIsLikeOp</code> to an OGC
     * <code>PropertyIsLikeType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>PropertyIsLikeType</code>.
     */
    public JAXBElement<PropertyIsLikeType> transform(final PropertyIsLikeOp operator)
    {
        Preconditions.checkArgument(operator != null, "The property is like operator cannot be null");

        final PropertyNameType propertyName = new PropertyNameType();
        propertyName.setValue(operator.getName());

        final LiteralType literal = new LiteralType();
        literal.getContent().add(operator.getPattern());

        final PropertyIsLikeType ogcOperator = new PropertyIsLikeType();
        ogcOperator.setPropertyName(propertyName);
        ogcOperator.setLiteral(literal);
        ogcOperator.setEscapeChar(operator.getEscapeChar());
        ogcOperator.setSingleChar(operator.getSingleChar());
        ogcOperator.setWildCard(operator.getWildCard());
        return OGC_OBJECT_FACTORY.createPropertyIsLike(ogcOperator);
    }

    /**
     * Transforms a common <code>PropertyIsNullOp</code> to an OGC
     * <code>PropertyIsNullType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>PropertyIsNullType</code>.
     */
    public JAXBElement<PropertyIsNullType> transform(final PropertyIsNullOp operator)
    {
        Preconditions.checkArgument(operator != null, "The property is null operator cannot be null");
        final PropertyNameType propertyName = new PropertyNameType();
        propertyName.setValue(operator.getName());

        final PropertyIsNullType ogcOperator = new PropertyIsNullType();
        ogcOperator.setPropertyName(propertyName);
        return OGC_OBJECT_FACTORY.createPropertyIsNull(ogcOperator);
    }

    /**
     * Transforms a common <code>LogicalOp</code> to an OGC
     * <code>LogicOpsType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>LogicOpsType</code>.
     */
    public JAXBElement<? extends LogicOpsType> transform(final LogicalOp operator)
    {
        Preconditions.checkArgument(operator != null, "The logical operator cannot be null");
        final JAXBElement<? extends LogicOpsType> element;
        if (operator instanceof BinaryLogicalOp)
        {
            element = transform((BinaryLogicalOp)operator);
        }
        else if (operator instanceof UnaryLogicalOp)
        {
            element = transform((UnaryLogicalOp)operator);
        }
        else
        {
            throw new IllegalArgumentException("Unexpected logical operator type: " + operator.getClass().getCanonicalName());
        }
        return element;
    }

    /**
     * Transforms a common <code>BinaryLogicalOp</code> to an OGC
     * <code>LogicOpsType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>LogicOpsType</code>.
     */
    public JAXBElement<BinaryLogicOpType> transform(final BinaryLogicalOp operator)
    {
        Preconditions.checkArgument(operator != null, "The binary logical operator cannot be null");

        final BinaryLogicOpType binaryLogicOp = new BinaryLogicOpType();
        for (final Operator subOperator : operator.getOperators())
        {
            final JAXBElement<?> subElement = transform(subOperator);
            if (subElement != null)
            {
                binaryLogicOp.getComparisonOpsOrSpatialOpsOrLogicOps().add(subElement);
            }
        }

        final JAXBElement<BinaryLogicOpType> element;
        switch (operator.getType())
        {
            case AND:
                element = OGC_OBJECT_FACTORY.createAnd(binaryLogicOp);
                break;
            case OR:
                element = OGC_OBJECT_FACTORY.createOr(binaryLogicOp);
                break;
            default:
                throw new IllegalArgumentException("Unexpected binary logical operator type: " + operator.getType());
        }
        return element;
    }

    /**
     * Transforms a common <code>UnaryLogicalOp</code> to an OGC
     * <code>UnaryLogicOpType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>UnaryLogicOpType</code>.
     */
    public JAXBElement<UnaryLogicOpType> transform(final UnaryLogicalOp operator)
    {
        Preconditions.checkArgument(operator != null, "The unary logical operator cannot be null");

        final JAXBElement<UnaryLogicOpType> element;
        if (operator instanceof LogicalNotOp)
        {
            element = transform((LogicalNotOp)operator);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Unexpected unary logical operator type: " + operator.getClass().getCanonicalName());
        }
        return element;
    }

    /**
     * Transforms a common <code>LogicalNotOp</code> to an OGC
     * <code>UnaryLogicOpType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>UnaryLogicOpType</code>.
     */
    public JAXBElement<UnaryLogicOpType> transform(final LogicalNotOp operator)
    {
        Preconditions.checkArgument(operator != null, "The logical NOT operator cannot be null");
        Preconditions.checkArgument(operator.getOperators().size() == 1,
                "The logical NOT operator must contain exact 1 sub-operator");

        // Transform the sub-operator and set it in the OGC operator.
        final UnaryLogicOpType unaryLogicOp = new UnaryLogicOpType();
        final Operator subOperator = operator.getOperators().get(0);
        if (subOperator instanceof ComparisonOp)
        {
            final JAXBElement<? extends ComparisonOpsType> subElement = transform((ComparisonOp)subOperator);
            unaryLogicOp.setComparisonOps(subElement);
        }
        else if (subOperator instanceof LogicalOp)
        {
            final JAXBElement<? extends LogicOpsType> subElement = transform((LogicalOp)subOperator);
            unaryLogicOp.setLogicOps(subElement);
        }
        else if (subOperator instanceof SpatialOp)
        {
            final JAXBElement<? extends SpatialOpsType> subElement = transform((SpatialOp)subOperator);
            unaryLogicOp.setSpatialOps(subElement);
        }
        else
        {
            throw new IllegalArgumentException("Unexpected sub-operator type: " + subOperator.getClass().getCanonicalName());
        }

        return OGC_OBJECT_FACTORY.createNot(unaryLogicOp);
    }

    /**
     * Transforms a common <code>SpatialOp</code> to an OGC
     * <code>SpatialOpsType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>SpatialOpsType</code>.
     */
    public JAXBElement<? extends SpatialOpsType> transform(final SpatialOp operator)
    {
        Preconditions.checkArgument(operator != null, "The spatial operator cannot be null");

        final JAXBElement<? extends SpatialOpsType> element;
        switch (operator.getType())
        {
            case BEYOND:
            case DWITHIN:
                element = createDistanceBuffer(operator);
                break;
            default:
                element = createBinarySpatialOp(operator);
        }
        return element;
    }

    /**
     * Transforms a common <code>SpatialOp</code> to an OGC
     * <code>BinarySpatialOpType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>BinarySpatialOpType</code>.
     */
    protected JAXBElement<BinarySpatialOpType> createBinarySpatialOp(final SpatialOp operator)
    {
        Preconditions.checkArgument(operator != null, "The spatial operator cannot be null");

        final PropertyNameType propertyName = new PropertyNameType();
        propertyName.setValue(operator.getName());

        final AbstractGeometryType gmlGeometry = jtsToGmlConverter.transform(operator.getGeometry());
        final JAXBElement<? extends AbstractGeometryType> geometryElement = OGC_OBJECT_FACTORY.createGeometry(gmlGeometry);

        final BinarySpatialOpType binarySpatialOp = new BinarySpatialOpType();
        binarySpatialOp.setGeometry(geometryElement);
        binarySpatialOp.setPropertyName(propertyName);

        final JAXBElement<BinarySpatialOpType> element;
        switch (operator.getType())
        {
            case CONTAINS:
                element = OGC_OBJECT_FACTORY.createContains(binarySpatialOp);
                break;
            case CROSSES:
                element = OGC_OBJECT_FACTORY.createCrosses(binarySpatialOp);
                break;
            case DISJOINT:
                element = OGC_OBJECT_FACTORY.createDisjoint(binarySpatialOp);
                break;
            case EQUALS:
                element = OGC_OBJECT_FACTORY.createEquals(binarySpatialOp);
                break;
            case INTERSECTS:
                element = OGC_OBJECT_FACTORY.createIntersects(binarySpatialOp);
                break;
            case OVERLAPS:
                element = OGC_OBJECT_FACTORY.createOverlaps(binarySpatialOp);
                break;
            case TOUCHES:
                element = OGC_OBJECT_FACTORY.createTouches(binarySpatialOp);
                break;
            case WITHIN:
                element = OGC_OBJECT_FACTORY.createWithin(binarySpatialOp);
                break;
            default:
                throw new IllegalArgumentException("Unexpected spatial operation type: " + operator.getType());
        }
        return element;
    }

    /**
     * Transforms a common <code>SpatialOp</code> to an OGC
     * <code>DistanceBufferType</code>.
     *
     * @param operator the operator to transform.
     * @return the OGC <code>DistanceBufferType</code>.
     * @throws IllegalArgumentException if the spatial operator does not have a
     *             type of either {@link SpatialOperatorType#BEYOND} or
     *             {@link SpatialOperatorType#DWITHIN}.
     */
    protected JAXBElement<DistanceBufferType> createDistanceBuffer(final SpatialOp operator)
    {
        Preconditions.checkArgument(operator.getDistance() != null, "The distance cannot be null");
        final PropertyNameType propertyName = new PropertyNameType();
        propertyName.setValue(operator.getName());

        final AbstractGeometryType gmlGeometry = jtsToGmlConverter.transform(operator.getGeometry());
        final JAXBElement<? extends AbstractGeometryType> geometryElement = OGC_OBJECT_FACTORY.createGeometry(gmlGeometry);

        final DistanceType distance = new DistanceType();
        distance.setValue(String.valueOf(operator.getDistance()));
        distance.setUnits(operator.getUnits());

        final DistanceBufferType distanceBuffer = new DistanceBufferType();
        distanceBuffer.setGeometry(geometryElement);
        distanceBuffer.setPropertyName(propertyName);
        distanceBuffer.setDistance(distance);

        final JAXBElement<DistanceBufferType> element;
        switch (operator.getType())
        {
            case BEYOND:
                element = OGC_OBJECT_FACTORY.createBeyond(distanceBuffer);
                break;
            case DWITHIN:
                element = OGC_OBJECT_FACTORY.createDWithin(distanceBuffer);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unexpected spatial operation type: " + operator.getType() + ". Must be either BEYOND or DWITHIN.");
        }
        return element;
    }

    /**
     * Transforms a common <code>Expression</code> to an OGC
     * <code>ExpressionType</code>.
     *
     * @param expression the expression to transform.
     * @return the OGC <code>ExpressionType</code> or <code>null</code> if the
     *         given expression is <code>null</code>..
     */
    public JAXBElement<?> transform(final Expression expression)
    {
        JAXBElement<?> element = null;
        if (expression != null)
        {
            if (expression instanceof BinaryOperationEx)
            {
                element = transform((BinaryOperationEx)expression);
            }
            else if (expression instanceof FunctionEx)
            {
                element = transform((FunctionEx)expression);
            }
            else if (expression instanceof LiteralEx)
            {
                element = transform((LiteralEx)expression);
            }
            else if (expression instanceof PropertyNameEx)
            {
                element = transform((PropertyNameEx)expression);
            }
            else
            {
                throw new IllegalArgumentException("Unexpected expression type: " + expression.getClass().getCanonicalName());
            }
        }
        return element;
    }

    /**
     * Transforms a common <code>BinaryOperationEx</code> to an OGC
     * <code>BinaryOperatorType</code>.
     *
     * @param expression the expression to transform.
     * @return the OGC <code>BinaryOperatorType</code>.
     */
    public JAXBElement<BinaryOperatorType> transform(final BinaryOperationEx expression)
    {
        Preconditions.checkArgument(expression != null, "The binary operation expression cannot be null");
        final BinaryOperatorType ogcExpression = new BinaryOperatorType();
        ogcExpression.getExpression().add(transform(expression.getLeftExpression()));
        ogcExpression.getExpression().add(transform(expression.getRightExpression()));

        final JAXBElement<BinaryOperatorType> element;
        switch (expression.getType())
        {
            case ADD:
                element = OGC_OBJECT_FACTORY.createAdd(ogcExpression);
                break;
            case DIVIDE:
                element = OGC_OBJECT_FACTORY.createDiv(ogcExpression);
                break;
            case MULTIPLY:
                element = OGC_OBJECT_FACTORY.createMul(ogcExpression);
                break;
            case SUBTRACT:
                element = OGC_OBJECT_FACTORY.createSub(ogcExpression);
                break;
            default:
                throw new IllegalArgumentException("Unexpected operation type: " + expression.getType());
        }
        return element;
    }

    /**
     * Transforms a common <code>FunctionEx</code> to an OGC
     * <code>FunctionType</code>.
     *
     * @param expression the expression to transform.
     * @return the OGC <code>FunctionType</code>.
     */
    public JAXBElement<FunctionType> transform(final FunctionEx expression)
    {
        Preconditions.checkArgument(expression != null, "The function expression cannot be null");
        final FunctionType ogcFunction = new FunctionType();
        ogcFunction.setName(expression.getName());
        for (final Expression argument : expression.getArguments())
        {
            ogcFunction.getExpression().add(transform(argument));
        }
        return OGC_OBJECT_FACTORY.createFunction(ogcFunction);
    }

    /**
     * Transforms a common <code>LiteralEx</code> to an OGC
     * <code>LiteralType</code>.
     *
     * @param expression the expression to transform.
     * @return the OGC <code>LiteralType</code>.
     */
    public JAXBElement<LiteralType> transform(final LiteralEx expression)
    {
        Preconditions.checkArgument(expression != null, "The literal expression cannot be null");
        final LiteralType ogcLiteral = new LiteralType();
        ogcLiteral.getContent().add(expression.getValue());
        return OGC_OBJECT_FACTORY.createLiteral(ogcLiteral);
    }

    /**
     * Transforms a common <code>PropertyNameEx</code> to an OGC
     * <code>PropertyNameType</code>.
     *
     * @param expression the expression to transform.
     * @return the OGC <code>PropertyNameType</code>.
     */
    public JAXBElement<PropertyNameType> transform(final PropertyNameEx expression)
    {
        Preconditions.checkArgument(expression != null, "The property name expression cannot be null");
        final PropertyNameType ogcPropertyName = new PropertyNameType();
        ogcPropertyName.setValue(expression.getName());
        return OGC_OBJECT_FACTORY.createPropertyName(ogcPropertyName);
    }
}
