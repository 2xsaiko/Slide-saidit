package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.PublicContribution;

/**
 * Created by ccrama on 9/19/2015.
 */
public class Vote extends AsyncTask<PublicContribution, Void, Void> {

    private final Vote.Type type;
    private final boolean on;
    private View v;
    private Context c;

    public Vote(Vote.Type type, boolean on, View v, Context c) {
        this.type = type;
        this.on = on;
        this.v = v;
        this.c = c;
        Reddit.setDefaultErrorHandler(c);
    }

    // public Vote(View v, Context c) {
    //     direction = VoteDirection.NO_VOTE;
    //
    //     this.v = v;
    //     this.c = c;
    // }

    @Override
    protected Void doInBackground(PublicContribution... sub) {
        if (Authentication.isLoggedIn) {
            try {
                switch(type) {
                    case INSIGHTFUL:
                        new AccountManager(Authentication.reddit).voteInsightful(sub[0], on);
                        break;
                    case FUN:
                        new AccountManager(Authentication.reddit).voteFun(sub[0], on);
                        break;
                }
            } catch (ApiException | RuntimeException e) {
                ((Activity) c).runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if (v != null && c != null && v.getContext() != null) {
                                Snackbar s = Snackbar.make(v, R.string.vote_err, Snackbar.LENGTH_SHORT);
                                View view = s.getView();
                                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                tv.setTextColor(Color.WHITE);
                                s.show();
                            }
                        } catch (Exception ignored) {

                        }
                        c = null;
                        v = null;
                    }
                });
                e.printStackTrace();
            }
        } else {
            ((Activity) c).runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        if (v != null && c != null && v.getContext() != null) {
                            Snackbar s = Snackbar.make(v, R.string.vote_err_login, Snackbar.LENGTH_SHORT);
                            View view = s.getView();
                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();

                        }
                    } catch (Exception ignored) {

                    }
                    c = null;
                    v = null;
                }
            });
        }


        return null;
    }

    public enum Type {
        INSIGHTFUL,
        FUN,
    }

}

