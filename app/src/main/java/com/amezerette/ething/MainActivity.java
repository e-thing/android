package com.amezerette.ething;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.view.KeyEvent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import 	android.widget.Toast;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private WebView webView;
    private String rootUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate() Restoring previous state");
            /* restore state */
        } else {
            Log.d(TAG, "onCreate() No saved state available");
            /* initialize app */
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerStateChanged(int arg0) {}

            @Override
            public void onDrawerSlide(View arg0, float arg1) {}

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.d("DEBUG", "onDrawerOpened "+webView.getUrl());
                String fragment = Uri.parse(webView.getUrl()).getFragment();
                String page = "";
                if(fragment.length()>1 && fragment.charAt(0) == '!'){
                    page = Uri.parse(fragment.substring(1)).getLastPathSegment();
                }
                Log.d("DEBUG", "page = "+page);

                NavigationView navigationView = (NavigationView) drawerView.findViewById(R.id.nav_view);
                Menu menu = navigationView.getMenu();
                int id = -1;

                if (page.equals("dashboard")) {
                    id = R.id.nav_dashboard;
                } else if (page.equals("devices")) {
                    id = R.id.nav_devices;
                } else if (page.equals("data")) {
                    id = R.id.nav_data;
                } else if (page.equals("apps")) {
                    id = R.id.nav_apps;
                } else if (page.equals("settings")) {
                    id = R.id.nav_settings;
                } else if (page.equals("rules")) {
                    id = R.id.nav_rules;
                }

                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    if (item.isChecked()) {
                        item.setChecked(false);
                    }
                }

                if(id!=-1) {
                    MenuItem m = menu.findItem(id);
                    if (m != null) {
                        Log.d("DEBUG", "check");
                        m.setChecked(true);
                    }
                }
            }

            @Override
            public void onDrawerClosed(View arg0) {}
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Log.d(TAG, "onResume()");

        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_general_file_key),
                MODE_PRIVATE);

        String url = preferences.getString("url", "");

        if(url.isEmpty()){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            if(url != rootUrl){
                // the url changed !
                if(rootUrl.length()>0){
                    // remove previous site
                    webView.clearHistory();
                    webView.clearCache(true);
                }
                rootUrl = url;
                webView.loadUrl(Uri.parse(rootUrl).buildUpon().appendQueryParameter("app", "android").build().toString());
            } else {
                Log.d(TAG, "UI.startRefresh()");
                webView.loadUrl("javascript:UI.startRefresh()");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first

        Log.d(TAG, "onStop()");
        Log.d(TAG, "UI.stopRefresh()");
        webView.loadUrl("javascript:UI.stopRefresh()");
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals(Uri.parse(rootUrl).getHost())) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
            //Your code to do
            Toast.makeText(MainActivity.this, "Your Internet Connection may not be active or the Ething url provided is invalid", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        TextView navHeaderSubtitle = (TextView) findViewById(R.id.nav_header_subtitle);
        navHeaderSubtitle.setText(rootUrl);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_refresh) {
            webView.reload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            webView.loadUrl("javascript:UI.go('dashboard')");
        } else if (id == R.id.nav_devices) {
            webView.loadUrl("javascript:UI.go('devices')");
        } else if (id == R.id.nav_data) {
            webView.loadUrl("javascript:UI.go('data')");
        } else if (id == R.id.nav_apps) {
            webView.loadUrl("javascript:UI.go('apps')");
        } else if (id == R.id.nav_settings) {
            webView.loadUrl("javascript:UI.go('settings')");
        } else if (id == R.id.nav_rules) {
            webView.loadUrl("javascript:UI.go('rules')");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getClientUrl(String page) {
        Builder clientUri = Uri.parse(rootUrl).buildUpon();
        clientUri.appendPath("client").appendPath("index.html");
        return clientUri.build().toString().concat("#!").concat(page);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }


}
