package ua.mintmalory.studentsprogress.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ua.mintmalory.studentsprogress.models.Student;


/**
 * Created by mintmalory on 07.01.17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "studentsInfo";

    public static final String TABLE_STUDENTS = "students";
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_MARKS = "marks";

    public static final String KEY_ID = "id";

    // STUDENTS Table - column names
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_BIRTHDAY = "birthday";

    // COURSES Table - column names
    public static final String KEY_COURSE_NAME = "name";

    // MARKS Table - column names
    public static final String KEY_STUDENT_ID = "student_id";
    public static final String KEY_COURSE_ID = "course_id";
    public static final String KEY_MARK = "mark";


    // STUDENTS table create statements
    private static final String CREATE_TABLE_STUDENTS = "CREATE TABLE " + TABLE_STUDENTS
            + "(" + KEY_ID + " CHAR(50) NOT NULL PRIMARY KEY,"
            + KEY_FIRST_NAME + " CHAR(20),"
            + KEY_LAST_NAME + " CHAR(20),"
            + KEY_BIRTHDAY + " DATE,"
            + " UNIQUE(" + KEY_ID + ") ON CONFLICT IGNORE " + ")";

    // create new tables
    // COURSES table create statement
    private static final String CREATE_TABLE_COURSES = "CREATE TABLE " + TABLE_COURSES
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_COURSE_NAME + " CHAR(20),"
            + " UNIQUE(" + KEY_COURSE_NAME + ") ON CONFLICT IGNORE" + ")";

    // MARKS table create statement
    private static final String CREATE_TABLE_MARKS = "CREATE TABLE " + TABLE_MARKS
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_STUDENT_ID + " CHAR(50) NOT NULL,"
            + KEY_COURSE_ID + " INTEGER NOT NULL,"
            + KEY_MARK + " INTEGER NOT NULL,"
            + "FOREIGN KEY (" + KEY_STUDENT_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + KEY_ID + "),"
            + "FOREIGN KEY (" + KEY_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + KEY_ID + ")" + ")";

    public static final int PAGE_SIZE = 20;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STUDENTS);
        db.execSQL(CREATE_TABLE_COURSES);
        db.execSQL(CREATE_TABLE_MARKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);

        onCreate(db);
    }

    public long createCourse(String courseName, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(KEY_COURSE_NAME, courseName);

        return db.insert(TABLE_COURSES, null, values);
    }

    public Cursor getCourseID(String courseName, SQLiteDatabase db) {
        String selectQuery = "SELECT id FROM " + TABLE_COURSES + " WHERE name = \'" + courseName + "\' LIMIT 1";
        return db.rawQuery(selectQuery, null);
    }

    public void deleteStudentMark(String studentId, SQLiteDatabase db) {
        db.delete(TABLE_MARKS, KEY_MARK + "=" + studentId, null);
    }

    public long createStudent(Student student, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, student.getId());
        values.put(KEY_FIRST_NAME, student.getFirstName());
        values.put(KEY_LAST_NAME, student.getLastName());
        values.put(KEY_BIRTHDAY, student.getBirthdayDate());

        return db.insert(TABLE_STUDENTS, null, values);
    }

    public long createMark(String studentID, long courseID, int mark, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(KEY_STUDENT_ID, studentID);
        values.put(KEY_COURSE_ID, courseID);
        values.put(KEY_MARK, mark);

        return db.insert(TABLE_MARKS, null, values);
    }

    public Cursor getStudentsPage(int pageNumber, SQLiteDatabase db) {
        final String TABLE_STUDENTS_RESULT = "students_res";
        int lastStudent = (pageNumber - 1) * PAGE_SIZE;

        String selectQuery = "SELECT " + TABLE_STUDENTS_RESULT + "." + KEY_ID + ", "
                + TABLE_STUDENTS_RESULT + "." + KEY_FIRST_NAME + ", "
                + TABLE_STUDENTS_RESULT + "." + KEY_LAST_NAME + ", "
                + TABLE_STUDENTS_RESULT + "." + KEY_BIRTHDAY + ", "
                + TABLE_COURSES + "." + KEY_COURSE_NAME + ", "
                + TABLE_MARKS + "." + KEY_MARK

                + " FROM (SELECT " + KEY_ID + ", " + KEY_FIRST_NAME + ", " + KEY_LAST_NAME + ", " + KEY_BIRTHDAY
                + " FROM " + TABLE_STUDENTS + " LIMIT " + PAGE_SIZE + " OFFSET " + lastStudent + ") AS " + TABLE_STUDENTS_RESULT

                + " INNER JOIN " + TABLE_MARKS + " ON " + TABLE_STUDENTS_RESULT + "." + KEY_ID + " = " + TABLE_MARKS + "." + KEY_STUDENT_ID
                + " INNER JOIN " + TABLE_COURSES + " ON " + TABLE_MARKS + "." + KEY_COURSE_ID + " = " + TABLE_COURSES + "." + KEY_ID;
        return db.rawQuery(selectQuery, null);
    }

    public Cursor getFilteredStudentsPage(int pageNumber, String courseName, String mark, SQLiteDatabase db) {
        db = this.getWritableDatabase();
        final String TABLE_STUDENTS_RESULT = "students_res";
        int lastStudent = (pageNumber - 1) * PAGE_SIZE;

        String selectQuery = "SELECT " + TABLE_STUDENTS_RESULT + "." + KEY_ID + ", "
                + TABLE_STUDENTS_RESULT + "." + KEY_FIRST_NAME + ", "
                + TABLE_STUDENTS_RESULT + "." + KEY_LAST_NAME + ", "
                + TABLE_STUDENTS_RESULT + "." + KEY_BIRTHDAY + ", "
                + TABLE_COURSES + "." + KEY_COURSE_NAME + ", "
                + TABLE_MARKS + "." + KEY_MARK

                + " FROM (SELECT " + TABLE_STUDENTS + "." + KEY_ID + ", "
                + TABLE_STUDENTS + "." + KEY_FIRST_NAME + ", "
                + TABLE_STUDENTS + "." + KEY_LAST_NAME + ", "
                + TABLE_STUDENTS + "." + KEY_BIRTHDAY

                + " FROM " + TABLE_STUDENTS
                + " INNER JOIN " + TABLE_MARKS + " ON " + TABLE_STUDENTS + "." + KEY_ID + " = " + TABLE_MARKS + "." + KEY_STUDENT_ID
                + " INNER JOIN " + TABLE_COURSES + " ON " + TABLE_MARKS + "." + KEY_COURSE_ID + " = " + TABLE_COURSES + "." + KEY_ID
                + " WHERE " + TABLE_COURSES + "." + KEY_COURSE_NAME + " = \'" + courseName + "\'"
                + " AND " + TABLE_MARKS + "." + KEY_MARK + " = \'" + mark + "\'"
                + " LIMIT " + PAGE_SIZE + " OFFSET " + lastStudent + ") AS " + TABLE_STUDENTS_RESULT

                + " INNER JOIN " + TABLE_MARKS + " ON " + TABLE_STUDENTS_RESULT + "." + KEY_ID + " = " + TABLE_MARKS + "." + KEY_STUDENT_ID
                + " INNER JOIN " + TABLE_COURSES + " ON " + TABLE_MARKS + "." + KEY_COURSE_ID + " = " + TABLE_COURSES + "." + KEY_ID;

        return db.rawQuery(selectQuery, null);
    }

    public Cursor getAllCourses(SQLiteDatabase db) {
        String selectQuery = "SELECT " + KEY_COURSE_NAME
                + " FROM " + TABLE_COURSES;

        return db.rawQuery(selectQuery, null);
    }
}
