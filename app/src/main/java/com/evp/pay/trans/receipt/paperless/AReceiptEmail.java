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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.pay.trans.receipt.paperless;

import android.graphics.Bitmap;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.receipt.PrintListener;
import com.evp.pay.utils.EmailInfo;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.mail.resolver.DataSourceFileResolver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The type A receipt email.
 */
abstract class AReceiptEmail {

    private static final String TAG = "ReceiptEmail";

    /**
     * The Listener.
     */
    protected PrintListener listener;

    /**
     * Send text email int.
     *
     * @param emailInfo    the email info
     * @param emailAddress the email address
     * @param subject      the subject
     * @param content      the content
     * @return the int
     */
//send email
    public int sendTextEmail(EmailInfo emailInfo, String emailAddress, String subject, String content) {
        try {
            Email email = new SimpleEmail();
            setBaseInfo(emailInfo, email);
            email.setSubject(subject);
            email.setMsg(content);
            email.addTo(emailAddress);
            email.send();
        } catch (EmailException e) {
            LogUtils.e(TAG, "", e);
            return -1;
        }

        return 0;
    }

    /**
     * Send html email int.
     *
     * @param emailInfo    the email info
     * @param emailAddress the email address
     * @param subject      the subject
     * @param content      the content
     * @param pic          the pic
     * @return the int
     */
    public int sendHtmlEmail(EmailInfo emailInfo, String emailAddress, String subject, String content, Bitmap pic) {
        String placeHolder = "<img/>";
        String htmlMsg = "<html><body>" +
                placeHolder + "</body></html>";
        try {
            ImageHtmlEmail email = new ImageHtmlEmail();

            setBaseInfo(emailInfo, email);
            File file = convert(pic, "receipt_tmp.jpg");
            email.setDataSourceResolver(new DataSourceFileResolver(file));
            String cid = email.embed(file); // 将图片嵌入邮件中，返回cid
            String img = "<img src='cid:" + cid + "'/>"; // 构造img标签，图片源为cid
            htmlMsg = htmlMsg.replace(placeHolder, img); // 替换html邮件正文中的占位符

            email.setSubject(subject);
            email.setTextMsg(content);
            email.setHtmlMsg(htmlMsg);
            email.addTo(emailAddress);
            email.send();
        } catch (EmailException | IOException e) {
            LogUtils.e(TAG, "", e);
            return -1;
        }
        return 0;
    }

    private int setBaseInfo(EmailInfo emailInfo, Email email) {
        try {
            email.setHostName(emailInfo.getHostName());
            email.setSmtpPort(emailInfo.getPort());
            email.setAuthentication(emailInfo.getUserName(), emailInfo.getPassword());
            email.setCharset("UTF-8");
            email.setSSLOnConnect(emailInfo.isSsl());
            if (emailInfo.isSsl())
                email.setSslSmtpPort(String.valueOf(emailInfo.getSslPort()));
            email.setFrom(emailInfo.getFrom());
        } catch (EmailException e) {
            LogUtils.e(TAG, "", e);
            return -1;
        }
        return 0;
    }

    private File convert(Bitmap bm, String fileName) throws IOException {
        String path = FinancialApplication.getApp().getFilesDir() + File.separator + "temp/";
        File dirFile = new File(path);
        if (!dirFile.exists() && !dirFile.mkdir()) {
            throw new IOException("Failed to mkdir");
        }
        File myCaptureFile = new File(path + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        return myCaptureFile;
    }
}
