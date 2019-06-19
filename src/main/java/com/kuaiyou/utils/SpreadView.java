package com.kuaiyou.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kuaiyou.adbid.AdSpreadBIDView;
import com.kuaiyou.interfaces.KySpreadListener;
import com.kuaiyou.mraid.interfaces.MRAIDNativeFeatureListener;
import com.kuaiyou.mraid.interfaces.MRAIDViewListener;
import com.kuaiyou.obj.AdsBean;

import java.util.HashMap;

/**
 * Created by zhangchen on 2017/12/20.
 */

public class SpreadView extends RelativeLayout implements View.OnTouchListener {
    private int screenWidth;
    private int screenHeight;
    private int layoutType;
    private int sHeight, sWidth;
    private KySpreadListener kyViewListener;
    private Rect touchRect;
    private int padding = 4;
    private float downX, downY;
    private double density;
    private double densityScale;
    private int hasLogo = 1;//1表示有logo，0表示没logo
    private boolean isHtml = false;
    private Rect closeRect = new Rect();

    private int deformation = 0;

    public SpreadView(Context context) {
        super(context);
        int[] screenSize = AdViewUtils.getWidthAndHeight(context.getApplicationContext(), true, true);
        screenWidth = screenSize[0];
        screenHeight = screenSize[1] - AdViewUtils.getDaoHangHeight(context) - AdViewUtils.getStatusBarHeight(context);
//        this.setBackgroundColor(Color.WHITE);
        this.densityScale = AdViewUtils.getScaledDensity(context);
        this.density = AdViewUtils.getDensity(context);
        padding = (int) (padding * density);
        touchRect = new Rect(0, 0, screenWidth, screenHeight);
    }

    public int getAdHeight(int hasLogo) {
        return screenHeight - (hasLogo * (screenWidth / 4));
    }

