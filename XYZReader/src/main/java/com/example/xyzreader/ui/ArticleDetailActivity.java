package com.example.xyzreader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity {

    private static final String TAG = "ArticleDetailActivity";

    public static final String ARG_ITEM = "item";
    public static final String ARG_ITEM_IMAGE = "item_image";
    public static final String ARG_ITEM_TITLE = "item_title";
    public static final String ARG_ITEM_AUTHOR = "item_author";
    public static final String ARG_ITEM_DATE = "item_date";
    public static final String ARG_ITEM_BODY = "item_body";

    private Bundle mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_article_detail);
        supportPostponeEnterTransition();
        mData = getIntent().getBundleExtra(ARG_ITEM);
        bindViews();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
    }

    private void scheduleTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });
    }

    private void bindViews() {
        if (mData != null) {
            final ImageView photo = (ImageView) findViewById(R.id.photo);
            Picasso.with(this).load(mData.getString(ARG_ITEM_IMAGE))
                    .into(photo, new Callback() {
                        @Override
                        public void onSuccess() {
                            scheduleTransition(photo);
                        }

                        @Override
                        public void onError() {
                            supportStartPostponedEnterTransition();
                        }
                    });
            String title = mData.getString(ARG_ITEM_TITLE);
            CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
            if(collapsingToolbarLayout != null) {
                collapsingToolbarLayout.setTitle(title);
                collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
            }
            TextView titleView = (TextView) findViewById(R.id.article_title);
            TextView date = (TextView) findViewById(R.id.article_date);
            TextView author = (TextView) findViewById(R.id.article_author);
            TextView bodyView = (TextView) findViewById(R.id.article_body);
            titleView.setText(title);
            date.setText(DateUtils.getRelativeTimeSpanString(
                    mData.getLong(ARG_ITEM_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString());
            author.setText(mData.getString(ARG_ITEM_AUTHOR));
            bodyView.setText(Html.fromHtml(mData.getString(ARG_ITEM_BODY)));
        }
    }
}
