package ua.mintmalory.studentsprogress.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.mintmalory.studentsprogress.R;
import ua.mintmalory.studentsprogress.adapters.CoursesListAdapter;
import ua.mintmalory.studentsprogress.adapters.StudentsListAdapter;
import ua.mintmalory.studentsprogress.models.Course;

/**
 * Created by mintmalory on 07.01.17.
 */

public class StudentDetailedDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        List<Course> coursesList = getCoursesList();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.courses_dialog_title))
                .setPositiveButton(getString(R.string.btn_ok_text), null);

        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.student_detail_dialog, null);

        ListView coursesLv = (ListView) view.findViewById(R.id.courses_list_lv);
        TextView avgMarkTv = (TextView) view.findViewById(R.id.avg_mark_tv);

        avgMarkTv.setText(String.format(Locale.US, getResources().getString(R.string.average_mark), getAvgMark(coursesList)));

        coursesLv.setAdapter(new CoursesListAdapter(coursesList, getActivity()));

        builder.setView(view);
        return builder.create();
    }

    private List<Course> getCoursesList() {
        if (getArguments() != null && getArguments().getParcelableArrayList(StudentsListAdapter.STUDENT_INFO) != null) {
            return getArguments().getParcelableArrayList(StudentsListAdapter.STUDENT_INFO);
        }

        return new ArrayList<>();
    }

    private float getAvgMark(List<Course> coursesList) {
        float avgMark = 0.0f;

        if (coursesList.size() > 0) {
            for (Course c : coursesList) {
                avgMark += c.getMark();
            }

            avgMark /= coursesList.size();
        }

        return avgMark;
    }
}
