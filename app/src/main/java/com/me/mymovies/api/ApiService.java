package com.me.mymovies.api;

import com.me.mymovies.data.MovieResponse;
import com.me.mymovies.data.ReviewResponse;
import com.me.mymovies.data.TrailerResponse;

import io.reactivex.Observable;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("discover/movie")
    Observable<MovieResponse> getMovies(
            @Query("api_key") String apiKey,
            @Query("language") String lang,
            @Query("sort_by") String sortBy,
            @Query("vote_count.gte") int minVoteCount,
            @Query("page") int page
    );

    @GET("movie/{id}/reviews")
    Observable<ReviewResponse> getReviews(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String lang
    );

    @GET("movie/{id}/videos")
    Observable<TrailerResponse> getTrailers(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String lang
    );
}
