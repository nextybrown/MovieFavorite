package io.push.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.push.movieapp.Adapter.FavoriteMovieAdapter;
import io.push.movieapp.Adapter.MyListAdapter;
import io.push.movieapp.Entity.MovieContract;
import io.push.movieapp.QueryResult.MovieResult;
import io.push.movieapp.Service.MovieService;
import io.push.movieapp.Service.ServiceGeneratore;
import io.push.movieapp.Entity.Movie;
import io.push.movieapp.QueryResult.MovieResult;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderCallbacks<MovieResult>{


    private final String LOG_CAT = MainActivity.class.getSimpleName();
    private static  String KEY_PARAM ="api_key";
    public  static  String API_KEY=BuildConfig.THE_MOVIE_DB_API_TOKEN;
    public  static  String POPULAR_MOVIE="popular";
    public  static  String TOP_RATE_MOVIE="top_rated";
    @BindView(R.id.myrecycler) RecyclerView mRecyclerView;
    private MyListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Movie> movies = new ArrayList<>();
    @BindView(R.id.img_no_network) ImageView mImageError ;
    private static final  int MOVIE_LOADER_ID=500;
    private static final int FAVORITE_LOALDER_ID=501;
    private StaggeredGridLayoutManager gaggeredGridLayoutManager;

    public static final String[] MAIN_MOVIES_PROJECTION = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_IMAGE_URL

    };

    public static final int INDEX_ID= 0;
    public static final int INDEX_MOVIE_ID= 1;
    public static final int INDEX_MOVIE_TITLE=2;
    public static final int INDEX_MOVIE_IMAGE_URL=3;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState!=null ){
            movies= (List<Movie>)savedInstanceState.get("movies");

        }
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
         // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(this,2);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mLayoutManager = new GridLayoutManager(this,2);
        } else{
            mLayoutManager = new GridLayoutManager(this,3);
        }
        mRecyclerView.setLayoutManager(mLayoutManager);


        // specify an adapter (see also next example)
        mAdapter = new MyListAdapter();
        mAdapter.setMovies(movies);
        mRecyclerView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID,null,this);
        //getSupportLoaderManager().initLoader(FAVORITE_LOALDER_ID,null,new FavoriteLoader());

         boolean online = isOnline();
        if (online){
            mRecyclerView.setVisibility(View.VISIBLE);
            mImageError.setVisibility(View.INVISIBLE);
          //  getSupportLoaderManager().getLoader(LOADER_ID).forceLoad();
        }else{
            mRecyclerView.setVisibility(View.INVISIBLE);
            mImageError.setVisibility(View.VISIBLE);
        }

      PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies",(ArrayList<? extends Parcelable>) movies);
       // getSupportLoaderManager().restartLoader(LOADER_ID, null,this);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent  intent = new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_favorite){
         List<Movie>movieList = new ArrayList<Movie>();
         Movie  movie;

            CursorLoader cursorLoader = new CursorLoader(getApplicationContext(),MovieContract.MovieEntry.CONTENT_URI,
                      MAIN_MOVIES_PROJECTION,null,null,null);

            Cursor cursor= cursorLoader.loadInBackground();

            if(cursor.getCount()!=0 && cursor.moveToFirst()){
                 FavoriteMovieAdapter favoriteMovieAdapter = new FavoriteMovieAdapter();
                 mRecyclerView.removeAllViews();
                 favoriteMovieAdapter.swapCursor(cursor);
                 mRecyclerView.setAdapter(favoriteMovieAdapter);
                 Log.d(LOG_CAT," this it the number of cursor"+cursor.getCount());
             }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_sort_type_key))) {
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);

            //getSupportLoaderManager().getLoader(LOADER_ID).forceLoad();
            Log.d(LOG_CAT," Sharepreference is changed");
        }
    }

    @Override
    public Loader <MovieResult> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case MOVIE_LOADER_ID:
                return new AsyncTaskLoader<io.push.movieapp.QueryResult.MovieResult>(this) {
                    private io.push.movieapp.QueryResult.MovieResult result;

                    @Override
                    protected void onStartLoading() {
                        if (result != null) {
                            deliverResult(result);
                        } else {
                            forceLoad();
                        }

                        super.onStartLoading();
                    }

                    @Override
                    public MovieResult loadInBackground() {
                        try {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String sharedPrefString = sharedPref.getString(SettingsActivity.PREF_SORT_TYPE_KEY, POPULAR_MOVIE);
                            Log.d(LOG_CAT, "++++++Share preference out put  " + sharedPrefString);
                            MovieService movieService = ServiceGeneratore.createService(MovieService.class);
                            Call<MovieResult> repos = movieService.listmovie(sharedPrefString, API_KEY);
                           // Log.d(LOG_CAT, "buffer reader " + ReponseMovies.toString() + "output");
                            return repos.execute().body();
                        } catch (IOException e) {
                            Log.e(LOG_CAT, "Error" + e.toString(), e);
                        }
                        return null;
                    }

                    @Override
                    public void deliverResult(MovieResult data) {
                        result = data;
                        super.deliverResult(data);

                    }
                };




            default:

                return null;


        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onLoadFinished(Loader<MovieResult> loader, MovieResult data) {
        if(data!= null ) {
            switch (loader.getId()){
                case MOVIE_LOADER_ID :
                    MovieResult movieResult =data;
                    movies= movieResult.getResults();
                    mAdapter.setMovies(movieResult.getResults());
                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    break;


            }

        }

    }

    @Override
    public void onLoaderReset(Loader<MovieResult> loader) {

    }






}
