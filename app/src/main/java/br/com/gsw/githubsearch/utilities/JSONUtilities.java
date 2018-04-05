package br.com.gsw.githubsearch.utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import br.com.gsw.githubsearch.data.Repository;

public class JSONUtilities {

    private static final String REPOSITORIES_LIST = "items";
    private static final String MESSAGE_CODE = "cod";

    private static final String REPO_ID = "id";
    private static final String REPO_NAME = "name";
    private static final String REPO_DESC = "description";
    private static final String REPO_LINK = "html_url";
    private static final String REPO_OWNER = "owner";
    private static final String REPO_IMAGE = "avatar_url";

    public static Repository[] getRepositoryDataFromJson(String reposJsonStr)
            throws JSONException {
        Repository[] parsedRepositoriesData;

        JSONObject resultsJson = new JSONObject(reposJsonStr);

        /* Is there an error? */
        if (resultsJson.has(MESSAGE_CODE)) {
            int errorCode = resultsJson.getInt(MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return null;
                default:
                    return null;
            }
        }

        JSONArray repositoriesArray = resultsJson.getJSONArray(REPOSITORIES_LIST);
        parsedRepositoriesData = new Repository[repositoriesArray.length()];

        for (int i = 0; i < repositoriesArray.length(); i++) {
            int id;
            String name, description, link, image;

            /* Get the JSON object representing the movie */
            JSONObject repository = repositoriesArray.getJSONObject(i);

            id = repository.getInt(REPO_ID);
            name = repository.getString(REPO_NAME);
            description = repository.getString(REPO_DESC);
            link = repository.getString(REPO_LINK);
            image = repository.getJSONObject(REPO_OWNER).getString(REPO_IMAGE);

            parsedRepositoriesData[i] = new Repository(id, name, image, description, link);
        }

        return parsedRepositoriesData;
    }
}
