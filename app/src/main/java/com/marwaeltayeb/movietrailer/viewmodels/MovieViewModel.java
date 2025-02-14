package com.marwaeltayeb.movietrailer.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import com.marwaeltayeb.movietrailer.models.Movie;
import com.marwaeltayeb.movietrailer.network.MovieDataSource;
import com.marwaeltayeb.movietrailer.network.MovieDataSourceFactory;
import com.marwaeltayeb.movietrailer.network.MovieService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

import static com.marwaeltayeb.movietrailer.network.MovieDataSourceFactory.movieDataSource;

/**
 * Created by Marwa on 7/10/2019.
 */
@HiltViewModel
public class MovieViewModel extends ViewModel {

    // Create liveData for PagedList and PagedKeyedDataSource
    public LiveData<PagedList<Movie>> moviePagedList;
    private LiveData<PageKeyedDataSource<Integer, Movie>> liveDataSource;

    // Constructor
    @Inject
    public MovieViewModel(MovieService movieService) {

        // Get our database source factory
        MovieDataSourceFactory movieDataSourceFactory = new MovieDataSourceFactory(movieService);

        // Get the live database source from database source factory
        liveDataSource = movieDataSourceFactory.getMovieLiveDataSource();

        // Get PagedList configuration
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(MovieDataSource.PAGE_SIZE).build();

        // Build the paged list
        moviePagedList = (new LivePagedListBuilder(movieDataSourceFactory, pagedListConfig)).build();
    }

    public void invalidateDataSource(){
        if(movieDataSource != null) movieDataSource.invalidate();
    }
}
