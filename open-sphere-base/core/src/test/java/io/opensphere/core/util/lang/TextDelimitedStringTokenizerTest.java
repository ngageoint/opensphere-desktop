package io.opensphere.core.util.lang;

import java.util.List;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link TextDelimitedStringTokenizer}.
 */
public class TextDelimitedStringTokenizerTest
{
    /**
     * Test {@link TextDelimitedStringTokenizer#tokenize(String)} when an open
     * or close is imbedded in the middle of a token.
     */
    @Test
    public void testImbeddedTextDelimiter()
    {
        String test = "Gieuseppi:Eddie |The:Fingers|:|Mad:Man| Antonucci:Lefty";
        StringTokenizer tok = new TextDelimitedStringTokenizer(":", "|");
        List<String> tokens = tok.tokenize(test);

        Assert.assertEquals("Gieuseppi", tokens.get(0));
        Assert.assertEquals("Eddie The:Fingers", tokens.get(1));
        Assert.assertEquals("Mad:Man Antonucci", tokens.get(2));
        Assert.assertEquals("Lefty", tokens.get(3));

        test = "Gieuseppi:Eddie |>The:Fingers<|:|>Mad:Man<| Antonucci:Lefty";
        tok = new TextDelimitedStringTokenizer(":", "|>", "<|");
        tokens = tok.tokenize(test);

        Assert.assertEquals("Gieuseppi", tokens.get(0));
        Assert.assertEquals("Eddie The:Fingers", tokens.get(1));
        Assert.assertEquals("Mad:Man Antonucci", tokens.get(2));
        Assert.assertEquals("Lefty", tokens.get(3));
    }

    /**
     * Test {@link TextDelimitedStringTokenizer#tokenize(String)} when the token
     * delimiter is multi-character and the open and close text delimiters do
     * not match and are both multi-character.
     */
    @Test
    public void testMulitNonMatchingTextDelimiters()
    {
        String test = "{({({({(One)])])])]:={({(Two)])]:={({({(Three)])])]:={({({(Fo:=ur)])])]:=Joey {({(The Thumb)])] Antonucci:={({(six:=seven)])]";
        StringTokenizer tok = new TextDelimitedStringTokenizer(":=", "{(", ")]");
        List<String> tokens = tok.tokenize(test);

        Assert.assertEquals("{({(One)])]", tokens.get(0));
        Assert.assertEquals("{(Two)]", tokens.get(1));
        Assert.assertEquals("{(Three)]", tokens.get(2));
        Assert.assertEquals("{(Fo:=ur)]", tokens.get(3));
        Assert.assertEquals("Joey {(The Thumb)] Antonucci", tokens.get(4));
        Assert.assertEquals("{(six", tokens.get(5));
        Assert.assertEquals("seven)]", tokens.get(6));
    }

    /**
     * Test {@link TextDelimitedStringTokenizer#tokenize(String)} when there is
     * only one text delimiter and it appears in doubles (or more).
     */
    @Test
    public void testMulitTextDelimiters()
    {
        String test = "||||One||||:||Two||:|||Three|||:|||Fo:ur|||:Joey ||The Thumb|| Antonucci:||six:seven||";
        StringTokenizer tok = new TextDelimitedStringTokenizer(":", "|");
        List<String> tokens = tok.tokenize(test);

        Assert.assertEquals("||One||", tokens.get(0));
        Assert.assertEquals("|Two|", tokens.get(1));
        Assert.assertEquals("|Three|", tokens.get(2));
        Assert.assertEquals("|Fo:ur|", tokens.get(3));
        Assert.assertEquals("Joey |The Thumb| Antonucci", tokens.get(4));
        Assert.assertEquals("|six", tokens.get(5));
        Assert.assertEquals("seven|", tokens.get(6));
    }

    /**
     * Test {@link TextDelimitedStringTokenizer#tokenize(String)} when there is
     * only one text delimiter and it is mulit-character.
     */
    @Test
    public void testMultiCharacterTextDelimiter()
    {
        String test = "One:<|>Two:Three<|>:Four:Five";
        StringTokenizer tok = new TextDelimitedStringTokenizer(":", "<|>");
        List<String> tokens = tok.tokenize(test);

        Assert.assertEquals("One", tokens.get(0));
        Assert.assertEquals("Two:Three", tokens.get(1));
        Assert.assertEquals("Four", tokens.get(2));
        Assert.assertEquals("Five", tokens.get(3));
    }

