package com.bealc.popularmovies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *  This class is for the creation of the activity/fragment to display all the
 *  relevant detail about a movie. Relevant detail includes movie title, movie
 *  poster, movie synopsis, movie rating and movie release date.
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, new DetailFragment())
                    .commit();
        }
    }

    /**
     *  A placeholder fragment containing the view displaying all the movie detail.
     */
    public static class DetailFragment extends Fragment {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private String mMovieTitle;
        private String mMoviePosterPath;
        private String mMovieOverview;
        private String mMovieRating;
        private String mMovieReleaseDate;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail activity called via intent. Inspect intent for movie data.
            Bundle extras = getActivity().getIntent().getExtras();
            Intent intent = getActivity().getIntent();
            if(intent != null && extras != null) {
                mMovieTitle = intent.getStringExtra(MovieFragment.EXTRA_MOVIE_TITLE);
                mMoviePosterPath = intent.getStringExtra(MovieFragment.EXTRA_MOVIE_POSTER_PATH);
                mMovieOverview = intent.getStringExtra(MovieFragment.EXTRA_MOVIE_OVERVIEW);
                mMovieRating = intent.getStringExtra(MovieFragment.EXTRA_MOVIE_RATING);
                mMovieReleaseDate = intent.getStringExtra(MovieFragment.EXTRA_MOVIE_RELEASE_DATE);

                // Set text and image to corresponding fields provided in the layout xml.
                ((TextView) rootView.findViewById(R.id.textview_title)).setText(mMovieTitle);
                ImageView imgView = (ImageView) rootView.findViewById(R.id.imageview_thumbnail);
                Picasso.with(getContext()).load(mMoviePosterPath).into(imgView);
                ((TextView) rootView.findViewById(R.id.textview_container_overview)).setText(mMovieOverview);
                ((TextView) rootView.findViewById(R.id.textview_container_rating)).setText(mMovieRating);
                ((TextView) rootView.findViewById(R.id.textview_container_rdate)).setText(mMovieReleaseDate);
            }
            return rootView;
        }

        // The action bar currently shows only the Share icon.
        // Clicking on the Share icon allows user to share movie detail.
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detail, menu);

            // Retrieve share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share content.
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach an intent to this ShareActionProvider.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }

        /*
           The following blocks of code are obtained from stackoverflow website.
           In order to share movie detail, the view containing movie detail is first
           converted into a bitmap. The bitmap is saved to file and the file is shared.
        */

        // Convert view into a bitmap.
        public static Bitmap loadBitmapFromView(View v, int width, int height) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // fill canvas background with white color instead of the default (transparent)
            canvas.drawColor(Color.WHITE);
            v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
            v.draw(canvas);
            return bitmap;
        }

        // Create intent to be used with ActionShareProvider.
        private Intent createShareMovieIntent() {
            ScrollView scrollView = (ScrollView) getView().findViewById(R.id.scrollView);
            int width = scrollView.getWidth();

            // Getting the height of the first children view of the scrollview.
            int height = scrollView.getChildAt(0).getHeight();
            Bitmap bitmap = loadBitmapFromView(scrollView, width, height);

            // Save bitmap to cache directory.
            try {
                File cachePath = new File(getContext().getCacheDir(), "images");
                cachePath.mkdirs();
                FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Generate a content URI for the bitmap file in order to share the file with other app.
            File imagePath = new File(getContext().getCacheDir(), "images");
            File newFile = new File(imagePath, "image.png");
            Uri contentUri = FileProvider.getUriForFile(getContext(), "com.bealc.popularmovies.fileprovider", newFile);

            // Create intent and add extended data to it.
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setDataAndType(contentUri, getContext().getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            return shareIntent;
        }

    }

}
