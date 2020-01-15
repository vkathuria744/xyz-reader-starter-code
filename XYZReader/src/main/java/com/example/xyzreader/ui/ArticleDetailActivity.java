package com.example.xyzreader.ui;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ShareCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toolbar;


import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.flaviofaria.kenburnsview.KenBurnsView;


/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        supportPostponeEnterTransition();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        if (!getResources().getBoolean(R.bool.isLand)) {
            hideStatusBarOnly();
        }


        getSupportLoaderManager().initLoader(2, null, this);

        if (getIntent() != null && getIntent().getData() != null) {
            mStartId = ItemsContract.Items.getItemId(getIntent().getData());
        }

        setupUI();

    }


    private void setupUI() {

        String title = getIntent().getStringExtra("title");
        String imageUrl = getIntent().getStringExtra("image");

        final String author = getIntent().getStringExtra("author");

        FloatingActionButton share_fab = findViewById(R.id.share_fab);

        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.fab_state_list_anim);
        animatorSet.setTarget(share_fab);
        animatorSet.start();

        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                .setType("text/plain")
                        .setText(String.format("hello read this", author))
                        .getIntent(), "Share"));

            }


        });

        CollapsingToolbarLayout titleView = findViewById(R.id.article_title);

        if (!getResources().getBoolean(R.bool.isLand))  {
            titleView.setTitle(title);

        }

        final KenBurnsView mPhotoView = findViewById(R.id.poster);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !getResources().getBoolean(R.bool.isLand))  {
            mPhotoView.setTransitionName(getIntent().getStringExtra("image-transition"));

        }

        if (!getResources().getBoolean(R.bool.isLand))  {
            mPhotoView.pause();

            prepareImage(mPhotoView, imageUrl);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPhotoView.resume();
                }

            }, 1000);
        }

    }

    @Override
    public void onBackPressed() {

        supportFinishAfterTransition();
        super.onBackPressed();

    }

    @Override
    public boolean onSupportNavigateUp()  {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        int itemThatWasClicked = item.getItemId();

        switch (itemThatWasClicked)  {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.home:
                onBackPressed();
                return true;

                default:
                    break;



        }

        return super.onOptionsItemSelected(item);

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)  {
        return ArticleLoader.newAllArticlesInstance(this);

    }

    @Override
    public void onLoadFinished(androidx.loader.content.Loader<Object> loader, Object data) {

    }

    @Override
    public void onLoaderReset(androidx.loader.content.Loader<Object> loader) {

    }

    @Override

    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursor = data;

        if (mStartId > 0)  {
            mCursor.moveToFirst();

            while (!mCursor.isAfterLast())  {

                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    break;
                }

                mCursor.moveToNext();
            }


            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_article_detail, ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID)))
                    .commit();

            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader)  {
        mCursor = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(2, null, this);
    }

    @Override
    protected void onStop()  {
        super.onStop();

        getSupportLoaderManager().destroyLoader(2);


    }

    public void hideStatusBarOnly() {
        Window decorWindow = getWindow();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  {
            decorWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            decorWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            decorWindow.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void prepareImage(final ImageView photoView, String imageUrl) {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(imageUrl)
                .listener(new RequestListener<Bitmap>()  {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource)  {

                        onScheduledStartPostponedTransition(photoView);
                        return false;
                    }
                })
                .into(photoView);
    }

    public void onScheduledStartPostponedTransition(final ImageView photoView) {

        photoView.getViewTreeObserver().addOnPreDrawListener(

                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {

                        photoView.getViewTreeObserver().removeOnPreDrawListener(this);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  {
                            startPostponedEnterTransition();
                        }

                        return false;
                    }
                });



    }

}
