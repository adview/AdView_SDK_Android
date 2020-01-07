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
    private KySpreadListener kySpreadListener;
    private Rect touchRect;
    private int padding = 4;
    private float downX, downY;
    private double density;
    private double densityScale;
    private int hasLogo = 1;//1表示有logo，0表示没logo
    private int logoHeight = 0;
    private boolean isHtml = false;
    private Rect closeRect = new Rect();

    private InstlView instlView = null; //wilder 20190612
    private int deformation = 0;

    public SpreadView(Context context) {
        super(context);
        int[] screenSize = AdViewUtils.getWidthAndHeight(context.getApplicationContext(), true, true);
        screenWidth = screenSize[0];
        //这个高度减去导航条和状态条的高度
        screenHeight = screenSize[1] - AdViewUtils.getNaviBarHeight(context) - AdViewUtils.getStatusBarHeight(context);
        logoHeight = screenWidth /3;
        //this.setBackgroundColor(Color.BLUE); //wilder 2020
        this.densityScale = AdViewUtils.getScaledDensity(context);
        this.density = AdViewUtils.getDensity(context);
        padding = (int) (padding * density);
        touchRect = new Rect(0, 0, screenWidth, screenHeight);
    }

    public int getAdHeight(int hasLogo) {

        return screenHeight - (hasLogo * (screenWidth / 4));
    }

    public InstlView getInstlView() {
        return instlView;
    }

    public void init() {
        ImageView logo = new ImageView(getContext());
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER); //FIT_CENTER wilder 2020
        logo.setId(ConstantValues.SPREAD_UI_LOGOIMAGEID);

        try {
            if (null != kySpreadListener) {
                Drawable drawable = kySpreadListener.getSpreadLogo();
                if (null != drawable)
                    ((ImageView) findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID)).setImageDrawable(drawable);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        addView(logo);
    }


    public void updateLogo() {
        try {
            if (null != kySpreadListener) {
                Drawable drawable = kySpreadListener.getSpreadLogo();
                if (null != drawable)
                    ((ImageView) findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID)).setImageDrawable(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLogoLayer() {
        try {
            ImageView logoImage;
            if (null != (logoImage = (ImageView)findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID))) {
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

    public void setSpreadViewListener(KySpreadListener kySpreadListener) {
        this.kySpreadListener = kySpreadListener;
    }

    public void initWidgetLayout(int width, int height, int adType, int layoutType, int deformation, int hasLogo) {
        this.layoutType = layoutType;
        this.sWidth = screenWidth;
        this.sHeight = screenHeight; //sWidth * height / width;//wilder 2020
        this.hasLogo = hasLogo;
        this.isHtml = ( adType == ConstantValues.RESP_ADTYPE_HTML );
        this.deformation = deformation;
        HashMap<String, Integer> sizeMap = new HashMap<String, Integer>();
        switch (adType) {
            case ConstantValues.RESP_ADTYPE_MIXED:
                sizeMap.put(ConstantValues.INSTL_WIDTH_KEY, sWidth);
                sizeMap.put(ConstantValues.INSTL_HEIGHT_KEY, sWidth * 5 / 6);
                break;
            default:
                sizeMap.put(ConstantValues.INSTL_WIDTH_KEY, sWidth);
                if (layoutType == ConstantValues.SPREAD_UI_EXTRA3 || hasLogo == 0) {
                    if (deformation == ConstantValues.SPREAD_UI_SCALE_NOHTML && adType == ConstantValues.RESP_ADTYPE_HTML)
                        sizeMap.put(ConstantValues.INSTL_HEIGHT_KEY, sHeight);
                    else
                        sizeMap.put(ConstantValues.INSTL_HEIGHT_KEY, screenHeight);
                } else {
                    if (deformation == ConstantValues.SPREAD_UI_SCALE_INCLUDEHTML)
                        sizeMap.put(ConstantValues.INSTL_HEIGHT_KEY, screenHeight - (screenWidth / 4));
                    else if (deformation == ConstantValues.SPREAD_UI_SCALE_NOHTML) {
                        if (adType != ConstantValues.RESP_ADTYPE_HTML)
                            sizeMap.put(ConstantValues.INSTL_HEIGHT_KEY, sHeight = (screenHeight - (screenWidth / 4)));
                        else
                            sizeMap.put(ConstantValues.INSTL_HEIGHT_KEY, sHeight);
                    } else
                        sizeMap.put(ConstantValues.INSTL_HEIGHT_KEY, sHeight);
                }
                break;
        }

        instlView = new InstlView(getContext(),sizeMap, adType );
        instlView.setId(adType != ConstantValues.RESP_ADTYPE_MIXED ? ConstantValues.SPREAD_UI_FRAMEID : ConstantValues.SPREAD_UI_MIXLAYOUTID);
        if (null != instlView && null != instlView.getMraidView())
            instlView.getMraidView().setClickCheckable(false);
        //wilder 2019 for spread listener
        instlView.setInstlViewListener(kySpreadListener);

        addView(instlView);

        if (adType != ConstantValues.RESP_ADTYPE_MIXED)
            addSpreadText();

        this.setOnTouchListener(this);
        instlView.setOnTouchListener(this);

        updateLogoLayer();
        addCloseBtn();
        instlView.removeView(instlView.findViewById(ConstantValues.UI_ADICON_ID));
        instlView.removeView(instlView.findViewById(ConstantValues.UI_ADLOGO_ID));
        addLogoIcon();
    }

    public void setContent(AdsBean adsBean, String bitmapPath) {
        if (null != findViewById(ConstantValues.SPREAD_UI_FRAMEID))
            ((InstlView) findViewById(ConstantValues.SPREAD_UI_FRAMEID)).setContent(adsBean, bitmapPath);
        else if (null != findViewById(ConstantValues.SPREAD_UI_MIXLAYOUTID))
            ((InstlView) findViewById(ConstantValues.SPREAD_UI_MIXLAYOUTID)).setContent(adsBean, bitmapPath);
        setAdTextDescription(adsBean.getAdTitle(), adsBean.getAdBgColor(), adsBean.getAdTitleColor());
//        setAdTextDescription("陪我玩会奥利奥小游戏好么？", adsBean.getAdBgColor(), adsBean.getAdTitleColor());
        setLogoIcon();
    }

    private void setAdTextDescription(String textDescription, String bgColor, String titleColor) {
        try {
            if (TextUtils.isEmpty(textDescription))
                return;
            if (null != findViewById(ConstantValues.SPREAD_UI_TEXTID)) {
                setTextSize();
                (((InstlView.CenterTextView) findViewById(ConstantValues.SPREAD_UI_TEXTID))).text = textDescription;
                if (!TextUtils.isEmpty(titleColor))
                    (((InstlView.CenterTextView) findViewById(ConstantValues.SPREAD_UI_TEXTID))).setTextColor(Color.parseColor(titleColor));
                else
                    (((InstlView.CenterTextView) findViewById(ConstantValues.SPREAD_UI_TEXTID))).setTextColor(Color.WHITE);
                if (!TextUtils.isEmpty(bgColor))
                    findViewById(ConstantValues.SPREAD_UI_TEXTID).setBackgroundColor(Color.parseColor(bgColor));
                else
                    findViewById(ConstantValues.SPREAD_UI_TEXTID).setBackgroundColor(Color.parseColor("#3e3e3d3d"));
                if (!TextUtils.isEmpty(textDescription) && null != kySpreadListener)
                    setBehaveIcon(kySpreadListener.getBehaveIcon());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBehaveIcon(String behaveIcon) {
        if (null != findViewById(ConstantValues.MIXED_UI_BEHAVEICON_ID)) {
            //BitmapDrawable selfBehavIcon = new BitmapDrawable(getResources(),getClass().getResourceAsStream(behaveIcon));
            BitmapDrawable selfBehavIcon = new BitmapDrawable(getResources(),AdViewUtils.getImageFromAssetsFile(behaveIcon));
            ((ImageView) findViewById(ConstantValues.MIXED_UI_BEHAVEICON_ID)).setImageDrawable(selfBehavIcon);
            findViewById(ConstantValues.MIXED_UI_BEHAVEICON_ID).setBackgroundColor(Color.parseColor("#663e3d3d"));
        }
    }

    private void setTextSize() {
        try {
            InstlView.CenterTextView title = (InstlView.CenterTextView) findViewById(ConstantValues.SPREAD_UI_TEXTID);
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
                    case ConstantValues.UI_MRAIDVIEW_ID:
                        break;
                    case ConstantValues.SPREAD_UI_NOTIFYLAYOUTID:
                        child.layout(0, 0, screenWidth, screenWidth / 5);
                        break;
                    case ConstantValues.SPREAD_UI_COUNTERID:
                        int spreadNotifyType = 1;
                        if (null != kySpreadListener) {
                            spreadNotifyType = kySpreadListener.getNotifyType();
                        }
                        if (spreadNotifyType == AdSpreadBIDView.NOTIFY_COUNTER_NUM || spreadNotifyType == AdSpreadBIDView.NOTIFY_COUNTER_TEXT) {
                            child.layout((int) (screenWidth - screenWidth / 5 - padding * density), (int) (padding * density), (int) (screenWidth - padding * density / 2), (int) ((screenWidth / 11) + (padding * density)));
                        }
                        break;

                    case ConstantValues.SPREAD_UI_LOGOIMAGEID:
                        switch (layoutType) {
                            case ConstantValues.SPREAD_UI_NULL:
                            case ConstantValues.SPREAD_UI_TOP:
                            case ConstantValues.SPREAD_UI_CENTER:
                            case ConstantValues.SPREAD_UI_BOTTOM:
                            case ConstantValues.SPREAD_UI_ALLCENTER:
                            case ConstantValues.SPREAD_UI_EXTRA2:
                            case ConstantValues.SPREAD_UI_EXTRA1:
                                if (hasLogo == ConstantValues.SPREAD_RESP_HAS_LOGO) {
                                    //child.layout(0, screenHeight - (screenWidth / 4), screenWidth, screenHeight); //wilder 2020
                                    //Drawable drawable = kySpreadListener.getSpreadLogo();
                                    //int h = drawable.getIntrinsicHeight();
                                    child.layout(0, screenHeight - (logoHeight), screenWidth, screenHeight);
                                }
                                else {
                                    child.setVisibility(View.GONE);
                                }
                                break;
                            case ConstantValues.SPREAD_UI_EXTRA3:
                                child.setVisibility(View.GONE);
                                break;
                        }
                        break;
                    case ConstantValues.SPREAD_UI_FRAMEID:
                        switch (deformation) {
                            case ConstantValues.SPREAD_UI_SCALE_NOHTML:
                            case ConstantValues.SPREAD_UI_SCALE_INCLUDEHTML:
                                switch (layoutType) {
                                    case ConstantValues.SPREAD_UI_CENTER:
                                        if (!isHtml)
                                            child.layout(0, 0, sWidth, (screenHeight - (logoHeight * hasLogo)));
                                        else
                                            child.layout(0, (screenHeight - (logoHeight * hasLogo)) / 2 - sHeight / 2, sWidth, (screenHeight - (logoHeight * hasLogo)) / 2 + sHeight / 2);
                                        break;
                                    case ConstantValues.SPREAD_UI_BOTTOM:
                                        if (!isHtml)
                                            child.layout(0, 0, sWidth, (screenHeight - (logoHeight * hasLogo)));
                                        else
                                            child.layout(0, (screenHeight - (logoHeight * hasLogo)) - sHeight, sWidth, screenHeight - (logoHeight * hasLogo));
                                        break;
                                    case ConstantValues.SPREAD_UI_TOP:
                                    case ConstantValues.SPREAD_UI_ALLCENTER:
                                    case ConstantValues.SPREAD_UI_EXTRA3:
                                    case ConstantValues.SPREAD_UI_EXTRA2:
                                    case ConstantValues.SPREAD_UI_EXTRA1:
                                        if (!isHtml)
                                            child.layout(0, 0, sWidth, (screenHeight - (logoHeight * hasLogo)));
                                        else
                                            child.layout(0, 0, sWidth, sHeight);
                                        break;
                                }
                                break;
                            default:
                                switch (layoutType) {
                                    case ConstantValues.SPREAD_UI_CENTER:
                                        //wilder 2020 更改坐标
                                        child.layout(0, 0 /*(screenHeight - (logoHeight * hasLogo)) / 2 - sHeight / 2*/,
                                                        sWidth, (screenHeight - (logoHeight * hasLogo)) / 2 + sHeight / 2);
                                        break;
                                    case ConstantValues.SPREAD_UI_BOTTOM:
                                        child.layout(0, (screenHeight - (logoHeight * hasLogo)) - sHeight, sWidth, screenHeight - (logoHeight * hasLogo));
                                        break;
                                    case ConstantValues.SPREAD_UI_TOP:
                                    case ConstantValues.SPREAD_UI_ALLCENTER:
                                    case ConstantValues.SPREAD_UI_EXTRA3:
                                    case ConstantValues.SPREAD_UI_EXTRA2:
                                    case ConstantValues.SPREAD_UI_EXTRA1:
                                        child.layout(0, 0, sWidth, sHeight);
                                        break;
                                }
                                break;
                        }
                        break;
                    case ConstantValues.UI_ADICON_ID:
                        if (null != findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID) && 0 != findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID).getHeight())
                            if (sHeight == screenHeight)
                                child.layout(0, screenHeight - screenWidth / 25, screenWidth / 8, screenHeight);
                            else if (sHeight > screenHeight - (screenWidth / 4))
                                child.layout(0, (screenHeight - screenWidth / 4) - screenWidth / 25, screenWidth / 8, (screenHeight - screenWidth / 4));
                            else {
                                switch (layoutType) {
                                    case ConstantValues.SPREAD_UI_CENTER:
                                        child.layout(0, ((screenHeight - (logoHeight * hasLogo)) / 2 + sHeight / 2) - screenWidth / 25, sWidth, (screenHeight - (logoHeight * hasLogo)) / 2 + sHeight / 2);
                                        break;
                                    case ConstantValues.SPREAD_UI_BOTTOM:
                                        child.layout(0, (screenHeight - (logoHeight * hasLogo)) - screenWidth / 25, sWidth, screenHeight - (logoHeight * hasLogo));
                                        break;
                                    case ConstantValues.SPREAD_UI_TOP:
                                    case ConstantValues.SPREAD_UI_ALLCENTER:
                                    case ConstantValues.SPREAD_UI_EXTRA3:
                                    case ConstantValues.SPREAD_UI_EXTRA2:
                                    case ConstantValues.SPREAD_UI_EXTRA1:
                                        child.layout(0, sHeight - screenWidth / 25, screenWidth / 8, sHeight);
                                        break;
                                }
                            }
                        else {
                            if (null != (adLayout = ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREAD_UI_FRAMEID))) {
                                int bottom = adLayout.getBottom();
                                child.layout(0, bottom - screenWidth / 25, screenWidth / 8, bottom);
                            }
                        }
                        break;
                    case ConstantValues.UI_ADLOGO_ID:
                        if (null != findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID) && 0 != findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID).getHeight()) {
                            if (sHeight == screenHeight)
                                child.layout(screenWidth - (screenWidth / 8), screenHeight - screenWidth / 25, screenWidth, screenHeight);
                            else if (sHeight > screenHeight - (screenWidth / 4))
                                child.layout(screenWidth - (screenWidth / 8), (screenHeight - screenWidth / 4) - screenWidth / 25, screenWidth, (screenHeight - screenWidth / 4));
                            else {
//                                child.layout(screenWidth - (screenWidth / 8), sHeight - screenWidth / 25, screenWidth, sHeight);
                                switch (layoutType) {
                                    case ConstantValues.SPREAD_UI_CENTER:
                                        child.layout(screenWidth - (screenWidth / 8), ((screenHeight - (logoHeight * hasLogo)) / 2 + sHeight / 2) - screenWidth / 25, screenWidth, (screenHeight - (logoHeight * hasLogo)) / 2 + sHeight / 2);
                                        break;
                                    case ConstantValues.SPREAD_UI_BOTTOM:
                                        child.layout(screenWidth - (screenWidth / 8), (screenHeight - (logoHeight * hasLogo)) - screenWidth / 25, screenWidth, screenHeight - (logoHeight * hasLogo));
                                        break;
                                    case ConstantValues.SPREAD_UI_TOP:
                                    case ConstantValues.SPREAD_UI_ALLCENTER:
                                    case ConstantValues.SPREAD_UI_EXTRA3:
                                    case ConstantValues.SPREAD_UI_EXTRA2:
                                    case ConstantValues.SPREAD_UI_EXTRA1:
                                        child.layout(screenWidth - (screenWidth / 8), sHeight - screenWidth / 25, screenWidth, sHeight);
                                        break;
                                }
                            }
                        } else {
                            if (null != (adLayout = ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREAD_UI_FRAMEID))) {
                                int bottom = adLayout.getBottom();
                                child.layout(screenWidth - (screenWidth / 8), bottom - screenWidth / 25, screenWidth, bottom);
                            }
                        }
                        break;
                    case ConstantValues.SPREAD_UI_TEXTID:
                        if (hasLogo == ConstantValues.SPREAD_RESP_HAS_LOGO) {
                            if (null != ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID)) {
                                if (sHeight > screenHeight - (screenWidth / 4))
                                    child.layout(0, screenHeight - (screenWidth / 2), screenWidth * 3 / 4, screenHeight - (screenWidth / 4));
                                else
                                    child.layout(0, sHeight - (screenWidth / 4), screenWidth * 3 / 4, sHeight);
                            }
                        } else {
                            if (null != (adLayout = ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREAD_UI_FRAMEID))) {
                                int bottom = adLayout.getBottom();
                                child.layout(0, bottom - (screenWidth / 4), screenWidth * 3 / 4, bottom);
                            }
                        }
                        break;
                    case ConstantValues.MIXED_UI_BEHAVEICON_ID:
                        if (hasLogo == ConstantValues.SPREAD_RESP_HAS_LOGO) {
                            if (null != ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID)) {
                                if (sHeight > screenHeight - (screenWidth / 4))
                                    child.layout((screenWidth * 3 / 4), screenHeight - (screenWidth / 2), screenWidth, screenHeight - (screenWidth / 4));
                                else
                                    child.layout((screenWidth * 3 / 4), sHeight - (screenWidth / 4), screenWidth, sHeight);
                            }
                        } else {
                            if (null != (adLayout = ((RelativeLayout) child.getParent()).findViewById(ConstantValues.SPREAD_UI_FRAMEID))) {
                                int bottom = adLayout.getBottom();
                                child.layout((screenWidth * 3 / 4), bottom - (screenWidth / 4), screenWidth, bottom);
                            }
                        }
                        break;
                    case ConstantValues.SPREAD_UI_MIXLAYOUTID:
                        if (null != findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID)) {
                            int logoHeight = findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID).getHeight();
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
        ImageView logoImage = (ImageView)findViewById(ConstantValues.UI_ADLOGO_ID);
        ImageView iconImage = (ImageView)findViewById(ConstantValues.UI_ADICON_ID);
        if (null != kySpreadListener) {
            if (null != logoImage) {
                String logoPath = kySpreadListener.getAdLogo();
                logoImage.setImageDrawable(new BitmapDrawable(getContext().getResources(), logoPath));
            }
            if (null != iconImage) {
                String iconPath = kySpreadListener.getAdIcon();
                iconImage.setImageDrawable(new BitmapDrawable(getContext().getResources(), iconPath));
            }
        }
    }

    private void addCloseBtn() {
        RelativeLayout notifyLayout = new RelativeLayout(getContext());

        InstlView.CenterTextView adCounter = new InstlView.CenterTextView(getContext());
        //TextView adCounter = new TextView(getContext());
        notifyLayout.setId(ConstantValues.SPREAD_UI_NOTIFYLAYOUTID);
        adCounter.setId(ConstantValues.SPREAD_UI_COUNTERID);

        adCounter.textSize = (int) (18 * densityScale);
        adCounter.setTextColor(Color.WHITE);


        addView(notifyLayout);
        addView(adCounter);
        adCounter.setOnTouchListener(this);

    }

    private void addLogoIcon() {
        ImageView adLogo = new ImageView(getContext());
        ImageView adIcon = new ImageView(getContext());
        adLogo.setId(ConstantValues.UI_ADLOGO_ID);
        adIcon.setId(ConstantValues.UI_ADICON_ID);
        addView(adLogo);
        addView(adIcon);

        adIcon.setScaleType(ImageView.ScaleType.FIT_START);
        adLogo.setScaleType(ImageView.ScaleType.FIT_END);
    }

    private void addSpreadText() {
        ImageView behavIcon = new ImageView(getContext());
        TextView title = new InstlView.CenterTextView(getContext());
        title.setId(ConstantValues.SPREAD_UI_TEXTID);
        behavIcon.setId(ConstantValues.MIXED_UI_BEHAVEICON_ID);
        behavIcon.setPadding((int) (screenWidth / 4 * .3), (int) (screenWidth / 4 * .3), (int) (screenWidth / 4 * .3), (int) (screenWidth / 4 * .3));
        behavIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(title);
        addView(behavIcon);
        title.setOnTouchListener(this);
        behavIcon.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (null != findViewById(ConstantValues.SPREAD_UI_COUNTERID)) {
            findViewById(ConstantValues.SPREAD_UI_COUNTERID).getGlobalVisibleRect(closeRect);
            if (closeRect.right != 0 && kySpreadListener.getNotifyType() == AdSpreadBIDView.NOTIFY_COUNTER_NUM)
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
                    if (v.getId() == ConstantValues.SPREAD_UI_COUNTERID) {
//                    if (closeRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        if (null != kySpreadListener)
                            kySpreadListener.onCloseBtnClicked();
                    } else if (v.getId() == ConstantValues.SPREAD_UI_TEXTID || v.getId() == ConstantValues.MIXED_UI_BEHAVEICON_ID) {
                        if (null != kySpreadListener)
                            kySpreadListener.onViewClicked(event, null, null, event.getX(), event.getY());
                    } else if (touchRect.contains((int) event.getRawX(), (int) event.getRawY()))
//                        kySpreadListener.onViewCLicked(downX, downY, event.getX(), event.getY());
//                }
                        break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