    /**
     * Test {@link TextDelimitedStringTokenizer#tokenize(String)} when the token
     * delimiter is multi-character and there are no text delimiters.
     */
    @Test
    public void testMultiCharacterTokenDelimiter()
    {
        String test = "One[:Two[:Three[:Four[:Five";
        StringTokenizer tok = new TextDelimitedStringTokenizer("[:");
        List<String> tokens = tok.tokenize(test);

        Assert.assertEquals("One", tokens.get(0));
        Assert.assertEquals("Two", tokens.get(1));
        Assert.assertEquals("Three", tokens.get(2));
        Assert.assertEquals("Four", tokens.get(3));
        Assert.assertEquals("Five", tokens.get(4));

        test = "One::Two::::Four::Five";
        tok = new TextDelimitedStringTokenizer("::");
        tokens = tok.tokenize(test);

        Assert.assertEquals("One", tokens.get(0));
        Assert.assertEquals("Two", tokens.get(1));
        Assert.assertEquals("", tokens.get(2));
        Assert.assertEquals("Four", tokens.get(3));
        Assert.assertEquals("Five", tokens.get(4));
    }

    /**
     * Test {@link TextDelimitedStringTokenizer#tokenize(String)} when the open
     * and close text delimiters are not the same.
     */
    @Test
    public void testNonMatchingTextDelimiter()
    {
        String test = "Uno:{Two:Three}:eight:Whip it!";
        StringTokenizer tok = new TextDelimitedStringTokenizer(":", "{", "}");
        List<String> tokens = tok.tokenize(test);

        Assert.assertEquals("Uno", tokens.get(0));
        Assert.assertEquals("Two:Three", tokens.get(1));
        Assert.assertEquals("eight", tokens.get(2));
        Assert.assertEquals("Whip it!", tokens.get(3));

        test = "Uno:{-Two:Three-}:eight:Whip it!";
        tok = new TextDelimitedStringTokenizer(":", "{-", "-}");
        tokens = tok.tokenize(test);

        Assert.assertEquals("Uno", tokens.get(0));
        Assert.assertEquals("Two:Three", tokens.get(1));
        Assert.assertEquals("eight", tokens.get(2));
        Assert.assertEquals("Whip it!", tokens.get(3));
    }

    /**
     * Test {@link TextDelimitedStringTokenizer#tokenize(String)} when the text
     * delimiter is a single character.
     */
    @Test
    public void testSingleCharacterTextDelimiter()
    {
        String test = "Zero:|Two:Three|:Quattro:Gollum";
        StringTokenizer tok = new TextDelimitedStringTokenizer(":", "|");
        List<String> tokens = tok.tokenize(test);

        Assert.assertEquals("Zero", tokens.get(0));
        Assert.assertEquals("Two:Three", tokens.get(1));
        Assert.assertEquals("Quattro", tokens.get(2));
        Assert.assertEquals("Gollum", tokens.get(3));
    }

    /**
     * Test {@link TextDelimitedStringTokenizer#tokenize(String)} when there is
     * only token delimiters and no text delimiters.
     */
    @Test
    public void testSingleCharacterTokenDelimiter()
    {
        String test = ":One : Two: Three ::     :Four:Five:";
        StringTokenizer tok = new TextDelimitedStringTokenizer(":");
        List<String> tokens = tok.tokenize(test);

        Assert.assertEquals("", tokens.get(0));
        Assert.assertEquals("One", tokens.get(1));
        Assert.assertEquals("Two", tokens.get(2));
        Assert.assertEquals("Three", tokens.get(3));
        Assert.assertEquals("", tokens.get(4));
        Assert.assertEquals("", tokens.get(5));
        Assert.assertEquals("Four", tokens.get(6));
        Assert.assertEquals("Five", tokens.get(7));
    }
}
