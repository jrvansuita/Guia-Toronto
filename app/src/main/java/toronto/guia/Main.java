package toronto.guia;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class Main extends AppCompatActivity {

    private FrameLayout frameContent;
    private WebView webView;
    private ProgressBar progressBar;
    private SwipeToRefreshLayout swipe;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        bind();
        listeners();
        defaults();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                open();
            }
        }, 3000);
    }


    private void bind() {
        frameContent = (FrameLayout) findViewById(R.id.content);
        webView = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        swipe = (SwipeToRefreshLayout) findViewById(R.id.swipe);
    }

    private void listeners() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().contains(getUrl())) {
                    return false;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);

                if (progress == 100) {
                    swipe.setRefreshing(false);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            swipe.setRefreshing(false);
                        }
                    }, 1000);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkNetwork();

                if (hasInternet(Main.this))
                    webView.reload();
            }
        });
    }

    private void defaults() {
        webView.setVisibility(View.GONE);
        webView.getSettings().setJavaScriptEnabled(true);

        progressBar.setProgress(0);
        swipe.setScrollableView(webView);
        swipe.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorAccent);
    }

    private String getUrl() {
        return getString(R.string.url);
    }

    private void open() {
        checkNetwork();

        if (hasInternet(this)) {
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(getString(R.string.protocol) + getUrl());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkNetwork();
    }

    private void checkNetwork() {
        if (!hasInternet(this)) {
            snackbar = Snackbar.make(frameContent, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(ContextCompat.getColor(this, (android.R.color.white)))
                    .setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            open();
                        }
                    });

            snackbar.show();
        }
    }

    public static boolean hasInternet(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager con = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (con == null) {
            return false;
        }

        NetworkInfo inf = con.getActiveNetworkInfo();

        return inf != null && inf.isConnected();
    }

}
