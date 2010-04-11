package org.apelikecoder.bgdictum;

import android.provider.BaseColumns;

public interface DB {
    
    public static final String DATABASE = "bgdictum.db";
    public static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS ";
    public static final String TABLE_WORDS = "words";
    public static final String TABLE_TRANSLATIONS = "translations";
    public static final String START_TABLE = " (", SEPARATE_COLUMN = ", ", END_TABLE = ");";
    public static final String TYPE_TEXT = " TEXT";
    public static final String TYPE_ID = " INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_WORD = "word";
    public static final String COLUMN_WORD_ID = "word_id";
    public static final String COLUMN_TRANSCRIPTION = "transcription";
    public static final String COLUMN_TRANSLATION = "translation";
}
