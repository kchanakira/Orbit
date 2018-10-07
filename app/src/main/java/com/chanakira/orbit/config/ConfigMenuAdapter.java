package com.chanakira.orbit.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chanakira.orbit.R;
import com.chanakira.orbit.config.ConfigMenu.*;

import java.util.ArrayList;

public class ConfigMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ConfigMenuAdapter.class.getSimpleName();

    static final String EXTRA_SHARED_PREF = "com.chanakira.orbit.config.extra.EXTRA_SHARED_PREF";

    static final int TYPE_BOOLEAN = 0;
    static final int TYPE_COLOR = 1;

    private Context mContext;
    private ArrayList<ConfigMenu.ConfigItemType> mSettingsDataSet;

    private SharedPreferences mSharedPreferences;

    public ConfigMenuAdapter(Context context, ArrayList<ConfigMenu.ConfigItemType> settingsDataSet) {
        mContext = context;
        mSettingsDataSet = settingsDataSet;

        mSharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch(viewType) {
            case TYPE_COLOR:
            default:
                viewHolder = new ColorPickerViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.config_menu_color_item, parent, false));
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ConfigMenu.ConfigItemType configItemType = mSettingsDataSet.get(position);

        switch(holder.getItemViewType()) {
            case TYPE_COLOR:
                ColorPickerViewHolder colorPickerViewHolder = (ColorPickerViewHolder) holder;
                ColorConfigItem colorConfigItem = (ColorConfigItem) configItemType;

                int iconResourceId = colorConfigItem.getIconResourceId();
                String name = colorConfigItem.getName();
                String sharedPrefString = colorConfigItem.getSharedPrefString();

                colorPickerViewHolder.setIcon(iconResourceId);
                colorPickerViewHolder.setName(name);
                colorPickerViewHolder.setSharedPrefString(sharedPrefString);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mSettingsDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        ConfigItemType configItemType = mSettingsDataSet.get(position);

        return configItemType.getConfigType();
    }

    public class ColorPickerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Button mAppearanceButton;
        private String mSharedPrefString;

        public ColorPickerViewHolder(View view) {
            super(view);

            mAppearanceButton = view.findViewById(R.id.color_picker_button);
            view.setOnClickListener(this);
        }

        public void setName(String name) {
            mAppearanceButton.setText(name);
        }

        public void setIcon(int resourceId) {
            Context context = mAppearanceButton.getContext();
            mAppearanceButton.setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(resourceId),
                    null,
                    null,
                    null);
        }

        public void setSharedPrefString(String sharedPrefString) {
            mSharedPrefString = sharedPrefString;
        }

        @Override
        public void onClick(View view) {
            Intent launchIntent = new Intent(view.getContext(), ColorPickerActivity.class);
            launchIntent.putExtra(EXTRA_SHARED_PREF, mSharedPrefString);

            Activity activity = (Activity) view.getContext();
            activity.startActivityForResult(launchIntent, ConfigActivity.UPDATE_COLORS_CONFIG_REQUEST_CODE);
        }
    }
}
