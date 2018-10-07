package com.chanakira.orbit.config;

import android.content.Context;
import android.graphics.Color;

import com.chanakira.orbit.R;

import java.util.ArrayList;

public class ConfigMenu {

    public interface ConfigItemType {
        int getConfigType();
    }

    /**
     * Returns Material Design color options.
     */
    public static ArrayList<Integer> getColorOptionsDataSet() {
        ArrayList<Integer> colorOptionsDataSet = new ArrayList<>();
        colorOptionsDataSet.add(Color.parseColor("#000000")); // Black
        colorOptionsDataSet.add(Color.parseColor("#9E9E9E")); // Grey
        colorOptionsDataSet.add(Color.parseColor("#607D8B")); // Blue Grey
        colorOptionsDataSet.add(Color.parseColor("#795548")); // Brown

        colorOptionsDataSet.add(Color.parseColor("#FFEB3B")); // Yellow
        colorOptionsDataSet.add(Color.parseColor("#FFC107")); // Amber
        colorOptionsDataSet.add(Color.parseColor("#FF9800")); // Orange
        colorOptionsDataSet.add(Color.parseColor("#FF5722")); // Deep Orange

        colorOptionsDataSet.add(Color.parseColor("#F44336")); // Red
        colorOptionsDataSet.add(Color.parseColor("#E91E63")); // Pink

        colorOptionsDataSet.add(Color.parseColor("#9C27B0")); // Purple
        colorOptionsDataSet.add(Color.parseColor("#673AB7")); // Deep Purple
        colorOptionsDataSet.add(Color.parseColor("#3F51B5")); // Indigo
        colorOptionsDataSet.add(Color.parseColor("#2196F3")); // Blue
        colorOptionsDataSet.add(Color.parseColor("#03A9F4")); // Light Blue

        colorOptionsDataSet.add(Color.parseColor("#00BCD4")); // Cyan
        colorOptionsDataSet.add(Color.parseColor("#009688")); // Teal
        colorOptionsDataSet.add(Color.parseColor("#4CAF50")); // Green
        colorOptionsDataSet.add(Color.parseColor("#8BC34A")); // Lime Green
        colorOptionsDataSet.add(Color.parseColor("#CDDC39")); // Lime

        colorOptionsDataSet.add(Color.parseColor("#FFFFFF")); // White

        return colorOptionsDataSet;
    }

    public static ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {
        ArrayList<ConfigItemType> settingsMenu = new ArrayList<>();

        ConfigItemType backgroundColorConfigItem = new ColorConfigItem(
                context.getString(R.string.config_background_color_label),
                R.drawable.ic_color_lens_black_24dp,
                context.getString(R.string.pref_background_color));

        settingsMenu.add(backgroundColorConfigItem);

        ConfigItemType satelliteColorConfigItem = new ColorConfigItem(
                context.getString(R.string.config_satellite_color_label),
                R.drawable.ic_color_lens_black_24dp,
                context.getString(R.string.pref_satellite_color));

        settingsMenu.add(satelliteColorConfigItem);

        ConfigItemType textColorConfigItem = new ColorConfigItem(
                context.getString(R.string.config_text_color_label),
                R.drawable.ic_color_lens_black_24dp,
                context.getString(R.string.pref_text_color));

        settingsMenu.add(textColorConfigItem);

        return settingsMenu;
    }

    public static class ColorConfigItem implements ConfigItemType {

        private String name;
        private int iconResourceId;
        private String sharedPrefString;

        ColorConfigItem(String name, int iconResourceId, String sharedPrefString) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            this.sharedPrefString = sharedPrefString;
        }

        String getName() {
            return name;
        }

        int getIconResourceId() {
            return iconResourceId;
        }

        String getSharedPrefString() {
            return sharedPrefString;
        }

        @Override
        public int getConfigType() {
            return ConfigMenuAdapter.TYPE_COLOR;
        }
    }
}
