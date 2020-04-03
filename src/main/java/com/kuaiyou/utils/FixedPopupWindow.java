package com.kuaiyou.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Administrator on 2017/4/20.
 */
public class FixedPopupWindow {

    private int mWidth = WindowManager.LayoutParams.WRAP_CONTENT;
    private int mHeight = WindowManager.LayoutParams.WRAP_CONTENT;
    private boolean mIsShowing;
    private Context mContext;
    private WindowManager mWindowManager;
    private View mContentView;
    private PopupDecorView mDecorView;
    private OnDismissListener mOnDismissListener;

    public FixedPopupWindow(View contentView, int width, int height) {

        if (contentView != null) {
            mContext = contentView.getContext();
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }

        setContentView(contentView);
        mWidth = width;
        mHeight = height;

    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void setContentView(View contentView) {
        if (isShowing()) {
            return;
        }

        mContentView = contentView;

        if (mContext == null && mContentView != null) {
            mContext = mContentView.getContext();
        }

        if (mWindowManager == null && mContentView != null) {
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }

        // Setting the default for attachedInDecor based on SDK version here
        // instead of in the constructor since we might not have the context
        // object in the constructor. We only want to set default here if the
        // app hasn't already set the attachedInDecor.
//        if (mContext != null && !mAttachedInDecorSet) {
//            // Attach popup window in decor frame of parent window by default for
//            // {@link Build.VERSION_CODES.LOLLIPOP_MR1} or greater. Keep current
//            // behavior of not attaching to decor frame for older SDKs.
//            setAttachedInDecor(mContext.getApplicationInfo().targetSdkVersion
//                    >= Build.VERSION_CODES.LOLLIPOP_MR1);
//        }

    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void showAtLocation(View view, int gravity) {
        if (isShowing() || mContentView == null) {
            return;
        }

        mIsShowing = true;
//        mIsDropdown = false;

        final WindowManager.LayoutParams p = createPopupLayoutParams(view.getWindowToken());
        preparePopup(p);

        // Only override the default if some gravity was specified.
        if (gravity != Gravity.NO_GRAVITY) {
            p.gravity = gravity;
        }

//        p.x = x;
//        p.y = y;

        invokePopup(p);
    }

    private void invokePopup(WindowManager.LayoutParams p) {
        if (mContext != null) {
            p.packageName = mContext.getPackageName();
        }

        final PopupDecorView decorView = mDecorView;
        decorView.setFitsSystemWindows(false);//mLayoutInsetDecor);

//        setLayoutDirectionFromAnchor();

        mWindowManager.addView(decorView, p);

//        if (mEnterTransition != null) {
//            decorView.requestEnterTransition(mEnterTransition);
//        }
    }

//    private void setLayoutDirectionFromAnchor() {
//        if (mAnchor != null) {
//            View anchor = mAnchor.get();
//            if (anchor != null && mPopupViewInitialLayoutDirectionInherited) {
//                mDecorView.setLayoutDirection(anchor.getLayoutDirection());
//            }
//        }
//    }

    private int computeFlags(int curFlags) {
        curFlags &= ~(
                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                        WindowManager.LayoutParams.FLAG_SPLIT_TOUCH);
//        if(mIgnoreCheekPress) {
//            curFlags |= WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;
//        }
//        if (!mFocusable) {
//            curFlags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//            if (mInputMethodMode == INPUT_METHOD_NEEDED) {
//                curFlags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//            }
//        } else if (mInputMethodMode == INPUT_METHOD_NOT_NEEDED) {
//            curFlags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//        }
//        if (!mTouchable) {
//            curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//        }
//        if (mOutsideTouchable) {
//            curFlags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
//        }
//        if (!mClippingEnabled || mClipToScreen) {
//            curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
//        }
//        if (isSplitTouchEnabled()) {
//            curFlags |= WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
//        }
//        if (mLayoutInScreen) {
//            curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
//        }
//        if (mLayoutInsetDecor) {
//            curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
//        }
//        if (mNotTouchModal) {
        curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//        }
//        if (mAttachedInDecor) {
        curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR;
//        }
        return curFlags;
    }

    private WindowManager.LayoutParams createPopupLayoutParams(IBinder token) {
        final WindowManager.LayoutParams p = new WindowManager.LayoutParams();

        // These gravity settings put the view at the top left corner of the
        // screen. The view is then positioned to the appropriate location by
        // setting the x and y offsets to match the anchor's bottom-left
        // corner.
        p.flags = computeFlags(p.flags);
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.token = token;
        p.format = PixelFormat.TRANSLUCENT;
//        p.softInputMode = mSoftInputMode;
//        p.windowAnimations = computeAnimationResource();

//        if (mBackground != null) {
//            p.format = mBackground.getOpacity();
//        } else {
//            p.format = PixelFormat.TRANSLUCENT;
//        }
//
//        if (mHeightMode < 0) {
//            p.height = mLastHeight = mHeightMode;
//        } else {
//            p.height = mLastHeight = mHeight;
//        }
//
//        if (mWidthMode < 0) {
//            p.width = mLastWidth = mWidthMode;
//        } else {
//            p.width = mLastWidth = mWidth;
//        }

//        p.privateFlags = PRIVATE_FLAG_WILL_NOT_REPLACE_ON_RELAUNCH
//                | PRIVATE_FLAG_LAYOUT_CHILD_WINDOW_IN_PARENT_FRAME;

        // Used for debugging.
        p.setTitle("PopupWindow:" + Integer.toHexString(hashCode()));

        return p;
    }

    private void preparePopup(WindowManager.LayoutParams p) {
        if (mContentView == null || mContext == null || mWindowManager == null) {
            throw new IllegalStateException("You must specify a valid content view by "
                    + "calling setContentView() before attempting to show the popup.");
        }

        // The old decor view may be transitioning out. Make sure it finishes
        // and cleans up before we try to create another one.
//        if (mDecorView != null) {
//            mDecorView.cancelTransitions();
//        }

        // When a background is available, we embed the content view within
        // another view that owns the background drawable.
//        if (mBackground != null) {
//            mBackgroundView = createBackgroundView(mContentView);
//            mBackgroundView.setBackground(mBackground);
//        } else {
//            mBackgroundView = mContentView;
//        }

        mDecorView = createDecorView(mContentView);

        // The background owner should be elevated so that it casts a shadow.
//        mBackgroundView.setElevation(mElevation);

        // We may wrap that in another view, so we'll need to manually specify
        // the surface insets.
//        p.setSurfaceInsets(mBackgroundView, true /*manual*/, true /*preservePrevious*/);

//        mPopupViewInitialLayoutDirectionInherited =
//                (mContentView.getRawLayoutDirection() == View.LAYOUT_DIRECTION_INHERIT);
    }

    private PopupDecorView createDecorView(View contentView) {
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.gravity=Gravity.CENTER;
        final PopupDecorView decorView = new PopupDecorView(mContext);
        decorView.addView(contentView, layoutParams);
//        decorView.addView(contentView, MATCH_PARENT, height);
        decorView.setClipChildren(false);
        decorView.setClipToPadding(false);
        decorView.invalidate();
        //记住这个颜色的背景色
        //decorView.setBackgroundColor(Color.parseColor(ConstantValues.INSTL_POPWINDOW_WEBVIEW_BACKGOUNDCOLOR));
        decorView.setBackgroundColor(Color.TRANSPARENT);

        return decorView;
    }


    public void dismiss() {
        if (!isShowing()) {
            return;
        }
        final PopupDecorView decorView = mDecorView;
        final View contentView = mContentView;

        final ViewGroup contentHolder;
        final ViewParent contentParent = contentView.getParent();
        if (contentParent instanceof ViewGroup) {
            contentHolder = ((ViewGroup) contentParent);
        } else {
            contentHolder = null;
        }

//        // Ensure any ongoing or pending transitions are canceled.
//        decorView.cancelTransitions();

        mIsShowing = false;
//        mIsTransitioningToDismiss = true;

        // This method may be called as part of window detachment, in which
        // case the anchor view (and its root) will still return true from
        // isAttachedToWindow() during execution of this method; however, we
        // can expect the OnAttachStateChangeListener to have been called prior
        // to executing this method, so we can rely on that instead.
//        final Transition exitTransition = mExitTransition;
//        if (mIsAnchorRootAttached && exitTransition != null && decorView.isLaidOut()) {
//            // The decor view is non-interactive and non-IME-focusable during exit transitions.
//            final WindowManager.LayoutParams p = (WindowManager.LayoutParams) decorView.getLayoutParams();
//            p.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//            p.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//            p.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//            mWindowManager.updateViewLayout(decorView, p);
//
//            // Once we start dismissing the decor view, all state (including
//            // the anchor root) needs to be moved to the decor view since we
//            // may open another popup while it's busy exiting.
//            final View anchorRoot = mAnchorRoot != null ? mAnchorRoot.get() : null;
//            final Rect epicenter = getTransitionEpicenter();
//            exitTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
//                @Override
//                public Rect onGetEpicenter(Transition transition) {
//                    return epicenter;
//                }
//            });
//            decorView.startExitTransition(exitTransition, anchorRoot,
//                    new TransitionListenerAdapter() {
//                        @Override
//                        public void onTransitionEnd(Transition transition) {
//                            dismissImmediate(decorView, contentHolder, contentView);
//                        }
//                    });
//        } else {
        dismissImmediate(decorView, contentHolder, contentView);
        if (null != mOnDismissListener)
            mOnDismissListener.onDismiss();
//        }

        // Clears the anchor view.
//        detachFromAnchor();

//        if (mOnDismissListener != null) {
//            mOnDismissListener.onDismiss();
//        }
    }

    private void dismissImmediate(View decorView, ViewGroup contentHolder, View contentView) {
        // If this method gets called and the decor view doesn't have a parent,
        // then it was either never added or was already removed. That should
        // never happen, but it's worth checking to avoid potential crashes.
        if (decorView.getParent() != null) {
            mWindowManager.removeViewImmediate(decorView);
        }

        if (contentHolder != null) {
            contentHolder.removeView(contentView);
        }

        // This needs to stay until after all transitions have ended since we
        // need the reference to cancel transitions in preparePopup().
        mDecorView = null;
//        mBackgroundView = null;
//        mIsTransitioningToDismiss = false;
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    private class PopupDecorView extends FrameLayout {

        public PopupDecorView(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (getKeyDispatcherState() == null) {
                    return super.dispatchKeyEvent(event);
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    final KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    final KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null && state.isTracking(event) && !event.isCanceled()) {
                        //dismiss(); //wilder 2020 for back can support
                        return true;
                    }
                }
                return super.dispatchKeyEvent(event);
            } else {
                return super.dispatchKeyEvent(event);
            }
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (mTouchInterceptor != null && mTouchInterceptor.onTouch(this, ev)) {
//            return true;
//        }
            return super.dispatchTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();

            if ((event.getAction() == MotionEvent.ACTION_DOWN)
                    && ((x < 0) || (x >= getWidth()) || (y < 0) || (y >= getHeight()))) {
//                popupWindow.dismiss();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
//                popupWindow.dismiss();
                return true;
            } else {
                return super.onTouchEvent(event);
            }
        }

        /**
         * Requests that an enter transition run after the next layout pass.
         */
//    public void requestEnterTransition(Transition transition) {
//        final ViewTreeObserver observer = getViewTreeObserver();
//        if (observer != null && transition != null) {
//            final Transition enterTransition = transition.clone();
//
//            // Postpone the enter transition after the first layout pass.
//            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    final ViewTreeObserver observer = getViewTreeObserver();
//                    if (observer != null) {
//                        observer.removeOnGlobalLayoutListener(this);
//                    }
//
//                    final Rect epicenter = getTransitionEpicenter();
//                    enterTransition.setEpicenterCallback(new Transition.EpicenterCallback() {
//                        @Override
//                        public Rect onGetEpicenter(Transition transition) {
//                            return epicenter;
//                        }
//                    });
////                    startEnterTransition(enterTransition);
//                }
//            });
//        }
//    }

        /**
         * Starts the pending enter transition, if one is set.
         */
//    private void startEnterTransition(Transition enterTransition) {
//        final int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            final View child = getChildAt(i);
//            enterTransition.addTarget(child);
//            child.setVisibility(View.INVISIBLE);
//        }
//
//        TransitionManager.beginDelayedTransition(this, enterTransition);
//
//        for (int i = 0; i < count; i++) {
//            final View child = getChildAt(i);
//            child.setVisibility(View.VISIBLE);
//        }
//    }

        /**
         * Starts an exit transition immediately.
         * <p/>
         * <strong>Note:</strong> The transition listener is guaranteed to have
         * its {@code onTransitionEnd} method called even if the transition
         * never starts; however, it may be called with a {@code null} argument.
         */
//    public void startExitTransition(Transition transition, final View anchorRoot,
//                                    final Transition.TransitionListener listener) {
//        if (transition == null) {
//            return;
//        }
//
//        // The anchor view's window may go away while we're executing our
//        // transition, in which case we need to end the transition
//        // immediately and execute the listener to remove the popup.
//        anchorRoot.addOnAttachStateChangeListener(mOnAnchorRootDetachedListener);
//
//        // The exit listener MUST be called for cleanup, even if the
//        // transition never starts or ends. Stash it for later.
//        mPendingExitListener = new TransitionListenerAdapter() {
//            @Override
//            public void onTransitionEnd(Transition transition) {
//                anchorRoot.removeOnAttachStateChangeListener(mOnAnchorRootDetachedListener);
//                listener.onTransitionEnd(transition);
//
//                // The listener was called. Our job here is done.
//                mPendingExitListener = null;
//            }
//        };
//
//        final Transition exitTransition = transition.clone();
//        exitTransition.addListener(mPendingExitListener);
//
//        final int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            final View child = getChildAt(i);
//            exitTransition.addTarget(child);
//        }
//
//        TransitionManager.beginDelayedTransition(this, exitTransition);
//
//        for (int i = 0; i < count; i++) {
//            final View child = getChildAt(i);
//            child.setVisibility(View.INVISIBLE);
//        }
//    }

        /**
         * Cancels all pending or current transitions.
         */
//    public void cancelTransitions() {
//        TransitionManager.endTransitions(this);
//
//        if (mPendingExitListener != null) {
//            mPendingExitListener.onTransitionEnd(null);
//        }
//    }

        private final OnAttachStateChangeListener mOnAnchorRootDetachedListener =
                new OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        v.removeOnAttachStateChangeListener(this);

//                    TransitionManager.endTransitions(PopupDecorView.this);
                    }
                };
    }
}
