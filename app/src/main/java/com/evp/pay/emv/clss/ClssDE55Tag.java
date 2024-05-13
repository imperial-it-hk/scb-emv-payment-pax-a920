/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2019-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date                  Author	                 Action
 * 20190108  	         lixc                    Create
 * ===========================================================================================
 */
package com.evp.pay.emv.clss;

import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.EKernelType;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Clss de 55 tag.
 */
class ClssDE55Tag {
    private int emvTag;
    private byte option;
    private int len;

    /**
     * The constant DE55_MUST_SET.
     */
    public static final byte DE55_MUST_SET = 0x10;// 必须存在
    /**
     * The constant DE55_OPT_SET.
     */
    public static final byte DE55_OPT_SET = 0x20;// 可选择存在
    /**
     * The constant DE55_COND_SET.
     */
    public static final byte DE55_COND_SET = 0x30;// 根据条件存在

    /**
     * Instantiates a new Clss de 55 tag.
     *
     * @param emvTag the emv tag
     * @param option the option
     * @param len    the len
     */
    public ClssDE55Tag(int emvTag, byte option, int len) {
        this.emvTag = emvTag;
        this.option = option;
        this.len = len;
    }

    /**
     * Gets emv tag.
     *
     * @return the emv tag
     */
    public int getEmvTag() {
        return emvTag;
    }

    /**
     * Sets emv tag.
     *
     * @param emvTag the emv tag
     */
    public void setEmvTag(int emvTag) {
        this.emvTag = emvTag;
    }

    /**
     * Gets option.
     *
     * @return the option
     */
    public byte getOption() {
        return option;
    }

    /**
     * Sets option.
     *
     * @param option the option
     */
    public void setOption(byte option) {
        this.option = option;
    }

    /**
     * Gets len.
     *
     * @return the len
     */
    public int getLen() {
        return len;
    }

    /**
     * Sets len.
     *
     * @param len the len
     */
    public void setLen(int len) {
        this.len = len;
    }

    /**
     * Gen clss de 55 tags list.
     *
     * @return the list
     */
// clss sale tags list
    public static List<ClssDE55Tag> genClssDE55Tags(EKernelType kernel) {
        List<ClssDE55Tag> clssDE55Tags = new ArrayList<>();

        //Master card only
        if(kernel == EKernelType.MC) {
            clssDE55Tags.add(new ClssDE55Tag(0x5F2A, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.PAN_SEQ_NO, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x82, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x84, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TVR, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TRANS_DATE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TRANS_TYPE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.AMOUNT, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.AMOUNT_OTHER, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F09, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F10, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.COUNTRY_CODE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F1E, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.APP_CRYPTO, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.CRYPTO, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F33, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F34, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.ATC, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F37, DE55_MUST_SET, 0));
        }
        //UnionPay only
        else if(kernel == EKernelType.PBOC) {
            clssDE55Tags.add(new ClssDE55Tag(0x5F2A, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.PAN_SEQ_NO, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x82, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x84, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TVR, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TRANS_DATE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TRANS_TYPE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.AMOUNT, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.AMOUNT_OTHER, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F09, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F10, DE55_OPT_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.COUNTRY_CODE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F1E, DE55_OPT_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.APP_CRYPTO, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.CRYPTO, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F33, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F35, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.ATC, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F37, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F41, DE55_OPT_SET, 0));
        } else {
            clssDE55Tags.add(new ClssDE55Tag(0x5F2A, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x82, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x84, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TVR, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TRANS_DATE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.TRANS_TYPE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.AMOUNT, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.AMOUNT_OTHER, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F10, DE55_OPT_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.COUNTRY_CODE, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F1E, DE55_OPT_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.APP_CRYPTO, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.CRYPTO, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F33, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F34, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(TagsTable.ATC, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F37, DE55_MUST_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F5B, DE55_OPT_SET, 0));
            clssDE55Tags.add(new ClssDE55Tag(0x9F6E, DE55_OPT_SET, 0));
        }

        return clssDE55Tags;
    }
}
