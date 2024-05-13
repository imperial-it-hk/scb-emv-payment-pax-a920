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
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.ViewPager;

import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.app.quickclick.QuickClickProtection;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 菜单选项
 *
 * @author Steven.W
 */
public class MenuPage extends LinearLayout {
    private Context context;

    /*
     * 菜单项列表
     */
    private List<GridViewAdapter.GridItem> itemList;

    private WrapContentHeightViewPager mViewPager;
    /*
     * 页面指示器（。。。）容器
     */
    private LinearLayout pageIndicatorLayout;
    /*
     * 页面指示器（。。。）
     */
    private ImageView[] pageIndicator;
    /*
     * 总页面数
     */
    private int numPages;
    /*
     * 当前页面索引
     */
    private int currentPageIndex;
    /*
     * 每页最大显示item数目
     */
    private int maxItemNumPerPage = 9;
    /*
     * 列数
     */
    private int columns = 3;


    private QuickClickProtection quickClickProtection = QuickClickProtection.getInstance();
    private OnProcessListener processListener;

    /**
     * Instantiates a new Menu page.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public MenuPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.itemList = new ArrayList<>();
        initView();
    }

    /**
     * Instantiates a new Menu page.
     *
     * @param context           the context
     * @param maxItemNumPerPage the max item num per page
     * @param columns           the columns
     * @param list              the list
     */
    public MenuPage(Context context, int maxItemNumPerPage, int columns, List<GridViewAdapter.GridItem> list) {
        super(context);
        this.context = context;
        this.columns = columns;
        this.maxItemNumPerPage = maxItemNumPerPage;
        this.itemList = list;
        initView();
        initPageIndicator();
        initOptionsMenu();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_menu, null);
        mViewPager = (WrapContentHeightViewPager) view.findViewById(R.id.view_pager);
        pageIndicatorLayout = (LinearLayout) view.findViewById(R.id.ll_dots);
        int color = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.SECONDARY_COLOR));
        view.setBackgroundColor(color);
        mViewPager.setBackgroundColor(color);
        addView(view);
    }

    /**
     * 设置当前页面指示器
     *
     * @param position
     */
    private void setCurrentIndicator(int position) {
        if (position < 0 || position > numPages - 1 || currentPageIndex == position) {
            return;
        }
        for (ImageView i : pageIndicator) {
            i.setImageDrawable(FinancialApplication.getApp().getResources().getDrawable(R.drawable.guide_dot_normal));
        }
        pageIndicator[position].setImageDrawable(FinancialApplication.getApp().getResources().getDrawable(R.drawable.guide_dot_select));
        currentPageIndex = position;
    }

    /**
     * 获取每个页面girdView
     *
     * @param pageIndex
     * @return
     */
    private View getViewPagerItem(int pageIndex) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.viewpage_gridview, null);
        CustomGridView gridView = (CustomGridView) layout.findViewById(R.id.vp_gv);
        gridView.setNumColumns(columns);
        gridView.setVerticalSpacing(10);
        gridView.setHorizontalSpacing(10);
        GridViewAdapter adapter = new GridViewAdapter(context,
                itemList.subList(pageIndex * maxItemNumPerPage,
                        Math.min((pageIndex + 1) * maxItemNumPerPage, itemList.size())));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 连续多次点击未处理
                if (quickClickProtection.isStarted()) {
                    return;
                }
                quickClickProtection.start();
                if ((position + currentPageIndex * maxItemNumPerPage) < itemList.size()) {
                    if (itemList.get(position + currentPageIndex * maxItemNumPerPage).getIndex() != null) {
                        int tmp = Utils.parseIntSafe(itemList.get(position + currentPageIndex * maxItemNumPerPage).getIndex(), -1);
                        if(tmp == -1) {
                            process(itemList.get(position + currentPageIndex * maxItemNumPerPage).getIndex());
                        } else {
                            process(tmp);
                        }
                    } else {
                        process(position + currentPageIndex * maxItemNumPerPage);
                    }
                }
            }
        });
        gridView.setLongClickable(false);
        return gridView;
    }

    /**
     * 点击菜单项目处理
     *
     * @param index
     */
    private void process(int index) {
        processListener.process(index);
    }

    private void process(String index) {
        processListener.process(index);
    }

    /**
     * Init options menu.
     */
// 初始化选项菜单
    public void initOptionsMenu() {
        List<View> gridViewList = new ArrayList<>();
        for (int i = 0; i < numPages; i++) {
            gridViewList.add(getViewPagerItem(i));
        }
        mViewPager.setAdapter(new ViewPagerAdapter(gridViewList));
    }

    /**
     * 设置ViewPager显示position指定的页面
     *
     * @param position the position
     */
    public void setCurrentPager(int position) {
        mViewPager.setCurrentItem(position);
    }

    /**
     * Init page indicator.
     */
