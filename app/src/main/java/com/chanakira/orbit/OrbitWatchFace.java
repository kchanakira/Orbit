package com.chanakira.orbit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class OrbitWatchFace extends CanvasWatchFaceService {

    /*
     * Updates rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<OrbitWatchFace.Engine> mWeakReference;

        public EngineHandler(OrbitWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            OrbitWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private static final float HOUR_STROKE_WIDTH = 5f;
        private static final float MINUTE_STROKE_WIDTH = 3f;
        private static final float SECOND_TICK_STROKE_WIDTH = 2f;
        private static final int SHADOW_RADIUS = 3;

        private static final float MINUTES_DISTANCE = 0.70f;
        private static final float SECONDS_DISTANCE = 0.85f;
        private static final float DATE_DISTANCE = 0.45f;


        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;

        private SharedPreferences mSharedPreferences;

        private float mCenterX;
        private float mCenterY;

        private float mSecondHandLength;
        private float sMinuteHandLength;
        private float sDateHandLength;

        private float mHourSatelliteRadius;
        private float mMinutesSatelliteRadius;
        private float mSecondsSatelliteRadius;

        /* Colors for all hands (hour, minute, seconds, ticks), and background. */
        private int mBackgroundColor;
        private int mWatchHandColor;
        private int mWatchHandShadowColor;
        private int mTextColor;

        private Paint mHourPaint;
        private Paint mMinutePaint;
        private Paint mSecondPaint;
        private Paint mDatePaint;
        private Paint mTickAndCirclePaint;
        private Paint mBackgroundPaint;

        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        private boolean mUse24HourClock;
        private boolean mDrawHourOutline;
        private boolean mDrawMinutesOutline;
        private boolean mDrawSecondsOutline;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            Context context = getApplicationContext();
            mSharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
            mCalendar = Calendar.getInstance();

            setWatchFaceStyle(new WatchFaceStyle.Builder(OrbitWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());

            loadSavedPreferences();
            initializeBackground();
            initializeWatchFace();
        }

        private void loadSavedPreferences() {
            String use24HourClockPrefKey = getApplicationContext().getString(R.string.pref_use_24_hour_clock);
            String backgroundColorPrefKey = getApplicationContext().getString(R.string.pref_background_color);
            String satelliteColorPrefKey = getApplicationContext().getString(R.string.pref_satellite_color);
            String textColorPrefKey = getApplicationContext().getString(R.string.pref_text_color);
            String showHourOutlinePrefKey = getApplicationContext().getString(R.string.pref_show_hour_outline);
            String showMinuteOutlinePrefKey = getApplicationContext().getString(R.string.pref_show_minute_outline);
            String showSecondOutlinePrefKey = getApplicationContext().getString(R.string.pref_show_second_outline);

            mBackgroundColor = mSharedPreferences.getInt(backgroundColorPrefKey, Color.BLACK);
            mWatchHandColor = mSharedPreferences.getInt(satelliteColorPrefKey, Color.WHITE);
            mWatchHandShadowColor = Color.BLACK;
            mTextColor = mSharedPreferences.getInt(textColorPrefKey, Color.WHITE);

            mUse24HourClock = mSharedPreferences.getBoolean(use24HourClockPrefKey, false);
            mDrawHourOutline = mSharedPreferences.getBoolean(showHourOutlinePrefKey, true);
            mDrawMinutesOutline = mSharedPreferences.getBoolean(showMinuteOutlinePrefKey, false);
            mDrawSecondsOutline = mSharedPreferences.getBoolean(showSecondOutlinePrefKey, false);
        }

        private void initializeBackground() {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mBackgroundColor);
        }

        private void initializeWatchFace() {
            mHourPaint = new Paint();
            mHourPaint.setColor(mTextColor);
            mHourPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setTextAlign(Paint.Align.CENTER);
            mHourPaint.setTextSize(45);

            mMinutePaint = new Paint();
            mMinutePaint.setColor(mTextColor);
            mMinutePaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setTextAlign(Paint.Align.CENTER);
            mMinutePaint.setTextSize(25);

            mSecondPaint = new Paint();
            mSecondPaint.setColor(mTextColor);
            mSecondPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setTextAlign(Paint.Align.CENTER);
            mSecondPaint.setTextSize(18);

            mDatePaint = new Paint();
            mDatePaint.setColor(mTextColor);
            mDatePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mDatePaint.setAntiAlias(true);
            mDatePaint.setTextAlign(Paint.Align.CENTER);
            mDatePaint.setTextSize(18);

            mTickAndCirclePaint = new Paint();
            mTickAndCirclePaint.setColor(mWatchHandColor);
            mTickAndCirclePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickAndCirclePaint.setAntiAlias(true);
            mTickAndCirclePaint.setStyle(Paint.Style.STROKE);
            mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;

            updateWatchHandStyle();

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void updateWatchHandStyle() {
            if (mAmbient) {
                mHourPaint.setColor(Color.WHITE);
                mMinutePaint.setColor(Color.WHITE);
                mSecondPaint.setColor(Color.WHITE);
                mDatePaint.setColor(Color.WHITE);
                mTickAndCirclePaint.setColor(Color.WHITE);

                mHourPaint.setAntiAlias(false);
                mMinutePaint.setAntiAlias(false);
                mSecondPaint.setAntiAlias(false);
                mDatePaint.setAntiAlias(false);
                mTickAndCirclePaint.setAntiAlias(false);

                mTickAndCirclePaint.clearShadowLayer();

            } else {
                mHourPaint.setColor(mTextColor);
                mMinutePaint.setColor(mTextColor);
                mSecondPaint.setColor(mTextColor);
                mDatePaint.setColor(mTextColor);
                mTickAndCirclePaint.setColor(mWatchHandColor);

                mHourPaint.setAntiAlias(true);
                mMinutePaint.setAntiAlias(true);
                mSecondPaint.setAntiAlias(true);
                mDatePaint.setAntiAlias(true);
                mTickAndCirclePaint.setAntiAlias(true);

                mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            mCenterX = width / 2f;
            mCenterY = height / 2f;

            /*
             * Calculate lengths of different hands based on watch screen size.
             */
            mSecondHandLength = mCenterX * SECONDS_DISTANCE;
            sMinuteHandLength = mCenterX * MINUTES_DISTANCE;
            sDateHandLength = mCenterX * DATE_DISTANCE;

            /*
             * Calculate the radii of different satellites based on watch screen size.
             */
            mHourSatelliteRadius = (float) (mCenterX * 0.333);
            mMinutesSatelliteRadius = (float) (mCenterX * 0.12665);
            mSecondsSatelliteRadius = (float) (mCenterX * 0.0999);
        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            drawBackground(canvas);
            drawWatchFace(canvas);
        }

        private void drawBackground(Canvas canvas) {
            if (mAmbient) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawColor(mBackgroundColor);
            }
        }

        private void drawWatchFace(Canvas canvas) {
            final int seconds = mCalendar.get(Calendar.SECOND);
            final int minutes = mCalendar.get(Calendar.MINUTE);
            final int hour = mUse24HourClock
                    ? mCalendar.get(Calendar.HOUR_OF_DAY)
                    : mCalendar.get(Calendar.HOUR) != 0
                        ? mCalendar.get(Calendar.HOUR)
                        : 12;

            final float secondsFloat = (seconds + mCalendar.get(Calendar.MILLISECOND) / 1000f);
            final float secondsRotation = secondsFloat * 6f;
            final float minutesRotation = minutes * 6f;

            // Draw the hours satellite
            drawSatellite(
                    canvas,
                    mHourPaint,
                    hour,
                    mHourSatelliteRadius,
                    0,
                    0,
                    !mAmbient && mDrawHourOutline);

            // Draw the minutes satellite
            drawSatellite(
                    canvas,
                    mMinutePaint,
                    minutes,
                    mMinutesSatelliteRadius,
                    sMinuteHandLength,
                    minutesRotation,
                    !mAmbient && mDrawMinutesOutline);

            // Draw the seconds satellite
            if (!mAmbient) {
                drawSatellite(
                        canvas,
                        mSecondPaint,
                        seconds,
                        mSecondsSatelliteRadius,
                        mSecondHandLength,
                        secondsRotation,
                        !mAmbient && mDrawSecondsOutline);
            }

            // Draw the date text
            //if (!mAmbient) {
                final float dateTextY = mCenterY + sDateHandLength - ((mDatePaint.descent() + mDatePaint.ascent()) / 2);

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                canvas.drawText(String.format(Locale.getDefault(), "%s", dateFormat.format(mCalendar.getTime())), mCenterX, dateTextY, mDatePaint);
            //}
        }

        private void drawSatellite(Canvas canvas, Paint paint, int number, float radius, float handLength, float rotation, boolean drawOutline) {
            final float satelliteX = (float) (mCenterX + handLength * Math.sin(Math.toRadians(rotation)));
            final float satelliteY = (float) (mCenterY - handLength * Math.cos(Math.toRadians(rotation)));
            final float textY = satelliteY - ((paint.descent() + paint.ascent()) / 2);

            if (drawOutline) {
                canvas.drawCircle(satelliteX, satelliteY, radius, mTickAndCirclePaint);
            }

            canvas.drawText(String.format(Locale.getDefault(), "%d", number), satelliteX, textY, paint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                loadSavedPreferences();
                updateWatchHandStyle();
                registerReceiver();

                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            OrbitWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            OrbitWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
