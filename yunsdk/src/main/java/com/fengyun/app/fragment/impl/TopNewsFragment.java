package com.fengyun.app.fragment.impl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.fengyun.R;
import com.fengyun.app.fragment.BaseFragment;
import com.fengyun.app.fragment.ITopNewsFragment;
import com.fengyun.app.ibehavior.IListAcitivty;
import com.fengyun.http.presenter.topnews.ITopNewsPresenter;
import com.fengyun.http.presenter.topnews.impl.TopNewsPrensenter;
import com.fengyun.model.topnews.NewsList;
import com.fengyun.view.adapter.TopNewsAdapter;
import com.fengyun.view.recycler.GridItemDividerDecoration;
import com.fengyun.widget.WrapContentLinearLayoutManager;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xinghongfei on 16/8/17.
 */
public class TopNewsFragment extends BaseFragment implements ITopNewsFragment{

    private int currentIndex;
    private ITopNewsPresenter mTopNewsPrensenter;

    RecyclerView recycle;
    ProgressBar progress;

    private boolean isLoading;

    private TopNewsAdapter mTopNewsAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView.OnScrollListener loadingMoreListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.topnews_fragment_layout, container, false);
        recycle = (RecyclerView) view.findViewById(R.id.recycle_topnews);
        progress = (ProgressBar) view.findViewById(R.id.prograss);
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTopNewsPrensenter = new TopNewsPrensenter((ITopNewsFragment) this);
        mTopNewsAdapter = new TopNewsAdapter(getContext(), ((IListAcitivty)getContext()).getDetailAcitivity());

        loadingMoreListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) //向下滚动
                {
                    int visibleItemCount = mLinearLayoutManager.getChildCount();
                    int totalItemCount = mLinearLayoutManager.getItemCount();
                    int pastVisiblesItems = mLinearLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        isLoading = true;
                        loadMoreDate();
                    }
                }
            }
        };

        mLinearLayoutManager = new WrapContentLinearLayoutManager(getContext());
        recycle.setLayoutManager(mLinearLayoutManager);
        recycle.setHasFixedSize(true);
        recycle.addItemDecoration(new GridItemDividerDecoration(getContext(), R.dimen.divider_height, R.color.divider));
        // TODO: 16/8/13 add  animation
        recycle.setItemAnimator(new DefaultItemAnimator());
        recycle.setAdapter(mTopNewsAdapter);
        recycle.addOnScrollListener(loadingMoreListener);

        loadDate();
    }

    private void loadDate() {
        if (mTopNewsAdapter.getItemCount() > 0) {
            mTopNewsAdapter.clearData();
        }
        currentIndex = 0;
        mTopNewsPrensenter.getNewsList(currentIndex);

    }

    private void loadMoreDate() {
        mTopNewsAdapter.loadingStart();
        currentIndex += 20;
        mTopNewsPrensenter.getNewsList(currentIndex);
    }

    @Override
    public void updateListItem(NewsList newsList) {
        isLoading = false;
        progress.setVisibility(View.INVISIBLE);
        mTopNewsAdapter.addItems(newsList.getNewsList());
    }

    @Override
    public void showProgressDialog() {
        if (currentIndex == 0) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hidProgressDialog() {
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showError(String error) {
        if (recycle != null) {
            Snackbar.make(recycle, getString(R.string.snack_infor), Snackbar.LENGTH_SHORT).setAction("重试", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTopNewsPrensenter.getNewsList(currentIndex);
                }
            }).show();

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTopNewsPrensenter.unsubscrible();
    }
}
