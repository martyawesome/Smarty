package com.martyawesome.smarty.app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.martyawesome.ribbit.app.R;
import com.martyawesome.smarty.app.adapters.UserAdapter;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by User on 6/19/2014.
 */
public class FriendsFragment extends Fragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected List<ParseUser> mFriends;
    protected ParseUser mCurrentUser;
    protected GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.friendsGrid);
        TextView emptyTextView = (TextView) rootView.findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setProgressBarIndeterminateVisibility(true);

        if (isOnline()) {
            mCurrentUser = ParseUser.getCurrentUser();
            mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
            ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
            query.addAscendingOrder(ParseConstants.KEY_USERNAME);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> friends, ParseException e) {
                    getActivity().setProgressBarIndeterminateVisibility(false);

                    if (e == null) {
                        mFriends = friends;

                        String[] usernames = new String[mFriends.size()];
                        int i = 0;
                        for (ParseUser user : mFriends) {
                            usernames[i] = user.getUsername();
                            i++;
                        }
                        if(mGridView.getAdapter()==null) {
                            UserAdapter adapter = new UserAdapter(getActivity(), mFriends);
                            mGridView.setAdapter(adapter);
                        }else{
                            ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                        }
                    } else {
                        Log.e(TAG, e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.general_error)
                                .setTitle(R.string.error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        } else {
            getActivity().setProgressBarIndeterminateVisibility(false);
            Toast.makeText(getActivity().getBaseContext(), getString(R.string.internet_error), Toast.LENGTH_LONG).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}