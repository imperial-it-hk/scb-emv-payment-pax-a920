package com.evp.bizlib.tle;

public class TleConst {
    public static final String TLE_HEAD          = "HTLE";
    public static final String ENC_METHOD        = "2";
    public static final String KEY_SCHEME        = "0";
    public static final String MAC_ALGO          = "1";
    public static final String SALT              = "1";
    public static final int    HEADER_SIZE       = 41;
    public static final int    SALT_SIZE         = 8;
    public static final int    SALT_POSITION     = 41;
    public static final int    SIZE_POSITION     = 31;
    public static final int    ENC_DATA_POSITION = 49;
    public static final short  MSG_TYPE_SIZE     = 2;
    public static final short  MAC_BITMAP_POS    = 7;
    public static final byte[] NO_PIN_TRANSLATE  = new byte[8];
    public static final byte[] PIN_TRANSLATE     = new byte[]{'P',0x00,0x00,0x00,0x00,0x00,0x00,0x00};
    public static final byte[] EMPTY_TWK_ID      = new byte[]{0x30, 0x30, 0x30, 0x30};
    public static final byte[] TWK_ID_FILL       = new byte[6];
    public static final int    FIRST_ISO_FIELD   = 2;
    public static final int    LAST_ISO_FIELD    = 192;
    public static final int    TMK_FIELD62_SIZE  = 293;
    public static final int    TWK_FIELD62_SIZE  = 62;
    //TMK & TWK download constants
    public static final String DL_TYPE           = "4";
    public static final String RQ_TYPE           = "1";
    public static final String PIN_HASH_TAIL     = "1234";
    public static final byte[] HASH3_PREFIX      = new byte[]{(byte)0xA2};
    public static final byte[] HASH3_POSTFIX     = new byte[]{(byte)0x00, (byte)0x00, (byte)0x00};
    public static final short  HASH3_LENGTH      = 4;
    public static final int    RSA_KEY_LENGTH    = 2048;
    public static final String PIN_KEY_RQ        = "PK1";
}
