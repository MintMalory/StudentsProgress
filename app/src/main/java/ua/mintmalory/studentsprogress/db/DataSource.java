package ua.mintmalory.studentsprogress.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ua.mintmalory.studentsprogress.models.Course;
import ua.mintmalory.studentsprogress.models.Student;

import static ua.mintmalory.studentsprogress.db.DatabaseHelper.KEY_BIRTHDAY;
import static ua.mintmalory.studentsprogress.db.DatabaseHelper.KEY_COURSE_NAME;
import static ua.mintmalory.studentsprogress.db.DatabaseHelper.KEY_FIRST_NAME;
import static ua.mintmalory.studentsprogress.db.DatabaseHelper.KEY_ID;
import static ua.mintmalory.studentsprogress.db.DatabaseHelper.KEY_LAST_NAME;
import static ua.mintmalory.studentsprogress.db.DatabaseHelper.KEY_MARK;

/**
 * Created by mintmalory on 07.01.17.
 */

public class DataSource {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DataSource(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void openForWriting() {
        db = dbHelper.getWritableDatabase();
    }

    public void openForReading() {
        db = dbHelper.getReadableDatabase();
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public void createStudentsWithMarks(List<Student> students) {
        openForWriting();
        db.beginTransaction();
        for (Student student : students) {
            createStudentWithMarks(student);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        close();
    }

    private void createStudentWithMarks(Student student) {
        if (dbHelper.createStudent(student, db) < 0) {
            dbHelper.deleteStudentMark(student.getId(), db);
        }

        long courseId;
        for (Course course : student.getCourses()) {
            courseId = getCourseID(course.getName());

            if (courseId < 0) {
                courseId = dbHelper.createCourse(course.getName(), db);
            }

            dbHelper.createMark(student.getId(), courseId, course.getMark(), db);
        }
    }

    public List<Student> getStudentsPage(int pageNumber) {
        openForReading();
        Cursor c = dbHelper.getStudentsPage(pageNumber, db);

        List<Student> studentsPage = studentsCursorToList(c);
        close();
        return studentsPage;
    }

    public List<Student> getFilteredStudentsPage(int pageNumber, String courseName, String mark) {
        openForReading();
        Cursor c = dbHelper.getFilteredStudentsPage(pageNumber, courseName, mark, db);

        List<Student> studentsPage = studentsCursorToList(c);
        close();
        return studentsPage;
    }

    @NonNull
    private List<Student> studentsCursorToList(Cursor c) {
        List<Student> studentsPage = new ArrayList<>();
        if (c.moveToFirst()) {
            Student st;
            do {
                st = new Student();
                st.setId(c.getString(c.getColumnIndex(KEY_ID)));

                if (studentsPage.contains(st)) {
                    st = studentsPage.get(studentsPage.indexOf(st));
                } else {
                    st.setFirstName(c.getString(c.getColumnIndex(KEY_FIRST_NAME)));
                    st.setLastName(c.getString(c.getColumnIndex(KEY_LAST_NAME)));
                    st.setBirthdate(c.getString(c.getColumnIndex(KEY_BIRTHDAY)));
                    st.setCourses(new ArrayList<Course>());
                    studentsPage.add(st);
                }

                Course course = new Course();
                course.setName(c.getString(c.getColumnIndex(KEY_COURSE_NAME)));
                course.setMark(c.getInt(c.getColumnIndex(KEY_MARK)));

                st.getCourses().add(course);

            } while (c.moveToNext());
        }

        c.close();
        return studentsPage;
    }

    public String[] getArrayOfCourses() {
        openForReading();
        Cursor c = dbHelper.getAllCourses(db);

        String[] courses = new String[c.getCount()];
        int index = 0;
        if (c.moveToFirst()) {
            do {
                courses[index++] = c.getString(c.getColumnIndex(KEY_COURSE_NAME));
            } while (c.moveToNext());
        }

        c.close();
        close();
        return courses;
    }

    private long getCourseID(String courseName) {
        Cursor c = dbHelper.getCourseID(courseName, db);

        if (c.moveToFirst()) {
            long courseID = c.getLong((c.getColumnIndex(KEY_ID)));
            c.close();
            return courseID;
        }

        c.close();
        return -1;
    }
}
