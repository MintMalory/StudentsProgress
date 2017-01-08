package ua.mintmalory.studentsprogress;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.wunderlist.slidinglayer.SlidingLayer;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.mintmalory.studentsprogress.adapters.StudentsListAdapter;
import ua.mintmalory.studentsprogress.db.DataSource;
import ua.mintmalory.studentsprogress.db.DatabaseHelper;
import ua.mintmalory.studentsprogress.models.Student;

public class MainActivity extends AppCompatActivity {

    public static final int TIME_TO_DOWNLOAD_ZONE = 6;
    private RecyclerView studentsListRv;
    private List<Student> studentsList = new ArrayList<>();
    private StudentsListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SlidingLayer slidingLayer;
    private Spinner coursesListSp;
    private boolean isListFiltered = false;
    private ViewFlipper vFlipper;

    private String filterCourseName;
    private String filterMark;

    private DataSource dataSource;

    private volatile boolean isDownloading = false;
    private boolean isMenuAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setSubtitle(R.string.author_name);

        coursesListSp = (Spinner) findViewById(R.id.courses_list_sp);
        vFlipper = (ViewFlipper) findViewById(R.id.view_switcher);
        dataSource = new DataSource(this);

        initRecyclerView();
        initSlidingMenu();

        if (isOnline()) {
            flipToLayout(R.id.activity_main_downloading);
            downloadStudentsList();
        } else {
            flipToLayout(R.id.activity_main_normal);
            if (dataSource.getArrayOfCourses().length > 0) {
                getSupportActionBar().setSubtitle(R.string.author_name_plus_offline);
                populateRecyclerView();
                populateSpinner();
            } else {
                flipToLayout(R.id.activity_main_no_internet);
                isMenuAvailable = false;
            }
        }
    }

    private void populateSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, dataSource.getArrayOfCourses());
        coursesListSp.setAdapter(spinnerAdapter);
    }

    private void flipToLayout(int layoutID) {
        while (vFlipper.getCurrentView().getId() != layoutID) {
            vFlipper.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            vFlipper.showNext();
        }
    }

    private void initRecyclerView() {
        studentsListRv = (RecyclerView) findViewById(R.id.students_list_rv);
        layoutManager = new LinearLayoutManager(this);
        studentsListRv.setLayoutManager(layoutManager);
        adapter = new StudentsListAdapter(this, studentsList);
        studentsListRv.setAdapter(adapter);

        studentsListRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int pastVisibleItems = layoutManager.findLastVisibleItemPosition();

                if (pastVisibleItems > 0 && pastVisibleItems >= adapter.getItemCount() - TIME_TO_DOWNLOAD_ZONE && !isDownloading) {
                    isDownloading = true;

                    if (isListFiltered) {
                        SelectingFilteredStudentsNextPage task = new SelectingFilteredStudentsNextPage();
                        task.execute(adapter.getItemCount() / DatabaseHelper.PAGE_SIZE + 1);
                    } else {
                        SelectingStudentsNextPage task = new SelectingStudentsNextPage();
                        task.execute(adapter.getItemCount() / DatabaseHelper.PAGE_SIZE + 1);
                    }

                }
            }
        });
    }

    private void initSlidingMenu() {
        slidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
        slidingLayer.setStickTo(SlidingLayer.STICK_TO_RIGHT);
        slidingLayer.setChangeStateOnTap(false);

        final TextView markTv = (TextView) findViewById(R.id.filter_mark_tv);

        Button clearBtn = (Button) findViewById(R.id.clear_btn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coursesListSp.setSelection(0);
                markTv.setText("");
                hideKeyboard();

                studentsListRv.scrollToPosition(0);

                if (!isListFiltered) {
                    slidingLayer.closeLayer(true);
                    return;
                }

                flipToLayout(R.id.activity_main_normal);
                isListFiltered = false;
                filterCourseName = null;
                filterMark = null;

                populateRecyclerView();

                slidingLayer.closeLayer(true);
            }
        });

        Button okBtn = (Button) findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (markTv.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, getString(R.string.no_mark_err_msg), Toast.LENGTH_LONG).show();
                    return;
                }

                studentsListRv.scrollToPosition(0);

                isListFiltered = true;
                filterCourseName = coursesListSp.getSelectedItem().toString();
                filterMark = markTv.getText().toString();

                studentsList.clear();
                SelectingFilteredStudentsNextPage task = new SelectingFilteredStudentsNextPage();
                task.execute(1);

                slidingLayer.closeLayer(true);
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void downloadStudentsList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitService.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService service = retrofit.create(RetrofitService.class);

        Call<List<Student>> call = service.getStudentsList();
        call.enqueue(new Callback<List<Student>>() {
            @Override
            public void onResponse(Call<List<Student>> call, Response<List<Student>> response) {
                if (response.code() == 200) {
                    DBPopulationTask task = new DBPopulationTask();
                    task.execute(response.body());
                } else {
                    flipToLayout(R.id.activity_main_no_internet);
                }
            }

            @Override
            public void onFailure(Call<List<Student>> call, Throwable t) {
                flipToLayout(R.id.activity_main_no_internet);
            }
        });
    }

    private void populateRecyclerView() {
        studentsList.clear();
        studentsList.addAll(dataSource.getStudentsPage(1));
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_filter:
                if (isMenuAvailable) {
                    slidingLayer.openLayer(true);
                } else {
                    Toast.makeText(this, getString(R.string.no_data_for_filtering_err_msg), Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (slidingLayer.isOpened()) {
            slidingLayer.closeLayer(true);
        } else {
            super.onBackPressed();
        }
    }

    class DBPopulationTask extends AsyncTask<List<Student>, Void, Void> {

        @Override
        protected Void doInBackground(List<Student>... lists) {
            dataSource.createStudentsWithMarks(lists[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            populateSpinner();
            populateRecyclerView();
            flipToLayout(R.id.activity_main_normal);
        }
    }

    class SelectingStudentsNextPage extends AsyncTask<Integer, Void, List<Student>> {

        @Override
        protected List<Student> doInBackground(Integer... integers) {
            List<Student> nextPage = dataSource.getStudentsPage(integers[0]);
            return nextPage;
        }

        @Override
        protected void onPostExecute(List<Student> students) {
            super.onPostExecute(students);

            studentsList.addAll(students);
            adapter.notifyDataSetChanged();
            isDownloading = false;
        }
    }

    class SelectingFilteredStudentsNextPage extends AsyncTask<Integer, Void, List<Student>> {
        @Override
        protected List<Student> doInBackground(Integer... integers) {
            List<Student> nextPage = dataSource.getFilteredStudentsPage(integers[0], filterCourseName, filterMark);
            return nextPage;
        }

        @Override
        protected void onPostExecute(List<Student> students) {
            super.onPostExecute(students);

            studentsList.addAll(students);
            adapter.notifyDataSetChanged();
            isDownloading = false;

            if (adapter.getItemCount() == 0) {
                Toast.makeText(MainActivity.this, getString(R.string.no_data_found_msg), Toast.LENGTH_LONG).show();
            }
        }
    }
}
