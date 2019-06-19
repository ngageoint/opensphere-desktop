package io.opensphere.mantle.data.cache.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ByteString;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.impl.AbstractDynamicMetaDataList;
import io.opensphere.mantle.data.impl.encoder.EncodeType;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.impl.DynamicEnumerationCombinedIntKey;
import io.opensphere.mantle.util.dynenum.impl.DynamicEnumerationCombinedLongKey;
import io.opensphere.mantle.util.dynenum.util.ColumnAndValueIntKeyUtility;
import io.opensphere.mantle.util.dynenum.util.DynamicEnumerationIntKeyUtility;

/**
 * The Class DynamicMetaDataListCodeGenerator.
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.GodClass" })
public class DynamicMetaDataListCodeGenerator
{
    /**
     * A constant in which a single indentation is defined.
     */
    private static final String SINGLE_INDENT = "  ";

    /**
     * A constant in which an close curly bracket and a newline are defined for reuse.
     */
    private static final String CLOSE_BRACKET_NEWLINE = "}\n";

    /**
     * A constant in which an open curly bracket and a newline are defined for reuse.
     */
    private static final String OPEN_BRACKET_NEWLINE = "{\n";

    /** The Constant OBJECT_DECLERATOR. */
    public static final String OBJECT_DECLERATOR = "Object";

    /** The Constant PACKAGE. */
    public static final String PACKAGE = "io.opensphere.mantle.data.dynamic";

    /** The my class name. */
    private final String myClassName;

    /** The my data type info. */
    private final DataTypeInfo myDataTypeInfo;

    /** The my field name list. */
    private final List<String> myFieldNameList;

    /** The my field name to class map. */
    private final Map<String, Class<?>> myFieldNameToClassMap;

    /** The my field name to field declerator map. */
    private final Map<String, DecleratorType> myFieldNameToFieldDecleratorMap;

    /** The my field name to key name map. */
    private final Map<String, String> myFieldNameToKeyNameMap;

    /** The my full class name. */
    private final String myFullClassName;

    /** The my hash code. */
    private final int myHashCode;

    /** The my mdi. */
    private final MetaDataInfo myMDI;

    /** The my num keys. */
    private final int myNumKeys;

    /** The my version. */
    private final int myVersion;

    /**
     * Instantiates a new dynamic meta data list code generator.
     *
     * @param className the class name
     * @param version the version
     * @param dti the dti
     * @param hashCode the hash code
     */
    public DynamicMetaDataListCodeGenerator(String className, int version, DataTypeInfo dti, int hashCode)
    {
        Utilities.checkNull(className, "className");
        Utilities.checkNull(dti, "dti");
        myHashCode = hashCode;
        myVersion = version;
        myDataTypeInfo = dti;
        myClassName = className + "_v" + Integer.toString(version);
        myFullClassName = PACKAGE + "." + myClassName;
        myMDI = dti.getMetaDataInfo();
        if (myMDI.getKeyNames().isEmpty())
        {
            throw new IllegalArgumentException("A class cannot be generated for a data type which has no keys.");
        }
        myFieldNameToKeyNameMap = new HashMap<>();
        myFieldNameToClassMap = new HashMap<>();
        myFieldNameToFieldDecleratorMap = new HashMap<>();
        myFieldNameList = new ArrayList<>();

        List<String> keyNameList = myMDI.getKeyNames();
        myNumKeys = keyNameList.size();
        Map<String, Class<?>> keyToClassMap = myMDI.getKeyClassTypeMap();
        Class<?> cl = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            String keyName = keyNameList.get(i);
            cl = keyToClassMap.get(keyName);
            String prefix = getFieldPrefixForClass(cl);

            String fieldName = prefix + Integer.toString(i);
            myFieldNameList.add(fieldName);
            myFieldNameToKeyNameMap.put(fieldName, keyName);
        }
    }

    //@formatter:off

    /**
     * Generate get keys function.
     *
     * @param sb the sb
     */
    public void generateGetKeysFunction(StringBuilder sb)
    {
        sb.append("  public final List<String> getKeys()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    return Collections.unmodifiableList(ourKeyList);\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate get value for key function.
     *
     * @param sb the sb
     */
    public void generateGetValueForKeyFunction(StringBuilder sb)
    {
        sb.append("  public final Object getValue(String key)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    Object result = null;\n");

        String fieldName = null;
        DecleratorType decl = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            fieldName = myFieldNameList.get(i);
            decl = myFieldNameToFieldDecleratorMap.get(fieldName);

            sb.append("    ").append(i == 0 ? "if" : "else if").append(" (ourKeys[").append(i)
                    .append("].equals(key))");
            if (decl == DecleratorType.DATE)
            {
                sb.append("{ result = ").append(fieldName).append(" == -1L ? null : new java.sql.Date(").append(fieldName)
                        .append("); }\n");
            }
            else if (decl == DecleratorType.DATE_ALT)
            {
                sb.append("{ result = ").append(fieldName).append(" == -1L ? null : new java.util.Date(").append(fieldName)
                        .append("); }\n");
            }
            else if (decl == DecleratorType.BYTE_STRING)
            {
                sb.append("{ result = ").append(fieldName).append(" == null ? null : new ByteString(").append(fieldName)
                        .append("); }\n");
            }
            else if (decl == DecleratorType.STRING)
            {
                sb.append("{ result = ").append(fieldName).append(" == null ? null : new String(")
                        .append(fieldName).append(", StringUtilities.DEFAULT_CHARSET); }\n");
            }
            else if (decl == DecleratorType.BYTE)
            {
                sb.append("{ result = ").append(fieldName).append(" == Short.MIN_VALUE ? null : (byte)").append(fieldName)
                        .append("; }\n");
            }
            else if (decl == DecleratorType.SHORT)
            {
                sb.append("{ result = ").append(fieldName).append(" == Short.MIN_VALUE ? null : ").append(fieldName)
                        .append("; }\n");
            }
            else if (decl == DecleratorType.DYNAMIC_ENUMERATION_KEY)
            {
                sb.append(OPEN_BRACKET_NEWLINE);
                sb.append("    short typeKey = ourDynamicEnumerationTypeKey;\n");
                sb.append("    short mdiKey = ColumnAndValueIntKeyUtility.extractMdkIdFromCombinedIntKey(").append(fieldName)
                        .append(");\n");
                sb.append("    short valKey = ColumnAndValueIntKeyUtility.extractValIdFromCombinedIntKey(").append(fieldName)
                        .append(");\n");
                sb.append("{ result = ");
                sb.append(fieldName);
                sb.append(" == 0 ? null : new DynamicEnumerationCombinedLongKey(typeKey,mdiKey,valKey); }\n");
                sb.append(CLOSE_BRACKET_NEWLINE);
            }
            else if (decl == DecleratorType.INTEGER)
            {
                sb.append("{ result = ").append(fieldName).append(" == Integer.MIN_VALUE ? null : ").append(fieldName)
                        .append("; }\n");
            }
            else if (decl == DecleratorType.LONG)
            {
                sb.append("{ result = ").append(fieldName).append(" == Long.MIN_VALUE ? null : ").append(fieldName)
                        .append("; }\n");
            }
            else if (decl == DecleratorType.FLOAT)
            {
                sb.append("{ result = Float.isNaN(").append(fieldName).append(") ? null : ").append(fieldName).append("; }\n");
            }
            else if (decl == DecleratorType.DOUBLE)
            {
                sb.append("{ result = Double.isNaN(").append(fieldName).append(") ? null : ").append(fieldName).append("; }\n");
            }
            else if (decl == DecleratorType.BOOLEAN)
            {
                sb.append("{ result = ").append(fieldName).append(" == -1 ? null : ").append(fieldName)
                        .append(" == 1 ? true : false; }\n");
            }
            else
            {
                sb.append("{ result = ").append(fieldName).append("; }\n");
            }
        }
        sb.append("    return result;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate get values function.
     *
     * @param sb the sb
     */
    public void generateGetValuesFunction(StringBuilder sb)
    {
        sb.append("  public final List<Object> getValues()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    return Arrays.asList(toArray());\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate has key function.
     *
     * @param sb the sb
     */
    public void generateHasKeyFunction(StringBuilder sb)
    {
        sb.append("  public final boolean hasKey(String key)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    return ourKeySet.contains(key);\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate source.
     *
     * @return the string
     */
    @SuppressWarnings("PMD.LooseCoupling")
    public String generateSource()
    {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("package ").append(PACKAGE).append(";\n");
        sb.append("import ").append(StringUtilities.class.getName()).append(";\n");
        sb.append("import ").append(ByteString.class.getName()).append(";\n");
        sb.append("import ").append(DynamicMetaDataList.class.getName()).append(";\n");
        sb.append("import ").append(AbstractDynamicMetaDataList.class.getName()).append(";\n");
        sb.append("import ").append(Pattern.class.getName()).append(";\n");
        sb.append("import ").append(Matcher.class.getName()).append(";\n");
        sb.append("import ").append(List.class.getName()).append(";\n");
        sb.append("import ").append(Arrays.class.getName()).append(";\n");
        sb.append("import ").append(Set.class.getName()).append(";\n");
        sb.append("import ").append(Date.class.getName()).append(";\n");
        sb.append("import ").append(HashSet.class.getName()).append(";\n");
        sb.append("import ").append(IOException.class.getName()).append(";\n");
        sb.append("import ").append(ObjectOutputStream.class.getName()).append(";\n");
        sb.append("import ").append(ObjectInputStream.class.getName()).append(";\n");
        sb.append("import ").append(Collections.class.getName()).append(";\n");
        sb.append("import ").append(Serializable.class.getName()).append(";\n");
        sb.append("import ").append(EncodeType.class.getName()).append(";\n");
        sb.append("import ").append(Utilities.class.getName()).append(";\n");
        sb.append("import ").append(MetaDataProvider.class.getName()).append(";\n");
        sb.append("import ").append(DynamicEnumerationKey.class.getName()).append(";\n");
        sb.append("import ").append(DynamicEnumerationCombinedIntKey.class.getName()).append(";\n");
        sb.append("import ").append(DynamicEnumerationCombinedLongKey.class.getName()).append(";\n");
        sb.append("import ").append(ColumnAndValueIntKeyUtility.class.getName()).append(";\n");
        sb.append("import ").append(DynamicEnumerationIntKeyUtility.class.getName()).append(";\n");
        sb.append("import ").append(org.apache.log4j.Logger.class.getName()).append(";\n");
        sb.append("public class ").append(myClassName).append(" extends ").append(AbstractDynamicMetaDataList.class.getSimpleName())
                .append("\n");
        sb.append(OPEN_BRACKET_NEWLINE);
        sb.append("  private static final Logger LOGGER = Logger.getLogger(").append(myClassName).append(".class);\n");
        sb.append("  private static final long serialVersionUID = 1L;\n");
        sb.append("  private static final int ourSize = ").append(Integer.toString(myNumKeys)).append(";\n");
        sb.append("  private static final String ourDataTypeInfoKey = \"")
                .append(myDataTypeInfo.getTypeKey().replace("\\", "\\\\")).append("\".intern();\n");
        sb.append("  private static final int ourVersion = ").append(myVersion).append(";\n");
        sb.append("  private static final int ourTypeHashCode = ").append(myHashCode).append(";\n");
        sb.append("  private static short ourDynamicEnumerationTypeKey;\n");
        sb.append("  private static final String[] ourKeys = {");

        Iterator<String> keyItr = myMDI.getKeyNames().iterator();
        while (keyItr.hasNext())
        {
            sb.append('\"').append(keyItr.next()).append('\"');
            if (keyItr.hasNext())
            {
                sb.append(',');
            }
        }
        sb.append("};\n");

        sb.append("  private static final List<String> ourKeyList = Arrays.asList(ourKeys);\n");
        sb.append("  private static final Set<String> ourKeySet = new HashSet<>(ourKeyList);\n");

        generateFieldDeclarations(sb);

        sb.append("  public ").append(myClassName).append("(){ super(); }\n\n");
        generateGetVersionFunction(sb);
        sb.append('\n');
        generateGetTypeHashCodeFunction(sb);
        sb.append('\n');
        generateDataTypeInfoKeyFunction(sb);
        sb.append('\n');
        generateGetValueFunction(sb);
        sb.append('\n');
        generateHasKeyFunction(sb);
        sb.append('\n');
        generateGetKeysFunction(sb);
        sb.append('\n');
        generateGetValuesFunction(sb);
        sb.append('\n');
        generateGetValueForKeyFunction(sb);
        sb.append('\n');
        generateSetValueFunction(sb);
        sb.append('\n');
        generateGetClassFunction(sb);
        sb.append('\n');
        generateSizeFunction(sb);
        sb.append('\n');
        generateToArrayFunction(sb);
        sb.append('\n');
        generateSetFunction(sb);
        sb.append('\n');
        generateEncodeFunction(sb);
        sb.append('\n');
        generateDecodeFunction(sb);
        sb.append('\n');
        generateEqualsFunction(sb);
        sb.append('\n');
        generateHashCodeFunction(sb);
        sb.append('\n');
        generateSetEqualToMethod(sb);
        sb.append('\n');
        generateNewInstanceMethod(sb);
        sb.append('\n');
        generateToStringFunction(sb);
        sb.append('\n');
        generateGetLongPortion(sb);
        sb.append('\n');
        generateGetDoublePortion(sb);
        sb.append(CLOSE_BRACKET_NEWLINE);
        return sb.toString();
    }

    /**
     * Gets the field prefix for class.
     *
     * @param fieldClass the field class
     * @return the field prefix for class
     */
    public final String getFieldPrefixForClass(Class<?> fieldClass)
    {
        String prefix;
        if (Double.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fDouble";
        }
        else if (Float.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fFloat";
        }
        else if (Byte.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fByte";
        }
        else if (Integer.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fInteger";
        }
        else if (Short.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fShort";
        }
        else if (Long.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fLong";
        }
        else if (Date.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fDate";
        }
        else if (Boolean.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fBbolean";
        }
        else if (String.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fString";
        }
        else if (DynamicEnumerationKey.class.getName().equals(fieldClass.getName())
                || DynamicEnumerationCombinedIntKey.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fDynamicEnum";
        }
        else if (ByteString.class.getName().equals(fieldClass.getName()))
        {
            prefix = "fByteString";
        }
        else
        {
            prefix = "fObject";
        }
        return prefix;
    }

    /**
     * Gets the fully qualified class name.
     *
     * @return the fully qualified class name
     */
    public String getFullyQualifiedClassName()
    {
        return myFullClassName;
    }

    /**
     * Generate data type info key function.
     *
     * @param sb the sb
     */
    private void generateDataTypeInfoKeyFunction(StringBuilder sb)
    {
        sb.append("  @Override\n");
        sb.append("  public String getDataTypeInfoKey()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    return ourDataTypeInfoKey;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate decode function.
     *
     * @param sb the sb
     */
    private void generateDecodeFunction(StringBuilder sb)
    {
        sb.append("  @Override\n");
        sb.append("  public void decode(ObjectInputStream ois) throws IOException\n");
        sb.append(SINGLE_INDENT).append("{\n");
        sb.append("    EncodeType et = null;\n");
        String fieldName = null;
        DecleratorType decl = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            fieldName = myFieldNameList.get(i);
            decl = myFieldNameToFieldDecleratorMap.get(fieldName);

            switch (decl)
            {
                case BOOLEAN:
                    sb.append("    ").append(fieldName).append(" = ois.readByte();\n");
                    break;
                case DOUBLE:
                    sb.append("    ").append(fieldName).append(" = ois.readDouble();\n");
                    break;
                case FLOAT:
                    sb.append("    ").append(fieldName).append(" = ois.readFloat();\n");
                    break;
                case BYTE:
                    sb.append("    ").append(fieldName).append(" = ois.readShort();\n");
                    break;
                case SHORT:
                    sb.append("    ").append(fieldName).append(" = ois.readShort();\n");
                    break;
                case INTEGER:
                case DYNAMIC_ENUMERATION_KEY:
                    sb.append("    ").append(fieldName).append(" = ois.readInt();\n");
                    break;
                case DATE:
                case DATE_ALT:
                case LONG:
                    sb.append("    ").append(fieldName).append(" = ois.readLong();\n");
                    break;
                case OBJECT:
                    sb.append("    et = EncodeType.decode(ois);\n");
                    sb.append("    ").append(fieldName).append(" = null;\n");
                    sb.append("    if( !et.isNullType() )\n");
                    sb.append("    {\n");
                    sb.append("      try\n");
                    sb.append("      {\n");
                    sb.append("        ").append(fieldName).append(" = ois.readObject();\n");
                    sb.append("      }\n");
                    sb.append("      catch (ClassNotFoundException e)\n");
                    sb.append("      {\n");
                    sb.append("        throw new IOException(\"Unknown class found while decoding ").append(myClassName)
                            .append(" metadata field [").append(myFieldNameToKeyNameMap.get(fieldName)).append("].\", e);\n");
                    sb.append("      }\n");
                    sb.append("    }\n");
                    break;
                case STRING:
                case BYTE_STRING:
                    sb.append("    et = EncodeType.decode(ois);\n");
                    sb.append("    ").append(fieldName).append(" = null;\n");
                    sb.append("    if( !et.isNullType() )\n");
                    sb.append("    {\n");
                    sb.append("       short length = ois.readShort();\n");
                    sb.append("       ").append(fieldName).append(" = new byte[length];\n");
                    sb.append("       ois.read(").append(fieldName).append(");\n");
                    sb.append("    }\n");
                    break;
                default:
                    break;
            }
        }
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate encode function.
     *
     * @param sb the sb
     */
    private void generateEncodeFunction(StringBuilder sb)
    {
        sb.append("  @Override\n");
        sb.append("  public void encode(ObjectOutputStream oos) throws IOException\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);

        String fieldName = null;
        DecleratorType decl = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            fieldName = myFieldNameList.get(i);
            decl = myFieldNameToFieldDecleratorMap.get(fieldName);

            switch (decl)
            {
                case BOOLEAN:
                    sb.append("    oos.writeByte(").append(fieldName).append(");\n");
                    break;
                case DOUBLE:
                    sb.append("    oos.writeDouble(").append(fieldName).append(");\n");
                    break;
                case FLOAT:
                    sb.append("    oos.writeFloat(").append(fieldName).append(");\n");
                    break;
                case BYTE:
                    sb.append("    oos.writeShort(").append(fieldName).append(");\n");
                    break;
                case SHORT:
                    sb.append("    oos.writeShort(").append(fieldName).append(");\n");
                    break;
                case INTEGER:
                case DYNAMIC_ENUMERATION_KEY:
                    sb.append("    oos.writeInt(").append(fieldName).append(");\n");
                    break;
                case DATE:
                case DATE_ALT:
                case LONG:
                    sb.append("    oos.writeLong(").append(fieldName).append(");\n");
                    break;
                case OBJECT:
                    sb.append("    if( ").append(fieldName).append(" == null )\n");
                    sb.append("    {\n");
                    sb.append("      EncodeType.NULL.encode(oos);\n");
                    sb.append("    }\n");
                    sb.append("    else\n");
                    sb.append("    {\n");
                    sb.append("      EncodeType.OBJECT.encode(oos);\n");
                    sb.append("      oos.writeObject(").append(fieldName).append(");\n");
                    sb.append("    }\n");
                    break;
                case STRING:
                case BYTE_STRING:
                    sb.append("    if( ").append(fieldName).append(" == null )\n");
                    sb.append("    {\n");
                    sb.append("      EncodeType.NULL.encode(oos);\n");
                    sb.append("    }\n");
                    sb.append("    else\n");
                    sb.append("    {\n");
                    sb.append("      EncodeType.")
                            .append(decl == DecleratorType.STRING ? "STRING" : "BYTE_STRING").append(".encode(oos);\n");
                    sb.append("      oos.writeShort(").append(fieldName).append(".length);\n");
                    sb.append("      oos.write(").append(fieldName).append(");\n");
                    sb.append("    }\n");
                    break;
                default:
                    break;
            }
        }

        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate equals function.
     *
     * @param sb the sb
     */
    private void generateEqualsFunction(StringBuilder sb)
    {
        sb.append("  public boolean equals(Object obj)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    if (this == obj)\n");
        sb.append("      return true;\n");
        sb.append("    if (obj == null)\n");
        sb.append("      return false;\n");
        sb.append("    if (getClass() != obj.getClass())\n");
        sb.append("      return false;\n");
        sb.append("    ").append(myClassName).append(" other = (").append(myClassName).append(")obj;\n");

        String fieldName = null;
        DecleratorType decl = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            fieldName = myFieldNameList.get(i);
            decl = myFieldNameToFieldDecleratorMap.get(fieldName);
            if (decl == DecleratorType.OBJECT)
            {
                sb.append("    if (").append(fieldName).append(" == null)\n");
                sb.append("    {\n");
                sb.append("      if (other.").append(fieldName).append(" != null)\n");
                sb.append("      return false;\n");
                sb.append("    }\n");
                sb.append("    else if (!").append(fieldName).append(".equals(other.").append(fieldName).append("))\n");
                sb.append("        return false;\n");
            }
            else if (decl == DecleratorType.STRING || decl == DecleratorType.BYTE_STRING)
            {
                sb.append("    if (!Arrays.equals(").append(fieldName).append(", other.").append(fieldName).append("))\n");
                sb.append("    {\n");
                sb.append("        return false;\n");
                sb.append("    }\n");
            }
            else if (decl == DecleratorType.FLOAT)
            {
                sb.append("    if (Float.floatToIntBits(").append(fieldName).append(") != Float.floatToIntBits(other.")
                        .append(fieldName).append("))\n");
                sb.append("      return false;\n");
            }
            else if (decl == DecleratorType.DOUBLE)
            {
                sb.append("    if (Double.doubleToLongBits(").append(fieldName).append(") != Double.doubleToLongBits(other.")
                        .append(fieldName).append("))\n");
                sb.append("       return false;\n");
            }
            else
            {
                sb.append("    if (").append(fieldName).append(" != other.").append(fieldName).append(")\n");
                sb.append("        return false;\n");
            }
        }

        sb.append("     return true;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate field declarations.
     *
     * @param sb the sb
     */
    private void generateFieldDeclarations(StringBuilder sb)
    {
        Map<String, Class<?>> map = myMDI.getKeyClassTypeMap();
        String keyName;
        String fieldName;
        String defaultValue = null;
        for (int i = 0; i < myFieldNameList.size(); i++)
        {
            defaultValue = null;
            fieldName = myFieldNameList.get(i);
            keyName = myFieldNameToKeyNameMap.get(fieldName);
            Class<?> fieldClass = map.get(keyName);
            Class<?> classMapEntry = fieldClass;

            DecleratorType declerator = DecleratorType.OBJECT;
            if (Double.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.DOUBLE;
                defaultValue = "Double.NaN";
            }
            else if (Float.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.FLOAT;
                defaultValue = "Float.NaN";
            }
            else if (Byte.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.BYTE;
                defaultValue = "Short.MIN_VALUE";
            }
            else if (Integer.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.INTEGER;
                defaultValue = "Integer.MIN_VALUE";
            }
            else if (Short.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.SHORT;
                defaultValue = "Short.MIN_VALUE";
            }
            else if (Long.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.LONG;
                defaultValue = "Long.MIN_VALUE";
            }
            else if (java.sql.Date.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.DATE;
                defaultValue = "-1L";
            }
            else if (Date.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.DATE_ALT;
                defaultValue = "-1L";
            }
            else if (Boolean.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.BOOLEAN;
                defaultValue = "(byte)-1";
            }
            else if (String.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.STRING;
            }
            else if (ByteString.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.BYTE_STRING;
            }
            else if (DynamicEnumerationKey.class.getName().equals(fieldClass.getName())
                    || DynamicEnumerationCombinedLongKey.class.getName().equals(fieldClass.getName())
                    || DynamicEnumerationCombinedIntKey.class.getName().equals(fieldClass.getName()))
            {
                declerator = DecleratorType.DYNAMIC_ENUMERATION_KEY;
                defaultValue = "0";
            }
            else
            {
                classMapEntry = Object.class;
            }

            myFieldNameToClassMap.put(fieldName, classMapEntry);
            myFieldNameToFieldDecleratorMap.put(fieldName, declerator);
            if (defaultValue == null)
            {
                sb.append(SINGLE_INDENT).append(declerator.getDeclerator()).append(' ').append(fieldName).append(";\n");
            }
            else
            {
                sb.append(SINGLE_INDENT).append(declerator.getDeclerator()).append(' ').append(fieldName).append(" = ").append(defaultValue).append(";\n");
            }
        }
    }

    /**
     * Generate get class function.
     *
     * @param sb the sb
     */
    private void generateGetClassFunction(StringBuilder sb)
    {
        sb.append("  public final Class<?> getClass(int index)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    checkIndexForOutOfBounds(index);\n");
        for (int i = 0; i < myNumKeys; i++)
        {
            Class<?> cl = myFieldNameToClassMap.get(myFieldNameList.get(i));
            sb.append(i == 0 ? "    if" : "    else if").append(" (index == ").append(i).append(")");
            sb.append("{ return ").append(cl.getSimpleName()).append(".class; }\n");
        }
        sb.append("    else{ return null; }\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate the method which uses a regular express to search for a floating
     * point number imbedded within the string.
     *
     * @param sb The builder to which to add the generated method.
     */
    private void generateGetDoublePortion(StringBuilder sb)
    {
        sb.append("  public double getDoublePortion(String candidate)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    Pattern pat = Pattern.compile(\"[+-]?[0-9]*\\\\.*[0-9]*[eE]?[0-9]*\");\n");
        sb.append("    Matcher mat = pat.matcher(candidate);\n");
        sb.append("    if (mat.find() && mat.end() > 0)\n");
        sb.append("    {\n");
        sb.append("      return Double.parseDouble(candidate.substring(mat.start(), mat.end()));\n");
        sb.append("    }\n");
        sb.append("    return 0.;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate the method which uses a regular express to search for a integer
     * imbedded within the string.
     *
     * @param sb The builder to which to add the generated method.
     */
    private void generateGetLongPortion(StringBuilder sb)
    {
        sb.append("  public long getLongPortion(String candidate)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    Pattern pat = Pattern.compile(\"[+-]?[0-9]*\");\n");
        sb.append("    Matcher mat = pat.matcher(candidate);\n");
        sb.append("    if (mat.find() && mat.end() > 0)\n");
        sb.append("    {\n");
        sb.append("      return Long.parseLong(candidate.substring(mat.start(), mat.end()));\n");
        sb.append("    }\n");
        sb.append("    return 0L;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate get version function.
     *
     * @param sb the sb
     */
    private void generateGetTypeHashCodeFunction(StringBuilder sb)
    {
        sb.append("  @Override\n");
        sb.append("  public int getTypeHashCode()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    return ourTypeHashCode;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate get value function.
     *
     * @param sb the sb
     */
    private void generateGetValueFunction(StringBuilder sb)
    {
        sb.append("  public Object get(int index)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    checkIndexForOutOfBounds(index);\n");
        DecleratorType decl = null;
        String fieldName = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            fieldName = myFieldNameList.get(i);
            decl = myFieldNameToFieldDecleratorMap.get(fieldName);
            sb.append(i == 0 ? "    if" : "    else if").append(" (index == ").append(i).append(')');

            if (decl == DecleratorType.DATE)
            {
                sb.append("{  return ").append(fieldName).append(" == -1L ? null : new java.sql.Date(").append(fieldName)
                        .append("); }\n");
            }
            else if (decl == DecleratorType.DATE_ALT)
            {
                sb.append("{  return ").append(fieldName).append(" == -1L ? null : new java.util.Date(").append(fieldName)
                        .append("); }\n");
            }
            else if (decl == DecleratorType.BYTE_STRING)
            {
                sb.append("{  return ").append(fieldName).append(" == null ? null : new ByteString(").append(fieldName)
                        .append("); }\n");
            }
            else if (decl == DecleratorType.DYNAMIC_ENUMERATION_KEY)
            {
                sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
                sb.append("    short typeKey = ourDynamicEnumerationTypeKey;\n");
                sb.append("    short mdiKey = ColumnAndValueIntKeyUtility.extractMdkIdFromCombinedIntKey(");
                sb.append(fieldName);
                sb.append(");\n");
                sb.append("    short valKey = ColumnAndValueIntKeyUtility.extractValIdFromCombinedIntKey(");
                sb.append(fieldName);
                sb.append(");\n");
                sb.append("    return ");
                sb.append(fieldName);
                sb.append(" == 0 ? null : new DynamicEnumerationCombinedLongKey(typeKey,mdiKey,valKey);\n");
                sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
            }
            else if (decl == DecleratorType.STRING)
            {
                sb.append("{  return ").append(fieldName).append(" == null ? null : new String(")
                        .append(fieldName).append(", StringUtilities.DEFAULT_CHARSET); }\n");
            }
            else if (decl == DecleratorType.BYTE)
            {
                sb.append("{  return ").append(fieldName).append(" == Short.MIN_VALUE ? null : (byte)").append(fieldName)
                        .append("; }\n");
            }
            else if (decl == DecleratorType.SHORT)
            {
                sb.append("{  return ").append(fieldName).append(" == Short.MIN_VALUE ? null : ").append(fieldName)
                        .append("; }\n");
            }
            else if (decl == DecleratorType.INTEGER)
            {
                sb.append("{  return ").append(fieldName).append(" == Integer.MIN_VALUE ? null : ").append(fieldName)
                        .append("; }\n");
            }
            else if (decl == DecleratorType.LONG)
            {
                sb.append("{  return ").append(fieldName).append(" == Long.MIN_VALUE ? null : ").append(fieldName)
                        .append("; }\n");
            }
            else if (decl == DecleratorType.FLOAT)
            {
                sb.append("{  return Float.isNaN(").append(fieldName).append(") ? null : ").append(fieldName).append("; }\n");
            }
            else if (decl == DecleratorType.DOUBLE)
            {
                sb.append("{  return Double.isNaN(").append(fieldName).append(") ? null : ").append(fieldName).append("; }\n");
            }
            else if (decl == DecleratorType.BOOLEAN)
            {
                sb.append("{  return ").append(fieldName).append(" == -1 ? null : ").append(fieldName)
                        .append(" == 1 ? true : false; }\n");
            }
            else
            {
                sb.append("{ return ").append(fieldName).append("; }\n");
            }
        }
        sb.append("    else{ return null; }\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate get version function.
     *
     * @param sb the sb
     */
    private void generateGetVersionFunction(StringBuilder sb)
    {
        sb.append("  @Override\n");
        sb.append("  public int getVersion()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    return ourVersion;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate handle strings that are supposed to be numbers section.
     *
     * @param sb the sb
     */
    private void generateHandleStringsThatAreSupposedToBeNumbersSection(StringBuilder sb)
    {
        boolean foundFirstOfNumericTypeWithStringValue = false;
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.BYTE))
        {
            sb.append("          if (Byte.class.getName() == cl.getName())\n");
            sb.append("          {\n");
            genSetPortionForStringNumericType(sb, "            ", DecleratorType.BYTE, "(byte)getLongPortion((String)val)",
                    "Short.MIN_VALUE", "0");
            sb.append("          }\n");
            foundFirstOfNumericTypeWithStringValue = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.SHORT))
        {
            sb.append("          ").append(foundFirstOfNumericTypeWithStringValue ? "else " : "")
                    .append("if (Short.class.getName() == cl.getName())\n");
            sb.append("          {\n");
            genSetPortionForStringNumericType(sb, "            ", DecleratorType.SHORT, "(short)getLongPortion((String)val)",
                    "Short.MIN_VALUE", "0");
            sb.append("          }\n");
            foundFirstOfNumericTypeWithStringValue = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.INTEGER))
        {
            sb.append("          ").append(foundFirstOfNumericTypeWithStringValue ? "else " : "")
                    .append(" if (Integer.class.getName() == cl.getName())\n");
            sb.append("          {\n");
            genSetPortionForStringNumericType(sb, "            ", DecleratorType.INTEGER, "(int)getLongPortion((String)val)",
                    "Integer.MIN_VALUE", "0");
            sb.append("          }\n");
            foundFirstOfNumericTypeWithStringValue = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.LONG))
        {
            sb.append("          ").append(foundFirstOfNumericTypeWithStringValue ? "else " : "")
                    .append(" if (Long.class.getName() == cl.getName())\n");
            sb.append("          {\n");
            genSetPortionForStringNumericType(sb, "            ", DecleratorType.LONG, "getLongPortion((String)val)",
                    "Long.MIN_VALUE", "0");
            sb.append("          }\n");
            foundFirstOfNumericTypeWithStringValue = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.FLOAT))
        {
            sb.append("          ").append(foundFirstOfNumericTypeWithStringValue ? "else " : "")
                    .append(" if (Float.class.getName() == cl.getName())\n");
            sb.append("          {\n");
            genSetPortionForStringNumericType(sb, "            ", DecleratorType.FLOAT, "(float)getDoublePortion((String)val)",
                    "Float.NaN", "0.0f");
            sb.append("          }\n");
            foundFirstOfNumericTypeWithStringValue = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.DOUBLE))
        {
            sb.append("          ").append(foundFirstOfNumericTypeWithStringValue ? "else " : "")
                    .append(" if (Double.class.getName() == cl.getName())\n");
            sb.append("          {\n");
            genSetPortionForStringNumericType(sb, "            ", DecleratorType.DOUBLE, "getDoublePortion((String)val)",
                    "Double.NaN", "0.0");
            sb.append("          }\n");
            foundFirstOfNumericTypeWithStringValue = true;
        }
    }

    /**
     * Generate hash code function.
     *
     * @param sb the sb
     */
    private void generateHashCodeFunction(StringBuilder sb)
    {
        sb.append("  public int hashCode()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    final int prime = 31;\n");
        sb.append("    int result = 1;\n");
        sb.append("    long temp;\n");

        String fieldName = null;
        DecleratorType decl = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            fieldName = myFieldNameList.get(i);
            decl = myFieldNameToFieldDecleratorMap.get(fieldName);
            if (decl == DecleratorType.BOOLEAN || decl == DecleratorType.BYTE || decl == DecleratorType.INTEGER
                    || decl == DecleratorType.SHORT || decl == DecleratorType.DYNAMIC_ENUMERATION_KEY)
            {
                sb.append("    result = prime * result + ").append(fieldName).append(";\n");
            }
            else if (decl == DecleratorType.OBJECT)
            {
                sb.append("    result = prime * result + ((").append(fieldName).append(" == null) ? 0 : ").append(fieldName)
                        .append(".hashCode());\n");
            }
            else if (decl == DecleratorType.STRING || decl == DecleratorType.BYTE_STRING)
            {
                sb.append("    result = prime * result + Arrays.hashCode(").append(fieldName).append(");\n");
            }
            else if (decl == DecleratorType.DOUBLE)
            {
                sb.append("    temp = Double.doubleToLongBits(").append(fieldName).append(");\n");
                sb.append("    result = prime * result + (int)(temp ^ (temp >>> 32));\n");
            }
            else if (decl == DecleratorType.FLOAT)
            {
                sb.append("    result = prime * result + Float.floatToIntBits(").append(fieldName).append(");\n");
            }
            else if (decl == DecleratorType.LONG || decl == DecleratorType.DATE || decl == DecleratorType.DATE_ALT)
            {
                sb.append("    result = prime * result + (int)(").append(fieldName).append(" ^ (").append(fieldName)
                        .append(" >>> 32));\n");
            }
        }

        // ").append(fieldName).append("
        sb.append("    return result;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate new instance method.
     *
     * @param sb the sb
     */
    private void generateNewInstanceMethod(StringBuilder sb)
    {
        sb.append("  public DynamicMetaDataList newInstance()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    return new ").append(myClassName).append("();\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate set equal to method.
     *
     * @param sb the sb
     */
    private void generateSetEqualToMethod(StringBuilder sb)
    {
        sb.append("  @Override\n");
        sb.append("  public void setEqualTo(MetaDataProvider provider)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    Utilities.checkNull(provider, \"provider\");\n");
        sb.append("    if(getClass() != provider.getClass())\n");
        sb.append("    {\n");
        sb.append("      for (String key : getKeys())\n");
        sb.append("      {\n");
        sb.append("        setValue(key, (Serializable)provider.getValue(key));\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    else\n");
        sb.append("    {\n");
        sb.append("      ").append(myClassName).append(" other = (").append(myClassName).append(")provider;\n");
        String fieldName = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            fieldName = myFieldNameList.get(i);
            sb.append("      ").append(fieldName).append(" = other.").append(fieldName).append(";\n");
        }
        sb.append("    }\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate set function.
     *
     * @param sb the sb
     */
    private void generateSetFunction(StringBuilder sb)
    {
        sb.append("  public Object set(int index, Object val)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    Class<?> cl = getClass(index);\n");
        sb.append("    Object oldValue = get(index);\n");
        sb.append("    if (Number.class.isAssignableFrom(cl))\n");
        sb.append("    {\n");
        sb.append("      if (val != null && !Number.class.isAssignableFrom(val.getClass()))\n");
        sb.append("      {\n");
        sb.append("         if (String.class.getName() == val.getClass().getName())\n");
        sb.append("         {\n");
        generateHandleStringsThatAreSupposedToBeNumbersSection(sb);
        sb.append("         }\n");
        sb.append("         else\n");
        sb.append("         {\n");
        sb.append("           throw new IllegalArgumentException(\"Index \" + index + \" cannot be").append(
                " assigned to with a non numeric value. Used \" + val.getClass().getName());\n");
        sb.append("         }\n");
        sb.append("      }\n");
        sb.append("      else\n");
        sb.append("      {\n");
        sb.append("         Number nb = (Number)val;\n");

        boolean foundFirstType = false;
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.BYTE))
        {
            sb.append("         if (Byte.class.getName() == cl.getName())\n");
            sb.append("         {\n");
            genSetPortionForNonNullableType(sb, "           ", DecleratorType.BYTE, "nb.byteValue()", "Short.MIN_VALUE");
            sb.append("         }\n");
            foundFirstType = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.SHORT))
        {
            sb.append("         ").append(foundFirstType ? "else " : "").append("if (Short.class.getName() == cl.getName())\n");
            sb.append("         {\n");
            genSetPortionForNonNullableType(sb, "           ", DecleratorType.SHORT, "nb.shortValue()", "Short.MIN_VALUE");
            sb.append("         }\n");
            foundFirstType = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.INTEGER))
        {
            sb.append("         ").append(foundFirstType ? "else " : "")
                    .append(" if (Integer.class.getName() == cl.getName())\n");
            sb.append("         {\n");
            genSetPortionForNonNullableType(sb, "           ", DecleratorType.INTEGER, "nb.intValue()", "Integer.MIN_VALUE");
            sb.append("         }\n");
            foundFirstType = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.LONG))
        {
            sb.append("         ").append(foundFirstType ? "else " : "").append(" if (Long.class.getName() == cl.getName())\n");
            sb.append("         {\n");
            genSetPortionForNonNullableType(sb, "           ", DecleratorType.LONG, "nb.longValue()", "Long.MIN_VALUE");
            sb.append("         }\n");
            foundFirstType = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.FLOAT))
        {
            sb.append("         ").append(foundFirstType ? "else " : "").append(" if (Float.class.getName() == cl.getName())\n");
            sb.append("         {\n");
            genSetPortionForNonNullableType(sb, "           ", DecleratorType.FLOAT, "nb.floatValue()", "Float.NaN");
            sb.append("         }\n");
            foundFirstType = true;
        }
        if (myFieldNameToFieldDecleratorMap.containsValue(DecleratorType.DOUBLE))
        {
            sb.append("         ").append(foundFirstType ? "else " : "").append(" if (Double.class.getName() == cl.getName())\n");
            sb.append("         {\n");
            genSetPortionForNonNullableType(sb, "           ", DecleratorType.DOUBLE, "nb.doubleValue()", "Double.NaN");
            sb.append("         }\n");
            foundFirstType = true;
        }
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    else if (Boolean.class.getName() == cl.getName())\n");
        sb.append("    {\n");
        sb.append("      if (val != null && !Boolean.class.isAssignableFrom(val.getClass()))\n");
        sb.append("      {\n");
        sb.append("        throw new IllegalArgumentException(\"Index \" + index + \" cannot be assigned");
        sb.append(" to with a non boolean value. Used \" + val.getClass().getName());\n");
        sb.append("      }\n");
        sb.append("      else\n");
        sb.append("      {\n");
        sb.append("         byte bVal = val == null ? (byte)-1 : ((Boolean)val).booleanValue() ? (byte)1 : (byte)0;\n");
        genSetPortionForType(sb, "         ", DecleratorType.BOOLEAN, "bVal");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    else if (java.sql.Date.class.getName() == cl.getName())\n");
        sb.append("    {\n");
        sb.append("      if (val != null && !java.sql.Date.class.isAssignableFrom(val.getClass()))\n");
        sb.append("      {\n");
        sb.append("        throw new IllegalArgumentException(\"Index \" + index + \" cannot be assigned to with");
        sb.append(" a non date value. Used \" + val.getClass().getName());\n");
        sb.append("      }\n");
        sb.append("      else\n");
        sb.append("      {\n");
        sb.append("        long date = val == null ? -1L : ((java.sql.Date)val).getTime();\n");
        genSetPortionForType(sb, "        ", DecleratorType.DATE, "date");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    else if (java.util.Date.class.getName() == cl.getName())\n");
        sb.append("    {\n");
        sb.append("      if (val != null && !org.joda.time.DateTime.class.isAssignableFrom(val.getClass())"
                + " && !java.util.Date.class.isAssignableFrom(val.getClass()))\n");
        sb.append("      {\n");
        sb.append("        throw new IllegalArgumentException(\"Index \" + index + \" cannot be assigned to with");
        sb.append(" a non date value. Used \" + val.getClass().getName());\n");
        sb.append("      }\n");
        sb.append("      else\n");
        sb.append("      {\n");
        sb.append("        long date;\n");
        sb.append("        if (java.util.Date.class.isAssignableFrom(val.getClass()))\n");
        sb.append("        {\n");
        sb.append("          date = val == null ? -1L : ((java.util.Date)val).getTime();\n");
        sb.append("        }\n");
        sb.append("        else\n");
        sb.append("        {\n");
        sb.append("          date = val == null ? -1L : ((org.joda.time.DateTime)val).toDate().getTime();\n");
        sb.append("        }\n");
        genSetPortionForType(sb, "        ", DecleratorType.DATE_ALT, "date");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    else if (DynamicEnumerationKey.class.getName() == cl.getName())\n");
        sb.append("    {\n");
        sb.append("      Object tVal = val;\n");
        sb.append("      if( tVal == null || !DynamicEnumerationKey.class.isAssignableFrom(val.getClass()) )\n");
        sb.append("      {\n");
        sb.append("        String key = getKeys().get(index);\n");
        sb.append("        tVal = getDynamicEnumRegistry().addValue(ourDataTypeInfoKey, key, tVal);\n");
        sb.append("      }\n");
        sb.append("      int combinedId = 0;\n");
        sb.append("      if( tVal instanceof DynamicEnumerationKey )\n");
        sb.append("      {\n");
        sb.append("        DynamicEnumerationKey dek = (DynamicEnumerationKey)tVal;\n");
        sb.append("        ourDynamicEnumerationTypeKey = dek.getTypeId();\n");
        sb.append("        combinedId = ColumnAndValueIntKeyUtility.createCombinedIntKeyValue(dek.getMetaDataKeyId(), dek.getValueId());\n");
        sb.append("      }\n");
        genSetPortionForType(sb, "        ", DecleratorType.DYNAMIC_ENUMERATION_KEY, "combinedId");
        sb.append("    }\n");
        sb.append("    else if (String.class.getName() == cl.getName())\n");
        sb.append("    {\n");
        sb.append("      byte[] valToAssign = val == null ? null : val.toString().getBytes(StringUtilities.DEFAULT_CHARSET);\n");
        genSetPortionForType(sb, "      ", DecleratorType.STRING, "valToAssign");
        sb.append("    }\n");
        sb.append("    else if (ByteString.class.getName() == cl.getName())\n");
        sb.append("    {\n");
        sb.append("      byte[] valToAssign = val == null ? null");
        sb.append(" : val instanceof ByteString ? ((ByteString)val).getData() : ByteString.getBytes(val.toString());\n");
        genSetPortionForType(sb, "      ", DecleratorType.BYTE_STRING, "valToAssign");
        sb.append("    }\n");
        // Object generic
        sb.append("    else\n");
        sb.append("    {\n");
        genSetPortionForType(sb, "      ", DecleratorType.OBJECT, "val");
        sb.append("    }\n");
        sb.append("    return oldValue;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate set value function.
     *
     * @param sb the sb
     */
    private void generateSetValueFunction(StringBuilder sb)
    {
        sb.append("  @Override\n");
        sb.append("  public boolean setValue(String key, Serializable value)\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("     boolean setValue = false;\n");
        sb.append("     int index = ourKeyList.indexOf(key);\n");
        sb.append("     if( index != -1 )\n");
        sb.append("     {\n");
        sb.append("       try\n");
        sb.append("       {\n");
        sb.append("         set(index,value);\n");
        sb.append("       }\n");
        sb.append("       catch( IllegalArgumentException e)\n");
        sb.append("       {\n");
        sb.append("         setValue = false;\n");
        sb.append("       }\n");
        sb.append("     }\n");
        sb.append("     return setValue;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate size function.
     *
     * @param sb the sb
     */
    private void generateSizeFunction(StringBuilder sb)
    {
        sb.append("  public int size()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("     return ").append(myNumKeys).append(";\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate to array function.
     *
     * @param sb the sb
     */
    private void generateToArrayFunction(StringBuilder sb)
    {
        sb.append("  public Object[] toArray()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    Object[] result = new Object[size()];\n");
        DecleratorType decl = null;
        String fieldName = null;
        for (int i = 0; i < myNumKeys; i++)
        {
            fieldName = myFieldNameList.get(i);
            decl = myFieldNameToFieldDecleratorMap.get(fieldName);
            if (decl == DecleratorType.DATE)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(" == -1L ? null : new java.sql.Date(")
                        .append(fieldName).append(");\n");
            }
            else if (decl == DecleratorType.DATE_ALT)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(" == -1L ? null : new java.util.Date(")
                        .append(fieldName).append(");\n");
            }
            else if (decl == DecleratorType.BYTE_STRING)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(" == null ? null : new ByteString(")
                        .append(fieldName).append(");\n");
            }
            else if (decl == DecleratorType.STRING)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName)
                        .append(" == null ? null : new String(").append(fieldName).append(", StringUtilities.DEFAULT_CHARSET);\n");
            }
            else if (decl == DecleratorType.DYNAMIC_ENUMERATION_KEY)
            {
                sb.append("    {\n");
                sb.append("      short typeKey = ourDynamicEnumerationTypeKey;\n");
                sb.append("      short mdiKey = ColumnAndValueIntKeyUtility.extractMdkIdFromCombinedIntKey(").append(fieldName)
                        .append(");\n");
                sb.append("      short valKey = ColumnAndValueIntKeyUtility.extractValIdFromCombinedIntKey(").append(fieldName)
                        .append(");\n");
                sb.append("      result[").append(i).append("] = ").append(fieldName)
                        .append(" == 0 ? null : new DynamicEnumerationCombinedLongKey(typeKey,mdiKey,valKey);\n");
                sb.append("    }\n");
            }
            else if (decl == DecleratorType.BYTE)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(" == Short.MIN_VALUE ? null : (byte)")
                        .append(fieldName).append(";\n");
            }
            else if (decl == DecleratorType.SHORT)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(" == Short.MIN_VALUE ? null : ")
                        .append(fieldName).append(";\n");
            }
            else if (decl == DecleratorType.INTEGER)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(" == Integer.MIN_VALUE ? null : ")
                        .append(fieldName).append(";\n");
            }
            else if (decl == DecleratorType.LONG)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(" == Long.MIN_VALUE ? null : ")
                        .append(fieldName).append(";\n");
            }
            else if (decl == DecleratorType.FLOAT)
            {
                sb.append("    result[").append(i).append("] = Float.isNaN(").append(fieldName).append(") ? null : ")
                        .append(fieldName).append(";\n");
            }
            else if (decl == DecleratorType.DOUBLE)
            {
                sb.append("    result[").append(i).append("] = Double.isNaN(").append(fieldName).append(") ? null : ")
                        .append(fieldName).append(";\n");
            }
            else if (decl == DecleratorType.BOOLEAN)
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(" == -1 ? null : ").append(fieldName)
                        .append(" == 1 ? true : false;\n");
            }
            else
            {
                sb.append("    result[").append(i).append("] = ").append(fieldName).append(";\n");
            }
        }
        sb.append("    return result;\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Generate to string function.
     *
     * @param sb the sb
     */
    private void generateToStringFunction(StringBuilder sb)
    {
        sb.append("  @Override\n");
        sb.append("  public String toString()\n");
        sb.append(SINGLE_INDENT).append(OPEN_BRACKET_NEWLINE);
        sb.append("    StringBuilder sb = new StringBuilder();\n");
        sb.append("    sb.append(\"[\");\n");
        sb.append("    for( int i = 0; i < ourSize; i++ )\n");
        sb.append("    {\n");
        sb.append("       sb.append(\"{[\").append(i).append(\"]\\\"\");\n");
        sb.append("       sb.append(ourKeys[i]).append(\"\\\"\");\n");
        sb.append("       sb.append(\",\");\n");
        sb.append("       sb.append(getClass(i).getSimpleName());\n");
        sb.append("       sb.append(\",\");\n");
        sb.append("       sb.append(get(i));\n");
        sb.append("       sb.append(\"}\");\n");
        sb.append("       if ( i != ourSize-1 ){ sb.append(\",\\n\"); }\n");
        sb.append("    }\n");
        sb.append("    sb.append(\"]\");\n");
        sb.append("    return sb.toString();\n");
        sb.append(SINGLE_INDENT).append(CLOSE_BRACKET_NEWLINE);
    }

    /**
     * Gen set portion for numeric type.
     *
     * @param sb the sb
     * @param leader the leader
     * @param decleratorType the declerator type
     * @param getNumValueMethod the get num value method
     * @param nullValue the null value
     */
    private void genSetPortionForNonNullableType(StringBuilder sb, String leader, DecleratorType decleratorType,
            String getNumValueMethod, String nullValue)
    {
        String fieldName = null;
        boolean foundFirstOfType = false;
        for (int i = 0; i < myFieldNameList.size(); i++)
        {
            fieldName = myFieldNameList.get(i);
            if (myFieldNameToFieldDecleratorMap.get(fieldName) == decleratorType)
            {
                sb.append(leader).append(foundFirstOfType ? "else " : "").append("if ( index == ").append(i).append(")\n")
                        .append(leader).append(OPEN_BRACKET_NEWLINE)
                        .append(leader).append(SINGLE_INDENT).append(fieldName).append(" = val == null ? ").append(nullValue).append(" : ")
                        .append(getNumValueMethod).append(";\n")
                        .append(leader).append(CLOSE_BRACKET_NEWLINE);
                foundFirstOfType = true;
            }
        }
    }

    /**
     * Gen set portion for string numeric type.
     *
     * @param sb the sb
     * @param leader the leader
     * @param decleratorType the declerator type
     * @param getAsNumFromStringMethod the get as num from string method
     * @param cantParseValue the cant parse value
     * @param emptyStringValue the empty string value
     */
    private void genSetPortionForStringNumericType(StringBuilder sb, String leader, DecleratorType decleratorType,
            String getAsNumFromStringMethod, String cantParseValue, String emptyStringValue)
    {
        String fieldName = null;
        boolean foundFirstOfType = false;
        for (int i = 0; i < myFieldNameList.size(); i++)
        {
            fieldName = myFieldNameList.get(i);
            if (myFieldNameToFieldDecleratorMap.get(fieldName) == decleratorType)
            {
                sb.append(leader).append(foundFirstOfType ? "else " : "").append("if ( index == ").append(i).append(")\n");
                sb.append(leader).append(OPEN_BRACKET_NEWLINE);
                sb.append(leader).append("  if(\"\".equals(val))\n");
                sb.append(leader).append("  {\n");
                sb.append(leader).append("      ").append(fieldName).append(" = ").append(emptyStringValue).append(";\n");
                sb.append(leader).append("  }\n");
                sb.append(leader).append("  else\n");
                sb.append(leader).append("  {\n");
                sb.append(leader).append("    try\n");
                sb.append(leader).append("    {\n");
                sb.append(leader).append("      ").append(fieldName).append(" = ").append(getAsNumFromStringMethod).append(";\n");
                sb.append(leader).append("    }\n");
                sb.append(leader).append("    catch( NumberFormatException e)\n");
                sb.append(leader).append("    {\n");
                sb.append(leader).append("      LOGGER.warn(\"Non parsable value for ").append(decleratorType).append("\" ,e);\n");
                sb.append(leader).append("      ").append(fieldName).append(" = ").append(cantParseValue).append(";\n");
                sb.append(leader).append("    }\n");
                sb.append(leader).append("  }\n");
                sb.append(leader).append(CLOSE_BRACKET_NEWLINE);
                foundFirstOfType = true;
            }
        }
    }

    //@formatter:on

    /**
     * Gen set portion for numeric type.
     *
     * @param sb the sb
     * @param leader the leader
     * @param decleratorType the declerator type
     * @param getNumValueMethod the get num value method
     */
    private void genSetPortionForType(StringBuilder sb, String leader, DecleratorType decleratorType, String getNumValueMethod)
    {
        String fieldName = null;
        boolean foundFirstOfType = false;
        for (int i = 0; i < myFieldNameList.size(); i++)
        {
            fieldName = myFieldNameList.get(i);
            if (myFieldNameToFieldDecleratorMap.get(fieldName) == decleratorType)
            {
                sb.append(leader).append(foundFirstOfType ? "else " : "").append("if ( index == ").append(i).append(")" + "{ ")
                        .append(fieldName).append(" = ").append(getNumValueMethod).append("; }\n");
                foundFirstOfType = true;
            }
        }
    }

    /**
     * The Enum DecleratorType.
     */
    public enum DecleratorType
    {
        /** The BOOLEAN. */
        BOOLEAN,

        /** The BYTE. */
        BYTE,

        /** The BYTE string. */
        BYTE_STRING,

        /** The DATE. */
        DATE,

        /** The DATE_ALT. */
        DATE_ALT,

        /** The Types. */
        DOUBLE,

        /** DynamicEnumerationKey. */
        DYNAMIC_ENUMERATION_KEY,

        /** The FLOAT. */
        FLOAT,

        /** The INTEGER. */
        INTEGER,

        /** The LONG. */
        LONG,

        /** The OBJECT. */
        OBJECT,

        /** The SHORT. */
        SHORT,

        /** The STRING. */
        STRING;

        /**
         * Gets the declerator.
         *
         * @return the declerator
         */
        public String getDeclerator()
        {
            String decVal = OBJECT_DECLERATOR;
            switch (this)
            {
                case DOUBLE:
                    decVal = "double";
                    break;
                case FLOAT:
                    decVal = "float";
                    break;
                case LONG:
                case DATE:
                case DATE_ALT:
                    decVal = "long";
                    break;
                case INTEGER:
                    decVal = "int";
                    break;
                case SHORT:
                    decVal = "short";
                    break;
                case BYTE:
                    decVal = "short";
                    break;
                case STRING:
                    decVal = "byte[]";
                    break;
                case BOOLEAN:
                    decVal = "byte";
                    break;
                case DYNAMIC_ENUMERATION_KEY:
                    decVal = "int";
                    break;
                case BYTE_STRING:
                    decVal = "byte[]";
                    break;
                default:
                    decVal = OBJECT_DECLERATOR;
                    break;
            }
            return decVal;
        }
    }
}
