package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Arrays;

import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Activities.SendMessage;
import me.ccrama.redditslide.Activities.Submit;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Activities.Wiki;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;

public class OpenRedditLink {

    public OpenRedditLink(Context context, String url) {
        openUrl(context, url, true);
    }

    public OpenRedditLink(Context context, String url, boolean openIfOther) {
        openUrl(context, url, openIfOther);
    }

    //Returns true if link was in fact handled by this method. If false, further action should be taken
    public static boolean openUrl(Context context, String url, boolean openIfOther) {
        String oldUrl = url;
        boolean np = false;

        LogUtil.v("Link is " + url);
        url = formatRedditUrl(url);
        if (url.isEmpty()) {
            LinkUtil.openExternally(oldUrl);
            return false;
        } else if (url.startsWith("np")) {
            np = true;
            url = url.substring(2);
        }

        RedditLinkType type = getRedditLinkType(url);

        String[] parts = url.split("/");
        String endParameters = "";
        if (parts[parts.length - 1].startsWith("?")) {
            endParameters = parts[parts.length - 1];
            parts = Arrays.copyOf(parts, parts.length - 1);
        }

        Intent i = null;
        switch (type) {
            case SHORTENED: {
                i = new Intent(context, CommentsScreenSingle.class);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts[1]);
                break;
            }
            case WIKI: {
                i = new Intent(context, Wiki.class);
                i.putExtra(Wiki.EXTRA_SUBREDDIT, parts[2]);
                String page;
                if (parts.length >= 5) {
                    page = parts[4];
                    if (page.contains("#")) {
                        page = page.substring(0, page.indexOf("#"));
                    }
                    i.putExtra(Wiki.EXTRA_PAGE, page);
                }
                break;
            }
            case SEARCH: {
                i = new Intent(context, Search.class);
                String end = parts[parts.length - 1];
                end = end.replace(":", "%3A");

                boolean restrictSub = end.contains("restrict_sr=on");
                if (restrictSub) {
                    i.putExtra(Search.EXTRA_SUBREDDIT, parts[2]);
                } else {
                    i.putExtra(Search.EXTRA_SUBREDDIT, "all");
                }
                Uri urlParams = Uri.parse(oldUrl);
                if (urlParams.getQueryParameterNames().contains("q")) {
                    i.putExtra(Search.EXTRA_TERM, urlParams.getQueryParameter("q"));
                }
                if (urlParams.getQueryParameterNames().contains("author")) {
                    i.putExtra(Search.EXTRA_AUTHOR, urlParams.getQueryParameter("author"));
                }
                if (urlParams.getQueryParameterNames().contains("nsfw")) {
                    i.putExtra(Search.EXTRA_NSFW, urlParams.getQueryParameter("nsfw").equals("yes"));
                }
                if (urlParams.getQueryParameterNames().contains("self")) {
                    i.putExtra(Search.EXTRA_SELF, urlParams.getQueryParameter("self").equals("yes"));
                }
                if (urlParams.getQueryParameterNames().contains("selftext")) {
                    i.putExtra(Search.EXTRA_SELF, urlParams.getQueryParameter("selftext").equals("yes"));
                }
                if (urlParams.getQueryParameterNames().contains("url")) {
                    i.putExtra(Search.EXTRA_URL, urlParams.getQueryParameter("url"));
                }
                if (urlParams.getQueryParameterNames().contains("site")) {
                    i.putExtra(Search.EXTRA_SITE, urlParams.getQueryParameter("site"));
                }
                break;
            }
            case SUBMIT: {
                i = new Intent(context, Submit.class);
                i.putExtra(Submit.EXTRA_SUBREDDIT, parts[2]);
                Uri urlParams = Uri.parse(oldUrl);
                if (urlParams.getQueryParameterNames().contains("title")) {
                    // Submit already uses EXTRA_SUBJECT for title and EXTRA_TEXT for URL for sharing so we use those
                    i.putExtra(Intent.EXTRA_SUBJECT, urlParams.getQueryParameter("title"));
                }

                boolean isSelfText = false;

                // Reddit behavior: If selftext is true or if selftext doesn't exist and text does exist then page
                // defaults to showing self post page. If selftext is false, or doesn't exist and no text then the page
                // defaults to showing the link post page.
                // We say isSelfText=true for the "no selftext, no text, no url" condition because that's slide's
                // default behavior for the submit page, whereas reddit's behavior would say isSelfText=false.
                if (urlParams.getQueryParameterNames().contains("selftext")
                        && urlParams.getQueryParameter("selftext").equals("true")) {
                    isSelfText = true;
                } else if (!urlParams.getQueryParameterNames().contains("selftext")
                        && (urlParams.getQueryParameterNames().contains("text")
                            || !urlParams.getQueryParameterNames().contains("url"))) {
                    isSelfText = true;
                }

                i.putExtra(Submit.EXTRA_IS_SELF, isSelfText);
                if (urlParams.getQueryParameterNames().contains("text")) {
                    i.putExtra(Submit.EXTRA_BODY, urlParams.getQueryParameter("text"));
                }
                if (urlParams.getQueryParameterNames().contains("url")) {
                    i.putExtra(Intent.EXTRA_TEXT, urlParams.getQueryParameter("url"));
                }
                break;
            }
            case COMMENT_PERMALINK: {
                i = new Intent(context, CommentsScreenSingle.class);
                if (parts[1].equalsIgnoreCase("u") || parts[1].equalsIgnoreCase("user")) {
                    // Prepend u_ because user profile posts are made to /r/u_username
                    i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, "u_" + parts[2]);
                } else {
                    i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, parts[2]);
                }
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts[4]);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                if (parts.length >= 7) {
                    i.putExtra(CommentsScreenSingle.EXTRA_LOADMORE, true);
                    String end = parts[6];
                    String endCopy = end;
                    if (end.contains("?")) end = end.substring(0, end.indexOf("?"));

                    if (end.length() >= 3) i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, end);

                    if (endCopy.contains("?context=") || !endParameters.isEmpty()) {
                        if (!endParameters.isEmpty()) {
                            endCopy = endParameters;
                        }
                        LogUtil.v("Adding end params");
                        try {
                            int contextNumber = Integer.valueOf(
                                    endCopy.substring(endCopy.indexOf("?context=") + 9,
                                            endCopy.length()));
                            i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT_NUMBER, contextNumber);
                        } catch (Exception ignored) {

                        }
                    }
                }
                break;
            }
            case SUBMISSION: {
                i = new Intent(context, CommentsScreenSingle.class);
                if (parts[1].equalsIgnoreCase("u") || parts[1].equalsIgnoreCase("user")) {
                    // Prepend u_ because user profile posts are made to /r/u_username
                    i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, "u_" + parts[2]);
                } else {
                    i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, parts[2]);
                }
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts[4]);
                break;
            }
            case SUBMISSION_WITHOUT_SUB: {
                i = new Intent(context, CommentsScreenSingle.class);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, Reddit.EMPTY_STRING);
                i.putExtra(CommentsScreenSingle.EXTRA_NP, np);
                i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, parts[2]);
                break;
            }
            case SUBREDDIT: {
                i = new Intent(context, SubredditView.class);
                i.putExtra(SubredditView.EXTRA_SUBREDDIT, parts[2]);
                break;
            }
            case MULTIREDDIT: {
                i = new Intent(context, MultiredditOverview.class);
                i.putExtra(MultiredditOverview.EXTRA_PROFILE, parts[2]);
                i.putExtra(MultiredditOverview.EXTRA_MULTI, parts[4]);
                break;
            }
            case MESSAGE: {
                i = new Intent(context, SendMessage.class);
                try {
                    Uri urlParams = Uri.parse(oldUrl);
                    if (urlParams.getQueryParameterNames().contains("to")) {
                        i.putExtra(SendMessage.EXTRA_NAME, urlParams.getQueryParameter("to"));
                    }
                    if (urlParams.getQueryParameterNames().contains("subject")) {
                        i.putExtra(SendMessage.EXTRA_SUBJECT, urlParams.getQueryParameter("subject"));
                    }
                    if (urlParams.getQueryParameterNames().contains("message")) {
                        i.putExtra(SendMessage.EXTRA_MESSAGE, urlParams.getQueryParameter("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case USER: {
                String name = parts[2];
                if (name.equals("me") && Authentication.isLoggedIn) name = Authentication.name;
                i = new Intent(context, Profile.class);
                i.putExtra(Profile.EXTRA_PROFILE, name);
                break;
            }
            case HOME: {
                i = new Intent(context, MainActivity.class);
                break;
            }
            case OTHER: {
                if (openIfOther) {
                    if (context instanceof Activity) {
                        LinkUtil.openUrl(oldUrl, Palette.getStatusBarColor(), (Activity) context);
                    } else {
                        i = new Intent(context, Website.class);
                        i.putExtra(LinkUtil.EXTRA_URL, oldUrl);
                    }
                } else {
                    return false;
                }
                break;
            }
        }
        if (i != null) {
            if (context instanceof OpenContent) {
               // i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
               // i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(i);
        }
        return true;
    }

    public OpenRedditLink(Context c, String submission, String subreddit, String id) {
        Intent i = new Intent(c, CommentsScreenSingle.class);
        i.putExtra(CommentsScreenSingle.EXTRA_SUBREDDIT, subreddit);
        i.putExtra(CommentsScreenSingle.EXTRA_CONTEXT, id);
        i.putExtra(CommentsScreenSingle.EXTRA_SUBMISSION, submission);
        c.startActivity(i);
    }

    /**
     * Takes an reddit.com url and formats it for easier use
     *
     * @param url The url to format
     * @return Formatted url without subdomains, language tags & other unused prefixes
     */
    public static String formatRedditUrl(String url) {

        if(url == null){
            return "";
        }


        if(url.contains("amp.saidit.net")){
            url = url.substring(url.indexOf("amp.saidit.net") + 14, url.length());
        }

        // Strip unused prefixes that don't require special handling
        url = url.replaceFirst("(?i)^(https?://)?(www\\.)?((ssl|pay|amp|old|new)\\.)?", "");

        if (url.matches("(?i)[a-z0-9-_]+\\.saidit\\.net.*")) { // tests for subdomain
            String subdomain = url.split("\\.", 2)[0];
            String domainRegex = "(?i)" + subdomain + "\\.saidit\\.net";
            if (subdomain.equalsIgnoreCase("np")) {
                // no participation link: https://www.reddit.com/r/NoParticipation/wiki/index
                url = url.replaceFirst(domainRegex, "saidit.net");
                url = "np" + url;
            } else if (subdomain.matches("beta|blog|code|mod|out|store")) {
                return "";
            } else if (subdomain.matches("(?i)([_a-z0-9]{2}-)?[_a-z0-9]{1,2}")) {
                /*
                    Either the subdomain is a language tag (with optional region) or
                    a single letter domain, which for simplicity are ignored.
                 */
                url = url.replaceFirst(domainRegex, "saidit.net");
            } else {
                // subdomain is a subreddit, change subreddit.reddit.com to reddit.com/r/subreddit
                url = url.replaceFirst(domainRegex, "saidit.net/s/" + subdomain);
            }
        }

        if (url.startsWith("/")) url = "saidit.net" + url;
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

        // Converts links such as saidit.net/help to saidit.net/r/saidit/wiki
        if (url.matches("(?i)[^/]++/(?>w|wiki|help)(?>$|/.*)")) {
            url = url.replaceFirst("(?i)/(?>w|wiki|help)", "/s/saidit/wiki");
        }

        return url;
    }

    /**
     * Determines the reddit link type
     *
     * @param url saidit.net link
     * @return LinkType
     */
    public static RedditLinkType getRedditLinkType(String url) {
//        if (url.matches("(?i)redd\\.it/\\w+")) {
//             Redd.it link. Format: redd.it/post_id
//            return RedditLinkType.SHORTENED;
//        } else
        if (url.matches("(?i)saidit\\.net/message/compose.*")) {
            return RedditLinkType.MESSAGE;
        } else if (url.matches("(?i)saidit\\.net(?:/s/[a-z0-9-_.]+)?/(?:w|wiki|help).*")) {
            // Wiki link. Format: saidit.net/s/$subreddit/w[iki]/$page [optional]
            return RedditLinkType.WIKI;
        } else if (url.matches("(?i)saidit\\.net/s/[a-z0-9-_.]+/about.*")) {
            // Unhandled link. Format: saidit.net/s/$subreddit/about/$page [optional]
            return RedditLinkType.OTHER;
        } else if (url.matches("(?i)saidit\\.net/s/[a-z0-9-_.]+/search.*")) {
            // Wiki link. Format: saidit.net/s/$subreddit/search?q= [optional]
            return RedditLinkType.SEARCH;
        } else if (url.matches("(?i)saidit\\.net/s/[a-z0-9-_.]+/submit.*")) {
            // Submit post link. Format: saidit.net/s/$subreddit/submit
            return RedditLinkType.SUBMIT;
        } else if (url.matches("(?i)saidit\\.net/(?:s|u(?:ser)?)/[a-z0-9-_.]+/comments/\\w+/[\\w-]*/.*")) {
            // Permalink to comments. Format: saidit.net/s [or u or user]/$subreddit/comments/$post_id/$post_title [can be empty]/$comment_id
            return RedditLinkType.COMMENT_PERMALINK;
        } else if (url.matches("(?i)saidit\\.net/(?:s|u(?:ser)?)/[a-z0-9-_.]+/comments/\\w+.*")) {
            // Submission. Format: saidit.net/s [or u or user]/$subreddit/comments/$post_id/$post_title [optional]
            return RedditLinkType.SUBMISSION;
        } else if (url.matches("(?i)saidit\\.net/comments/\\w+.*")) {
            // Submission without a given subreddit. Format: saidit.net/comments/$post_id/$post_title [optional]
            return RedditLinkType.SUBMISSION_WITHOUT_SUB;
        } else if (url.matches("(?i)saidit\\.net/s/[a-z0-9-_.]+.*")) {
            // Subreddit. Format: saidit.net/r/$subreddit/$sort [optional]
            return RedditLinkType.SUBREDDIT;
        } else if (url.matches("(?i)saidit\\.net/u(?:ser)?/[a-z0-9-_]+.*/m/[a-z0-9_]+.*")) {
            // Multireddit. Format: saidit.net/u [or user]/$username/m/$multireddit/$sort [optional]
            return RedditLinkType.MULTIREDDIT;
        } else if (url.matches("(?i)saidit\\.net/u(?:ser)?/[a-z0-9-_]+.*")) {
            // User. Format: saidit.net/u [or user]/$username/$page [optional]
            return RedditLinkType.USER;
        } else if (url.matches("^saidit\\.net$")) {
            // Saidit home link
            return RedditLinkType.HOME;
        } else {
            //Open all links that we can't open in another app
            return RedditLinkType.OTHER;
        }
    }

    public enum RedditLinkType {
        SHORTENED,
        WIKI,
        COMMENT_PERMALINK,
        SUBMISSION,
        SUBMISSION_WITHOUT_SUB,
        SUBREDDIT,
        USER,
        SEARCH,
        MESSAGE,
        MULTIREDDIT,
        SUBMIT,
        HOME,
        OTHER
    }


}
