package com.kuaiyou.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kuaiyou.interfaces.KyInstalListener;
import com.kuaiyou.interfaces.KySpreadListener;
import com.kuaiyou.interfaces.AdVGListener;
import com.kuaiyou.mraid.MRAIDView;
import com.kuaiyou.mraid.interfaces.MRAIDNativeFeatureListener;
import com.kuaiyou.mraid.interfaces.MRAIDViewListener;
import com.kuaiyou.obj.AdsBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLDecoder;
import java.util.HashMap;

public class InstlView extends RelativeLayout implements View.OnTouchListener,MRAIDViewListener, MRAIDNativeFeatureListener {
    private double density = 2.0;
    private float scaledDensity;
    private int padding = 4;
    private SoftReference<Bitmap> titleBitmap;
    private int titleHeight;
    private Paint backgroudPaint;

    private float downX, downY;
    private HashMap<String, Integer> sizeMap;
    private Context context;

    private AdsBean adsBean;
    private AdVGListener instlViewListener = null;

    private Matrix matrix;
    private Rect rect;
    private int adType;
    private int screenSize[];

    public InstlView(Context context, HashMap<String, Integer> sizeMap) {
        this(context, sizeMap, ConstantValues.RESP_ADTYPE_HTML);
    }

    public InstlView(Context context, HashMap<String, Integer> sizeMap, int adType ) {
        super(context);
        setWillNotDraw(false);
        this.setBackgroundColor(Color.WHITE); //wilder 2020 BLACK->RED
        this.adType = adType;
        this.context = context;
        try {
            this.sizeMap = sizeMap;
            screenSize = AdViewUtils.getWidthAndHeight(context, false, true);
            if (adType == ConstantValues.RESP_ADTYPE_MIXED) {
                //混合资源广告
                titleBitmap = new SoftReference<Bitmap>(new BitmapDrawable(getResources(),
                        AdViewUtils.getImageFromAssetsFile("mixed_bg.jpg")).getBitmap());
//                titleBitmap = new SoftReference<Bitmap>(BitmapFactory.decodeStream(getClass()
//                        .getResourceAsStream(ConstantValues.WEBVIEW_IMAGE_BASE_PATH + "mixed_bg.jpg")));
                if (null != titleBitmap || null != titleBitmap.get()) {
                    matrix = new Matrix();
                    float scale = (float) sizeMap.get(ConstantValues.INSTL_WIDTH_KEY) / (float) titleBitmap.get().getWidth();
                    titleHeight = (int) (titleBitmap.get().getHeight() * scale);
                    matrix.setScale(scale, scale);
                }
                backgroudPaint = new Paint();
                backgroudPaint.setColor(Color.parseColor("#ece8e5"));
            }
            padding = (int) (padding * density);
            initWidgetLayout(adType);
            density = AdViewUtils.getDensity(getContext());
            scaledDensity = AdViewUtils.getScaledDensity(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInstlViewListener(AdVGListener kyListener) {
        this.instlViewListener = kyListener;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (null != instlViewListener && instlViewListener instanceof KyInstalListener) {
            ((KyInstalListener) instlViewListener).onVisiblityChange(visibility);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(sizeMap.get(ConstantValues.INSTL_WIDTH_KEY), sizeMap.get(ConstantValues.INSTL_HEIGHT_KEY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        try {
            int instlWidth = sizeMap.get(ConstantValues.INSTL_WIDTH_KEY);
            int instlHeight = sizeMap.get(ConstantValues.INSTL_HEIGHT_KEY);

            if (titleHeight == 0)
                titleHeight = instlHeight / 3;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                switch (child.getId()) {
                    case ConstantValues.MIXED_UI_TITLEID:
                        child.layout(titleHeight, titleHeight / 6, instlWidth - titleHeight / 4, (titleHeight / 2));
                        break;
                    case ConstantValues.MIXED_UI_SUBTITLE_ID:
                        child.layout(titleHeight, (titleHeight / 2), instlWidth - titleHeight / 4, titleHeight / 6 * 5);
                        break;
                    case ConstantValues.MIXED_UI_DESCRIPTTEXT_ID:
                        child.layout(titleHeight / 5, titleHeight / 6 * 7, instlWidth - titleHeight / 5, instlHeight - (instlWidth / 5) - (titleHeight / 5));
                        break;
                    case ConstantValues.MIXED_UI_BEHAVEICON_ID:
                        child.layout(instlWidth / 3, instlHeight - (instlWidth / 5), instlWidth / 3 * 2, instlHeight - (instlWidth / 5) + (instlWidth / 10));
                        break;
                    case ConstantValues.MIXED_UI_ICONID:
                        child.layout(titleHeight / 6, titleHeight / 6, titleHeight / 6 * 5, titleHeight / 6 * 5);
                        break;
                    case ConstantValues.UI_ADLOGO_ID:
                        child.layout(instlWidth - (instlWidth / 8), instlHeight - (instlWidth / 25), instlWidth, instlHeight);
                        break;
                    case ConstantValues.UI_ADICON_ID:
                        child.layout(0, instlHeight - (instlWidth / 25), (instlWidth / 8), instlHeight);
                        break;
                    case ConstantValues.UI_CLOSEBTN_ID:
                        try {
                            if (null == screenSize)
                                screenSize = AdViewUtils.getWidthAndHeight(getContext(), false, true);
                            if (null == screenSize)
                                screenSize = new int[]{1080, 1920};
                            if (screenSize[0] == 0) {
                                screenSize[0] = 1080;
                                screenSize[1] = 1920;
                            }
                            child.layout(instlWidth - screenSize[0] / 14, padding, instlWidth - padding, screenSize[0] / 14);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case ConstantValues.UI_WEBVIEW_ID:
//                    child.getLayoutParams().width = instlWidth;
//                    child.getLayoutParams().height = instlHeight;
                    case ConstantValues.UI_MRAIDVIEW_ID:
                        child.layout(0, 0, instlWidth, instlHeight);
//                    ((MRAIDView) child).setLayoutParmas(instlWidth, instlHeight);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if (adType != ConstantValues.RESP_ADTYPE_MIXED) {
                return;
            }
            if (null != backgroudPaint) {
                if (rect == null)
                    rect = new Rect(0, 0, sizeMap.get(ConstantValues.INSTL_WIDTH_KEY), sizeMap.get(ConstantValues.INSTL_HEIGHT_KEY));
                canvas.drawRect(rect, backgroudPaint);
            }
            if (null != titleBitmap && null != titleBitmap.get())
                canvas.drawBitmap(titleBitmap.get(), matrix, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWidgetLayout( int adType ) {
        ImageView closeBtn = new ImageView(getContext());
        ImageView adLogo = new ImageView(getContext());
        ImageView adIcon = new ImageView(getContext());
        if (adType == ConstantValues.RESP_ADTYPE_MIXED) {
            ImageView icon = new ImageView(getContext());
            TextView title = new TextView(getContext());
            TextView subTitle = new TextView(getContext());
            TextView descript = new TextView(getContext());
            CenterTextView behaveBtn = new CenterTextView(getContext());

            title.setId(ConstantValues.MIXED_UI_TITLEID);
            subTitle.setId(ConstantValues.MIXED_UI_SUBTITLE_ID);
            descript.setId(ConstantValues.MIXED_UI_DESCRIPTTEXT_ID);
            behaveBtn.setId(ConstantValues.MIXED_UI_BEHAVEICON_ID);
            icon.setId(ConstantValues.MIXED_UI_ICONID);

            title.setTextColor(Color.WHITE);
            subTitle.setTextColor(Color.WHITE);
            descript.setTextColor(Color.BLACK);
            behaveBtn.setTextColor(Color.WHITE);
            behaveBtn.setBackgroundDrawable(AdViewUtils.getColorDrawable(getContext(), "#2c91a6"));

            addView(icon);
            addView(behaveBtn);
            addView(title);
            addView(subTitle);
            addView(descript);
        } else {
            MRAIDView mraidView = new MRAIDView(getContext(), this, this,
                                true,
                                    sizeMap.get(ConstantValues.INSTL_WIDTH_KEY),
                                    sizeMap.get(ConstantValues.INSTL_HEIGHT_KEY)
                                    );
            WebView webView = mraidView.getMraidWebView();
            mraidView.setId(ConstantValues.UI_MRAIDVIEW_ID);
            webView.setId(ConstantValues.UI_WEBVIEW_ID);
            //wilder 1205
            RelativeLayout.LayoutParams  layoutParmars = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            //wilder 2020,这里调整为需求宽高,正常情况不需要
//            RelativeLayout.LayoutParams  layoutParmars = new RelativeLayout.LayoutParams( sizeMap.get(ConstantValues.INSTL_WIDTH_KEY),
//                                                            sizeMap.get(ConstantValues.INSTL_HEIGHT_KEY));
            layoutParmars.addRule(RelativeLayout.CENTER_IN_PARENT);
            mraidView.setLayoutParams(layoutParmars);

            addView( mraidView );
            //end 1205

        }
        adLogo.setId(ConstantValues.UI_ADLOGO_ID);
        adIcon.setId(ConstantValues.UI_ADICON_ID);
        closeBtn.setId(ConstantValues.UI_CLOSEBTN_ID);

        addView(closeBtn);
        addView(adLogo);
        addView(adIcon);

        adIcon.setScaleType(ImageView.ScaleType.FIT_START);
        adLogo.setScaleType(ImageView.ScaleType.FIT_END);

        closeBtn.setOnTouchListener(this);
        setOnTouchListener(this);

    }

    public void setActionBtnText(int adAction) {
        try {
            CenterTextView iBtn = (CenterTextView) findViewById(ConstantValues.MIXED_UI_BEHAVEICON_ID);
            if (null != iBtn) {
                switch (adAction) {
                    case ConstantValues.RESP_ACT_DOWNLOAD:
                        iBtn.text = ("Download Free");
                        break;
                    case ConstantValues.RESP_ACT_OPENWEB:
                        iBtn.text = ("Watch Free");
                        break;
                    default:
                        iBtn.text = ("Check Free");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setContent(AdsBean adsBean, String imagePath) {
        this.adsBean = adsBean;
        try {
            switch (adsBean.getAdType()) {
                case ConstantValues.RESP_ADTYPE_MIXED:
                    ImageView icon = (ImageView) findViewById(ConstantValues.MIXED_UI_ICONID);
                    TextView title = (TextView) findViewById(ConstantValues.MIXED_UI_TITLEID);
                    TextView subTitle = (TextView) findViewById(ConstantValues.MIXED_UI_SUBTITLE_ID);
                    TextView description = (TextView) findViewById(ConstantValues.MIXED_UI_DESCRIPTTEXT_ID);
                    if (null != imagePath) {
                        icon.setImageDrawable(new BitmapDrawable(getResources(), getClass().getResourceAsStream(imagePath)));
                    }
                    if (!TextUtils.isEmpty(adsBean.getAdTitle()))
                        title.setText(adsBean.getAdTitle());
                    if (!TextUtils.isEmpty(adsBean.getAdSubTitle()))
                        subTitle.setText(adsBean.getAdSubTitle());
                    if (!TextUtils.isEmpty(adsBean.getAdText()))
                        description.setText(adsBean.getAdText());
                    setActionBtnText(adsBean.getAdAct());
                    setTextSize();
                    invalidate();
                    break;
            }
            if (null != instlViewListener) {
                if (instlViewListener.getCloseble())
                    setCloseBtn();
            }

            if (null != instlViewListener) {
                setAdLogo(0, instlViewListener.getAdLogo());
                setAdLogo(1, instlViewListener.getAdIcon());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCloseBtn() {
        try {
            ImageView view = (ImageView) findViewById(ConstantValues.UI_CLOSEBTN_ID);
            if (null != view) {
                String resoursePath = ConstantValues.WEBVIEW_IMAGE_BASE_PATH + "close_ad_btn.png";
                //if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    Bitmap bm = AdViewUtils.getImageFromAssetsFile("close_ad_btn.png");
                    view.setImageDrawable(new BitmapDrawable(this.getResources(), bm));
                    //view.setImageDrawable(new BitmapDrawable(this.getResources(), getClass().getResourceAsStream(resoursePath)));
//                }
//                else {
//                    view.setImageDrawable(new BitmapDrawable(this.getResources(), getClass().getResourceAsStream(resoursePath)));
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                findViewById(ConstantValues.UI_CLOSEBTN_ID).setBackgroundColor(Color.GRAY);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public View getAdLogoView() {

        return (ImageView) findViewById(ConstantValues.UI_ADLOGO_ID);
    }

    public View getAdIconView() {

        return (ImageView) findViewById(ConstantValues.UI_ADICON_ID);
    }

    public View getAdCloseButton() {

        return (ImageView) findViewById(ConstantValues.UI_CLOSEBTN_ID);
    }

    private void setAdLogo(int isLogo, String path) {
        ImageView view;
        try {
            if (null == path)
                return;
            if (isLogo == 0)
                view = (ImageView) findViewById(ConstantValues.UI_ADLOGO_ID);
            else
                view = (ImageView) findViewById(ConstantValues.UI_ADICON_ID);
            if (TextUtils.isEmpty(path) || null == view) //in spread mode, view will be null
                return;
            if (path.startsWith("/assets")) {
                //wilder load assets file
                Bitmap bm = AdViewUtils.getImageFromAssetsFile(path.replace("/assets/", ""));
                view.setImageDrawable(new BitmapDrawable(this.getResources(), bm));
                //view.setImageDrawable(new BitmapDrawable(this.getResources(), getClass().getResourceAsStream(path)));
            }
            else {
                view.setImageDrawable(new BitmapDrawable(this.getResources(), path));
                //view.setImageDrawable(new BitmapDrawable(this.getResources(), getClass().getResourceAsStream(path)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTextSize() {
        try {
            TextView title = (TextView) findViewById(ConstantValues.MIXED_UI_TITLEID);
            TextView subTitle = (TextView) findViewById(ConstantValues.MIXED_UI_SUBTITLE_ID);
            TextView content = (TextView) findViewById(ConstantValues.MIXED_UI_DESCRIPTTEXT_ID);
            CenterTextView clickBtn = (CenterTextView) findViewById(ConstantValues.MIXED_UI_BEHAVEICON_ID);
            if (density <= 1.5) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                clickBtn.textSize = (int) (14 * scaledDensity);//(TypedValue.COMPLEX_UNIT_SP, 14);
            } else if (density == 2) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                clickBtn.textSize = (int) (15 * scaledDensity);//(TypedValue.COMPLEX_UNIT_SP, 15);
            } else if (density == 3) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                clickBtn.textSize = (int) (16 * scaledDensity);//(TypedValue.COMPLEX_UNIT_SP, 16);
            } else if (density >= 4) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
                subTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                clickBtn.textSize = (int) (17 * scaledDensity);//(TypedValue.COMPLEX_UNIT_SP, 17);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MRAIDView getMraidView() {
        return (MRAIDView) findViewById(ConstantValues.UI_MRAIDVIEW_ID);
    }

    public WebView getWebView() {
        return (WebView) findViewById(ConstantValues.UI_WEBVIEW_ID);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(event.getX() - downX) < padding * padding / 2 && Math.abs(event.getY() - downY) < padding * padding / 2 && event.getEventTime() - event.getDownTime() < 1000) {
                    int instlWidth = sizeMap.get(ConstantValues.INSTL_WIDTH_KEY);
                    int instlHeight = sizeMap.get(ConstantValues.INSTL_HEIGHT_KEY);
                    if (v.getId() == ConstantValues.UI_CLOSEBTN_ID) {
                        if (null != instlViewListener) {
                            instlViewListener.onCloseBtnClicked();
                        }
                    } else if (event.getX() < instlWidth && event.getY() > instlHeight / 3 * 2) {
                        if (adType == ConstantValues.RESP_ADTYPE_MIXED) {
                            if (null != instlViewListener)
                                instlViewListener.onViewClicked(event, null,null, event.getX(), event.getY());
                        }
                    }
                }
                break;
        }
        return true;
    }

    public static class CenterTextView extends /*TextView*/ AppCompatTextView {
        public String text;
        private String[] multiLineStr;
        public int textSize;
        private TextPaint mPaint;
        private Rect mBound;
        private float startX, startY;
        private int color;

        public CenterTextView(Context context) {
            super(context);
        }

        public void setTextColor(int color) {
            this.color = color;
        }

        /**
         * 最大支持3行
         *
         * @param maxSize
         * @return
         */
        private Rect measureTextSize(int maxSize) {
            Rect mBound = new Rect();
            try {
                int[] line = new int[3];
                int lineCount = 0;
                mPaint.getTextBounds(text, 0, text.length(), mBound);
                if (mBound.width() < maxSize) {
                    return mBound;
                } else {
                    for (int i = 0; i < text.length(); i++) {
                        mPaint.getTextBounds(text, line[(lineCount - 1) < 0 ? 0 : lineCount - 1], i + 1, mBound);
                        if (mBound.width() < maxSize)
                            continue;
                        else {
                            if (lineCount == 2)
                                continue;
                            line[lineCount] = i + 1;
                            lineCount++;
                        }
                    }
                    multiLineStr = new String[lineCount + 1];
                    if (lineCount == 1) {
                        multiLineStr[0] = text.substring(0, line[0] - 1);
                        multiLineStr[1] = text.substring(line[0] - 1, text.length());
                    } else if (lineCount == 2) {
                        multiLineStr[0] = text.substring(0, line[0] - 1);
                        multiLineStr[1] = text.substring(line[0] - 1, line[1] - 1);
                        multiLineStr[2] = text.substring(line[1] - 1, text.length());
                    }
                    mPaint.getTextBounds(multiLineStr[0], 0, multiLineStr[0].length(), mBound);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mBound;
        }

        private void initCenterSize(int maxWidth) {
            mBound = new Rect();
            mPaint = new TextPaint();
            mPaint.setColor(color);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextSize(textSize);
            mBound = measureTextSize(maxWidth);
            Paint.FontMetricsInt fm = mPaint.getFontMetricsInt();
            startX = getWidth() / 2 - mBound.width() / 2;
            startY = getHeight() / 2 - fm.descent + (fm.descent - fm.ascent) / 2;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            try {
                if (!TextUtils.isEmpty(text)) {
                    initCenterSize(canvas.getWidth());
                    /*
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    //paint.setStrokeWidth(3);
                    //paint.setTextSize(80);
                    //String testString = "测试：ijkJQKA:1234";
                    paint.setColor(Color.CYAN);
                    canvas.drawRect(mBound, paint);
                    */
                    if (null != multiLineStr) {
                        if (multiLineStr.length == 2) {
                            canvas.drawText(multiLineStr[0], startX, (float) (startY - 0.5f * ((float) mBound.height() * 1.5)), mPaint);
                            canvas.drawText(multiLineStr[1], startX, (float) (startY + 0.5f * ((float) mBound.height() * 1.5)), mPaint);
                        } else if (multiLineStr.length == 3) {
                            canvas.drawText(multiLineStr[0], startX, (float) (startY - ((float) mBound.height() * 1.5)), mPaint);
                            canvas.drawText(multiLineStr[1], startX, (float) (startY), mPaint);
                            canvas.drawText(multiLineStr[2], startX, (float) (startY + ((float) mBound.height() * 1.5)), mPaint);
                        }
                    } else {
                        canvas.drawText(text, startX, startY, mPaint);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //////////////////////////MRAID listener  ///////////////////////////////////////////////////////////
    @Override
    public void mraidNativeFeatureDownload(String url) {
        if (null != instlViewListener)
            instlViewListener.checkClick(url);
    }

    @Override
    public void mraidNativeFeatureCallTel(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL); // android.intent.action.DIAL
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void mraidNativeFeatureOpenDeeplink(String url) {
        if (url.startsWith("mraid")) {
            try {
                url = URLDecoder.decode(url.replace("mraid://openDeeplink?url=", ""), "UTF-8");
                adsBean.setDeeplink(url);
                if (null != instlViewListener)
                    instlViewListener.checkClick(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mraidNativeFeatureOpenBrowser(String url) {
        if (null != instlViewListener)
            instlViewListener.checkClick(url);
    }

    @Override
    public void mraidNativeFeatureSendSms(String url) {
        AdViewUtils.sendSms(context, url);
    }

    @Override
    public void mraidNativeFeatureCreateCalendarEvent(String eventJSON) {
    }

    @Override
    public void mraidNativeFeatureStorePicture(String url) {
    }
    /* **************************************************************************************
   -----------------------------    MRAIDViewListener   -----------------------------
    *************************************************************************************** */
    @Override
    public void mraidViewOMJSInjected( MRAIDView mraidView ) {
        //OMSDk v1.2 , add friendly obstructions
        if (null != instlViewListener && instlViewListener instanceof KySpreadListener) {
            //spread,如果想要测试obstruction,可以屏蔽adlogoid或者adiconid,这样显示就不是100%了
            //mraidView.AddOMObstructions(((KySpreadListener)instlViewListener).getSpreadView().findViewById(ConstantValues.UI_ADLOGO_ID));
            //mraidView.AddOMObstructions(((KySpreadListener)instlViewListener).getSpreadView().findViewById(ConstantValues.UI_ADICON_ID));
            mraidView.AddOMObstructions(((KySpreadListener)instlViewListener).getSpreadView().findViewById(ConstantValues.UI_CLOSEBTN_ID));
            mraidView.AddOMObstructions(((KySpreadListener)instlViewListener).getSpreadView().findViewById(ConstantValues.SPREAD_UI_NOTIFYLAYOUTID));
            mraidView.AddOMObstructions(((KySpreadListener)instlViewListener).getSpreadView().findViewById(ConstantValues.SPREAD_UI_COUNTERID));
            mraidView.AddOMObstructions(((KySpreadListener)instlViewListener).getSpreadView().findViewById(ConstantValues.SPREAD_UI_LOGOIMAGEID));
            mraidView.AddOMObstructions(((KySpreadListener)instlViewListener).getSpreadView().findViewById(ConstantValues.SPREAD_UI_TEXTID));
            mraidView.AddOMObstructions(((KySpreadListener)instlViewListener).getSpreadView().findViewById(ConstantValues.MIXED_UI_BEHAVEICON_ID));
        }else {
            //instl
            mraidView.AddOMObstructions(getAdLogoView());
            mraidView.AddOMObstructions(getAdIconView());
            mraidView.AddOMObstructions(getAdCloseButton());
        }
        //start omsession, after add obstructions
        mraidView.startOMSession();
        //omsdk ends
    }

    @Override
    public void mraidViewLoaded(MRAIDView mraidView) {
        //(wilder 20190622) only for spread
        if (null != instlViewListener && instlViewListener instanceof KySpreadListener) {
            ((KySpreadListener) instlViewListener).mraidViewHasLoaded();
        }
    }

    @Override
    public void mraidViewExpand(MRAIDView mraidView) {
    }

    @Override
    public void mraidViewClose(MRAIDView mraidView) {
    }

    @Override
    public boolean mraidViewResize(MRAIDView mraidView, int width, int height, int offsetX, int offsetY) {
        return false;
    }

    @Override
    public void onShouldOverride(String url) {
        // 至少触摸过才可以点击跳转
        if (adsBean.getTouchStatus() > MRAIDView.ACTION_DEFAULT) {
            if (null != instlViewListener)
                instlViewListener.checkClick(url);
//            clickCheck(url, adsBean, applyAdBean, retAdBean);
//                }
//            }
        }
    }
    @Override
    public WebResourceResponse onShouldIntercept(String url) {
        if (null != instlViewListener)
            return instlViewListener.shouldInterceptRequest(url);
        return null;
    }

    @Override
    public void loadDataError(int errorType) {
        if (null != instlViewListener)
            instlViewListener.onAdFailed(null,"Custom://" + errorType, false);
    }


}
