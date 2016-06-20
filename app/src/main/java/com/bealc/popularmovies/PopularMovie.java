package com.bealc.popularmovies;

/**
 * Created by maile on 6/4/2016.
 * This PopularMovie class is to be used by the custom MovieAdapter class.
 */
public class PopularMovie {
    private String mId;
    private String mtitle;
    private String mOverview;
    private String mReleaseDate;
    private String mPosterPath;
    private String mVoteAverage;

    public PopularMovie(String id, String originalTitle, String overview, String releaseDate, String posterPath, String voteAverage) {
        this.mId = id;
        this.mtitle = originalTitle;
        this.mOverview = overview;
        this.mReleaseDate = releaseDate;
        this.mPosterPath = posterPath;
        this.mVoteAverage = voteAverage;
    }

    public String getmId() {
        return mId;
    }

    public String getmTitle() {
        return mtitle;
    }

    public String getmOverview() {
        return mOverview;
    }

    public String getmReleaseDate() {
        return mReleaseDate;
    }

    public String getmPosterPath() {
        return mPosterPath;
    }

    public String getmVoteAverage() {
        return mVoteAverage;
    }
}
