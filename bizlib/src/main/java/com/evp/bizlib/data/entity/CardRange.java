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
package com.evp.bizlib.data.entity;

import androidx.annotation.NonNull;

import com.evp.bizlib.data.local.db.dao.CardRangeDao;
import com.evp.bizlib.data.local.db.dao.DaoSession;
import com.evp.bizlib.data.local.db.dao.IssuerDao;
import com.evp.bizlib.data.local.db.helper.DaoManager;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

/**
 * card range table
 */
@Entity(nameInDb = "card_range")
public class CardRange implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ID_FIELD_NAME = "card_id";
    public static final String NAME_FIELD_NAME = "card_name";
    private static final String ISSUER_NAME = "issuer_name";
    public static final String RANGE_LOW_FIELD_NAME = "card_range_low";
    public static final String RANGE_HIGH_FIELD_NAME = "card_range_high";
    public static final String LENGTH_FIELD_NAME = "card_length";

    @Id(autoincrement = true)
    @Property(nameInDb = ID_FIELD_NAME)
    private Long id;

    @Property(nameInDb = NAME_FIELD_NAME)
    private String name;

    @Property(nameInDb = ISSUER_NAME)
    private String issuerName;

    @Property(nameInDb = RANGE_LOW_FIELD_NAME)
    @NotNull
    @Unique
    private String panRangeLow;

    @Property(nameInDb = RANGE_HIGH_FIELD_NAME)
    @NotNull
    @Unique
    private String panRangeHigh;

    @Property(nameInDb = LENGTH_FIELD_NAME)
    private int panLength;

    private long issuer_id;
    @ToOne(joinProperty = "issuer_id")
    private Issuer issuer;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 47675115)
    private transient CardRangeDao myDao;

    @Generated(hash = 1724583466)
    private transient Long issuer__resolvedKey;

    public CardRange() {
    }

    public CardRange(String name, String panRangeLow, String panRangeHigh, int panLength, Issuer issuer) {
        this.setName(name);
        this.setPanRangeLow(panRangeLow);
        this.setPanRangeHigh(panRangeHigh);
        this.setPanLength(panLength);
        this.setIssuer(issuer);
    }

    public CardRange(Long id, String name, String panRangeLow, String panRangeHigh, int panLength, Issuer issuer) {
        this.setId(id);
        this.setName(name);
        this.setPanRangeLow(panRangeLow);
        this.setPanRangeHigh(panRangeHigh);
        this.setPanLength(panLength);
        this.setIssuer(issuer);
    }

    @Generated(hash = 1881461910)
    public CardRange(Long id, String name, String issuerName, @NotNull String panRangeLow, @NotNull String panRangeHigh, int panLength,
            long issuer_id) {
        this.id = id;
        this.name = name;
        this.issuerName = issuerName;
        this.panRangeLow = panRangeLow;
        this.panRangeHigh = panRangeHigh;
        this.panLength = panLength;
        this.issuer_id = issuer_id;
    }

    public void update(@NonNull CardRange cardRange) {
        name = cardRange.getName();
        panLength = cardRange.getPanLength();
        issuer = cardRange.getIssuer();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getPanRangeLow() {
        return this.panRangeLow;
    }

    public void setPanRangeLow(String panRangeLow) {
        this.panRangeLow = panRangeLow;
    }

    public String getPanRangeHigh() {
        return this.panRangeHigh;
    }

    public void setPanRangeHigh(String panRangeHigh) {
        this.panRangeHigh = panRangeHigh;
    }

    public int getPanLength() {
        return this.panLength;
    }

    public void setPanLength(int panLength) {
        this.panLength = panLength;
    }

    public long getIssuer_id() {
        return this.issuer_id;
    }

    public void setIssuer_id(long issuer_id) {
        this.issuer_id = issuer_id;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1331604593)
    public void setIssuer(@NotNull Issuer issuer) {
        if (issuer == null) {
            throw new DaoException("To-one property 'issuer_id' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.issuer = issuer;
            issuer_id = issuer.getId();
            issuer__resolvedKey = issuer_id;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Keep
    public Issuer getIssuer() {
        long __key = this.issuer_id;
        if (issuer__resolvedKey == null || !issuer__resolvedKey.equals(__key)) {
            DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                daoSession = DaoManager.getInstance().getDaoSession();
            }
            IssuerDao targetDao = daoSession.getIssuerDao();
            Issuer issuerNew = targetDao.load(__key);
            synchronized (this) {
                issuer = issuerNew;
                issuer__resolvedKey = __key;
            }
        }
        return issuer;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1937518686)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCardRangeDao() : null;
    }

}
