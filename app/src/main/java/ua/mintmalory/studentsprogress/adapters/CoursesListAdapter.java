package ua.mintmalory.studentsprogress.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ua.mintmalory.studentsprogress.R;
import ua.mintmalory.studentsprogress.models.Course;

/**
 * Created by mintmalory on 07.01.17.
 */

public class CoursesListAdapter extends BaseAdapter {
    private List<Course> coursesInfo;
    private LayoutInflater inflater;

    public CoursesListAdapter(List<Course> coursesInfo, Context context) {
        this.coursesInfo = coursesInfo;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return coursesInfo.size();
    }

    @Override
    public Object getItem(int i) {
        return coursesInfo.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_marks_list, viewGroup, false);
        }

        Course course = coursesInfo.get(i);

        ((TextView) view.findViewById(R.id.course_name_tv)).setText(course.getName());
        ((TextView) view.findViewById(R.id.mark_tv)).setText(String.valueOf(course.getMark()));

        return view;
    }
}
