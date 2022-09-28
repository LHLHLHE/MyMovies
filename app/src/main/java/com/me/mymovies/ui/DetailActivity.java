package com.me.mymovies.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.me.mymovies.R;
import com.me.mymovies.adapters.ReviewAdapter;
import com.me.mymovies.adapters.TrailerAdapter;
import com.me.mymovies.data.FavouriteMovie;
import com.me.mymovies.data.Movie;
import com.me.mymovies.data.Review;
import com.me.mymovies.data.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageViewBigPoster;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewOverview;
    private ImageView imageViewAddToFavourite;
    private ScrollView scrollViewInfo;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;

    private int id;
    private Movie movie;
    private FavouriteMovie favouriteMovie;

    private DetailViewModel detailViewModel;

    private static String lang;

    private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/";
    private static final String BIG_POSTER_SIZE = "w780";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itemMain:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.itemFavourite:
                Intent intentToFavourite = new Intent(this, FavouriteActivity.class);
                startActivity(intentToFavourite);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        detailViewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lang = Locale.getDefault().getLanguage();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else {
            finish();
        }
        setFavourite();
        imageViewBigPoster = findViewById(R.id.imageViewBigPoster);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewRating = findViewById(R.id.textViewRating);
        textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        textViewOverview = findViewById(R.id.textViewOverview);
        imageViewAddToFavourite = findViewById(R.id.imageViewAddToFavourite);
        scrollViewInfo = findViewById(R.id.scrollViewInfo);
        movie = detailViewModel.getMovieById(id);
        trailerAdapter = new TrailerAdapter();
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
                Intent intentToTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intentToTrailer);
            }
        });
        reviewAdapter = new ReviewAdapter();
        detailViewModel.getReviewsLiveData(movie.getId(), lang).observe(this, new Observer<List<Review>>() {
            @Override
            public void onChanged(List<Review> reviews) {
                ArrayList<Review> reviewArrayList = new ArrayList<>();
                if (reviews != null) {
                    reviewArrayList.addAll(reviews);
                    reviewAdapter.setReviews(reviewArrayList);
                }
            }
        });
        detailViewModel.getTrailersLiveData(movie.getId(), lang).observe(this, new Observer<List<Trailer>>() {
            @Override
            public void onChanged(List<Trailer> trailers) {
                ArrayList<Trailer> trailerArrayList = new ArrayList<>();
                if (trailers != null) {
                    trailerArrayList.addAll(trailers);
                    trailerAdapter.setTrailers(trailerArrayList);
                }
            }
        });
        setTitle(movie.getTitle());
        Picasso.get().load(BASE_POSTER_URL + BIG_POSTER_SIZE + movie.getPosterPath()).into(imageViewBigPoster);
        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewRating.setText(Double.toString(movie.getVoteAverage()));
        textViewReleaseDate.setText(movie.getReleaseDate());
        textViewOverview.setText(movie.getOverview());
        recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setAdapter(trailerAdapter);
        recyclerViewReviews.setAdapter(reviewAdapter);
        scrollViewInfo.smoothScrollTo(0, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void onClickChangeFavourite(View view) {
        if (favouriteMovie == null) {
            detailViewModel.insertFavouriteMovie(new FavouriteMovie(movie));
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
        } else {
            detailViewModel.deleteFavouriteMovie(favouriteMovie);
            Toast.makeText(this, R.string.removed_from_favourites, Toast.LENGTH_SHORT).show();
        }
        setFavourite();
    }

    private void setFavourite() {
        detailViewModel.getFavouritesLiveData().observe(this, favouriteMovies1 -> {
            favouriteMovie = detailViewModel.getFavouriteMovieById(id);
            if (favouriteMovie == null) {
                imageViewAddToFavourite.setImageResource(R.drawable.favourite_add_to);
            } else {
                imageViewAddToFavourite.setImageResource(R.drawable.favourite_remove);
            }
        });
    }
}