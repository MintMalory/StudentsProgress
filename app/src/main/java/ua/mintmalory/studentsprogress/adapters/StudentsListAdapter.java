package ua.mintmalory.studentsprogress.adapters;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ua.mintmalory.studentsprogress.R;
import ua.mintmalory.studentsprogress.fragments.StudentDetailedDialogFragment;
import ua.mintmalory.studentsprogress.models.Student;

/**
 * Created by mintmalory on 06.01.17.
 */

public class StudentsListAdapter extends RecyclerView.Adapter<StudentsListAdapter.ViewHolder> {
    private List<Student> studentsList;
    public static final String TAG_DIALOG = "STUDENT_DETAIL_DIALOG";
    public static final String STUDENT_INFO = "STUDENT_INFO";
    private AppCompatActivity context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mStudentsNameTv;
        private TextView mStudentsBirthdayTv;
        private ImageView mInfoIconIv;

        public ViewHolder(View v) {
            super(v);
            mStudentsNameTv = (TextView) v.findViewById(R.id.students_name_tv);
            mStudentsBirthdayTv = (TextView) v.findViewById(R.id.students_birthday_tv);
            mInfoIconIv = (ImageView) v.findViewById(R.id.icon_info_iv);
        }
    }

    public StudentsListAdapter(AppCompatActivity context, List<Student> studentsList) {
        this.context = context;
        this.studentsList = studentsList;
    }

    @Override
    public StudentsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_students_list, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mStudentsNameTv.setText(studentsList.get(position).getFirstName() + " " + studentsList.get(position).getLastName());
        holder.mStudentsBirthdayTv.setText(studentsList.get(position).getBirthdate());
        holder.mInfoIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StudentDetailedDialogFragment dialog = new StudentDetailedDialogFragment();
                Bundle b = new Bundle();
                b.putParcelableArrayList(STUDENT_INFO, new ArrayList<>(studentsList.get(position).getCourses()));
                dialog.setArguments(b);

                dialog.show(context.getSupportFragmentManager(), TAG_DIALOG);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentsList.size();
    }
}


