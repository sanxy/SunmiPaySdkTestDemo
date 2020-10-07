package com.sm.sdk.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sm.sdk.demo.R;

public class TitleView extends RelativeLayout {

    private ImageView mIvLeft;
    private TextView mTvCenter;

    public TitleView(Context context) {
        super(context);
        init(context);
    }

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = View.inflate(context, R.layout.ui_common_title, this);
        mTvCenter = view.findViewById(R.id.tv_center);
        mIvLeft = view.findViewById(R.id.iv_left);
    }

    public TextView getCenterTextView() {
        return mTvCenter;
    }

    public ImageView getLeftImageView() {
        return mIvLeft;
    }

    public void setLeftImageOnClickListener(OnClickListener l) {
        mIvLeft.setOnClickListener(l);
    }

}
