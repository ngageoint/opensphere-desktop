package io.opensphere.core.util.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link StringUtilities}.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class StringUtilitiesTest
{
    /** Test string. */
    private static final String IND = "ind";

    /** Test string. */
    private static final String MANK = "mank";

    /** Test string. */
    private static final String MANKIND = "mankind";

    /** Test string. */
    private static final Object OBJ_IND = new Object()
    {
        @Override
        public String toString()
        {
            return IND;
        }
    };

    /** Test string. */
    private static final Object OBJ_MANK = new Object()
    {
        @Override
        public String toString()
        {
            return MANK;
        }
    };

    /**
     * Test {@link StringUtilities#addHTMLLineBreaks(String, int)}.
     */
    @Test
    public void testAddHTMLLineBreaks() // /String text, int length)
    {
        String input = "This is a test sentence of words that will be tested.";

        // Length longer than current string and should be returned unmolested.
        String result = StringUtilities.addHTMLLineBreaks(input, 60);
        Assert.assertEquals(result, input);

        // A "<p>" should be added to the string around every 15 characters.
        result = StringUtilities.addHTMLLineBreaks(input, 15);
        Assert.assertEquals(result, "This is a test <p>sentence of words <p>that will be tested.");

        // A "<p>" should be added to the string around every 5 characters.
        result = StringUtilities.addHTMLLineBreaks(input, 5);
        Assert.assertEquals(result, "This <p>is a <p>test <p>sentence <p>of words <p>that <p>will <p>be tested.");

        // A "<p>" should be added to the string around every 1 characters.
        result = StringUtilities.addHTMLLineBreaks(input, 1);
        Assert.assertEquals(result, "This <p>is <p>a <p>test <p>sentence <p>of <p>words <p>that <p>will <p>be <p>tested.");

        // A length of less than 1 should return original string.
        result = StringUtilities.addHTMLLineBreaks(input, -1);
        Assert.assertEquals(result, "This is a test sentence of words that will be tested.");

        // Passing a value of null should return null.
        result = StringUtilities.addHTMLLineBreaks(null, 5);
        Assert.assertEquals(result, null);

        // Test for extra spaces in sentence.
        input = "This is     a test sentence of words      that will be tested.";
        result = StringUtilities.addHTMLLineBreaks(input, 15);
        Assert.assertEquals(result, "This is     a test <p>sentence of words <p>     that will <p>be tested.");
    }

    /**
     * Test {@link StringUtilities#addLineBreaks(String, int)}.
     */
    @Test
    public void testAddLineBreaks()
    {
        String input = "This is a very long sentence of words that will be tested.";

        String lb = StringUtilities.LINE_SEP;

        // Length of zero should return original string unmolested.
        String result = StringUtilities.addLineBreaks(input, 0);
        Assert.assertEquals(result, input);

        // Length less than zero should return original string unmolested.
        result = StringUtilities.addLineBreaks(input, -1);
        Assert.assertEquals(result, input);

        // Length longer than string should return original string unmolested.
        result = StringUtilities.addLineBreaks(input, 100);
        Assert.assertEquals(result, input);

        // Length 2
        result = StringUtilities.addLineBreaks(input, 2);
        Assert.assertEquals(result, "This" + lb + "is" + lb + "a very" + lb + "long" + lb + "sentence" + lb + "of" + lb + "words"
                + lb + "that" + lb + "will" + lb + "be" + lb + "tested.");

        // Length 5
        result = StringUtilities.addLineBreaks(input, 5);
        Assert.assertEquals(result,
                "This is" + lb + "a very" + lb + "long sentence" + lb + "of words" + lb + "that will" + lb + "be tested.");

        // Length 10
        result = StringUtilities.addLineBreaks(input, 10);
        Assert.assertEquals(result, "This is a very" + lb + "long sentence" + lb + "of words that" + lb + "will be tested.");

        // Length 20
        result = StringUtilities.addLineBreaks(input, 20);
        Assert.assertEquals(result, "This is a very long sentence" + lb + "of words that will be" + lb + "tested.");

        // Length 30
        result = StringUtilities.addLineBreaks(input, 30);
        Assert.assertEquals(result, "This is a very long sentence of" + lb + "words that will be tested.");

        // Length 40
        result = StringUtilities.addLineBreaks(input, 40);
        Assert.assertEquals(result, "This is a very long sentence of words that" + lb + "will be tested.");

        // Now test with more than one space between words (only one space is
        // eaten on line boundary).
        input = "This  is  a  test   with   a  bunch   of    spaces.";
        // Length 5
        result = StringUtilities.addLineBreaks(input, 5);
        Assert.assertEquals(result,
                "This " + lb + "is  a" + lb + " test" + lb + "  with" + lb + "  a  bunch" + lb + "  of " + lb + "  spaces.");

        // Length 10
        result = StringUtilities.addLineBreaks(input, 10);
        Assert.assertEquals(result, "This  is  a" + lb + " test   with" + lb + "  a  bunch" + lb + "  of    spaces.");

        // Test that null message returns null
        result = StringUtilities.addLineBreaks(null, 10);
        Assert.assertEquals(result, null);
    }

    /**
     * Test {@link StringUtilities#capitalize(String)}.
     */
    @Test
    public void testCapitalize()
    {
        Assert.assertEquals("", StringUtilities.capitalize(""));
        Assert.assertEquals("A", StringUtilities.capitalize("a"));
        Assert.assertEquals("A", StringUtilities.capitalize("A"));
        Assert.assertEquals("Ab", StringUtilities.capitalize("ab"));
    }

    /**
     * Test {@link StringUtilities#concat(String... strings)}.
     */
    @SuppressWarnings("boxing")
    @Test
    public void testConcat()
    {
        // Test normal usage
        Assert.assertEquals(MANKIND, StringUtilities.concat(MANK, IND));

        // Test null String
        Assert.assertEquals(MANKIND, StringUtilities.concat(MANK, null, IND));

        // Test number
        Assert.assertEquals("mank55ind", StringUtilities.concat(MANK, 55, IND));
    }

    /**
     * Test {@link StringUtilities#concat(StringBuilder, Object...)}.
     */
    @SuppressWarnings("boxing")
    @Test
    public void testConcat2()
    {
        // Test normal usage
        Assert.assertEquals(MANKIND, StringUtilities.concat(new StringBuilder(MANK), IND).toString());

        // Test null String
        Assert.assertEquals(MANKIND, StringUtilities.concat(new StringBuilder(MANK), null, IND).toString());
    }

    /**
     * Test {@link StringUtilities#concat(Object... strings)}.
     */
    @SuppressWarnings("boxing")
    @Test
    public void testConcatObj()
    {
        // Test normal usage
        Assert.assertEquals(MANKIND, StringUtilities.concat(OBJ_MANK, OBJ_IND));

        // Test null String
        Assert.assertEquals(MANKIND, StringUtilities.concat(OBJ_MANK, null, OBJ_IND));

        // Test number
        Assert.assertEquals("mank55ind", StringUtilities.concat(OBJ_MANK, 55, OBJ_IND));
    }

    /**
     * Test {@link StringUtilities#convertToHTML(String)}.
     */
    @Test
    public void testConvertToHTML()
    {
        Assert.assertEquals(null, StringUtilities.convertToHTML(null));
        Assert.assertEquals("<html></html>", StringUtilities.convertToHTML(""));
        Assert.assertEquals("<html>test</html>", StringUtilities.convertToHTML("test"));
        Assert.assertEquals("<html>test1<br/>test2</html>", StringUtilities.convertToHTML("test1\ntest2"));
        Assert.assertEquals("<html>test1<br/>test2</html>", StringUtilities.convertToHTML("test1\n\rtest2"));
        Assert.assertEquals("<html>test1<br/>test2</html>", StringUtilities.convertToHTML("test1\r\ntest2"));
        Assert.assertEquals("<html>test1<br/><br/>test2</html>", StringUtilities.convertToHTML("test1\n\ntest2"));
        Assert.assertEquals("<html>test1<t/>test2</html>", StringUtilities.convertToHTML("test1\ttest2"));
        Assert.assertEquals("<html>test1<t/><br/>test2</html>", StringUtilities.convertToHTML("test1\t\ntest2"));
    }

    /**
     * Test {@link StringUtilities#convertToHTML(String, boolean)}.
     */
    @Test
    public void testConvertToHTMLStringBoolean()
    {
        Assert.assertEquals(null, StringUtilities.convertToHTML(null, true));
        Assert.assertEquals("<html><pre></pre></html>", StringUtilities.convertToHTML("", true));
        Assert.assertEquals("<html><pre>test</pre></html>", StringUtilities.convertToHTML("test", true));
        Assert.assertEquals("<html><pre>test1<br/>test2</pre></html>", StringUtilities.convertToHTML("test1\ntest2", true));
        Assert.assertEquals("<html><pre>test1<br/>test2</pre></html>", StringUtilities.convertToHTML("test1\n\rtest2", true));
        Assert.assertEquals("<html><pre>test1<br/>test2</pre></html>", StringUtilities.convertToHTML("test1\r\ntest2", true));
        Assert.assertEquals("<html><pre>test1<br/><br/>test2</pre></html>",
                StringUtilities.convertToHTML("test1\n\ntest2", true));
        Assert.assertEquals("<html><pre>test1<t/>test2</pre></html>", StringUtilities.convertToHTML("test1\ttest2", true));
        Assert.assertEquals("<html><pre>test1<t/><br/>test2</pre></html>", StringUtilities.convertToHTML("test1\t\ntest2", true));

        Assert.assertEquals(null, StringUtilities.convertToHTML(null, false));
        Assert.assertEquals("<html></html>", StringUtilities.convertToHTML("", false));
        Assert.assertEquals("<html>test</html>", StringUtilities.convertToHTML("test", false));
        Assert.assertEquals("<html>test1<br/>test2</html>", StringUtilities.convertToHTML("test1\ntest2", false));
        Assert.assertEquals("<html>test1<br/>test2</html>", StringUtilities.convertToHTML("test1\n\rtest2", false));
        Assert.assertEquals("<html>test1<br/>test2</html>", StringUtilities.convertToHTML("test1\r\ntest2", false));
        Assert.assertEquals("<html>test1<br/><br/>test2</html>", StringUtilities.convertToHTML("test1\n\ntest2", false));
        Assert.assertEquals("<html>test1<t/>test2</html>", StringUtilities.convertToHTML("test1\ttest2", false));
        Assert.assertEquals("<html>test1<t/><br/>test2</html>", StringUtilities.convertToHTML("test1\t\ntest2", false));
    }

    /**
     * Test {@link StringUtilities#convertToHTMLTable(String)}.
     */
    @Test
    public void testConvertToHTMLTable()
    {
        Assert.assertEquals(null, StringUtilities.convertToHTMLTable(null));
        Assert.assertEquals("<html><table></table></html>", StringUtilities.convertToHTMLTable(""));
        Assert.assertEquals("<html><table><tr><td>test</td></tr></table></html>", StringUtilities.convertToHTMLTable("test"));
        Assert.assertEquals("<html><table><tr><td>test1</td></tr><tr><td>test2</td></tr></table></html>",
                StringUtilities.convertToHTMLTable("test1\ntest2"));
        Assert.assertEquals("<html><table><tr><td>test1</td></tr><tr><td>test2</td></tr></table></html>",
                StringUtilities.convertToHTMLTable("test1\n\rtest2"));
        Assert.assertEquals("<html><table><tr><td>test1</td></tr><tr><td>test2</td></tr></table></html>",
                StringUtilities.convertToHTMLTable("test1\r\ntest2"));
        Assert.assertEquals("<html><table><tr><td>test1</td></tr><tr><td>test2</td></tr></table></html>",
                StringUtilities.convertToHTMLTable("test1\n\ntest2"));
        Assert.assertEquals("<html><table><tr><td>test1</td><td>test2</td></tr></table></html>",
                StringUtilities.convertToHTMLTable("test1\ttest2"));
        Assert.assertEquals("<html><table><tr><td>test1</td></tr><tr><td>test2</td></tr></table></html>",
                StringUtilities.convertToHTMLTable("test1\t\ntest2"));
    }

    /** Test {@link StringUtilities#count(char, String)}. */
    @Test
    public void testCount()
    {
        String str = "redgreenyellowyellowblueyellowblackred";
        Assert.assertEquals(8, StringUtilities.count('e', str));
        Assert.assertEquals(0, StringUtilities.count('x', str));
        Assert.assertEquals(0, StringUtilities.count('x', null));
    }

    /** Test {@link StringUtilities#count(CharSequence, String)}. */
    @Test
    public void testCountCharSequenceString()
    {
        String str = "redgreenyellowyellowblueyellowblackred";
        Assert.assertEquals(3, StringUtilities.count(str, "yellow"));
        Assert.assertEquals(2, StringUtilities.count(str, "r.d"));
        Assert.assertEquals(1, StringUtilities.count(str, "g.*n"));
        Assert.assertEquals(8, StringUtilities.count(str, "e"));
    }

    /** Test {@link StringUtilities#count(java.util.regex.Matcher)}. */
    @Test
    public void testCountMatcher()
    {
        String str = "redgreenyellowyellowblueyellowblackred";
        Assert.assertEquals(3, StringUtilities.count(Pattern.compile("yellow").matcher(str)));
        Assert.assertEquals(2, StringUtilities.count(Pattern.compile("r.d").matcher(str)));
        Assert.assertEquals(1, StringUtilities.count(Pattern.compile("g.*n").matcher(str)));
        Assert.assertEquals(8, StringUtilities.count(Pattern.compile("e").matcher(str)));
    }

    /** Test {@link StringUtilities#cut(String, int)}. */
    @Test
    public void testCut()
    {
        Assert.assertEquals(null, StringUtilities.cut(null, 5));
        Assert.assertEquals("", StringUtilities.cut("", 5));
        Assert.assertEquals("as", StringUtilities.cut("as", 2));
        Assert.assertEquals("as", StringUtilities.cut("as", 6));
        Assert.assertEquals("...", StringUtilities.cut("asdfgh", 2));
        Assert.assertEquals("as...", StringUtilities.cut("asdfgh", 5));
    }

    /**
     * Test {@link StringUtilities#endsWith(String, char)}.
     */
    @Test
    public void testEndsWith()
    {
        Assert.assertFalse(StringUtilities.endsWith(null, 'a'));
        Assert.assertFalse(StringUtilities.endsWith("", 'a'));
        Assert.assertFalse(StringUtilities.endsWith(" ", 'a'));
        Assert.assertFalse(StringUtilities.endsWith("a ", 'a'));
        Assert.assertFalse(StringUtilities.endsWith("b", 'a'));
        Assert.assertFalse(StringUtilities.endsWith("ab", 'a'));
        Assert.assertTrue(StringUtilities.endsWith("a", 'a'));
        Assert.assertTrue(StringUtilities.endsWith("aa", 'a'));
        Assert.assertTrue(StringUtilities.endsWith("ba", 'a'));
    }

    /**
     * Test {@link StringUtilities#endsWith(String, java.util.List)}.
     */
    @Test
    public void testEndsWithStringList()
    {
        Assert.assertFalse(StringUtilities.endsWith(null, Arrays.asList("a")));
        Assert.assertFalse(StringUtilities.endsWith("abcdefg", null));
        Assert.assertFalse(StringUtilities.endsWith(null, null));
        Assert.assertTrue(StringUtilities.endsWith("abcdefg", Arrays.asList("efg")));
        Assert.assertTrue(StringUtilities.endsWith("abcdefg", Arrays.asList("efg", "def", "hij")));
        Assert.assertTrue(StringUtilities.endsWith("abcdefg", Arrays.asList("def", "efg", "hij")));
        Assert.assertTrue(StringUtilities.endsWith("abcdefg", Arrays.asList("def", "hij", "efg")));
        Assert.assertFalse(StringUtilities.endsWith("abcdefg", Arrays.asList("def", "hij")));
        Assert.assertFalse(StringUtilities.endsWith("abcdefg", Arrays.asList("def")));
        Assert.assertFalse(StringUtilities.endsWith("abcdefg", Collections.<String>emptyList()));
    }

    /**
     * Test {@link StringUtilities#expandProperties(String, Properties)}.
     */
    @Test
    public void testExpandProperties()
    {
        Properties props = new Properties();
        props.setProperty("hero", "Luke");
        props.setProperty("villain", "${father}");
        props.setProperty("father", "Darth");
        props.setProperty("son", "hero");

        Assert.assertNull(StringUtilities.expandProperties(null, props));

        String input = "${${son}} ${villain} ${undefined}";
        String expected = "Luke Darth ${undefined}";
        String result = StringUtilities.expandProperties(input, props);
        Assert.assertEquals(expected, result);

        props.setProperty("loop", "${loop}");
        result = StringUtilities.expandProperties("${loop}", props);
        expected = "${loop}";
        Assert.assertEquals(expected, result);
    }

    /**
     * Test {@link StringUtilities#explodeLineOnUnquotedCommas(String)}.
     */
    @Test
    public void testExplodeLineOnUnquotedCommas()
    {
        String parisLine = "Paris,48.853409,2.348800,FR";
        String berlinLine = "Berlin,52.516666,13.400000,DE";
        String tokyoLine = "\"Tokyo,35.689526,139.691681,JP\"";
        String commerceCityLine = "Commerce City,12345\",\"6789,foo\",\"bar";

        String[] parisResult = StringUtilities.explodeLineOnUnquotedCommas(parisLine);
        String[] berlinResult = StringUtilities.explodeLineOnUnquotedCommas(berlinLine);
        String[] tokyoResult = StringUtilities.explodeLineOnUnquotedCommas(tokyoLine);
        String[] commerceResult = StringUtilities.explodeLineOnUnquotedCommas(commerceCityLine);
        String[] nullResult = StringUtilities.explodeLineOnUnquotedCommas(null);

        Assert.assertEquals("Paris", parisResult[0]);
        Assert.assertEquals("48.853409", parisResult[1]);
        Assert.assertEquals("2.348800", parisResult[2]);
        Assert.assertEquals("FR", parisResult[3]);

        Assert.assertEquals("Berlin", berlinResult[0]);
        Assert.assertEquals("52.516666", berlinResult[1]);
        Assert.assertEquals("13.400000", berlinResult[2]);
        Assert.assertEquals("DE", berlinResult[3]);

        Assert.assertEquals("\"Tokyo,35.689526,139.691681,JP\"", tokyoResult[0]);

        Assert.assertEquals("Commerce City", commerceResult[0]);
        Assert.assertEquals("12345\",\"6789", commerceResult[1]);
        Assert.assertEquals("foo\",\"bar", commerceResult[2]);

        Assert.assertArrayEquals(null, nullResult);

        Assert.assertEquals("two", StringUtilities.explodeLineOnUnquotedCommas("one,\"two\",three")[1]);
        Assert.assertEquals("two", StringUtilities.explodeLineOnUnquotedCommas("one, \" two \" ,three")[1]);
        Assert.assertEquals("\"two\"a", StringUtilities.explodeLineOnUnquotedCommas("\"one\", \"two\"a ,three")[1]);
        Assert.assertEquals("a\"two\"a", StringUtilities.explodeLineOnUnquotedCommas("\"one\", a\"two\"a ,three")[1]);
        Assert.assertEquals("a\"two\"", StringUtilities.explodeLineOnUnquotedCommas("\"one\", a\"two\" ,three")[1]);
        Assert.assertEquals("a\"t,wo\"", StringUtilities.explodeLineOnUnquotedCommas("\"one\", a\"t,wo\" ,three")[1]);
    }

    /**
     * Test {@link StringUtilities#getSubProperties(Properties, String)}.
     */
    @Test
    public void testGetSubProperties()
    {
        Properties props = new Properties();
        props.setProperty("group1.", "value1");
        props.setProperty("group1.prop1", "value1");
        props.setProperty("group1.prop2", "value2");
        props.setProperty("group1.prop3", "value3");
        props.setProperty("group2.prop1", "value4");
        props.setProperty("group2.prop2", "value4");
        props.setProperty("group2.prop3", "value4");

        Map<String, String> result = StringUtilities.getSubProperties(props, "group1.");
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("value1", result.get("prop1"));
        Assert.assertEquals("value2", result.get("prop2"));
        Assert.assertEquals("value3", result.get("prop3"));
    }

    /**
     * Test {@link StringUtilities#getSubProperties(Properties, String, Class)}.
     */
    @Test
    public void testGetSubPropertiesType()
    {
        Properties props = new Properties();
        props.setProperty("group1.prop1", "1");
        props.setProperty("group1.prop2", "2");
        props.setProperty("group1.prop3", "3");
        props.setProperty("group2.prop1", "4");
        props.setProperty("group2.prop2", "5");
        props.setProperty("group2.prop3", "6");

        Map<String, Integer> result = StringUtilities.getSubProperties(props, "group1.", Integer.class);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get("prop1"));
        Assert.assertEquals(Integer.valueOf(2), result.get("prop2"));
        Assert.assertEquals(Integer.valueOf(3), result.get("prop3"));
    }

    /**
     * Test {@link StringUtilities#getSubProperties(Properties, String, Class)}
     * with a bad argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSubPropertiesTypeBadArg()
    {
        StringUtilities.getSubProperties(new Properties(), "", String.class);
    }

    /** Test {@link StringUtilities#getUniqueName(String, Collection)}. */
    @Test
    public void testGetUniqueName()
    {
        Assert.assertEquals("prefix-1", StringUtilities.getUniqueName("prefix-", Collections.<String>emptySet()));
        Assert.assertEquals("prefix-4",
                StringUtilities.getUniqueName("prefix-", Arrays.asList("prefix-1", "prefix-2", "prefix-3")));
        Assert.assertEquals("prefix-4", StringUtilities.getUniqueName("prefix-", Arrays.asList("prefix-2", "prefix-3")));
        Assert.assertEquals("prefix (4)",
                StringUtilities.getUniqueName("prefix (", Arrays.asList("prefix (2)", "prefix (3)"), ")"));
    }

    /**
     * Test {@link StringUtilities#isInteger(String s)}.
     */
    @Test
    public void testIsInteger()
    {
        Assert.assertFalse(StringUtilities.isInteger(null));
        Assert.assertFalse(StringUtilities.isInteger("1A"));
        Assert.assertFalse(StringUtilities.isInteger("1.1"));
        Assert.assertFalse(StringUtilities.isInteger(" 1 "));
        Assert.assertTrue(StringUtilities.isInteger("1"));
        Assert.assertTrue(StringUtilities.isInteger("11"));
        Assert.assertTrue(StringUtilities.isInteger("+1"));
        Assert.assertTrue(StringUtilities.isInteger("-1"));
    }

    /**
     * Test {@link StringUtilities#join(String, Collection)} and
     * {@link StringUtilities#join(String, Object[])}.
     */
    @Test
    public void testJoin()
    {
        String result = StringUtilities.join(":", "alpha");
        String expected = "alpha";
        Assert.assertEquals(expected, result);

        result = StringUtilities.join(":", (Object)"alpha");
        Assert.assertEquals(expected, result);

        Object[] arr = { "alpha" };
        result = StringUtilities.join(":", arr);
        Assert.assertEquals(expected, result);

        result = StringUtilities.join(":", "alpha", "beta", null, "gamma");
        expected = "alpha:beta::gamma";
        Assert.assertEquals(expected, result);

        result = StringUtilities.join(":", (Object)"alpha", "beta", null, "gamma");
        Assert.assertEquals(expected, result);

        arr = new Object[] { "alpha", "beta", null, "gamma" };
        result = StringUtilities.join(":", arr);
        Assert.assertEquals(expected, result);

        Collection<Object> col = Arrays.asList(arr);
        result = StringUtilities.join(":", col);
        Assert.assertEquals(expected, result);
    }

    /**
     * Test {@link StringUtilities#pad(String, int)}.
     */
    @Test
    public void testPad()
    {
        Assert.assertEquals("     ", StringUtilities.pad(null, 5));
        Assert.assertEquals("     ", StringUtilities.pad("", 5));
        Assert.assertEquals("abc  ", StringUtilities.pad("abc", 5));
        Assert.assertEquals("abc", StringUtilities.pad("abc", 1));
    }

    /**
     * Test {@link StringUtilities#removeHTML(String)}.
     */
    @Test
    public void testRemoveHTML()
    {
        Assert.assertEquals(null, StringUtilities.removeHTML(null));
        Assert.assertEquals("text", StringUtilities.removeHTML("text"));
        Assert.assertEquals("  hi  ", StringUtilities.removeHTML(" <html> hi </html> "));
        Assert.assertEquals("/home/user", StringUtilities.removeHTML("/home/user"));
        Assert.assertEquals("hi there", StringUtilities.removeHTML("<p>hi&nbsp;there</p>"));
    }

    /**
     * Test {@link StringUtilities#replaceSpecialCharacters(String)}.
     */
    @Test
    public void testReplaceSpecialCharacters()
    {
        Assert.assertEquals(null, StringUtilities.replaceSpecialCharacters(null));
        Assert.assertEquals("text", StringUtilities.replaceSpecialCharacters("text"));
        Assert.assertEquals("text-123_f_ck_h0l3", StringUtilities.replaceSpecialCharacters("text-123 f*ck! @$$h0l3 "));
        Assert.assertEquals("_home_user", StringUtilities.replaceSpecialCharacters("/home/user"));
    }

    /**
     * Test {@link StringUtilities#slashJoin(String...)}.
     */
    @Test
    public void testSlashJoin()
    {
        Assert.assertEquals("axl/duff", StringUtilities.slashJoin("axl", "duff"));
        Assert.assertEquals("axl/duff", StringUtilities.slashJoin("axl/", "duff"));
        Assert.assertEquals("axl/duff", StringUtilities.slashJoin("axl", "/duff"));
    }

    /**
     * Test {@link StringUtilities#startsWith(String, char)}.
     */
    @Test
    @SuppressWarnings("PMD.SimplifyStartsWith")
    public void testStartsWith()
    {
        Assert.assertFalse(StringUtilities.startsWith(null, 'a'));
        Assert.assertFalse(StringUtilities.startsWith("", 'a'));
        Assert.assertFalse(StringUtilities.startsWith(" ", 'a'));
        Assert.assertFalse(StringUtilities.startsWith(" a", 'a'));
        Assert.assertFalse(StringUtilities.startsWith("b", 'a'));
        Assert.assertFalse(StringUtilities.startsWith("ba", 'a'));
        Assert.assertTrue(StringUtilities.startsWith("a", 'a'));
        Assert.assertTrue(StringUtilities.startsWith("aa", 'a'));
        Assert.assertTrue(StringUtilities.startsWith("ab", 'a'));
    }

    /**
     * Test for {@link StringUtilities#toHexString(byte[], String)}.
     */
    @Test
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    public void testToHexString()
    {
        Assert.assertEquals("", StringUtilities.toHexString(new byte[0], Nulls.STRING));
        Assert.assertEquals("", StringUtilities.toHexString(new byte[0], ":"));
        Assert.assertEquals("00", StringUtilities.toHexString(new byte[] { 0x0 }, Nulls.STRING));
        Assert.assertEquals("0000", StringUtilities.toHexString(new byte[] { 0x0, 0x0 }, Nulls.STRING));
        Assert.assertEquals("00:00", StringUtilities.toHexString(new byte[] { 0x0, 0x0 }, ":"));
        Assert.assertEquals("00::00", StringUtilities.toHexString(new byte[] { 0x0, 0x0 }, "::"));
        Assert.assertEquals("00:00:00", StringUtilities.toHexString(new byte[] { 0x0, 0x0, 0x0 }, ":"));
        Assert.assertEquals("00::00::00", StringUtilities.toHexString(new byte[] { 0x0, 0x0, 0x0 }, "::"));

        for (int i = 0; i < 0x100; ++i)
        {
            Assert.assertEquals(String.format("%02X", Integer.valueOf(i)),
                    StringUtilities.toHexString(new byte[] { (byte)i }, Nulls.STRING));
        }
    }

    /**
     * Test for {@link StringUtilities#trim(String, char...)}.
     */
    @Test
    public void testTrim()
    {
        String testSpace = "test ";
        String test = "test";
        Assert.assertEquals(test, StringUtilities.trim(testSpace));
        Assert.assertNull(StringUtilities.trim(null));
        Assert.assertEquals(testSpace, StringUtilities.trim(testSpace, new char[0]));
        Assert.assertEquals(test, StringUtilities.trim(testSpace, ' '));
        Assert.assertEquals(test, StringUtilities.trim(testSpace, ' ', 's'));
        Assert.assertEquals("te", StringUtilities.trim(testSpace, ' ', 's', 't'));
        Assert.assertEquals("", StringUtilities.trim(testSpace, ' ', 'e', 's', 't'));
    }

    /**
     * Test for {@link StringUtilities#trimBoth(String, char)}.
     */
    @Test
    public void testTrimBoth()
    {
        Assert.assertNull(StringUtilities.trimBoth(null, '/'));
        Assert.assertEquals("", StringUtilities.trimBoth("", '/'));
        Assert.assertEquals("", StringUtilities.trimBoth("/", '/'));
        Assert.assertEquals("hi", StringUtilities.trimBoth("hi", '/'));
        Assert.assertEquals("hi", StringUtilities.trimBoth("/hi", '/'));
        Assert.assertEquals("hi", StringUtilities.trimBoth("hi/", '/'));
        Assert.assertEquals("hi", StringUtilities.trimBoth("/hi/", '/'));
        Assert.assertEquals("hi", StringUtilities.trimBoth("//hi//", '/'));
    }

    /**
     * Test {@link StringUtilities#unEscapeString(String)}.
     */
    @Test
    public void testUnescape()
    {
        String expected;
        String result;

        expected = "<<>>\"\"&&''";
        result = StringUtilities.unEscapeString("&LT;&lt;&GT;&gt;&quot;&QUOT;&amp;&AMP;&apos;&APOS;");
        Assert.assertEquals(expected, result);

        expected = "&&<<>>&\"\"&&''&";
        result = StringUtilities.unEscapeString("&&&lt;&lt;&gt;&gt;&&quot;&quot;&amp;&amp;&apos;&apos;&");
        Assert.assertEquals(expected, result);

        expected = "&lt&gt&quot&amp&apos";
        result = StringUtilities.unEscapeString("&lt&gt&quot&amp&apos");
        Assert.assertEquals(expected, result);
    }
}
