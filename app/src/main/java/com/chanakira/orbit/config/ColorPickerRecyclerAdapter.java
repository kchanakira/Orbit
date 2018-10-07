package com.chanakira.orbit.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chanakira.orbit.R;

import java.util.ArrayList;

public class ColorPickerRecyclerAdapter extends RecyclerView.Adapter<ColorPickerRecyclerAdapter.ColorViewHolder> {

    private final String mSharedPrefKey;
    private final ArrayList<Integer> mColorOptions;

    public ColorPickerRecyclerAdapter(String sharedPrefKey, ArrayList<Integer> colorDataSet) {
        mSharedPrefKey = sharedPrefKey;
        mColorOptions = colorDataSet;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ColorViewHolder holder = new ColorViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.config_color_picker_item, parent, false));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        holder.setColor(mColorOptions.get(position));
    }

    @Override
    public int getItemCount() {
        return mColorOptions.size();
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @SuppressWarnings("deprecation")
        private CircledImageView mColorView;

        public ColorViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            mColorView = view.findViewById(R.id.color);
        }

        public void setColor(int color) {
            mColorView.setCircleColor(color);
        }

        @Override
        public void onClick(View view) {
            Activity activity = (Activity) view.getContext();
            Integer color = mColorOptions.get(getAdapterPosition());

            if (mSharedPrefKey != null && !mSharedPrefKey.isEmpty()) {
                SharedPreferences preferences = activity.getSharedPreferences(activity.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt(mSharedPrefKey, color);
                editor.apply();

                activity.setResult(Activity.RESULT_OK);
            }

            activity.finish();
        }
    }
}
