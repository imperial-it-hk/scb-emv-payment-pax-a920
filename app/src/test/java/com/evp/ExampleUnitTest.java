/*
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2019-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date	                 Author	                Action
 * 20190318  	         xieYb                  Create
 */

package com.evp;


import com.evp.poslib.gl.impl.GL;
import com.pax.gl.pack.ITlv;
import com.pax.gl.pack.exception.TlvException;

import org.junit.Test;

import java.util.Base64;
import java.util.List;

/**
 * The type Example unit test.
 */
public class ExampleUnitTest {
    /**
     * Test byte.
     */
    @Test
    public void testByte() {
//        byte[] receiptData = new byte[3];
//        receiptData[0]=0;
//        receiptData[1]=1;
//        receiptData[2]=2;
//        byte[] field63 = new byte[2 + receiptData.length];
//        byte[] subField = "29".getBytes();
//        System.arraycopy(subField, 0, field63, 0, subField.length);
//        System.arraycopy(receiptData, 0, field63, 2, receiptData.length);
//        byte[] result = new byte[]{(byte) 50, (byte)57, (byte)0, (byte)1, (byte)2};
//        assertArrayEquals(field63,result);

//        IConvert convert = new ConverterGLImp();
//        byte[] cpv01s = convert.strToBcdPaddingLeft("CPV01");
//        System.out.println(Arrays.toString(cpv01s));
//        System.out.println(Arrays.toString("CPV01".getBytes()));
        String qr = "hQVDUFYwMWETTwegAAAAVVVVUAhQcm9kdWN0MWETTwegAAAAZmZmUAhQcm9kdWN0MmJJWggSNFZ4kBI0WF8gDkNBUkRIT0xERVIvRU1WXy0IcnVlc2RlZW5kIZ8QBwYBCgMAAACfJghYT9OF+iNLzJ82AgABnzcEbVjvEw==";
        byte[] decode = Base64.getDecoder().decode(qr);
        ITlv tlv  = GL.getGL().getPacker().getTlv();
        try {
            ITlv.ITlvDataObjList unpack = tlv.unpack(decode);
            byte[] valueByTag = unpack.getValueByTag(0x61);
            List<byte[]> valueListByTag = unpack.getValueListByTag(0x61);
            System.out.println("hhhh");
        } catch (TlvException e) {
            e.printStackTrace();
        }
    }

    /**
     * Testst.
     */
    @Test
    public void testst(){
        byte[] command = new byte[]{0,-92,4,0};
        byte[] bytes = "2PAY.SYS.DDF01".getBytes();
        byte[] dataIn = new byte[]{50,80,65,89,46,83,89,83,46,68,68,70,48,49};
        byte[] request=new byte[]{0,-92,4,0,0,14,50,80,65,89,46,83,89,83,46,68,68,70,48,49,1,0};
        byte[] right = new byte[]{0,-92,4,0,14,50,80,65,89,46,83,89,83,46,68,68,70,48,49,0};
        short le = (short) 0xFFFF;
        System.out.println("hhhh");
    }
}