    public void init() {
        ImageView logo = new ImageView(getContext());
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setId(ConstantValues.SPREADLOGOIMAGEID);
        addView(logo);
        try {
            if (null != kyViewListener) {
                Drawable drawable = kyViewListener.getSpreadLogo();
                if (null != drawable)
                    ((ImageView) findViewById(ConstantValues.SPREADLOGOIMAGEID)).setImageDrawable(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void updateLogo() {
        try {
            if (null != kyViewListener) {
                Drawable drawable = kyViewListener.getSpreadLogo();
                if (null != drawable)
                    ((ImageView) findViewById(ConstantValues.SPREADLOGOIMAGEID)).setImageDrawable(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLogoLayer() {
        try {
            ImageView logoImage;
            if (null != (logoImage = (ImageView)findViewById(ConstantValues.SPREADLOGOIMAGEID))) {
                removeView(logoImage);
                addView(logoImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(screenWidth, screenHeight);
    }

    public void setSpreadViewListener(KySpreadListener kyViewListener) {
        this.kyViewListener = kyViewListener;
    }

    public void loadAdLayout(int width, int height, int adType, int layoutType, int deformation, int hasLogo, MRAIDNativeFeatureListener nativeFeatureListener, MRAIDViewListener mraidViewListener) {
        this.layoutType = layoutType;
        this.sWidth = screenWidth;
        this.sHeight = sWidth * height / width;
        this.hasLogo = hasLogo;
        this.isHtml = adType == ConstantValues.HTML;
        this.deformation = deformation;
        HashMap<String, Integer> sizeMap = new HashMap<String, Integer>();
        switch (adType) {
            case ConstantValues.MIXED:
                sizeMap.put(ConstantValues.INSTLWIDTH, sWidth);
                sizeMap.put(ConstantValues.INSTLHEIGHT, sWidth * 5 / 6);
                break;
            default:
                sizeMap.put(ConstantValues.INSTLWIDTH, sWidth);
                if (layoutType == ConstantValues.EXTRA3 || hasLogo == 0) {
                    if (deformation == ConstantValues.SCALE_NOHTML && adType == ConstantValues.HTML)
                        sizeMap.put(ConstantValues.INSTLHEIGHT, sHeight);
                    else
                        sizeMap.put(ConstantValues.INSTLHEIGHT, screenHeight);
                } else {
                    if (deformation == ConstantValues.SCALE_INCLUDEHTML)
                        sizeMap.put(ConstantValues.INSTLHEIGHT, screenHeight - (screenWidth / 4));
                    else if (deformation == ConstantValues.SCALE_NOHTML) {
                        if (adType != ConstantValues.HTML)
                            sizeMap.put(ConstantValues.INSTLHEIGHT, sHeight = (screenHeight - (screenWidth / 4)));
                        else
                            sizeMap.put(ConstantValues.INSTLHEIGHT, sHeight);
                    } else
                        sizeMap.put(ConstantValues.INSTLHEIGHT, sHeight);
                }
                break;
        }
        InstlView instlView = new InstlView(getContext(),
                sizeMap, adType, nativeFeatureListener, mraidViewListener);
        instlView.setId(adType != ConstantValues.MIXED ? ConstantValues.SPREADADFRAMEID : ConstantValues.SPREADMIXLAYOUTID);
        if (null != instlView && null != instlView.getMraidView())
            instlView.getMraidView().setClickCheckable(false);
        addView(instlView);
        if (adType != ConstantValues.MIXED)
            addSpreadText();

        this.setOnTouchListener(this);
        instlView.setOnTouchListener(this);
        updateLogoLayer();
        addCloseBtn();
        instlView.removeView(instlView.findViewById(ConstantValues.ADICONID));
        instlView.removeView(instlView.findViewById(ConstantValues.ADLOGOID));
        addLogoIcon();

    }

    public void setContent(AdsBean adsBean, String bitmapPath) {
        if (null != findViewById(ConstantValues.SPREADADFRAMEID))
            ((InstlView) findViewById(ConstantValues.SPREADADFRAMEID)).setContent(adsBean, bitmapPath);
        else if (null != findViewById(ConstantValues.SPREADMIXLAYOUTID))
            ((InstlView) findViewById(ConstantValues.SPREADMIXLAYOUTID)).setContent(adsBean, bitmapPath);
        setAdTextDescription(adsBean.getAdTitle(), adsBean.getAdBgColor(), adsBean.getAdTitleColor());
//        setAdTextDescription("陪我玩会奥利奥小游戏好么？", adsBean.getAdBgColor(), adsBean.getAdTitleColor());
        setLogoIcon();
    }

    private void setAdTextDescription(String textDescription, String bgColor, String titleColor) {
        try {
            if (TextUtils.isEmpty(textDescription))
                return;
            if (null != findViewById(ConstantValues.SPREADTEXTID)) {
                setTextSize();
                (((InstlView.CenterTextView) findViewById(ConstantValues.SPREADTEXTID))).text = textDescription;
                if (!TextUtils.isEmpty(titleColor))
                    (((InstlView.CenterTextView) findViewById(ConstantValues.SPREADTEXTID))).setTextColor(Color.parseColor(titleColor));
                else
                    (((InstlView.CenterTextView) findViewById(ConstantValues.SPREADTEXTID))).setTextColor(Color.WHITE);
                if (!TextUtils.isEmpty(bgColor))
                    findViewById(ConstantValues.SPREADTEXTID).setBackgroundColor(Color.parseColor(bgColor));
                else
                    findViewById(ConstantValues.SPREADTEXTID).setBackgroundColor(Color.parseColor("#3e3e3d3d"));
                if (!TextUtils.isEmpty(textDescription) && null != kyViewListener)
                    setBehaveIcon(kyViewListener.getBehaveIcon());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBehaveIcon(String behaveIcon) {
        if (null != findViewById(ConstantValues.BEHAVICONID)) {
            //BitmapDrawable selfBehavIcon = new BitmapDrawable(getResources(),getClass().getResourceAsStream(behaveIcon));
            BitmapDrawable selfBehavIcon = new BitmapDrawable(getResources(),AdViewUtils.getImageFromAssetsFile(behaveIcon));
            ((ImageView) findViewById(ConstantValues.BEHAVICONID)).setImageDrawable(selfBehavIcon);
            findViewById(ConstantValues.BEHAVICONID).setBackgroundColor(Color.parseColor("#663e3d3d"));
        }
    }

    private void setTextSize() {
        try {
            InstlView.CenterTextView title = (InstlView.CenterTextView) findViewById(ConstantValues.SPREADTEXTID);
            if (density <= 1.5) {
                title.textSize = (int) (16 * densityScale);
            } else if (density > 1.5 && density < 3) {
                title.textSize = (int) (18 * densityScale);
            } else if (density >= 3 && density < 4) {
                title.textSize = (int) (20 * densityScale);
            } else if (density >= 4) {
                title.textSize = (int) (21 * densityScale);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View adLayout = null;
        try {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                switch (child.getId()) {
                    case ConstantValues.MRAIDVIEWID:
                        break;
                    case ConstantValues.SPREADNOTIFYLAYOUT:
                        child.layout(0, 0, screenWidth, screenWidth / 5);
                        break;
                    case ConstantValues.SPREADADCOUNTER:
                        int spreadNotifyType = 1;
                        if (null != kyViewListener) {
                            spreadNotifyType = kyViewListener.getNotifyType();
                        }
                        if (spreadNotifyType == AdSpreadBIDView.NOTIFY_COUNTER_NUM || spreadNotifyType == AdSpreadBIDView.NOTIFY_COUNTER_TEXT) {
                            child.layout((int) (screenWidth - screenWidth / 5 - padding * density), (int) (padding * density), (int) (screenWidth - padding * density / 2), (int) ((screenWidth / 11) + (padding * density)));
                        }
                        break;

                    case ConstantValues.SPREADLOGOIMAGEID:
                        switch (layoutType) {
                            case ConstantValues.NULL:
                            case ConstantValues.TOP:
                            case ConstantValues.CENTER:
                            case ConstantValues.BOTTOM:
                            case ConstantValues.ALL_CENTER:
                            case ConstantValues.EXTRA2:
                            case ConstantValues.EXTRA1:
                                if (hasLogo == ConstantValues.HASLOGO)
                                    child.layout(0, screenHeight - (screenWidth / 4), screenWidth, screenHeight);
                                else
                                    child.setVisibility(View.GONE);
                                break;
                            case ConstantValues.EXTRA3:
                                child.setVisibility(View.GONE);
                                break;
                        }
                        break;
                    case ConstantValues.SPREADADFRAMEID:
                        switch (deformation) {
                            case ConstantValues.SCALE_NOHTML:
                            case ConstantValues.SCALE_INCLUDEHTML:
                                switch (layoutType) {
                                    case ConstantValues.CENTER:
                                        if (!isHtml)
                                            child.layout(0, 0, sWidth, (screenHeight - (screenWidth / 4 * hasLogo)));
                                        else
                                            child.layout(0, (screenHeight - (screenWidth / 4 * hasLogo)) / 2 - sHeight / 2, sWidth, (screenHeight - (screenWidth / 4 * hasLogo)) / 2 + sHeight / 2);
                                        break;
                                    case ConstantValues.BOTTOM:
                                        if (!isHtml)
                                            child.layout(0, 0, sWidth, (screenHeight - (screenWidth / 4 * hasLogo)));
                                        else
                                            child.layout(0, (screenHeight - (screenWidth / 4 * hasLogo)) - sHeight, sWidth, screenHeight - (screenWidth / 4 * hasLogo));
                                        break;
                                    case ConstantValues.TOP:
                                    case ConstantValues.ALL_CENTER:
                                    case ConstantValues.EXTRA3:
                                    case ConstantValues.EXTRA2:
                                    case ConstantValues.EXTRA1:
                                        if (!isHtml)
                                            child.layout(0, 0, sWidth, (screenHeight - (screenWidth / 4 * hasLogo)));
                                        else
                                            child.layout(0, 0, sWidth, sHeight);
                                        break;
                                }
                                break;
                            default:
                                switch (layoutType) {
                                    case ConstantValues.CENTER:
                                        child.layout(0, (screenHeight - (screenWidth / 4 * hasLogo)) / 2 - sHeight / 2, sWidth, (screenHeight - (screenWidth / 4 * hasLogo)) / 2 + sHeight / 2);
                                        break;
                                    case ConstantValues.BOTTOM:
                                        child.layout(0, (screenHeight - (screenWidth / 4 * hasLogo)) - sHeight, sWidth, screenHeight - (screenWidth / 4 * hasLogo));
                                        break;
                                    case ConstantValues.TOP:
                                    case ConstantValues.ALL_CENTER:
                                    case ConstantValues.EXTRA3:
                                    case ConstantValues.EXTRA2:
                                    case ConstantValues.EXTRA1:
                                        child.layout(0, 0, sWidth, sHeight);
                                        break;
                                }
                                break;
                        }
                        break;
                    case ConstantValues.ADICONID:
                        if (null != findViewById(ConstantValues.SPREADLOGOIMAGEID) && 0 != findViewById(ConstantValues.SPREADLOGOIMAGEID).getHeight())
                            if (sHeight == screenHeight)
                                child.layout(0, screenHeight - screenWidth / 25, screenWidth / 8, screenHeight);
                            else if (sHeight > screenHeight - (screenWidth / 4))
                                child.layout(0, (screenHeight - screenWidth / 4) - screenWidth / 25, screenWidth / 8, (screenHeight - screenWidth / 4));
                            else {
                                switch (layoutType) {
                                    case ConstantValues.CENTER:
                                        child.layout(0, ((screenHeight - (screenWidth / 4 * hasLogo)) / 2 + sHeight / 2) - screenWidth / 25, sWidth, (screenHeight - (screenWidth / 4 * hasLogo)) / 2 + sHeight / 2);
                                        break;
                                    case ConstantValues.BOTTOM:
                                        child.layout(0, (screenHeight - (screenWidth / 4 * hasLogo)) - screenWidth / 25, sWidth, screenHeight - (screenWidth / 4 * hasLogo));
                                        break;
                                    case ConstantValues.TOP:
                                    case ConstantValues.ALL_CENTER:
                                    case ConstantValues.EXTRA3:
                                    case ConstantValues.EXTRA2:
                                    case ConstantValues.EXTRA1:
                                        child.layout(0, sHeight - screenWidth / 25, screenWidth / 8, sHeight);
                                        break;
                                }
                            }
                        else {
                            if (null != (adLayout = ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREADADFRAMEID))) {
                                int bottom = adLayout.getBottom();
                                child.layout(0, bottom - screenWidth / 25, screenWidth / 8, bottom);
                            }
                        }
                        break;
                    case ConstantValues.ADLOGOID:
                        if (null != findViewById(ConstantValues.SPREADLOGOIMAGEID) && 0 != findViewById(ConstantValues.SPREADLOGOIMAGEID).getHeight()) {
                            if (sHeight == screenHeight)
                                child.layout(screenWidth - (screenWidth / 8), screenHeight - screenWidth / 25, screenWidth, screenHeight);
                            else if (sHeight > screenHeight - (screenWidth / 4))
                                child.layout(screenWidth - (screenWidth / 8), (screenHeight - screenWidth / 4) - screenWidth / 25, screenWidth, (screenHeight - screenWidth / 4));
                            else {
//                                child.layout(screenWidth - (screenWidth / 8), sHeight - screenWidth / 25, screenWidth, sHeight);
                                switch (layoutType) {
                                    case ConstantValues.CENTER:
                                        child.layout(screenWidth - (screenWidth / 8), ((screenHeight - (screenWidth / 4 * hasLogo)) / 2 + sHeight / 2) - screenWidth / 25, screenWidth, (screenHeight - (screenWidth / 4 * hasLogo)) / 2 + sHeight / 2);
                                        break;
                                    case ConstantValues.BOTTOM:
                                        child.layout(screenWidth - (screenWidth / 8), (screenHeight - (screenWidth / 4 * hasLogo)) - screenWidth / 25, screenWidth, screenHeight - (screenWidth / 4 * hasLogo));
                                        break;
                                    case ConstantValues.TOP:
                                    case ConstantValues.ALL_CENTER:
                                    case ConstantValues.EXTRA3:
                                    case ConstantValues.EXTRA2:
                                    case ConstantValues.EXTRA1:
                                        child.layout(screenWidth - (screenWidth / 8), sHeight - screenWidth / 25, screenWidth, sHeight);
                                        break;
                                }
                            }
                        } else {
                            if (null != (adLayout = ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREADADFRAMEID))) {
                                int bottom = adLayout.getBottom();
                                child.layout(screenWidth - (screenWidth / 8), bottom - screenWidth / 25, screenWidth, bottom);
                            }
                        }
                        break;
                    case ConstantValues.SPREADTEXTID:
                        if (hasLogo == ConstantValues.HASLOGO) {
                            if (null != ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREADLOGOIMAGEID)) {
                                if (sHeight > screenHeight - (screenWidth / 4))
                                    child.layout(0, screenHeight - (screenWidth / 2), screenWidth * 3 / 4, screenHeight - (screenWidth / 4));
                                else
                                    child.layout(0, sHeight - (screenWidth / 4), screenWidth * 3 / 4, sHeight);
                            }
                        } else {
                            if (null != (adLayout = ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREADADFRAMEID))) {
                                int bottom = adLayout.getBottom();
                                child.layout(0, bottom - (screenWidth / 4), screenWidth * 3 / 4, bottom);
                            }
                        }
                        break;
                    case ConstantValues.BEHAVICONID:
                        if (hasLogo == ConstantValues.HASLOGO) {
                            if (null != ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREADLOGOIMAGEID)) {
                                if (sHeight > screenHeight - (screenWidth / 4))
                                    child.layout((screenWidth * 3 / 4), screenHeight - (screenWidth / 2), screenWidth, screenHeight - (screenWidth / 4));
                                else
                                    child.layout((screenWidth * 3 / 4), sHeight - (screenWidth / 4), screenWidth, sHeight);
                            }
                        } else {
                            if (null != (adLayout = ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREADADFRAMEID))) {
                                int bottom = adLayout.getBottom();
                                child.layout((screenWidth * 3 / 4), bottom - (screenWidth / 4), screenWidth, bottom);
                            }
                        }
                        break;
                    case ConstantValues.SPREADMIXLAYOUTID:
                        if (null != findViewById(ConstantValues.SPREADLOGOIMAGEID)) {
                            int logoHeight = findViewById(ConstantValues.SPREADLOGOIMAGEID).getHeight();
                            if (0 != logoHeight)
                                child.layout(0, (screenHeight - logoHeight) / 2 - screenWidth * 5 / 12, screenWidth, (screenHeight - logoHeight) / 2 + (screenHeight - logoHeight) / 2);
                            else
                                child.layout(0, screenHeight / 2 - screenWidth * 5 / 12, screenWidth, screenHeight / 2 + (screenHeight - logoHeight) / 2);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setLogoIcon() {
        ImageView logoImage = (ImageView)findViewById(ConstantValues.ADLOGOID);
        ImageView iconImage = (ImageView)findViewById(ConstantValues.ADICONID);
        if (null != kyViewListener) {
            if (null != logoImage) {
                String logoPath = kyViewListener.getAdLogo();
                logoImage.setImageDrawable(new BitmapDrawable(getContext().getResources(), logoPath));
            }
            if (null != iconImage) {
                String iconPath = kyViewListener.getAdIcon();
                iconImage.setImageDrawable(new BitmapDrawable(getContext().getResources(), iconPath));
            }
        }
    }

    private void addCloseBtn() {
        RelativeLayout notifyLayout = new RelativeLayout(getContext());

        InstlView.CenterTextView adCounter = new InstlView.CenterTextView(getContext());
        //TextView adCounter = new TextView(getContext());
        notifyLayout.setId(ConstantValues.SPREADNOTIFYLAYOUT);
        adCounter.setId(ConstantValues.SPREADADCOUNTER);

        adCounter.textSize = (int) (18 * densityScale);
        adCounter.setTextColor(Color.WHITE);


        addView(notifyLayout);
        addView(adCounter);
        adCounter.setOnTouchListener(this);
    }

    private void addLogoIcon() {
        ImageView adLogo = new ImageView(getContext());
        ImageView adIcon = new ImageView(getContext());
        adLogo.setId(ConstantValues.ADLOGOID);
        adIcon.setId(ConstantValues.ADICONID);
        addView(adLogo);
        addView(adIcon);

        adIcon.setScaleType(ImageView.ScaleType.FIT_START);
        adLogo.setScaleType(ImageView.ScaleType.FIT_END);
    }

    private void addSpreadText() {
        ImageView behavIcon = new ImageView(getContext());
        TextView title = new InstlView.CenterTextView(getContext());
        title.setId(ConstantValues.SPREADTEXTID);
        behavIcon.setId(ConstantValues.BEHAVICONID);
        behavIcon.setPadding((int) (screenWidth / 4 * .3), (int) (screenWidth / 4 * .3), (int) (screenWidth / 4 * .3), (int) (screenWidth / 4 * .3));
        behavIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(title);
        addView(behavIcon);
        title.setOnTouchListener(this);
        behavIcon.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (null != findViewById(ConstantValues.SPREADADCOUNTER)) {
            findViewById(ConstantValues.SPREADADCOUNTER).getGlobalVisibleRect(closeRect);
            if (closeRect.right != 0 && kyViewListener.getNotifyType() == AdSpreadBIDView.NOTIFY_COUNTER_NUM)
                closeRect.left = closeRect.left + (closeRect.right - closeRect.left) / 2;
        }
        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
//                if (Math.abs(event.getX() - downX) < padding * padding / 2 && Math.abs(event.getY() - downY) < padding * padding / 2 && event.getEventTime() - event.getDownTime() < 1000) {
                    if (v.getId() == ConstantValues.SPREADADCOUNTER) {
//                    if (closeRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        if (null != kyViewListener)
                            kyViewListener.onCloseBtnClicked();
                    } else if (v.getId() == ConstantValues.SPREADTEXTID || v.getId() == ConstantValues.BEHAVICONID) {
                        if (null != kyViewListener)
                            kyViewListener.onViewClicked(event, null, null, event.getX(), event.getY());
                    } else if (touchRect.contains((int) event.getRawX(), (int) event.getRawY()))
//                        kyViewListener.onViewCLicked(downX, downY, event.getX(), event.getY());
//                }
                        break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
