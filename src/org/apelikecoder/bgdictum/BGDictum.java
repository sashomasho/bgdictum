package org.apelikecoder.bgdictum;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class BGDictum extends Activity implements DB,
        OnItemClickListener, View.OnClickListener {

    private SQLiteDatabase db;
    private AutoCompleteTextView searchField;
    private Button clear;
    private WordView translation;
    private InputMethodManager mgr;

    private int TRANSLATION_COLUMN_INDEX = -1;
    private static final int ID_MENU_PREFERENCES = 101;
    private static final int ID_MENU_QUIT = 102;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        searchField = (AutoCompleteTextView) findViewById(R.id.search_edit_query);
        translation = (WordView) findViewById(R.id.description_text);
        clear = (Button) findViewById(R.id.clear_text);
        clear.setOnClickListener(this);
        mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (!checkDB()) {
            startActivity(new Intent(this, Setup.class));
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            System.out.println(uri.toString());
            String word = uri.getHost();
            searchField.setText(word);
            searchField.setSelection(word.length());
        }
    }
    
    private boolean checkDB() {
        App app = (App) getApplication();
        db = app.getDb();
        if (db == null) {
            if (!app.setupDB())
                return false;
            db = app.getDb();
        }
        try {
            Cursor c = db.query(TABLE_WORDS, null, null, null, null, null, null);
            CursorAdapter ca = new WordAdapter(this, c, db);
            searchField.setAdapter(ca);
            searchField.setOnItemClickListener(this);
            c = db.query(TABLE_TRANSLATIONS, null, null, null, null, null, null);
            TRANSLATION_COLUMN_INDEX = c.getColumnIndexOrThrow(COLUMN_TRANSLATION);
            c.close();
        } catch (SQLiteException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        Cursor c = db.query(TABLE_TRANSLATIONS, null, COLUMN_WORD_ID + "='" + id + "'", null, null, null, null);
        if (c.moveToFirst()) {
            String s = c.getString(TRANSLATION_COLUMN_INDEX);
            translation.setWordInfo(searchField.getText().toString(), s);
        }
        c.close();
        mgr.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
    }


    public void onClick(View v) {
        searchField.setText("");
        showKeyboard();
    }

    private void showKeyboard() {
        //searchField.requestFocusFromTouch();
        mgr.showSoftInput(searchField, InputMethodManager.SHOW_FORCED);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem m = menu.add(Menu.NONE, ID_MENU_PREFERENCES, Menu.NONE, R.string.preferences);
        m.setIcon(android.R.drawable.ic_menu_preferences);
        m = menu.add(Menu.NONE, ID_MENU_QUIT, Menu.NONE, R.string.quit);
        m.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_MENU_PREFERENCES:
                startActivity(new Intent(this, Preferences.class));
                return true;
            case ID_MENU_QUIT:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
