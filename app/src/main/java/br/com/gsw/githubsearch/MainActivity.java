package br.com.gsw.githubsearch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.URL;
import java.util.Objects;

import br.com.gsw.githubsearch.data.Repository;
import br.com.gsw.githubsearch.data.RepositoryAdapter;
import br.com.gsw.githubsearch.data.RepositoryLoader;
import br.com.gsw.githubsearch.utilities.NetworkUtilities;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Repository[]>{

    //Unique identifier for the loader
    private static final int REPOSITORIES_LOADER = 11;

    //The activity's Toolbar
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    //The drawer Layout
    @BindView(R.id.search_fab)
    FloatingActionButton mSearchFab;

    //The recycler view used to display the feeds
    @BindView(R.id.rv_repositories_list)
    RecyclerView mRecyclerView;

    //The swipe layout
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    //The progress bar used to signal that the feeds are being loaded
    @BindView(R.id.pb_repositories_loading_indicator)
    ProgressBar mProgressBar;

    //The TextView that displays an error message
    @BindView(R.id.tv_error_message)
    TextView mErrorMessageDisplay;

    // Layout manager used by the RecyclerView
    private GridLayoutManager mLayoutManager;

    //The adapter used by the recycler view
    private RepositoryAdapter mAdapter;

    //The action spinner
    Spinner mSpinner;

    //The spinner position
    private int mPosition;

    //The GitHub query
    private String mQuery;

    //The sorting option
    private String mSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setting up Timber
        Timber.plant(new Timber.DebugTree());

        //Setting up ButterKnife
        ButterKnife.bind(this);

        //Setting up the ui
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());

        //Since the source compatibility is set to Java 8, I'm using a lambda expression for code readability
        mSearchFab.setOnClickListener(View -> showSearchDialog());

        Resources resources = getResources();
        if (resources != null) {
            mLayoutManager = new GridLayoutManager(this,
                    resources.getInteger(R.integer.list_column_count),
                    LinearLayoutManager.VERTICAL, false);
        }

        //Restoring state
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(getString(R.string.bundle_repo_query));
            mSort = savedInstanceState.getString(getString(R.string.bundle_repo_sort));
            mPosition = savedInstanceState.getInt(
                    getString(R.string.bundle_spinner_position));

            Parcelable recyclerLayoutState = savedInstanceState.getParcelable
                    (getString(R.string.bundle_recycler_position));

            if (recyclerLayoutState != null && mLayoutManager != null) {
                mLayoutManager.onRestoreInstanceState(recyclerLayoutState);
            }
        } else {
            //Setting up default query
            mQuery = getString(R.string.default_query);
            mSort = getString(R.string.sort_option_stars_value);
        }

        mRecyclerView.setLayoutManager(mLayoutManager);

        //Since the source compatibility is set to Java 8, I'm using a lambda statement for code readability
        mSwipeRefreshLayout.setOnRefreshListener(() -> {

            mAdapter.clearData();
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(true);
            makeRepositoriesRequest();
        });

        makeRepositoriesRequest();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.bundle_repo_query), mQuery);
        outState.putString(getString(R.string.bundle_repo_sort), mSort);
        outState.putInt(getString(R.string.bundle_spinner_position),
                mSpinner.getSelectedItemPosition());
        outState.putParcelable(getString(R.string.bundle_recycler_position),
                mLayoutManager.onSaveInstanceState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.sort_functions_spinner);
        mSpinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options_labels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);
        mSpinner.setSelection(mPosition);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        String option = adapterView.getItemAtPosition(pos).toString();

        if (Objects.equals(option, getString(R.string.sort_option_stars_label)))
            mSort = getString(R.string.sort_option_stars_value);
        else if (Objects.equals(option, getString(R.string.sort_option_forks_label)))
            mSort = getString(R.string.sort_option_forks_value);
        else if (Objects.equals(option, getString(R.string.sort_option_updated_label)))
            mSort = getString(R.string.sort_option_updated_value);
        else{
            Snackbar.make(findViewById(R.id.activity_layout),
                    getString(R.string.dialog_cancel_message),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        makeRepositoriesRequest();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * Shows a dialog that allows the user to inform the search query
     */
    private void showSearchDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(getApplicationContext());
        alert.setTitle(getString(R.string.app_name));
        alert.setMessage(getString(R.string.dialog_message));

        alert.setView(edittext);

        alert.setPositiveButton(getString(R.string.dialog_confirm), (dialog, whichButton) -> {
                mQuery = edittext.getText().toString();
                makeRepositoriesRequest();
                });

        alert.setNegativeButton(getString(R.string.dialog_cancel), (dialog, whichButton) ->
                Snackbar.make(findViewById(R.id.activity_layout),
                        getString(R.string.dialog_cancel_message),
                        Snackbar.LENGTH_LONG).show());

        alert.show();
    }

    private void makeRepositoriesRequest() {
        mErrorMessageDisplay.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        mAdapter.clearData();

        LoaderManager loaderManager = getSupportLoaderManager();

        Bundle requestBundle = new Bundle();
        requestBundle.putString(getString(R.string.bundle_repo_query), mQuery);
        requestBundle.putString(getString(R.string.bundle_repo_sort), mSort);

        Loader<String> reposLoader = loaderManager.getLoader(REPOSITORIES_LOADER);

        if (reposLoader == null)
            loaderManager.initLoader(REPOSITORIES_LOADER, requestBundle, this);
        else
            loaderManager.restartLoader(REPOSITORIES_LOADER, requestBundle, this);
    }

    @NonNull
    @Override
    public Loader<Repository[]> onCreateLoader(int id, @Nullable Bundle args) {
        return new RepositoryLoader(this, args);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Repository[]> loader, Repository[] data) {
        if ((data != null) && (data.length > 0)) {
            mErrorMessageDisplay.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mSwipeRefreshLayout.setRefreshing(false);

            mAdapter = new RepositoryAdapter(data, this);
            mAdapter.setHasStableIds(true);
            mRecyclerView.setAdapter(mAdapter);
        }
        else{
            mAdapter = new RepositoryAdapter(new Repository[0], this);
            mRecyclerView.setAdapter(mAdapter);
            mErrorMessageDisplay.setVisibility(View.VISIBLE);

            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Repository[]> loader) {
        mRecyclerView.setAdapter(null);
    }
}
