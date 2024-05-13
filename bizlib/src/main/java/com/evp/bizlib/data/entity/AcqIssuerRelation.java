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

import com.evp.bizlib.data.local.db.dao.AcqIssuerRelationDao;
import com.evp.bizlib.data.local.db.dao.AcquirerDao;
import com.evp.bizlib.data.local.db.dao.DaoSession;
import com.evp.bizlib.data.local.db.dao.IssuerDao;
import com.evp.bizlib.data.local.db.helper.DaoManager;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * Acquirer & Issuer Relation table
 */
@Entity(nameInDb = "acq_issuer_relation")
public class AcqIssuerRelation {

    @Id(autoincrement = true)
    private Long id;

    private long acquirer_id;
    @ToOne(joinProperty = "acquirer_id")
    private Acquirer acquirer;

    private long issuer_id;
    @ToOne(joinProperty = "issuer_id")
    private Issuer issuer;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1528047339)
    private transient AcqIssuerRelationDao myDao;

    @Generated(hash = 86676445)
    private transient Long acquirer__resolvedKey;

    @Generated(hash = 1724583466)
    private transient Long issuer__resolvedKey;

    public AcqIssuerRelation() {
    }

    public AcqIssuerRelation(Acquirer acquirer, Issuer issuer) {
        this.setAcquirer(acquirer);
        this.setIssuer(issuer);
    }

    public AcqIssuerRelation(Long id, Acquirer acquirer, Issuer issuer) {
        this.setId(id);
        this.setAcquirer(acquirer);
        this.setIssuer(issuer);
    }

    @Generated(hash = 625776086)
    public AcqIssuerRelation(Long id, long acquirer_id, long issuer_id) {
        this.id = id;
        this.acquirer_id = acquirer_id;
        this.issuer_id = issuer_id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getAcquirer_id() {
        return this.acquirer_id;
    }

    public void setAcquirer_id(long acquirer_id) {
        this.acquirer_id = acquirer_id;
    }

    public long getIssuer_id() {
        return this.issuer_id;
    }

    public void setIssuer_id(long issuer_id) {
        this.issuer_id = issuer_id;
    }


    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 346746627)
    public void setAcquirer(@NotNull Acquirer acquirer) {
        if (acquirer == null) {
            throw new DaoException(
                    "To-one property 'acquirer_id' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.acquirer = acquirer;
            acquirer_id = acquirer.getId();
            acquirer__resolvedKey = acquirer_id;
        }
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1331604593)
    public void setIssuer(@NotNull Issuer issuer) {
        if (issuer == null) {
            throw new DaoException(
                    "To-one property 'issuer_id' has not-null constraint; cannot set to-one to null");
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
    public Acquirer getAcquirer() {
        long __key = this.acquirer_id;
        if (acquirer__resolvedKey == null || !acquirer__resolvedKey.equals(__key)) {
            DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                daoSession = DaoManager.getInstance().getDaoSession();
            }
            AcquirerDao targetDao = daoSession.getAcquirerDao();
            Acquirer acquirerNew = targetDao.load(__key);
            synchronized (this) {
                acquirer = acquirerNew;
                acquirer__resolvedKey = __key;
            }
        }
        return acquirer;
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
    @Generated(hash = 649062456)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAcqIssuerRelationDao() : null;
    }

}
