package com.him.note_him_android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notes_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL("CREATE TABLE " + Note.TABLE_NAME + "(" + Note.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Note.COLUMN_NOTE + " TEXT," + Note.COLUMN_SUBJECT + " TEXT," + Note.COLUMN_ADDRESS + " TEXT," + Note.COLUMN_LAT + " TEXT," + Note.COLUMN_LNG + " TEXT," + Note.COLUMN_IMAGE + " TEXT," + Note.COLUMN_AUDIO + " TEXT," + Note.COLUMN_DESCRIPTION + " TEXT," + Note.COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
        db.execSQL("CREATE TABLE " + Subject.TABLE_NAME + "(" + Subject.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Subject.COLUMN_SUBJECT + " TEXT" + ")");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Note.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Subject.TABLE_NAME);
        onCreate(db);
    }

    public long insertNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Note.COLUMN_NOTE, note.getNote());
        values.put(Note.COLUMN_SUBJECT, note.getSubject());
        values.put(Note.COLUMN_ADDRESS, note.getAddress());
        values.put(Note.COLUMN_LAT, note.getLat());
        values.put(Note.COLUMN_LNG, note.getLng());
        values.put(Note.COLUMN_IMAGE, note.getImage());
        values.put(Note.COLUMN_AUDIO, note.getAudio());
        values.put(Note.COLUMN_DESCRIPTION, note.getDescription());
        long id = db.insert(Note.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public long insertSubject(String subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Subject.COLUMN_SUBJECT, subject);
        long id = db.insert(Subject.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public List<Note> searchNote(String title, String subject) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + Note.TABLE_NAME + " where " + Note.COLUMN_SUBJECT + " ='" + subject + "' and (" + Note.COLUMN_NOTE + " LIKE '%" + title + "%'" + " or " + Note.COLUMN_DESCRIPTION + " LIKE '%" + title + "%')", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setId(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)));
                    note.setNote(cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE)));
                    note.setSubject(cursor.getString(cursor.getColumnIndex(Note.COLUMN_SUBJECT)));
                    note.setImage(cursor.getString(cursor.getColumnIndex(Note.COLUMN_IMAGE)));
                    note.setLat(cursor.getString(cursor.getColumnIndex(Note.COLUMN_LAT)));
                    note.setLng(cursor.getString(cursor.getColumnIndex(Note.COLUMN_LNG)));
                    note.setAddress(cursor.getString(cursor.getColumnIndex(Note.COLUMN_ADDRESS)));
                    note.setAudio(cursor.getString(cursor.getColumnIndex(Note.COLUMN_AUDIO)));
                    note.setTimestamp(cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));

                    notes.add(note);
                } while (cursor.moveToNext());
            }
        }
        return notes;
    }

    public Note getNote(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Note.TABLE_NAME,
                new String[]{Note.COLUMN_ID, Note.COLUMN_NOTE, Note.COLUMN_TIMESTAMP},
                Note.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        Note note = new Note(
                cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE)),
                cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));

        // close the db connection
        cursor.close();

        return note;
    }

    public List<Note> getAllNotes(String subject) {
        List<Note> notes = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + Note.TABLE_NAME + " where " + Note.COLUMN_SUBJECT + " = '" + subject + "' ORDER BY " +
                Note.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)));
                note.setNote(cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE)));
                note.setSubject(cursor.getString(cursor.getColumnIndex(Note.COLUMN_SUBJECT)));
                note.setDescription(cursor.getString(cursor.getColumnIndex(Note.COLUMN_DESCRIPTION)));
                note.setImage(cursor.getString(cursor.getColumnIndex(Note.COLUMN_IMAGE)));
                note.setLat(cursor.getString(cursor.getColumnIndex(Note.COLUMN_LAT)));
                note.setLng(cursor.getString(cursor.getColumnIndex(Note.COLUMN_LNG)));
                note.setAddress(cursor.getString(cursor.getColumnIndex(Note.COLUMN_ADDRESS)));
                note.setAudio(cursor.getString(cursor.getColumnIndex(Note.COLUMN_AUDIO)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));

                notes.add(note);
            } while (cursor.moveToNext());
        }
        db.close();
        return notes;
    }

    public List<Subject> getAllSubject() {
        List<Subject> notes = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + Subject.TABLE_NAME + " ORDER BY " +
                Subject.COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Subject note = new Subject();
                note.setId(cursor.getInt(cursor.getColumnIndex(Subject.COLUMN_ID)));
                note.setSubject(cursor.getString(cursor.getColumnIndex(Subject.COLUMN_SUBJECT)));

                notes.add(note);
            } while (cursor.moveToNext());
        }
        db.close();
        return notes;
    }


    public int updateNoteSubject(int id, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Note.COLUMN_SUBJECT, subject);
        return db.update(Note.TABLE_NAME, values, Note.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Note.COLUMN_NOTE, note.getNote());
        values.put(Note.COLUMN_SUBJECT, note.getSubject());
        values.put(Note.COLUMN_ADDRESS, note.getAddress());
        values.put(Note.COLUMN_LAT, note.getLat());
        values.put(Note.COLUMN_LNG, note.getLng());
        values.put(Note.COLUMN_IMAGE, note.getImage());
        values.put(Note.COLUMN_AUDIO, note.getAudio());
        values.put(Note.COLUMN_DESCRIPTION, note.getDescription());

        return db.update(Note.TABLE_NAME, values, Note.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Note.TABLE_NAME, Note.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
}