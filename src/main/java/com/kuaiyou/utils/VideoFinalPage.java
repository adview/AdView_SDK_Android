package com.kuaiyou.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.SoftReference;

public class VideoFinalPage extends RelativeLayout {

    private final int TITLEID = 30001;
    private final int SUBTITLEID = 30002;
    private final int BUTTONTEXTID = 30003;
    private final int ICONID = 30004;
    private final int BACKGROUNGID = 30005;


    private TextView title;
    private TextView subTitle;

    private TextView buttonText;

    private ImageView icon;
    private ImageView background;

    private int bgHeight;

    private int orientation = 0;

    private SoftReference<Bitmap> iconReference = null;
    private SoftReference<Bitmap> bgReference = null;


    public VideoFinalPage(Context context) {
        super(context);
        orientation = context.getResources().getConfiguration().orientation;

        icon = new ImageView(context);
        background = new ImageView(context);

        title = new TextView(context);
        subTitle = new TextView(context);
        buttonText = new TextView(context);

        icon.setId(ICONID);
        title.setId(TITLEID);
        subTitle.setId(SUBTITLEID);
        buttonText.setId(BUTTONTEXTID);
        background.setId(BACKGROUNGID);

//        background.setBackgroundColor(Color.CYAN);
//        icon.setBackgroundColor(Color.RED);
//        title.setBackgroundColor(Color.GREEN);
//        subTitle.setBackgroundColor(Color.BLUE);
//        buttonText.setBackgroundColor(Color.YELLOW);

        title.setGravity(Gravity.CENTER);
        subTitle.setGravity(Gravity.CENTER);
        buttonText.setGravity(Gravity.CENTER);

        title.getPaint().setFakeBoldText(true);

        title.setTextColor(Color.BLACK);
        subTitle.setTextColor(Color.BLACK);
        buttonText.setTextColor(Color.WHITE);
        setBackgroundColor(Color.WHITE);

        title.setSingleLine(true);
        subTitle.setMaxLines(3);
        buttonText.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        subTitle.setEllipsize(TextUtils.TruncateAt.END);
        buttonText.setEllipsize(TextUtils.TruncateAt.END);

        buttonText.setBackground(AdViewUtils.getColorDrawable(context, "#00BFFF"));

        background.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(background);
        addView(icon);
        addView(title);
        addView(subTitle);
        addView(buttonText);


    }

