package com.chanakira.orbit.config;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.util.Log;

import com.chanakira.orbit.R;

public class ConfigActivity extends Activity {

    public static final String TAG = ConfigActivity.class.getSimpleName();

    static final int UPDATE_COLORS_CONFIG_REQUEST_CODE = 1001;

    private WearableRecyclerView mRecyclerView;
    private ConfigMenuAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        mAdapter = new ConfigMenuAdapter(getApplicationContext(), ConfigMenu.getDataToPopulateAdapter(this));

        mRecyclerView = findViewById(R.id.wearable_recycler_view);
        mRecyclerView.setEdgeItemsCenteringEnabled(true);
        mRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult(): " + requestCode + ", " + resultCode);
    }
}
