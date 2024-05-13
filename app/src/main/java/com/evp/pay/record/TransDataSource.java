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
import androidx.paging.PageKeyedDataSource;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;

import java.util.List;

public class TransDataSource extends PageKeyedDataSource<Integer, TransData> {
    private static final String TAG = "TransDataSource";
    private List<TransData> transDataList;
    private String acquirerName;
    private int initialLoadSize;
    private int pageItemSize;

    public TransDataSource(String acquirerName) {
        this.acquirerName = acquirerName;
    }

    /**
     * Load initial data.
     * <p>
     * This method is called first to initialize a PagedList with data. If it's possible to count
     * the items that can be loaded by the DataSource, it's recommended to pass the loaded data to
     * the callback via the three-parameter
     * {@link LoadInitialCallback#onResult(List, int, int, Object, Object)}. This enables PagedLists
     * presenting data from this source to display placeholders to represent unloaded items.
     * <p>
     * {@link LoadInitialParams#requestedLoadSize} is a hint, not a requirement, so it may be may be
     * altered or ignored.
     *
     * @param params   Parameters for initial load, including requested load size.
     * @param callback Callback that receives initial load data.
     */
    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, TransData> callback) {
        initialLoadSize = params.requestedLoadSize;
        load(0,initialLoadSize);
        callback.onResult(transDataList,0,1);
    }

    /**
     * Prepend page with the key specified by {@link LoadParams#key LoadParams.key}.
     * <p>
     * It's valid to return a different list size than the page size if it's easier, e.g. if your
     * backend defines page sizes. It is generally safer to increase the number loaded than reduce.
     * <p>
     * Data may be passed synchronously during the load method, or deferred and called at a
     * later time. Further loads going down will be blocked until the callback is called.
     * <p>
     * If data cannot be loaded (for example, if the request is invalid, or the data would be stale
     * and inconsistent, it is valid to call {@link #invalidate()} to invalidate the data source,
     * and prevent further loading.
     *
     * @param params   Parameters for the load, including the key for the new page, and requested load
     *                 size.
     * @param callback Callback that receives loaded data.
     */
    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, TransData> callback) {
        LogUtils.d(TAG,params.key);
    }

    /**
     * Append page with the key specified by {@link LoadParams#key LoadParams.key}.
     * <p>
     * It's valid to return a different list size than the page size if it's easier, e.g. if your
     * backend defines page sizes. It is generally safer to increase the number loaded than reduce.
     * <p>
     * Data may be passed synchronously during the load method, or deferred and called at a
     * later time. Further loads going down will be blocked until the callback is called.
     * <p>
     * If data cannot be loaded (for example, if the request is invalid, or the data would be stale
     * and inconsistent, it is valid to call {@link #invalidate()} to invalidate the data source,
     * and prevent further loading.
     *
     * @param params   Parameters for the load, including the key for the new page, and requested load
     *                 size.
     * @param callback Callback that receives loaded data.
     */
    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, TransData> callback) {
        int page = params.key;
        pageItemSize = params.requestedLoadSize;
        load(page,pageItemSize);
        callback.onResult(transDataList,page+1);
    }

    public void load(int page,int limit) {
        int offset = 0;
        if (page > 0) {
            offset = initialLoadSize + pageItemSize * (page - 1);
        }
        //加载limit条数据
        Acquirer acquirer = FinancialApplication.getAcqManager().findAcquirer(acquirerName);
        if (acquirer != null) {
            transDataList = GreendaoHelper.getTransDataHelper().findPagingTransData(acquirer, false, offset, limit);
        }
    }


}
