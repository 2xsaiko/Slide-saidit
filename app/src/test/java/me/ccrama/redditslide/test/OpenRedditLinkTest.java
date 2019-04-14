package me.ccrama.redditslide.test;

import org.junit.Test;

import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.OpenRedditLink.RedditLinkType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class OpenRedditLinkTest {

    // Less characters
    private String formatURL(String url) {
        return OpenRedditLink.formatRedditUrl(url);
    }
    private OpenRedditLink.RedditLinkType getType(String url) {
        return OpenRedditLink.getRedditLinkType(url);
    }

    // @Test
    // public void detectsShortened() {
    //     assertThat(getType(formatURL("https://redd.it/eorhm/")), is(RedditLinkType.SHORTENED));
    // }

    @Test
    public void detectsWiki() {
        assertThat(getType(formatURL("https://www.saidit.net/s/Android/wiki/index")), is(RedditLinkType.WIKI));
        assertThat(getType(formatURL("https://www.saidit.net/s/Android/help")), is(RedditLinkType.WIKI));
        assertThat(getType(formatURL("https://saidit.net/help")), is(RedditLinkType.WIKI));
    }

    @Test
    public void detectsComment() {
        assertThat(getType(formatURL("https://www.saidit.net/s/announcements/comments/eorhm/reddit_30_less_typing/c19qk6j")),
                is(RedditLinkType.COMMENT_PERMALINK));
        assertThat(getType(formatURL("https://www.saidit.net/s/announcements/comments/eorhm//c19qk6j")),
                is(RedditLinkType.COMMENT_PERMALINK));
    }

    @Test
    public void detectsSubmission() {
        assertThat(getType(formatURL("https://www.saidit.net/s/announcements/comments/eorhm/reddit_30_less_typing/")),
                is(RedditLinkType.SUBMISSION));
    }

    @Test
    public void detectsSubmissionWithoutSub() {
        assertThat(getType(formatURL("https://www.saidit.net/comments/eorhm/reddit_30_less_typing/")),
                is(RedditLinkType.SUBMISSION_WITHOUT_SUB));
    }

    @Test
    public void detectsSubreddit() {
        assertThat(getType(formatURL("https://www.saidit.net/s/android")), is(RedditLinkType.SUBREDDIT));
    }

    @Test
    public void detectsSearch() {
//        assertThat(getType(formatURL("https://www.saidit.net/search?q=test")),
//                is(RedditLinkType.SEARCH));
        assertThat(getType(formatURL("https://www.saidit.net/s/Android/search?q=test&restrict_sr=on&sort=relevance&t=all")),
                is(RedditLinkType.SEARCH));
    }

    @Test
    public void detectsUser() {
        assertThat(getType(formatURL("https://www.saidit.net/u/l3d00m")), is(RedditLinkType.USER));
    }

    @Test
    public void detectsHome() {
        assertThat(getType(formatURL("https://www.saidit.net/")), is(RedditLinkType.HOME));
    }

    @Test
    public void detectsOther() {
        assertThat(getType(formatURL("https://www.saidit.net/s/pics/about/moderators")), is(RedditLinkType.OTHER));
        assertThat(getType(formatURL("https://www.saidit.net/live/x9gf3donjlkq/discussions")), is(RedditLinkType.OTHER));
        assertThat(getType(formatURL("https://www.saidit.net/live/x9gf3donjlkq/contributors")), is(RedditLinkType.OTHER));
    }

    @Test
    public void formatsBasic() {
        assertThat(formatURL("https://www.saidit.net/live/wbjbjba8zrl6"), is("saidit.net/live/wbjbjba8zrl6"));
    }

    @Test
    public void formatsNp() {
        assertThat(formatURL("https://np.saidit.net/live/wbjbjba8zrl6"), is("npsaidit.net/live/wbjbjba8zrl6"));
    }

    @Test
    public void formatsSubdomains() {
        assertThat(formatURL("https://beta.saidit.net/"), is(""));
        assertThat(formatURL("https://blog.saidit.net/"), is(""));
        assertThat(formatURL("https://code.saidit.net/"), is(""));
        // https://www.reddit.com/r/modnews/comments/4z2nic/upcoming_change_updates_to_modredditcom/
        assertThat(formatURL("https://mod.saidit.net/"), is(""));
        // https://www.reddit.com/r/changelog/comments/49jjb7/reddit_change_click_events_on_outbound_links/
        assertThat(formatURL("https://out.saidit.net/"), is(""));
        assertThat(formatURL("https://store.saidit.net/"), is(""));
        assertThat(formatURL("https://pay.saidit.net/"), is("saidit.net"));
        assertThat(formatURL("https://ssl.saidit.net/"), is("saidit.net"));
        assertThat(formatURL("https://en-gb.saidit.net/"), is("saidit.net"));
        assertThat(formatURL("https://us.saidit.net/"), is("saidit.net"));
    }

    @Test
    public void formatsSubreddit() {
        assertThat(formatURL("/s/android"), is("saidit.net/s/android"));
        assertThat(formatURL("https://android.saidit.net"), is("saidit.net/s/android"));
    }

    @Test
    public void formatsWiki() {
        assertThat(formatURL("https://saidit.net/help"), is("saidit.net/s/saidit/wiki"));
        assertThat(formatURL("https://saidit.net/help/registration"), is("saidit.net/s/saidit/wiki/registration"));
        assertThat(formatURL("https://www.saidit.net/s/android/wiki/index"), is("saidit.net/s/android/wiki/index"));
    }

    @Test
    public void formatsProtocol() {
        assertThat(formatURL("http://saidit.net"), is("saidit.net"));
        assertThat(formatURL("https://saidit.net"), is("saidit.net"));
    }
}