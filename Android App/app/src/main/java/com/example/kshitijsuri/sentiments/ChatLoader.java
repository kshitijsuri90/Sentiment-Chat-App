package com.example.kshitijsuri.sentiments;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by Labtop on 18/02/18.
 */

public class ChatLoader extends AsyncTaskLoader<String> {

   // private static String REQUEST_URL ="http://content.guardianapis.com/search?q=debates&api-key=test";
    String mURL ;
    String data;

    public ChatLoader(Context context, String url, String data) {
        super(context);
        this.mURL = url;
        this.data = data;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        if (mURL == null) {
            return null;
        }

        return QueryUtils.fetchNewsData(mURL,data);
    }
}