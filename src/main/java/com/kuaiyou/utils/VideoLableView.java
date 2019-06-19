package com.kuaiyou.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.SoftReference;

public class VideoLableView extends RelativeLayout {

    private final int TITLEID = 20001;
    private final int SUBTITLEID = 20002;
    private final int BUTTONTEXTID = 20003;
    private final int ICONID = 20004;

    private TextView title;
    private TextView subTitle;
    private TextView buttonText;
    private ImageView icon;

    private SoftReference<Bitmap> iconReference = null;

    public VideoLableView(Context context) {
        super(context);
        icon = new ImageView(context);
        title = new TextView(context);
        subTitle = new TextView(context);
        buttonText = new TextView(context);

        icon.setId(ICONID);
        title.setId(TITLEID);
        subTitle.setId(SUBTITLEID);
        buttonText.setId(BUTTONTEXTID);


        title.getPaint().setFakeBoldText(true);
        buttonText.setGravity(Gravity.CENTER);


        title.setSingleLine(true);
        subTitle.setMaxLines(2);
        buttonText.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        subTitle.setEllipsize(TextUtils.TruncateAt.END);
        buttonText.setEllipsize(TextUtils.TruncateAt.END);

        title.setTextColor(Color.BLACK);
        subTitle.setTextColor(Color.BLACK);
        buttonText.setTextColor(Color.BLACK);
        buttonText.setBackground(AdViewUtils.getColorDrawableWithBounds(context, "#FFFFFF", "#000000"));
        setBackground(AdViewUtils.getColorDrawable(context, "#FFFFFF"));

        addView(icon);
        addView(title);
        addView(subTitle);
        addView(buttonText);
    }

    public void setTextSize() {
        double density = AdViewUtils.getDensity(getContext());
        int width = getLayoutParams().width;
        setPadding(width / 40, width / 40, width / 40, width / 40);
        if (density <= 1.5) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        } else if (density == 2) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        } else if (density == 3) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        } else if (density >= 4) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int height = getHeight();
        int width = getWidth();
        if (!changed)
            return;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            switch (child.getId()) {
                case ICONID:
                    RelativeLayout.LayoutParams iconLayoutParma = new LayoutParams(height - getPaddingLeft(), height - getPaddingLeft());
                    iconLayoutParma.addRule(RelativeLayout.CENTER_VERTICAL);
                    icon.setLayoutParams(iconLayoutParma);
                    break;
                case TITLEID:
                    RelativeLayout.LayoutParams titleLayoutParma = new LayoutParams((int) (((double) width - (double) height) * .6), height / 2 - getPaddingTop());
                    titleLayoutParma.addRule(RelativeLayout.RIGHT_OF, ICONID);
                    title.setLayoutParams(titleLayoutParma);
                    break;
                case SUBTITLEID:
                    RelativeLayout.LayoutParams subTitleLayoutParma = new LayoutParams((int) (((double) width - (double) height) * .6), height / 2 - getPaddingTop());
                    subTitleLayoutParma.addRule(RelativeLayout.RIGHT_OF, ICONID);
                    subTitleLayoutParma.addRule(RelativeLayout.BELOW, TITLEID);
                    subTitle.setLayoutParams(subTitleLayoutParma);
                    break;
                case BUTTONTEXTID:
                    RelativeLayout.LayoutParams buttonTextLayoutParma = new LayoutParams((int) (((double) width - (double) height) * .4) - getPaddingRight(), height / 2);
                    buttonTextLayoutParma.addRule(RelativeLayout.RIGHT_OF, TITLEID);
                    buttonTextLayoutParma.addRule(RelativeLayout.CENTER_VERTICAL);
                    buttonText.setLayoutParams(buttonTextLayoutParma);
                    break;
            }
        }
    }

    public void setData(Bundle bundle) {
        try {
            setTextSize();
            String iconPath = bundle.getString("iconPath");
            String title = bundle.getString("title");
            String subTitle = bundle.getString("subTitle");
            String buttonText = bundle.getString("buttonText");

            Bitmap icon = BitmapFactory.decodeFile(iconPath);
            iconReference = new SoftReference<Bitmap>(icon);

            this.icon.setImageBitmap(iconReference.get());
            this.title.setText(title);
            this.subTitle.setText(subTitle);
            this.buttonText.setText(buttonText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImageView getIcon() {
        return icon;
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getSubTitle() {
        return subTitle;
    }

    public TextView getButtonText() {
        return buttonText;
    }

    public void destoryView() {
        try {
            if (null != iconReference) {
                if (null != iconReference.get())
                    iconReference.get().recycle();
                iconReference.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
