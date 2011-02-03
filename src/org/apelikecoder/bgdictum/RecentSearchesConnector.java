package org.apelikecoder.bgdictum;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class RecentSearchesConnector extends SQLiteOpenHelper {
    private final static String TAG = "RecentSearches";

    static final String ITEM = "item";
    static final String DATE = "date";
    static final String ID = "_id";
    public final static String TABLE_SEARCHES = "recent_searches";
    private static final String CREATE_TABLE = "create table ";
    private static final String CREATE_COLUMNS = String.format(
            " (%s integer primary key, %s text not null, %s date not null)", ID, ITEM, DATE);

    private SimpleDateFormat dateFormat;

    public RecentSearchesConnector(Context context) {
        super(context, TAG, null, 1);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE + RecentSearchesConnector.TABLE_SEARCHES + CREATE_COLUMNS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addSearch(String text) {
        addItem(text, TABLE_SEARCHES);
    }

    private void addItem(String text, String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = String.format("%s = ?", ITEM);
        Cursor c = db.query(table, new String[] { ITEM }, whereClause, new String[] { text }, null,
                null, null);

        if (c.moveToFirst()) {
            ContentValues values = new ContentValues(1);
            values.put(DATE, dateFormat.format(new Date()));
            String s = c.getString(0);
            c.close();
            db.update(table, values, whereClause, new String[] { s });
            return;
        }
        c.close();

        ContentValues values = new ContentValues(2);
        values.put(ITEM, text);
        values.put(DATE, dateFormat.format(new Date()));

        db.beginTransaction();
        try {
            db.insertOrThrow(table, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public void deleteSearch(long id) {
        deleteItem(id, TABLE_SEARCHES);
    }

    private void deleteItem(long id, String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(table, String.format("%s = ?", ID), new String[] { String.valueOf(id) });
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getSearchCursor(String pfx) {
        return getQueriesCursor(TABLE_SEARCHES, pfx);
    }

    private Cursor getQueriesCursor(String table, String pfx) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = TextUtils.isEmpty(pfx) ? null : String.format("%s like ?", ITEM);
        String[] whereParams = TextUtils.isEmpty(pfx) ? null : new String[] { pfx + '%' };
        return db.query(table, new String[] { /*"item, _id" */ITEM, ID}, where, whereParams, null, null,
                String.format("%s DESC", DATE));
    }

    public int clearSearchHistory() {
        return getWritableDatabase().delete(TABLE_SEARCHES, null, null);
    }

}
