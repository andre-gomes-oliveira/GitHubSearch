package br.com.gsw.githubsearch.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

import br.com.gsw.githubsearch.R;
import br.com.gsw.githubsearch.utilities.JSONUtilities;
import br.com.gsw.githubsearch.utilities.NetworkUtilities;

public class RepositoryLoader extends AsyncTaskLoader<Repository[]> {

    private final Bundle mBundle;

    public RepositoryLoader(@NonNull Context context, @Nullable final Bundle args) {
        super(context);
        mBundle = args;
    }

    @Override
    protected void onStartLoading() {
        if (mBundle == null) {
            return;
        }

        forceLoad();
    }

    @Nullable
    @Override
    public Repository[] loadInBackground() {
        Context context = getContext();
        String query = mBundle.getString(context.getString(R.string.bundle_repo_query));
        String sortCriteria = mBundle.getString(context.getString(R.string.bundle_repo_sort));

        if (query != null && !query.isEmpty()) {
            try {
                URL detailsUrl = NetworkUtilities.buildSearchUrl(query, sortCriteria);
                String result = NetworkUtilities.getResponseFromHttpUrl(detailsUrl);

                if (result != null)
                    return JSONUtilities.getRepositoryDataFromJson(result);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }
}
