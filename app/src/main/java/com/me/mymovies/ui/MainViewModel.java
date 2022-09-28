package com.me.mymovies.ui;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.firebase.auth.FirebaseAuth;
import com.me.mymovies.api.ApiFactory;
import com.me.mymovies.api.ApiService;
import com.me.mymovies.data.Movie;
import com.me.mymovies.data.MovieDatabase;
import com.me.mymovies.data.MovieResponse;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends AndroidViewModel {

    private static MovieDatabase database;
    private LiveData<List<Movie>> movies;

    private CompositeDisposable compositeDisposable;

    private static final String API_KEY = "a69e7f33eb4ad4cca2ae159d2b82b752";
    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_TOP_RATED = "vote_average.desc";
    private static final int MIN_VOTE_COUNT_VALUE = 1000;

    public static final int POPULARITY = 0;
    public static final int TOP_RATED = 1;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(getApplication());
        movies = database.movieDao().getAllMovies();
        //favouriteMovies = database.movieDao().getAllFavouriteMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }


    @SuppressWarnings("unchecked")
    public void insertMovies(List<Movie> movies) {
        new InsertMoviesTask().execute(movies);
    }

    private static class InsertMoviesTask extends AsyncTask<List<Movie>, Void, Void> {
        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Movie>... lists) {
            if (lists != null && lists.length > 0) {
                database.movieDao().insertMovies(lists[0]);
            }
            return null;
        }
    }

    private void deleteAllMovies() {
        new DeleteMoviesTask().execute();
    }

    private static class DeleteMoviesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            database.movieDao().deleteAllMovies();
            return null;
        }
    }

    public void deleteMovie(Movie movie) {
        new DeleteMovieTask().execute(movie);
    }

    private static class DeleteMovieTask extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteMovie(movies[0]);
            }
            return null;
        }
    }

    public void loadMovies(String lang, int sortBy, int page) {
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        compositeDisposable = new CompositeDisposable();
        String methodOfSort;
        if(sortBy == POPULARITY) {
            methodOfSort = SORT_BY_POPULARITY;
        }
        else {
            methodOfSort = SORT_BY_TOP_RATED;
        }
        Disposable disposable = apiService.getMovies(API_KEY, lang, methodOfSort, MIN_VOTE_COUNT_VALUE, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MovieResponse>() {
                    @Override
                    public void accept(MovieResponse movieResponse) throws Exception {
                        deleteAllMovies();
                        insertMovies(movieResponse.getMovies());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
