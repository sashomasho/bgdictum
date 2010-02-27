package org.apelikecoder.bgdictum;

import java.io.File;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

public class App extends Application implements DB {

    public static interface PreferenceKeys {
        public static final String preference_use_light_theme = "preference_use_light_theme";
        public static final String preference_enable_word_click = "preference_enable_word_click";
        public static final String preference_enable_word_click_popup = "preference_enable_word_click_popup";
    }

    private SQLiteDatabase db;
    private String dataPath;

    @Override
    public void onCreate() {
        super.onCreate();
        initDataPath();
    }
    
    public SQLiteDatabase getDb() {
        return db;
    }
    
    public void initDataPath() {
        char sep = File.separatorChar;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + sep + "data" + sep + getPackageName() + sep;
        File f = new File(path);
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

        initDB(f.getAbsolutePath());
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
    
    private void initDB(String path) {
        System.out.println("openind db file " + path);
        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);

        StringBuilder query = new StringBuilder(CREATE_TABLE_QUERY);

        query.append(TABLE_GLOBAL_SETTINGS);
        query.append(START_TABLE);
        query.append(COLUMN_ID);
        query.append(TYPE_ID);
        query.append(SEPARATE_COLUMN);
        query.append(COLUMN_GLOBAL_SETTINGS_KEY);
        query.append(TYPE_TEXT);
        query.append(SEPARATE_COLUMN);
        query.append(COLUMN_GLOBAL_SETTINGS_VALUE);
        query.append(TYPE_TEXT);
        query.append(END_TABLE);

        db.execSQL(query.toString());

        return;
    }
    
    public String getDataPath() {
        return dataPath;
    }
    
}
