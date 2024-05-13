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
 * 20200825  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.pay.record;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;

import com.evp.bizlib.data.entity.TransData;

public class TransDataSourceFactory extends DataSource.Factory<Integer, TransData> {
    private String acquirerName;

    public TransDataSourceFactory(String acquirerName) {
        this.acquirerName = acquirerName;
    }

    /**
     * Create a DataSource.
     * <p>
     * The DataSource should invalidate itself if the snapshot is no longer valid. If a
     * DataSource becomes invalid, the only way to query more data is to create a new DataSource
     * from the Factory.
     * <p>
     * {@link LivePagedListBuilder} for example will construct a new PagedList and DataSource
     * when the current DataSource is invalidated, and pass the new PagedList through the
     * {@code LiveData<PagedList>} to observers.
     *
     * @return the new DataSource.
     */
    @NonNull
    @Override
    public DataSource<Integer, TransData> create() {
        return new TransDataSource(acquirerName);
    }
}
