package com.marwaeltayeb.movietrailer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.marwaeltayeb.movietrailer.R;

import dagger.hilt.android.AndroidEntryPoint;

import static com.marwaeltayeb.movietrailer.R.id.webView;
import static com.marwaeltayeb.movietrailer.utils.Constant.URL_OF_REVIEW;

@AndroidEntryPoint
public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        final ProgressBar loadingIndicator = findViewById(R.id.indicator);
        loadingIndicator.setVisibility(View.VISIBLE);
        WebView web = findViewById(webView);

        Intent intent = getIntent();
        String url = intent.getStringExtra(URL_OF_REVIEW);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl(url);

        web.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }
}
