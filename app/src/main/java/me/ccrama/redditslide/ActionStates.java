package me.ccrama.redditslide;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by carlo_000 on 2/26/2016.
 */
public class ActionStates {
    public static final Map<String, VoteState> votedFullnames = new HashMap<>();

    public static final ArrayList<String> savedFullnames = new ArrayList<>();
    public static final ArrayList<String> unSavedFullnames = new ArrayList<>();

    public static VoteState getVoteState(PublicContribution s) {
        if (!votedFullnames.containsKey(s.getFullName())) return s.getVote();

        VoteState state = votedFullnames.get(s.getFullName());
        if (state == null) return s.getVote();

        return state;
    }

    public static void setVoteState(PublicContribution s, VoteState direction) {
        votedFullnames.put(s.getFullName(), direction);
    }

    public static boolean isSaved(Submission s) {
        if (savedFullnames.contains(s.getFullName())) {
            return true;
        } else if (unSavedFullnames.contains(s.getFullName())) {
            return false;
        } else {
            return s.isSaved();
        }
    }

    public static boolean isSaved(Comment s) {
        if (savedFullnames.contains(s.getFullName())) {
            return true;
        } else if (unSavedFullnames.contains(s.getFullName())) {
            return false;
        } else {
            return s.isSaved();
        }
    }

    public static void setSaved(Submission s, boolean b) {
        String fullname = s.getFullName();
        savedFullnames.remove(fullname);
        if (b) {
            savedFullnames.add(fullname);
        } else {
            unSavedFullnames.add(fullname);
        }
    }

    public static void setSaved(Comment s, boolean b) {
        String fullname = s.getFullName();
        savedFullnames.remove(fullname);
        if (b) {
            savedFullnames.add(fullname);
        } else {
            unSavedFullnames.add(fullname);
        }
    }

}
