package aq.oceanbase.skyscroll.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import aq.oceanbase.skyscroll.logic.Game;
import aq.oceanbase.skyscroll.logic.questions.Question;

import java.sql.SQLException;

public class QuestionDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "questions";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_TEXT = "text";
    public static final String COLUMN_NAME_CORRECT_ANSWER = "correct";
    public static final String COLUMN_NAME_ANSWER1 = "ans1";
    public static final String COLUMN_NAME_ANSWER2 = "ans2";
    public static final String COLUMN_NAME_ANSWER3 = "ans3";

    private static final int COLUMN_INDEX_ID = 0;
    private static final int COLUMN_INDEX_TEXT = 1;
    private static final int COLUMN_INDEX_CORRECT_ANSWER = 2;
    private static final int COLUMN_INDEX_ANSWER = 3;

    private static final String DATABASE_NAME = "oceanbase.db";
    private static final int DATABASE_VER = 1;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "("
            + COLUMN_NAME_ID + " integer primary key autoincrement, "
            + COLUMN_NAME_TEXT + " text not null, "
            + COLUMN_NAME_CORRECT_ANSWER + " text not null, "
            + COLUMN_NAME_ANSWER1 + " text not null, "
            + COLUMN_NAME_ANSWER2 + " text, "
            + COLUMN_NAME_ANSWER3 + " text);";


    private SQLiteDatabase mDatabase;
    private String[] mColumns = { COLUMN_NAME_ID, COLUMN_NAME_TEXT,
            COLUMN_NAME_CORRECT_ANSWER, COLUMN_NAME_ANSWER1, COLUMN_NAME_ANSWER2, COLUMN_NAME_ANSWER3 };



    public QuestionDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void openW() throws SQLException {
        this.mDatabase = this.getWritableDatabase();
    }

    public void openR() throws SQLException {
        this.mDatabase = this.getReadableDatabase();
    }

    public void clearDB() throws SQLException {
        if ( !mDatabase.isOpen() ) {
            throw new SQLException("Database invalid");
        }

        this.mDatabase.delete(TABLE_NAME, null, null);
    }

    public void addQuestion(String text, String correctAnswer, String answers[]) {
        if (!mDatabase.isOpen()) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TEXT, text);
        values.put(COLUMN_NAME_CORRECT_ANSWER, correctAnswer);
        values.put(COLUMN_NAME_ANSWER1, answers.length >= 1 ? answers[0] : correctAnswer);
        values.put(COLUMN_NAME_ANSWER2, answers.length >= 2 ? answers[1] : null);
        values.put(COLUMN_NAME_ANSWER3, answers.length >= 3 ? answers[2] : null);

        long insertId = mDatabase.insert(TABLE_NAME, null, values);
    }

    public void addQuestion(Question question) {
        String[] answers = question.getVariants();
        this.addQuestion(question.getBody(), answers[0], new String[] {answers[1], answers[2], answers[3]});
    }

    public void deleteQuestion(long id) {
        if (mDatabase.isOpen()) {
            mDatabase.delete(TABLE_NAME, COLUMN_NAME_ID + " = " + id, null);
        }
    }

    public long getEntryCount() throws SQLException{
        if ( !mDatabase.isOpen() ) {
            throw new SQLException("Database not open");
        }

        return DatabaseUtils.queryNumEntries(mDatabase, TABLE_NAME);
    }

    public Question getQuestion(long id) throws SQLException {
        if ( !mDatabase.isOpen() ) {
            throw new SQLException("Database not open");
        }

        Cursor cursor = mDatabase.query(TABLE_NAME, mColumns, COLUMN_NAME_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        String text = cursor.getString(COLUMN_INDEX_TEXT);
        String[] answers = new String[4];
        answers[0] = cursor.getString(COLUMN_INDEX_CORRECT_ANSWER);
        for (int i = 0; i < Game.QUESTIONS_AMOUNT - 1; i++) {
            answers[i + 1] = cursor.getString(COLUMN_INDEX_ANSWER + i);
        }
        cursor.close();
        return new Question(text, answers, 0);
    }
}