// 初始化指示点
    public void initPageIndicator() {

        if (itemList.size() % maxItemNumPerPage == 0) {
            numPages = itemList.size() / maxItemNumPerPage;
        } else {
            numPages = itemList.size() / maxItemNumPerPage + 1;
        }
        if (0 < numPages) {
            pageIndicatorLayout.removeAllViews();
            if (1 == numPages) {
                pageIndicatorLayout.setVisibility(View.GONE);
            } else if (1 < numPages) {
                pageIndicatorLayout.setVisibility(View.VISIBLE);
                for (int j = 0; j < numPages; j++) {
                    ImageView image = new ImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20); // dot的宽高
                    params.setMargins(10, 0, 10, 0);
                    image.setImageDrawable(FinancialApplication.getApp().getResources().getDrawable(R.drawable.guide_dot_normal));
                    pageIndicatorLayout.addView(image, params);
                }
            }
        }
        if (numPages != 1) {
            pageIndicator = new ImageView[numPages];
            for (int i = 0; i < numPages; i++) {
                pageIndicator[i] = (ImageView) pageIndicatorLayout.getChildAt(i);
            }
            currentPageIndex = 0;
            pageIndicator[currentPageIndex].setImageDrawable(FinancialApplication.getApp().getResources().getDrawable(R.drawable.guide_dot_select));
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageSelected(int index) {
                    setCurrentIndicator(index);
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                    //do nothing
                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                    //do nothing
                }
            });
        }

    }

    /**
     * The type Builder.
     */
    public static class Builder {
        private Context context;
        private int maxItemNumPerPage;
        private int columns;
        private List<GridViewAdapter.GridItem> itemList;

        /**
         * Instantiates a new Builder.
         *
         * @param context           the context
         * @param maxItemNumPerPage the max item num per page
         * @param columns           the columns
         */
        public Builder(Context context, int maxItemNumPerPage, int columns) {
            this.context = context;
            this.maxItemNumPerPage = maxItemNumPerPage;
            this.columns = columns;
        }

        /**
         * Instantiates a new Builder.
         *
         * @param context           the context
         * @param maxItemNumPerPage the max item num per page
         * @param columns           the columns
         * @param itemList          the item list
         */
        public Builder(Context context, int maxItemNumPerPage, int columns, List<GridViewAdapter.GridItem> itemList) {
            this.context = context;
            this.maxItemNumPerPage = maxItemNumPerPage;
            this.columns = columns;
            this.itemList = new ArrayList<>();
            this.itemList.addAll(itemList);
        }

        /**
         * 设置与交易相关的菜单项
         *
         * @param title 菜单项的名称
         * @param icon  菜单项的图片ID
         * @return builder
         */
        public Builder addTransItem(String title, int icon) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridViewAdapter.GridItem(title, icon));
            return this;
        }

        public Builder addTransItem(String title, Bitmap icon, String index) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridViewAdapter.GridItem(title, icon, index));
            return this;
        }

        /**
         * 设置与交易无关的菜单项,只负责Activity的跳转
         *
         * @param title 菜单项的名称
         * @param icon  菜单项的图片ID
         * @return builder
         */
        public Builder addMenuItem(String title, int icon) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridViewAdapter.GridItem(title, icon));
            return this;
        }

        public Builder addMenuItem(String title, Bitmap icon, String index) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridViewAdapter.GridItem(title, icon, index));
            return this;
        }

        /**
         * 按菜单项的名称来移除，后续可根据需求增加不同参数的removeMenuItem方法
         *
         * @param title the title
         * @return builder
         */
        public Builder removeMenuItem(String title) {
            Iterator<GridViewAdapter.GridItem> it = itemList.iterator();
            while (it.hasNext()) {
                if (it.next().getName().equals(title)) {
                    it.remove();
                }
            }
            return this;
        }

        /**
         * 设置非交易类,使用Action跳转的菜单项
         *
         * @param title 菜单项的名称
         * @param icon  菜单项的图片ID
         * @return builder
         */
        public Builder addActionItem(String title, int icon) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridViewAdapter.GridItem(title, icon));
            return this;

        }

        public Builder addActionItem(String title, Bitmap icon, String index) {
            if (itemList == null) {
                itemList = new ArrayList<>();
            }
            itemList.add(new GridViewAdapter.GridItem(title, icon, index));
            return this;
        }

        /**
         * 创建并返回MenuPage视图
         *
         * @return menu page
         */
        public MenuPage create() {
            return new MenuPage(context, maxItemNumPerPage, columns, itemList);
        }

        public int getItemsCount() {
            if(itemList == null) {
                return 0;
            }
            return itemList.size();
        }
    }

    /**
     * 回炉再造，从MenuPage变回Builder
     *
     * @return builder
     */
    public Builder newBuilder() {
        return new Builder(this.context, this.maxItemNumPerPage, this.columns, this.itemList);
    }

    /**
     * Sets on process listener.
     *
     * @param processListener the process listener
     */
    public void setOnProcessListener(OnProcessListener processListener) {
        this.processListener = processListener;
    }

    /**
     * The interface On process listener.
     */
    public interface OnProcessListener {
        /**
         * Process.
         *
         * @param index the index
         */
        void process(int index);

        default void process(String index) {
            process(Utils.parseIntSafe(index, 0));
        }
    }
}
