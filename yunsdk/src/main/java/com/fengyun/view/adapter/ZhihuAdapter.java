package com.fengyun.view.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fengyun.R;
import com.fengyun.app.ibehavior.ILoadingMore;
import com.fengyun.config.Config;
import com.fengyun.graphics.ObservableColorMatrix;
import com.fengyun.model.zhihu.ZhihuDailyItem;
import com.fengyun.request.target.DribbbleTarget;
import com.fengyun.util.DBUtils;
import com.fengyun.util.DensityUtils;
import com.fengyun.widget.BadgedFourThreeImageView;


import java.util.ArrayList;


public class ZhihuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ILoadingMore {

    private static final int TYPE_LOADING_MORE = -1;
    private static final int TYPE_NORMAL = 1;

    private boolean loadingMore;
    private int mImageWidth;
    private int mImageHeigh;
    private ArrayList<ZhihuDailyItem> zhihuDailyItems = new ArrayList<>();
    private Context mContext;
    private Class mZhihuDescribeActivity;

    public ZhihuAdapter(Context context, Class zhihuDescribeActivity) {

        this.mContext = context;
        float width = mContext.getResources().getDimension(R.dimen.image_width);
        mImageWidth = DensityUtils.dip2px(mContext, width);
        mImageHeigh = mImageWidth * 3 / 4;
        mZhihuDescribeActivity = zhihuDescribeActivity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_NORMAL:
                return new ZhihuViewHolder(LayoutInflater.from(mContext).inflate(R.layout.zhihu_layout_item, parent, false));

            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(LayoutInflater.from(mContext).inflate(R.layout.infinite_loading, parent, false));

        }
        return null;

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        int type = getItemViewType(position);
        switch (type) {
            case TYPE_NORMAL:
                bindViewHolderNormal((ZhihuViewHolder) holder, position);
                break;
            case TYPE_LOADING_MORE:
                bindLoadingViewHold((LoadingMoreHolder) holder, position);
                break;
        }


    }

    private void bindLoadingViewHold(LoadingMoreHolder holder, int position) {
        holder.progressBar.setVisibility(loadingMore == true ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindViewHolderNormal(final ZhihuViewHolder holder, final int position) {

        final ZhihuDailyItem zhihuDailyItem = zhihuDailyItems.get(holder.getAdapterPosition());

        if (DBUtils.getDB(mContext).isRead(Config.ZHIHU, zhihuDailyItem.getId(), 1))
            holder.textView.setTextColor(Color.GRAY);
        else
            holder.textView.setTextColor(Color.BLACK);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDescribeActivity(holder, zhihuDailyItem, mZhihuDescribeActivity);

            }
        });
        holder.textView.setText(zhihuDailyItem.getTitle());
        holder.linearLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startDescribeActivity(holder, zhihuDailyItem, mZhihuDescribeActivity);
                    }
                });


        Glide.with(mContext)
                .load(zhihuDailyItems.get(position).getImages()[0])
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (!zhihuDailyItem.hasFadedIn) {
                            holder.imageView.setHasTransientState(true);
                            final ObservableColorMatrix cm = new ObservableColorMatrix();
                            final ObjectAnimator animator = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f);
                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    holder.imageView.setColorFilter(new ColorMatrixColorFilter(cm));
                                }
                            });
                            animator.setDuration(2000L);
                            animator.setInterpolator(new AccelerateInterpolator());
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    holder.imageView.clearColorFilter();
                                    holder.imageView.setHasTransientState(false);
                                    animator.start();
                                    zhihuDailyItem.hasFadedIn = true;

                                }
                            });
                        }

                        return false;
                    }
                }).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop().override(mImageWidth, mImageHeigh)
                .into(new DribbbleTarget(holder.imageView, false));


    }

    private void startDescribeActivity(ZhihuViewHolder holder, ZhihuDailyItem zhihuDailyItem, Class zhihuDescribeActivity) {

        DBUtils.getDB(mContext).insertHasRead(Config.ZHIHU, zhihuDailyItem.getId(), 1);
        holder.textView.setTextColor(Color.GRAY);
        Intent intent = new Intent(mContext, zhihuDescribeActivity);
        intent.putExtra("id", zhihuDailyItem.getId());
        intent.putExtra("title", zhihuDailyItem.getTitle());

//                final android.support.v4.util.Pair<View, String>[] pairs = Help.createSafeTransitionParticipants
//                        ((Activity) mContext, false,new android.support.v4.util.Pair<>(holder.imageView, mContext.getString(R.string.transition_shot)),
//                                new android.support.v4.util.Pair<>(holder.linearLayout, mContext.getString(R.string.transition_shot_background)));
//                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, pairs);
//                mContext.startActivity(intent, options.toBundle());
        mContext.startActivity(intent);

    }


    @Override
    public int getItemCount() {
        return zhihuDailyItems.size();
    }


    public void addItems(ArrayList<ZhihuDailyItem> list) {

        zhihuDailyItems.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {

            return TYPE_NORMAL;
        }
        return TYPE_LOADING_MORE;
    }

    private int getDataItemCount() {

        return zhihuDailyItems.size();
    }

    private int getLoadingMoreItemPosition() {
        return loadingMore ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

    // TODO: 16/8/13  don't forget call fellow method
    @Override
    public void loadingStart() {
        if (loadingMore) return;
        loadingMore = true;
        notifyItemInserted(getLoadingMoreItemPosition());
    }

    @Override
    public void loadingfinish() {
        if (!loadingMore) return;
        final int loadingPos = getLoadingMoreItemPosition();
        loadingMore = false;
        notifyItemRemoved(loadingPos);
    }


    public void clearData() {
        zhihuDailyItems.clear();
        notifyDataSetChanged();
    }

    private static class LoadingMoreHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingMoreHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView;
        }
    }

    private static class ZhihuViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        LinearLayout linearLayout;
        BadgedFourThreeImageView imageView;

        ZhihuViewHolder(View itemView) {
            super(itemView);
            imageView = (BadgedFourThreeImageView) itemView.findViewById(R.id.item_image_id);
            textView = (TextView) itemView.findViewById(R.id.item_text_id);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.zhihu_item_layout);
        }
    }


}
