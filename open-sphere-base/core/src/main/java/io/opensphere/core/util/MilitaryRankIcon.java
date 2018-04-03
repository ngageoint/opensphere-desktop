package io.opensphere.core.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import io.opensphere.core.util.swing.SwingUtilities;

/** Enumerable representation of Military Rank webfont. */
public enum MilitaryRankIcon implements FontIconEnum
{
    /** Enumerable representation of PV2. */
    RANK_03_E2("a"),

    /** Enumerable representation of PFC. */
    RANK_03_E3("b"),

    /** Enumerable representation of CPL. */
    RANK_03_E4_1("c"),

    /** Enumerable representation of SPC. */
    RANK_03_E4_2("d"),

    /** Enumerable representation of SGT. */
    RANK_03_E5("e"),

    /** Enumerable representation of SSG. */
    RANK_03_E6("f"),

    /** Enumerable representation of SFC. */
    RANK_03_E7("g"),

    /** Enumerable representation of 1SG. */
    RANK_03_E8_1("h"),

    /** Enumerable representation of MSG. */
    RANK_03_E8_2("i"),

    /** Enumerable representation of SMA. */
    RANK_03_E9_1("j"),

    /** Enumerable representation of CSM. */
    RANK_03_E9_2("k"),

    /** Enumerable representation of SGM. */
    RANK_03_E9_3("l"),

    /** Enumerable representation of A1C. */
    RANK_04_E2("m"),

    /** Enumerable representation of Amn. */
    RANK_04_E3("n"),

    /** Enumerable representation of SrA. */
    RANK_04_E4("o"),

    /** Enumerable representation of SSgt. */
    RANK_04_E5("p"),

    /** Enumerable representation of TSgt. */
    RANK_04_E6("q"),

    /** Enumerable representation of MSgt. */
    RANK_04_E7("r"),

    /** Enumerable representation of SMSgt. */
    RANK_04_E8("s"),

    /** Enumerable representation of CMSgt. */
    RANK_04_E9_1("t"),

    /** Enumerable representation of CMSgt. */
    RANK_04_E9_2("u"),

    /** Enumerable representation of CCM. */
    RANK_04_E9_3("v"),

    /** Enumerable representation of CMSAF. */
    RANK_04_E9_4("w"),

    /** Enumerable representation of ?. */
    RANK_02_02_E2("x"),

    /** Enumerable representation of ?. */
    RANK_02_02_E3("y"),

    /** Enumerable representation of SO3. */
    RANK_02_02_E4("z"),

    /** Enumerable representation of SO2. */
    RANK_02_02_E5("A"),

    /** Enumerable representation of SO1. */
    RANK_02_02_E6("B"),

    /** Enumerable representation of SOC. */
    RANK_02_02_E7("C"),

    /** Enumerable representation of SOCS. */
    RANK_02_02_E9("D"),

    /** Enumerable representation of SOCM. */
    RANK_02_02_E9_1("E"),

    /** Enumerable representation of SA. */
    RANK_02_00_E2("F"),

    /** Enumerable representation of SN. */
    RANK_02_00_E3("G"),

    /** Enumerable representation of PO3. */
    RANK_02_00_E4("H"),

    /** Enumerable representation of PO2. */
    RANK_02_00_E5("I"),

    /** Enumerable representation of PO1. */
    RANK_02_00_E6("J"),

    /** Enumerable representation of CPO. */
    RANK_02_00_E7("K"),

    /** Enumerable representation of SCPO. */
    RANK_02_00_E8("L"),

    /** Enumerable representation of MCPO. */
    RANK_02_00_E9_01("M"),

    /** Enumerable representation of MCPON. */
    RANK_02_00_E9_02("N"),

    /** Enumerable representation of HA. */
    RANK_02_01_E2("O"),

    /** Enumerable representation of HN. */
    RANK_02_01_E3("P"),

    /** Enumerable representation of HM3. */
    RANK_02_01_E4("Q"),

    /** Enumerable representation of HM2. */
    RANK_02_01_E5("R"),

    /** Enumerable representation of HM1. */
    RANK_02_01_E6("S"),

    /** Enumerable representation of HMC. */
    RANK_02_01_E7("T"),

    /** Enumerable representation of HMCS. */
    RANK_02_01_E8("U"),

    /** Enumerable representation of HMCM. */
    RANK_02_01_E9("V"),

    /** Enumerable representation of CW2 / CW4. */
    RANK_01_CW2("W"),

    /** Enumerable representation of CW5. */
    RANK_01_CW5("X"),

    /** Enumerable representation of 2ndLt. */
    RANK_01_O1("Y"),

    /** Enumerable representation of 1stLt. */
    RANK_01_O2("Z"),

    /** Enumerable representation of Capt. */
    RANK_01_O3("0"),

    /** Enumerable representation of Maj. */
    RANK_01_O4("1"),

    /** Enumerable representation of LtCol. */
    RANK_01_O5("2"),

    /** Enumerable representation of Col. */
    RANK_01_O6("3"),

    /** Enumerable representation of BGen. */
    RANK_01_O7("4"),

    /** Enumerable representation of MajGen. */
    RANK_01_O8("5"),

    /** Enumerable representation of LtGen. */
    RANK_01_O9("6"),

    /** Enumerable representation of Gen. */
    RANK_01_O10("7"),

    /** Enumerable representation of W1 / CW3. */
    RANK_01_W1("8"),

    /** Enumerable representation of PFC. */
    RANK_01_E2("9"),

    /** Enumerable representation of LCpl. */
    RANK_01_E3("!"),

    /** Enumerable representation of Cpl. */
    RANK_01_E4("\""),

    /** Enumerable representation of Sgt. */
    RANK_01_E5("#"),

    /** Enumerable representation of SSgt. */
    RANK_01_E6("$"),

    /** Enumerable representation of GySgt. */
    RANK_01_E7("%"),

    /** Enumerable representation of MSgt. */
    RANK_01_E8_1("&"),

    /** Enumerable representation of 1stSgt. */
    RANK_01_E8_2("'"),

    /** Enumerable representation of MGySgt. */
    RANK_01_E9_1("("),

    /** Enumerable representation of SgtMaj. */
    RANK_01_E9_2(")"),

    /** Enumerable representation of SgtMajMarCor. */
    RANK_01_E9_3("*");

    static
    {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(SwingUtilities.MILITARY_RANK_FONT);
    }

    /**
     * The font code defining the icon.
     */
    private String myFontCode;

    /**
     * Creates a new font code enum instance.
     *
     * @param pFontCode the font code defining the icon.
     */
    private MilitaryRankIcon(String pFontCode)
    {
        myFontCode = pFontCode;
    }

    /**
     * Gets the value of the {@link #myFontCode} field.
     *
     * @return the value stored in the {@link #myFontCode} field.
     */
    @Override
    public String getFontCode()
    {
        return myFontCode;
    }

    @Override
    public Font getFont()
    {
        return SwingUtilities.MILITARY_RANK_FONT;
    }
}
