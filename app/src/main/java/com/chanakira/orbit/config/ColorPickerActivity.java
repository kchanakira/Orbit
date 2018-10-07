package com.chanakira.orbit.config;

import android.app.Activity;
import android.os.Bundle;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;

import com.chanakira.orbit.R;

import static com.chanakira.orbit.config.ConfigMenuAdapter.EXTRA_SHARED_PREF;

public class ColorPickerActivity extends Activity {

    private static final String TAG = ColorPickerActivity.class.getSimpleName();

    private WearableRecyclerView mRecyclerView;
    private ColorPickerRecyclerAdapter mViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        String sharedPrefString = getIntent().getStringExtra(EXTRA_SHARED_PREF);

        mViewAdapter = new ColorPickerRecyclerAdapter(sharedPrefString, ConfigMenu.getColorOptionsDataSet());

        mRecyclerView = findViewById(R.id.wearable_recycler_view);
        mRecyclerView.setEdgeItemsCenteringEnabled(true);
        mRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mViewAdapter);
    }

}
