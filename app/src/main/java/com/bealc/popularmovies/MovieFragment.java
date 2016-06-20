package com.bealc.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *  A fragment containing the grid view of popular movies. Each item in the grid view, when clicked,
 *  will call the DetailActivity which displays the relevant movie detail.
 */
public class MovieFragment extends Fragment {

    // Names of extended data to be sent with the Intent calling the DetailActivity class.
    public static final String EXTRA_MOVIE_TITLE = "com.beal.popularmovies.title";
    public static final String EXTRA_MOVIE_POSTER_PATH = "com.beal.popularmovies.poster_path";
    public static final String EXTRA_MOVIE_OVERVIEW = "com.beal.popularmovies.synopsis";
    public static final String EXTRA_MOVIE_RATING = "com.beal.popularmovies.rating";
    public static final String EXTRA_MOVIE_RELEASE_DATE = "com.beal.popularmovies.release_date";

    private MovieAdapter movieAdapter;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create an object of the custom MovieAdapter class.
        movieAdapter = new MovieAdapter(getActivity(), new ArrayList<PopularMovie>());

        // Get a reference to the GridView.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView_movie);

        // Attach the movieAdapter to the GridView.
        gridView.setAdapter(movieAdapter);

        // Calls the DetailActivity class with all relevant movie
        // information when an item in the GridView is clicked.
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PopularMovie selectedMovie = movieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(EXTRA_MOVIE_TITLE, selectedMovie.getmTitle());
                intent.putExtra(EXTRA_MOVIE_POSTER_PATH, selectedMovie.getmPosterPath());
                intent.putExtra(EXTRA_MOVIE_OVERVIEW, selectedMovie.getmOverview());
                intent.putExtra(EXTRA_MOVIE_RATING, selectedMovie.getmVoteAverage());
                intent.putExtra(EXTRA_MOVIE_RELEASE_DATE, selectedMovie.getmReleaseDate());
                startActivity(intent);
            }
        });
        return rootView;
    }

    /*
     Fetch movie data from themovedb.org using Asynctask. The movie sorting preference (most popular
     or most highly rated) is obtained from SharedPreferences and passed as a parameter when
     executing the task.
    */
    private void updateMovie() {
        FetchMovieData movieData = new FetchMovieData();
        // Get preference for sorting movie - most popular or top_rated. Default is most popular.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String selection = prefs.getString(getString(R.string.pref_sort_movie_key), getString(R.string.pref_sort_by_types_default));
        // Execute the asyntask with the correct movie sorting preference.
        movieData.execute(selection);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Starts the Asynctask to pull movie data from themoviedb.org.
        updateMovie();
    }

    public class FetchMovieData extends AsyncTask<String, Void, List<PopularMovie>> {

        private final String LOG_TAG = FetchMovieData.class.getSimpleName();

        /**
         * Take the JSON data returned by themoviedb API and pull out
         * the data that is needed for this app.
         * Constructor takes JSON string and converts it into an
         * Object hierarchy.
         * @param movieJsonStr This is the parameter passed to themoviedb API
         * @return movieResult An arraylist of the PopularMovie objects
         * @throws JSONException If a JSON array/object cannot be created
         */
        private List<PopularMovie> getMovieDataFromJson(String movieJsonStr) throws JSONException {
            // Base url of the movie poster images
            final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
            // Size of the movie poster images
            final String SIZE = "w185";

            // Names of JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_ID = "id";
            final String TMDB_TITLE = "original_title";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_RELEASE_DATE = "release_date";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_VOTE_AVERAGE = "vote_average";

            // Get the JSON object representing movie
            JSONObject movieJSON = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJSON.getJSONArray(TMDB_RESULTS);

            // Get the number of movies found.
            int movieDataSize = movieArray.length();
            List<PopularMovie> movieResult = new ArrayList<PopularMovie>();

            // Extract the relevant JSON information from each movie and add to movieResult.
            for(int i = 0; i < movieDataSize; i++) {
                String id;
                String title;
                String overview;
                String releaseDate;
                String posterPath;
                String voteAverage;

                JSONObject movieObject = movieArray.getJSONObject(i);
                id = movieObject.getString(TMDB_ID);
                title = movieObject.getString(TMDB_TITLE);
                overview = movieObject.getString(TMDB_OVERVIEW);
                releaseDate = movieObject.getString(TMDB_RELEASE_DATE);
                posterPath = IMAGE_BASE_URL+SIZE+movieObject.getString(TMDB_POSTER_PATH);
                voteAverage = movieObject.getString(TMDB_VOTE_AVERAGE);
                movieResult.add(new PopularMovie(id, title, overview, releaseDate, posterPath, voteAverage));
            }

            return movieResult;
        }

        @Override
        protected List<PopularMovie> doInBackground(String... params) {

            // Verify size of params.
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // String to hold raw JSON results
            String movieJsonStr = null;

            // Construct the URL for themoviedb query.
            try {
                // Final String TMDB_BASE_URL = "http://api.themoviedb.org/3/movie/popular?";
                String tmdbUrl = "http://api.themoviedb.org/3/movie/";
                // sortType is either popular or top_rated.
                String sortType = params[0];
                final String TMDB_BASE_URL = tmdbUrl + sortType + "?";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIEDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create connection request to themoviedb and open connection.
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String.
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // No parsing needed if Stream is empty.
                    return null;
                }

                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        // Add movie data to the movieAdapter.
        @Override
        protected void onPostExecute(List<PopularMovie> result) {
            if (result != null) {
                movieAdapter.clear();
                for (PopularMovie movie : result) {
                    movieAdapter.add(movie);
                }
            }
        }
    }

}
