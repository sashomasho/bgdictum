package org.apelikecoder.bgdictum;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

class WordAdapter extends CursorAdapter implements Filterable, DB {

    private String[] QUERY_PROJECTION = new String[] { COLUMN_ID, COLUMN_WORD };
    private final int WORD_COLUMN_INDEX;
    private SQLiteDatabase db;

    public WordAdapter(Context context, Cursor c, SQLiteDatabase db) {
        super(context, c);
        WORD_COLUMN_INDEX = c.getColumnIndexOrThrow(COLUMN_WORD);
        this.db = db;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(
                R.layout.simple_dropdown_item_1line, parent, false);
        view.setText(cursor.getString(WORD_COLUMN_INDEX));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view).setText(cursor.getString(WORD_COLUMN_INDEX));
    }

    @Override
    public String convertToString(Cursor cursor) {
        return cursor.getString(WORD_COLUMN_INDEX);
    }
    
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence s) {
        Cursor c = null;
        if (s != null)
            c = db.query(DB.TABLE_WORDS, QUERY_PROJECTION, getLike(s.toString().toLowerCase()), null, null, null, null);            
        return c;
    }
    
    private String getLike(String s) {
        return DB.COLUMN_WORD + ">= '" + s + "' AND " + DB.COLUMN_WORD + "< '" + s + '\u044F' +"'";
    }

}