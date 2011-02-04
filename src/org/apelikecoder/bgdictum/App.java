package org.apelikecoder.bgdictum;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class App extends Application implements DB {

    public static interface PreferenceKeys {
        public static final String preference_use_light_theme = "preference_use_light_theme";
        public static final String preference_enable_word_click = "preference_enable_word_click";
        public static final String preference_enable_word_click_popup = "preference_enable_word_click_popup";
        public static final String preference_history = "history";
        public static final String preference_clear_history_on_exit = "preference_clear_history_on_exit";
    }

    private SQLiteDatabase db;
    private String dataPath;
    private RecentSearchesConnector recentConnector;

    @Override
    public void onCreate() {
        super.onCreate();
        recentConnector = new RecentSearchesConnector(this);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            initDataPath();
    }
    
    public SQLiteDatabase getDb() {
        return db;
    }
    
    public void initDataPath() {
        File f = new File(new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data"), getPackageName());
        if (!f.exists())
            f.mkdirs();
        if (!f.canWrite()) {
            Toast.makeText(this, R.string.no_sdcard_warning, Toast.LENGTH_SHORT);
            f = getDir("dict", MODE_PRIVATE);
        }
        dataPath = f.getAbsolutePath();
        System.out.println("DATAPATH: " + dataPath);
        return;
    }
    
    public boolean setupDB() {
        File f = new File(dataPath, DATABASE);
        if (!f.exists())
            return false;

        try {
            db = SQLiteDatabase.openDatabase(f.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (db == null) {
            String [] files = new File(dataPath).list();
            for (String s : files) {
                f = new File(dataPath + File.separatorChar + s);
                if (f.delete())
                    System.out.println(f.getAbsolutePath() + " is GONE");
            }
            return false;
        }
        return true;
    }
    
    public String getDataPath() {
        return dataPath;
    }

    public RecentSearchesConnector getRecentConnector() {
        return recentConnector;
    }

    public static void setupTheme(Context ctx) {
        boolean useLightTheme = PreferenceManager.getDefaultSharedPreferences(ctx)
            .getBoolean(PreferenceKeys.preference_use_light_theme, false);
        ctx.setTheme(useLightTheme ? R.style.BGDictumLight: R.style.BGDictumBlack);
    }
}
