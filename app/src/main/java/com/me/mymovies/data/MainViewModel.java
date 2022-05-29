package com.me.mymovies.data;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Build;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class MainViewModel extends AndroidViewModel {

    private static MovieDatabase database;
    private LiveData<List<Movie>> movies;
    private MutableLiveData<List<FavouriteMovie>> favouriteMovies = new MutableLiveData<>(new ArrayList<>());
    private User user;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference users;
    private DatabaseReference favouritesRef;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(getApplication());
        users = db.getReference("Users");
        favouritesRef = db.getReference("Favourites");
        movies = database.movieDao().getAllMovies();
        //favouriteMovies = database.movieDao().getAllFavouriteMovies();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public FavouriteMovie getFavouriteMovieById(int id) {
        Optional<FavouriteMovie> favouriteMovie = favouriteMovies.getValue().stream().filter(it -> it.getId() == id).findFirst();
        return favouriteMovie.orElse(null);
    }

    public LiveData<List<FavouriteMovie>> getFavouritesLiveData() {
        return favouriteMovies;
    }

    public void deleteAllMovies() {
        new DeleteMoviesTask().execute();
    }

    public void insertMovie(Movie movie) {
        new InsertMovieTask().execute(movie);
    }

    public void deleteMovie(Movie movie) {
        new DeleteMovieTask().execute(movie);
    }

    public void insertFavouriteMovie(FavouriteMovie movie) {
        favouritesRef.child(user.getId()).child(String.valueOf(movie.getId())).setValue(movie);
    }

    public void deleteFavouriteMovie(FavouriteMovie movie) {
        favouritesRef.child(user.getId()).child(String.valueOf(movie.getId())).removeValue();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

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

    private static class DeleteMovieTask extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().deleteMovie(movies[0]);
            }
            return null;
        }
    }

    private static class InsertMovieTask extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            if (movies != null && movies.length > 0) {
                database.movieDao().insertMovie(movies[0]);
            }
            return null;
        }
    }

    private static class DeleteMoviesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            database.movieDao().deleteAllMovies();
            return null;
        }
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

//    private static class GetFavouriteMovieTask extends AsyncTask<Integer, Void, FavouriteMovie> {
//        @Override
//        protected FavouriteMovie doInBackground(Integer... integers) {
//            if (integers != null && integers.length > 0) {
//                return database.movieDao().getFavouriteMovieById(integers[0]);
//            }
//            return null;
//        }
//    }
}