    public void setTextSize() {
        try {
            double density = AdViewUtils.getDensity(getContext());
            if (density <= 1.5) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            } else if (density == 2) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
                subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            } else if (density == 3) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            } else if (density >= 4) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
                subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePosition(int height) {
        this.bgHeight = height;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int height = getHeight();
        int width = getWidth();

        if (!changed)
            return;

        switch (orientation) {
//            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
//            case ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT:
//            case ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:
//            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
            case Configuration.ORIENTATION_PORTRAIT:
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    switch (child.getId()) {
                        case BACKGROUNGID:
                            LayoutParams backgroundLayoutParma = new LayoutParams(height, bgHeight == 0 ? height / 5 * 2 : bgHeight);
                            background.setLayoutParams(backgroundLayoutParma);
                            break;
                        case ICONID:
                            LayoutParams iconLayoutParma = new LayoutParams(width / 5, width / 5);
                            int margin = (bgHeight == 0 ? height / 5 * 2 : bgHeight) - width / 5 / 2;
                            iconLayoutParma.topMargin = margin < 0 ? 0 : margin;
                            iconLayoutParma.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            icon.setLayoutParams(iconLayoutParma);
                            break;
                        case TITLEID:
                            LayoutParams titleLayoutParma = new LayoutParams(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
                            titleLayoutParma.addRule(RelativeLayout.BELOW, ICONID);
                            titleLayoutParma.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            titleLayoutParma.topMargin = height / 20;
                            titleLayoutParma.bottomMargin = height / 20;
                            title.setLayoutParams(titleLayoutParma);
                            break;
                        case SUBTITLEID:
                            LayoutParams subTitleLayoutParma = new LayoutParams(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
                            subTitleLayoutParma.addRule(RelativeLayout.BELOW, TITLEID);
                            subTitleLayoutParma.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            subTitle.setLayoutParams(subTitleLayoutParma);
                            break;
                        case BUTTONTEXTID:
                            LayoutParams buttonTextLayoutParma = new LayoutParams((int) (width * .6), (int) (width * .6 * .2));

                            buttonTextLayoutParma.bottomMargin = height / 8;

                            buttonTextLayoutParma.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            buttonTextLayoutParma.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            buttonText.setLayoutParams(buttonTextLayoutParma);
                            break;
                    }
                }
                break;
//            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
//            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
//            case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
//            case ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE:
            case Configuration.ORIENTATION_LANDSCAPE:
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    switch (child.getId()) {
                        case BACKGROUNGID:
                            LayoutParams backgroundLayoutParma = new LayoutParams(width / 2, height);
                            background.setLayoutParams(backgroundLayoutParma);
                            break;
                        case ICONID:
                            LayoutParams iconLayoutParma = new LayoutParams(height / 5, height / 5);
//                            int margin = (bgHeight == 0 ? height / 5 * 2 : bgHeight) - width / 5 / 2;
                            iconLayoutParma.leftMargin = width * 3 / 4 - height / 10;
                            iconLayoutParma.topMargin = height / 6;
//                            iconLayoutParma.addRule(RelativeLayout.CENTER_IN_PARENT);
                            icon.setLayoutParams(iconLayoutParma);
                            break;
                        case TITLEID:
                            LayoutParams titleLayoutParma = new LayoutParams(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
                            titleLayoutParma.addRule(RelativeLayout.BELOW, ICONID);
//                            titleLayoutParma.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            titleLayoutParma.leftMargin = width / 2;
                            titleLayoutParma.topMargin = height / 20;
                            titleLayoutParma.bottomMargin = height / 20;
                            title.setLayoutParams(titleLayoutParma);
                            break;
                        case SUBTITLEID:
                            LayoutParams subTitleLayoutParma = new LayoutParams(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT);
                            subTitleLayoutParma.addRule(RelativeLayout.BELOW, TITLEID);
//                            subTitleLayoutParma.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            subTitleLayoutParma.leftMargin = width / 2;
                            subTitle.setLayoutParams(subTitleLayoutParma);
                            break;
                        case BUTTONTEXTID:
                            LayoutParams buttonTextLayoutParma = new LayoutParams((int) (width / 4), (int) (width / 3 * .2));

                            buttonTextLayoutParma.bottomMargin = height / 8;
                            buttonTextLayoutParma.leftMargin = width / 2 + width / 8;
                            buttonTextLayoutParma.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                            buttonTextLayoutParma.addRule(RelativeLayout.CENTER_HORIZONTAL);
                            buttonText.setLayoutParams(buttonTextLayoutParma);
                            break;
                    }
                }
                break;
        }


    }

    public void setData(Bundle bundle) {
        try {
            setTextSize();
            String bgPath = bundle.getString("bgPath");
            String iconPath = bundle.getString("iconPath");
            String title = bundle.getString("title");
            String subTitle = bundle.getString("subTitle");
            String buttonText = bundle.getString("buttonText");

            Bitmap icon = BitmapFactory.decodeFile(iconPath);
            Bitmap background = BitmapFactory.decodeFile(bgPath);

            iconReference = new SoftReference<Bitmap>(icon);
            bgReference = new SoftReference<Bitmap>(background);

            this.background.setImageBitmap(bgReference.get());
            this.icon.setImageBitmap(iconReference.get());
            this.title.setText(title);
            this.subTitle.setText(subTitle);
            this.buttonText.setText(buttonText);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public ImageView getIcon() {
        return icon;
    }

    public ImageView getBackgroundImage() {
        return background;
    }

    public void destoryView() {
        try {
            if (null != iconReference) {
                if (null != iconReference.get())
                    iconReference.get().recycle();
                iconReference.clear();
            }
            if (null != bgReference) {
                if (null != bgReference.get())
                    bgReference.get().recycle();
                bgReference.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
