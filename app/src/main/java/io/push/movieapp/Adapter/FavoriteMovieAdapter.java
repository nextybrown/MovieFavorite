package io.push.movieapp.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.push.movieapp.Entity.Movie;
import io.push.movieapp.MainActivity;
import io.push.movieapp.R;

/**
 * Created by nestorkokoafantchao on 3/31/17.
 */

public class FavoriteMovieAdapter extends CursorRecyclerViewAdapter<FavoriteMovieAdapter.MovieViewHolder> {

    private static final String LOG_CAT = FavoriteMovieAdapter.class.toString();

     private Cursor cursor;
    public FavoriteMovieAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }


    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_holder, parent, false);
        //set the view's size, margins, paddings and layout parameters
        MovieViewHolder vh = new MovieViewHolder(v);
        return vh;

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void onBindViewHolder(MovieViewHolder viewHolder, Cursor cursor) {

        viewHolder.loadImage(cursor.getColumnName(MainActivity.INDEX_MOVIE_IMAGE_URL));
        Log.d(LOG_CAT," this is my Ulr "+cursor.getColumnName(MainActivity.INDEX_MOVIE_IMAGE_URL));


    }

    public class  MovieViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.image_movie) ImageView imageView;
        @BindView(R.id.tv_title) TextView textViewTitle;
        @BindView(R.id.tv_rating) TextView textViewRating;
        public Context context;

        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            context=itemView.getContext();
        }

        public void loadImage(String  stringUrl ){
            Picasso.with(context).load(stringUrl).into(imageView);

        }


    }

}
