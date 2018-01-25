package io.opensphere.merge.algorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.merge.model.MergedDataRow;
import org.junit.Assert;

/**
 * Test.
 */
public class DataOpTest
{
    /**
     * The age column.
     */
    private static final String AGE_COLUMN = "AGE";

    /**
     * The alb type.
     */
    private static final String ALB_TYPE = "ALB";

    /**
     * alb columns.
     */
    private static Set<String> albCols = new TreeSet<>();

    /**
     * The alias column.
     */
    private static final String ALIAS_COLUMN = "ALIAS";

    /**
     * Bla type.
     */
    private static final String BLA_TYPE = "BLA";

    /**
     * bla columns.
     */
    private static Set<String> blaCols = new TreeSet<>();

    /**
     * The height column.
     */
    private static final String HEIGHT_COLUMN = "HEIGHT";

    /**
     * The name column.
     */
    private static final String NAME_COLUMN = "NAME";

    /**
     * Yar.
     */
    private static final String YAR = "YAR";

    /**
     * yar columns.
     */
    private static Set<String> yarCols = new TreeSet<>();

    static
    {
        blaCols.add(NAME_COLUMN);
        blaCols.add(AGE_COLUMN);
        blaCols.add(ALIAS_COLUMN);

        albCols.add(NAME_COLUMN);
        albCols.add(AGE_COLUMN);
        albCols.add(HEIGHT_COLUMN);

        yarCols.add("INITIAL");
        yarCols.add("DESC");
    }

    /**
     * Construct the metadata for the dataset called ALB_TYPE.
     *
     * @return bla
     */
    private static MetaDataInfo albMeta()
    {
        DefaultMetaDataInfo meta = new DefaultMetaDataInfo();
        meta.addKey(NAME_COLUMN, String.class, null);
        meta.addKey(AGE_COLUMN, Integer.class, null);
        meta.addKey(HEIGHT_COLUMN, Double.class, null);
        return meta;
    }

    /**
     * Construct a proto-record for the dataset called ALB_TYPE.
     *
     * @param name value of the field called NAME_COLUMN
     * @param age value of the field called AGE_COLUMN
     * @param h value of the field called HEIGHT_COLUMN
     * @return bla
     */
    private static Map<String, Object> albRec(String name, int age, double h)
    {
        Map<String, Object> rec = new TreeMap<>();
        rec.put(NAME_COLUMN, name);
        rec.put(AGE_COLUMN, Integer.valueOf(age));
        rec.put(HEIGHT_COLUMN, Double.valueOf(h));
        return rec;
    }

    /**
     * Construct the metadata for the dataset called BLA_TYPE.
     *
     * @return bla
     */
    private static MetaDataInfo blaMeta()
    {
        DefaultMetaDataInfo meta = new DefaultMetaDataInfo();
        meta.addKey(NAME_COLUMN, String.class, null);
        meta.addKey(AGE_COLUMN, Integer.class, null);
        meta.addKey(ALIAS_COLUMN, String.class, null);
        return meta;
    }

    /**
     * Construct a proto-record for the dataset called BLA_TYPE.
     *
     * @param name value of the field called NAME_COLUMN
     * @param age value of the field called AGE_COLUMN
     * @param nic value of the field called ALIAS_COLUMN
     * @return bla
     */
    private static Map<String, Object> blaRec(String name, int age, String nic)
    {
        Map<String, Object> rec = new TreeMap<>();
        rec.put(NAME_COLUMN, name);
        rec.put(AGE_COLUMN, Integer.valueOf(age));
        rec.put(ALIAS_COLUMN, nic);
        return rec;
    }

    /**
     * Construct a no-frills DataTypeInfo with the specified name and metadata.
     *
     * @param name bla
     * @param meta bla
     * @return bla
     */
    private static DataTypeInfo createType(String name, MetaDataInfo meta)
    {
        DefaultDataTypeInfo type = new DefaultDataTypeInfo(null, "", name, name, name, false);
        type.setMetaDataInfo(meta);
        return type;
    }

    /**
     * Create and populate the EnvSupport for testing purposes.
     *
     * @return bla
     */
    private static EnvSuppImpl initEnvSupp()
    {
        EnvSuppImpl supp = new EnvSuppImpl();
        supp.add(BLA_TYPE, blaCols, blaRec("Sam", 25, "Sammy"));
        supp.add(BLA_TYPE, blaCols, blaRec("Dean", 29, "Dean"));
        supp.add(BLA_TYPE, blaCols, blaRec("Robert", 50, "Bobby"));

        supp.add(ALB_TYPE, albCols, albRec("Bilbo", 111, 3.0));
        supp.add(ALB_TYPE, albCols, albRec("Frodo", 50, 3.2));
        supp.add(ALB_TYPE, albCols, albRec("Sam", 29, 3.1));

        supp.add(YAR, yarCols, yarRec("S", "Smart"));
        supp.add(YAR, yarCols, yarRec("D", "Dumb"));
        supp.add(YAR, yarCols, yarRec("B", "Bouncy"));
        supp.add(YAR, yarCols, yarRec("a", "Antsy"));
        supp.add(YAR, yarCols, yarRec("o", "Obtuse"));
        supp.add(YAR, yarCols, yarRec("m", "Mild"));
        return supp;
    }

    /**
     * Construct the metadata for the dataset called YAR.
     *
     * @return bla
     */
    private static MetaDataInfo yarMeta()
    {
        DefaultMetaDataInfo meta = new DefaultMetaDataInfo();
        meta.addKey("INITIAL", String.class, null);
        meta.addKey("DESC", String.class, null);
        return meta;
    }

