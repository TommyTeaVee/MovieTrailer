package com.marwaeltayeb.movietrailer.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.marwaeltayeb.movietrailer.R;
import com.marwaeltayeb.movietrailer.adapters.ReviewAdapter;
import com.marwaeltayeb.movietrailer.adapters.TrailerAdapter;
import com.marwaeltayeb.movietrailer.database.MovieRoomViewModel;
import com.marwaeltayeb.movietrailer.databinding.ActivityMovieBinding;
import com.marwaeltayeb.movietrailer.models.Movie;
import com.marwaeltayeb.movietrailer.utils.Genres;
import com.marwaeltayeb.movietrailer.utils.SharedPreferencesUtils;
import com.marwaeltayeb.movietrailer.viewmodels.ReviewViewModel;
import com.marwaeltayeb.movietrailer.viewmodels.TrailerViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

import static com.marwaeltayeb.movietrailer.R.id.listOfReviews;
import static com.marwaeltayeb.movietrailer.activities.FavoriteActivity.isFavoriteActivityRunning;
import static com.marwaeltayeb.movietrailer.utils.Constant.IMAGE_URL;
import static com.marwaeltayeb.movietrailer.utils.Constant.MOVIE;
import static com.marwaeltayeb.movietrailer.utils.DateUtils.formatDate;

@AndroidEntryPoint
public class MovieActivity extends AppCompatActivity {

    private ActivityMovieBinding binding;

    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;
    private RecyclerView reviewsRecyclerView, trailersRecyclerView;

    private ReviewViewModel reviewViewModel;
    private TrailerViewModel trailerViewModel;
    private MovieRoomViewModel movieRoomViewModel;

    public static String idOfMovie;
    private String title;
    private String formattedDate;
    private String vote;
    private String description;
    private String language;
    private String poster;
    private String backDrop;
    private ArrayList<Integer> genres;

    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie);

        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        trailerViewModel = new ViewModelProvider(this).get(TrailerViewModel.class);
        movieRoomViewModel = new ViewModelProvider(this).get(MovieRoomViewModel.class);

        setupRecyclerViews();

        receiveMovieDetails();

        getReviews();
        getTrailers();

        binding.fab.setOnClickListener(view -> toggleFavourite());
        binding.txtSeaAll.setOnClickListener(view -> goToSeeAllActivity());
    }

    private void setupRecyclerViews() {
        // Trailers
        trailersRecyclerView = findViewById(R.id.listOfTrailers);
        trailersRecyclerView.setHasFixedSize(true);
        trailersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        trailerAdapter = new TrailerAdapter(this);
        trailersRecyclerView.setAdapter(trailerAdapter);

        // Reviews
        reviewsRecyclerView = findViewById(listOfReviews);
        reviewsRecyclerView.setHasFixedSize(true);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        reviewAdapter = new ReviewAdapter();
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void receiveMovieDetails() {
        Intent intent = getIntent();
        movie = (Movie) intent.getSerializableExtra(MOVIE);

        idOfMovie = movie.getMovieId();
        title = movie.getMovieTitle();
        vote = movie.getMovieVote();
        description = movie.getMovieDescription();
        language = movie.getMovieLanguage();
        backDrop = movie.getMovieBackdrop();
        poster = movie.getMoviePoster();
        genres = movie.getGenreIds();

        binding.titleOfMovie.setText(title);
        binding.ratingOfMovie.setText(vote);
        binding.descriptionOfMovie.setText(description);

        if (isFavoriteActivityRunning) {
            binding.releaseDateOfMovie.setText(movie.getMovieReleaseDate());
        } else {
            formattedDate = getString(R.string.date, formatDate(movie.getMovieReleaseDate()));
            binding.releaseDateOfMovie.setText(formattedDate);
        }

        binding.languageOfMovie.setText(language);

        Glide.with(this)
                .load(IMAGE_URL + backDrop)
                .into(binding.backdropImage);

        getGenres();

        if (!isNetworkConnected()) {
            trailersRecyclerView.setVisibility(View.GONE);
            binding.noTrailers.setVisibility(View.VISIBLE);

            reviewsRecyclerView.setVisibility(View.GONE);
            binding.noReviews.setVisibility(View.VISIBLE);
        }

        // If movie is inserted
        if (SharedPreferencesUtils.getInsertState(this, idOfMovie)) {
            binding.fab.setImageResource(R.drawable.favorite_red);
        }
    }

    public void getTrailers() {
        trailerViewModel.getAllTrailers().observe(this, trailers -> {
            trailerAdapter.submitList(trailers);

            if (trailers != null && trailers.isEmpty()) {
                trailersRecyclerView.setVisibility(View.GONE);
                binding.noTrailers.setVisibility(View.VISIBLE);
            }
        });
    }

    public void getReviews() {
        reviewViewModel.getAllReviews().observe(this, reviews -> {
            reviewAdapter.submitList(reviews);

            if (reviews != null && reviews.isEmpty()) {
                reviewsRecyclerView.setVisibility(View.GONE);
                binding.noReviews.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getGenres() {
        int genre_one = 0;
        int genre_two = 0;
        int genre_three = 0;

        try {
            // get keys
            genre_one = Integer.parseInt(String.valueOf(movie.getGenreIds().get(0)));
            genre_two = Integer.parseInt(String.valueOf(movie.getGenreIds().get(1)));
            genre_three = Integer.parseInt(String.valueOf(movie.getGenreIds().get(2)));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // get values
        binding.genreOne.setText(Genres.getRealGenres().get(genre_one));
        binding.genreTwo.setText(Genres.getRealGenres().get(genre_two));
        binding.genreThree.setText(Genres.getRealGenres().get(genre_three));
    }

    private void toggleFavourite() {
        // If movie is not bookmarked
        if (!SharedPreferencesUtils.getInsertState(this, idOfMovie)) {
            binding.fab.setImageResource(R.drawable.favorite_red);
            insertFavoriteMovie();
            SharedPreferencesUtils.setInsertState(this, idOfMovie, true);
            showSnackBar("Bookmark Added");
        } else {
            binding.fab.setImageResource(R.drawable.favorite_border_red);
            deleteFavoriteMovieById();
            SharedPreferencesUtils.setInsertState(this, idOfMovie, false);
            showSnackBar("Bookmark Removed");
        }
    }

    private void insertFavoriteMovie() {
        movie = new Movie(idOfMovie, title, vote, description, formattedDate, language, poster, backDrop, genres);
        movieRoomViewModel.insert(movie);
    }

    private void deleteFavoriteMovieById() {
        movieRoomViewModel.deleteById(Integer.parseInt(idOfMovie));
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void showSnackBar(String text) {
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show();
    }

    private void goToSeeAllActivity() {
        Intent intent = new Intent(this, SeeAllActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return(super.onOptionsItemSelected(item));
    }
}
