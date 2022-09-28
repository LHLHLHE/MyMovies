package com.me.mymovies.ui;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.me.mymovies.api.ApiFactory;
import com.me.mymovies.api.ApiService;
import com.me.mymovies.data.FavouriteMovie;
import com.me.mymovies.data.Movie;
import com.me.mymovies.data.MovieDatabase;
import com.me.mymovies.data.Review;
import com.me.mymovies.data.ReviewResponse;
import com.me.mymovies.data.Trailer;
import com.me.mymovies.data.TrailerResponse;
import com.me.mymovies.data.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DetailViewModel extends AndroidViewModel {

    private static MovieDatabase database;
    private MutableLiveData<List<FavouriteMovie>> favouriteMovies = new MutableLiveData<>(new ArrayList<>());
    private User user;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference users;
    private DatabaseReference favouritesRef;
    private MutableLiveData<List<Review>> reviewsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Trailer>> trailersLiveData = new MutableLiveData<>();

    private CompositeDisposable compositeDisposable;

    private static final String API_KEY = "a69e7f33eb4ad4cca2ae159d2b82b752";


    public DetailViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(getApplication());
        users = db.getReference("Users");
        favouritesRef = db.getReference("Favourites");
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        getUser();
    }

    private void getFavouriteMovies() {
        ArrayList<FavouriteMovie> favourites = new ArrayList<>();
        favouritesRef.child(user.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                favourites.add(snapshot.getValue(FavouriteMovie.class));
                favouriteMovies.setValue(favourites);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUser() {
        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                getFavouriteMovies();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public Movie getMovieById(int id) {
        try {
            return new GetMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GetMovieTask extends AsyncTask<Integer, Void, Movie> {
        @Override
        protected Movie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getMovieById(integers[0]);
            }
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public FavouriteMovie getFavouriteMovieById(int id) {
        Optional<FavouriteMovie> favouriteMovie = favouriteMovies.getValue().stream().filter(it -> it.getId() == id).findFirst();
        return favouriteMovie.orElse(null);
    }

    public void insertFavouriteMovie(FavouriteMovie movie) {
        favouritesRef.child(user.getId()).child(String.valueOf(movie.getId())).setValue(movie);
    }

    public void deleteFavouriteMovie(FavouriteMovie movie) {
        favouritesRef.child(user.getId()).child(String.valueOf(movie.getId())).removeValue();
    }

    public LiveData<List<FavouriteMovie>> getFavouritesLiveData() {
        return favouriteMovies;
    }

    public void loadReviews(int id, String lang) {
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        compositeDisposable = new CompositeDisposable();
        Disposable disposable = apiService.getReviews(id, API_KEY, lang)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReviewResponse>() {
                    @Override
                    public void accept(ReviewResponse reviewResponse) throws Exception {
                        ArrayList<Review> reviews = new ArrayList<>();
                        reviews.addAll(reviewResponse.getReviews());
                        reviewsLiveData.setValue(reviews);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        compositeDisposable.add(disposable);
    }

    public void loadTrailers(int id, String lang) {
        ApiFactory apiFactory = ApiFactory.getInstance();
        ApiService apiService = apiFactory.getApiService();
        compositeDisposable = new CompositeDisposable();
        Disposable disposable = apiService.getTrailers(id, API_KEY, lang)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<TrailerResponse>() {
                    @Override
                    public void accept(TrailerResponse trailerResponse) throws Exception {
                        ArrayList<Trailer> trailers = new ArrayList<>();
                        trailers.addAll(trailerResponse.getTrailers());
                        trailersLiveData.setValue(trailers);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        compositeDisposable.add(disposable);
    }

    public LiveData<List<Review>> getReviewsLiveData(int id, String lang) {
        loadReviews(id, lang);
        return reviewsLiveData;
    }

    public LiveData<List<Trailer>> getTrailersLiveData(int id, String lang) {
        loadTrailers(id, lang);
        return trailersLiveData;
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

    //    private static class GetFavouriteMovieTask extends AsyncTask<Integer, Void, FavouriteMovie> {
//        @Override
//        protected FavouriteMovie doInBackground(Integer... integers) {
//            if (integers != null && integers.length > 0) {
//                return database.movieDao().getFavouriteMovieById(integers[0]);
//            }
//            return null;
//        }
//    }

    //    private static class DeleteFavouriteMovieTask extends AsyncTask<FavouriteMovie, Void, Void> {
//        @Override
//        protected Void doInBackground(FavouriteMovie... movies) {
//            if (movies != null && movies.length > 0) {
//                database.movieDao().deleteFavouriteMovie(movies[0]);
//            }
//            return null;
//        }
//    }
//
//    private static class InsertFavouriteMovieTask extends AsyncTask<FavouriteMovie, Void, Void> {
//        @Override
//        protected Void doInBackground(FavouriteMovie... movies) {
//            if (movies != null && movies.length > 0) {
//                database.movieDao().insertFavouriteMovie(movies[0]);
//            }
//            return null;
//        }
//    }
}
