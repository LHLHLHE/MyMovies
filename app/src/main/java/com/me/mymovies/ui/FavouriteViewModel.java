package com.me.mymovies.ui;

import android.app.Application;
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
import com.me.mymovies.data.FavouriteMovie;
import com.me.mymovies.data.MovieDatabase;
import com.me.mymovies.data.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FavouriteViewModel extends AndroidViewModel {

    private MutableLiveData<List<FavouriteMovie>> favouriteMovies = new MutableLiveData<>(new ArrayList<>());
    private User user;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference users;
    private DatabaseReference favouritesRef;

    public FavouriteViewModel(@NonNull Application application) {
        super(application);
        users = db.getReference("Users");
        favouritesRef = db.getReference("Favourites");
        //favouriteMovies = database.movieDao().getAllFavouriteMovies();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        getUser();
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

    public LiveData<List<FavouriteMovie>> getFavouritesLiveData() {
        return favouriteMovies;
    }


}
