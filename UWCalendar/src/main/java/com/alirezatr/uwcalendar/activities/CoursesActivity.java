package com.alirezatr.uwcalendar.activities;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alirezatr.uwcalendar.R;
import com.alirezatr.uwcalendar.adapters.CoursesListAdapter;
import com.alirezatr.uwcalendar.adapters.SubjectsListAdapter;
import com.alirezatr.uwcalendar.listeners.CoursesListener;
import com.alirezatr.uwcalendar.models.Course;
import com.alirezatr.uwcalendar.models.Subject;
import com.alirezatr.uwcalendar.network.NetworkManager;
import com.alirezatr.uwcalendar.utils.FilterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CoursesActivity extends ListActivity {
    private CoursesListAdapter adapter = new CoursesListAdapter();
    private List<Object[]> alphabet = new ArrayList<Object[]>();
    private HashMap<String, Integer> sections = new HashMap<String, Integer>();
    private NetworkManager networkManager;
    private String subject;
    ProgressDialog mProgressDialog;
    TextView mNetworkError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.layout.activity_open_translate, R.layout.activity_close_scale);
        setContentView(R.layout.list_alphabet);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ImageView view = (ImageView)findViewById(android.R.id.home);
        view.setPadding(5, 0, 10, 0);

        networkManager = new NetworkManager(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            subject = extras.getString("SUBJECT");
            actionBar.setTitle(subject);
            actionBar.setSubtitle("Waterloo Calendar");
            loadCourses(subject);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.layout.activity_open_scale, R.layout.activity_close_translate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setAdapter(ArrayList<Course> courses) {
        List rows = new ArrayList();
        int start = 0;
        int end;
        String previousDigit = null;
        Object[] tmpIndexItem;
        Course course;

        for(int i = 0; i < courses.size(); i++) {
            course = courses.get(i);
            String firstDigit = course.getCatalogNumber().substring(0, 1);

            if (previousDigit != null && !firstDigit.equals(previousDigit)) {
                end = rows.size() - 1;
                tmpIndexItem = new Object[2];
                tmpIndexItem[0] = start;
                tmpIndexItem[1] = end;
                alphabet.add(tmpIndexItem);

                start = end + 1;
            }

            if (!firstDigit.equals(previousDigit)) {
                rows.add(new CoursesListAdapter.Section(subject + firstDigit + "00s"));
                sections.put(subject + firstDigit + "00's", start);
            }

            rows.add(new CoursesListAdapter.Item(subject + course.getCatalogNumber() + " - " + course.getTitle(), course.getDescription()));
            previousDigit = firstDigit;
        }

        if(previousDigit != null) {
            tmpIndexItem = new Object[2];
            tmpIndexItem[0] = start;
            tmpIndexItem[1] = rows.size() - 1;
            alphabet.add(tmpIndexItem);
        }

        adapter.setRows(rows);
        setListAdapter(adapter);
    }

    public void loadCourses(String subject) {
        mNetworkError = (TextView) findViewById(R.id.network_fail);
        mProgressDialog.setMessage(getResources().getString(R.string.loading_courses));
        mProgressDialog.show();
        networkManager.getCourses(subject, new CoursesListener() {
            @Override
            public void onSuccess(ArrayList<Course> courses) {
                if(courses.size() == 0) {
                    mNetworkError.setVisibility(View.VISIBLE);
                    mProgressDialog.dismiss();
                } else {
                    setAdapter(courses);
                    ListView mListView = (ListView) findViewById(android.R.id.list);
                    mListView.setVisibility(View.VISIBLE);
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onError(Exception error) {
                mNetworkError.setVisibility(View.VISIBLE);
                mProgressDialog.dismiss();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Object o = this.getListAdapter().getItem(position);
        Course course = (Course) o;
        Intent intent = new Intent(getListView().getContext(), CourseActivity.class);
        intent.putExtra("SUBJECT", course.getSubject());
        intent.putExtra("catalog_number", course.getCatalogNumber());
        getListView().getContext().startActivity(intent);
    }
}
