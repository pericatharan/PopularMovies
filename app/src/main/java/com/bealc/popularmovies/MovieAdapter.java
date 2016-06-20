package com.bealc.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by maile on 6/4/2016.
 */
public class MovieAdapter extends ArrayAdapter<PopularMovie> {

    /**
     * This is a custom adapter where the context is used to inflate the layout
     * file and the List is the data used to populate the GridView.
     * @param context The current context and used to inflate the layout file.
     * @param popularMovies A list of PopularMovie objects to be displayed.
     */
    public MovieAdapter(Activity context, List<PopularMovie> popularMovies) {
        // Initialize the adapter's internal storage for the context and the list.
        super(context, 0, popularMovies);
    }

    /**
     * Provides a view for an AdapterView(GridView).
     * @param position The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the PopularMovie object from the custom adapter at the correct position
        PopularMovie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_item, parent, false);
        }

        ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.grid_item_imageview);

        // Using Picasso to fetch images and load into view
        Picasso.with(getContext()).load(movie.getmPosterPath()).into(thumbnailView);

        return convertView;
    }
}
