package com.tecesoj.alarmafutura;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AlarmRepository {
    private static final String STORE = "alarma_futura";
    private static final String KEY = "alarms";
    private AlarmRepository() {}

    public static final class Entry {
        public final int id;
        public final String name;
        public final long time;
        public Entry(int id, String name, long time) {
            this.id = id;
            this.name = name;
            this.time = time;
        }
    }

    public static synchronized int nextId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(STORE, Context.MODE_PRIVATE);
        int id = prefs.getInt("next_id", 0) + 1;
        prefs.edit().putInt("next_id", id).apply();
        return id;
    }

    public static synchronized List<Entry> list(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(STORE, Context.MODE_PRIVATE);
        ArrayList<Entry> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                result.add(new Entry(json.getInt("id"), json.getString("name"), json.getLong("time")));
            }
        } catch (Exception ignored) {
            // Un registro corrupto no debe impedir abrir la aplicación.
        }
        result.sort(Comparator.comparingLong(item -> item.time));
        return result;
    }

    public static synchronized void add(Context context, Entry entry) {
        List<Entry> items = list(context);
        items.add(entry);
        save(context, items);
    }

    public static synchronized void remove(Context context, int id) {
        List<Entry> items = list(context);
        items.removeIf(item -> item.id == id);
        save(context, items);
    }

    private static void save(Context context, List<Entry> items) {
        JSONArray array = new JSONArray();
        try {
            for (Entry item : items) {
                JSONObject json = new JSONObject();
                json.put("id", item.id);
                json.put("name", item.name);
                json.put("time", item.time);
                array.put(json);
            }
        } catch (Exception ignored) {}
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
                .edit().putString(KEY, array.toString()).apply();
    }
}