    /**
     * Construct a proto-record for the dataset called YAR.
     *
     * @param initial value of the field called "INITIAL"
     * @param desc value of the field called "DESC"
     * @return bla
     */
    private static Map<String, Object> yarRec(String initial, String desc)
    {
        Map<String, Object> rec = new TreeMap<>();
        rec.put("INITIAL", initial);
        rec.put("DESC", desc);
        return rec;
    }

    /**
     * Tests complex join.
     */
    @Test
    public void testComplexJoin()
    {
        JoinData jd = new JoinData();
        jd.setSupp(initEnvSupp());
        jd.setUseExact(false);
        jd.getSrc().add(new JoinInfo(createType(YAR, yarMeta()), "INITIAL"));
        jd.getSrc().add(new JoinInfo(createType(ALB_TYPE, albMeta()), NAME_COLUMN));
        jd.getSrc().add(new JoinInfo(createType(BLA_TYPE, blaMeta()), ALIAS_COLUMN));
        jd.join();
        List<MergedDataRow> data = jd.getAllData();
        Assert.assertEquals(6, data.size());
        Assert.assertEquals(Integer.valueOf(29), data.get(0).getData().get(AGE_COLUMN));
        Assert.assertEquals(Double.valueOf(3.1), data.get(0).getData().get(HEIGHT_COLUMN));
        Assert.assertEquals("Sam", data.get(0).getData().get(NAME_COLUMN));
        Assert.assertNull(data.get(1).getData().get(AGE_COLUMN));
        Assert.assertNull(data.get(1).getData().get(HEIGHT_COLUMN));
        Assert.assertEquals("Dean", data.get(1).getData().get(NAME_COLUMN));
        Assert.assertEquals("Robert", data.get(4).getData().get(NAME_COLUMN));
    }

    /**
     * Tests merge.
     */
    @Test
    public void testMerge()
    {
        MergeData md = new MergeData();
        md.setSupp(initEnvSupp());
        md.getSrc().add(createType(BLA_TYPE, blaMeta()));
        md.getSrc().add(createType(ALB_TYPE, albMeta()));
        md.merge();
        List<MergedDataRow> data = md.getAllData();
        Assert.assertEquals(6, data.size());
        Assert.assertEquals("Sammy", data.get(0).getData().get(ALIAS_COLUMN));
        Assert.assertNull(data.get(0).getData().get(HEIGHT_COLUMN));
        Assert.assertEquals(Double.valueOf(3.2), data.get(4).getData().get(HEIGHT_COLUMN));
        Assert.assertNull(data.get(4).getData().get(ALIAS_COLUMN));
    }

    /**
     * Tests join.
     */
    @Test
    public void testNumericalJoin()
    {
        JoinData jd = new JoinData();
        jd.setSupp(initEnvSupp());
        jd.setUseExact(true);
        jd.getSrc().add(new JoinInfo(createType(BLA_TYPE, blaMeta()), AGE_COLUMN));
        jd.getSrc().add(new JoinInfo(createType(ALB_TYPE, albMeta()), AGE_COLUMN));
        jd.join();
        List<MergedDataRow> data = jd.getAllData();
        Assert.assertNull(data.get(0).getData().get(HEIGHT_COLUMN));
        Assert.assertEquals(Double.valueOf(3.1), data.get(1).getData().get(HEIGHT_COLUMN));
        Assert.assertEquals(Double.valueOf(3.2), data.get(2).getData().get(HEIGHT_COLUMN));
    }

    /**
     * Tests join.
     */
    @Test
    public void testSimpleJoin()
    {
        JoinData jd = new JoinData();
        jd.setSupp(initEnvSupp());
        jd.setUseExact(true);
        jd.getSrc().add(new JoinInfo(createType(BLA_TYPE, blaMeta()), NAME_COLUMN));
        jd.getSrc().add(new JoinInfo(createType(ALB_TYPE, albMeta()), NAME_COLUMN));
        jd.join();
        List<MergedDataRow> data = jd.getAllData();
        Assert.assertEquals(Double.valueOf(3.1), data.get(0).getData().get(HEIGHT_COLUMN));
        Assert.assertNull(data.get(1).getData().get(HEIGHT_COLUMN));
        Assert.assertNull(data.get(2).getData().get(HEIGHT_COLUMN));
    }

    /**
     * Implements the EnvSupport interface for test purposes.
     */
    private static class EnvSuppImpl implements EnvSupport
    {
        /** Repository used to answer calls to method getRecords. */
        private final Map<String, List<DataElement>> recMap = new TreeMap<>();

        /**
         * Add a record to the resident repository.
         *
         * @param type name of the type to which the record belongs
         * @param cols the set of columns in that type
         * @param rec a proto-record, as a Map of String to Object
         */
        public void add(String type, Set<String> cols, Map<String, Object> rec)
        {
            List<DataElement> data = recMap.get(type);
            if (data == null)
            {
                recMap.put(type, data = new LinkedList<>());
            }
            data.add(Util.createElement(rec, cols));
        }

        @Override
        public String columnMatch(Col c1, Col c2)
        {
            String matchName = null;
            if (c1.name.equals(c2.name))
            {
                matchName = c1.name;
            }

            return matchName;
        }

        @Override
        public List<DataElement> getRecords(DataTypeInfo type)
        {
            List<DataElement> ret = recMap.get(type.getTypeKey());
            if (ret == null)
            {
                return new LinkedList<>();
            }
            return ret;
        }
    }
}
