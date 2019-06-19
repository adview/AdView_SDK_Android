package com.kuaiyou.loader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Map;
import java.util.Set;


/**
 * SharedPreferences 工具类，
 *
 * @author Magic_Chen
 */
public class PreferencesUtils {


    /**
     * 根据文件名获�?SharedPreferences
     *
     * @param context
     * @param name
     * @return SharedPreferences
     */
    public synchronized static SharedPreferences getSharedPreferences(Context context,
                                                                      String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }


    public synchronized static Object getSharedPreferenceValue(Context context, String name, String value) {
        SharedPreferences preferences = getSharedPreferences(context, name);
        Map<String, ?> valueMap = preferences.getAll();
        return valueMap.get(value);
    }


    /**
     * 根据已知SharedPreferences 保存数据
     *
     * @param preferences
     * @param key
     * @param value
     * @return true表示保存成功，否则false
     */
    @SuppressLint("NewApi")
    public synchronized static boolean commitSharedPreferencesValue(
            SharedPreferences preferences, String key, Object value) {
        try {
            Editor editor = preferences.edit();
            // editor.put
            if (value instanceof Boolean)
                editor.putBoolean(key, (Boolean) value);
            else if (value instanceof Float)
                editor.putFloat(key, (Float) value);
            else if (value instanceof Integer)
                editor.putInt(key, (Integer) value);
            else if (value instanceof Long)
                editor.putLong(key, (Long) value);
            else if (value instanceof String)
                editor.putString(key, (String) value);
            else if (value instanceof Set) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
                    editor.putStringSet(key, (Set<String>) value);
            } else
                return false;
            return editor.commit();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据已知文件名保存数据
     *
     * @param context
     * @param key
     * @param value
     * @return true表示保存成功，否则false
     */
    public synchronized static boolean commitSharedPreferencesValue(Context context,
                                                                    String name, String key, Object value) {
        SharedPreferences preferences = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
        return commitSharedPreferencesValue(preferences, key, value);
    }

    /**
     * 根据已知文件名保存数据
     *
     * @param context
     * @param key
     * @return true表示保存成功，否则false
     */
    public synchronized static boolean addSharedPreferencesValue(Context context,
                                                                 String name, String key, long value) {
        SharedPreferences preferences = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
        long tempSize = preferences.getLong(key, 0l);
        tempSize = tempSize + value;
        return commitSharedPreferencesValue(preferences, key, tempSize);
    }

    /**
     * 根据已知文件名保存数据
     *
     * @param context
     * @param key
     * @return true表示保存成功，否则false
     */
    public synchronized static boolean minusSharedPreferencesValue(Context context,
                                                                   String name, String key, long value) {
        SharedPreferences preferences = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
        long tempSize = preferences.getLong(key, 0l);
//        if(TextUtils.isEmpty(tempSize)||tempSize.equals("null"))
//            return false;
//        long size= Long.valueOf(tempSize);
        if (tempSize <= 0)
            return true;
        return commitSharedPreferencesValue(preferences, key, tempSize - value);
    }

    /**
     * 根据已知文件名保存数据
     *
     * @param context
     * @param key
     * @return true表示保存成功，否则false
     */
    public synchronized static boolean deleteSharedPreferencesValue(Context context,
                                                                    String name, String key) {
        SharedPreferences preferences = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove(key);
        return editor.commit();
    }

    public static boolean clearValue(Context context,
                                     String name) {
        SharedPreferences preferences = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.clear();
        return editor.commit();

    }

}
