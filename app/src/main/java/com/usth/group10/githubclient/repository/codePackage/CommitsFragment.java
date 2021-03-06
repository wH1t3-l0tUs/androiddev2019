package com.usth.group10.githubclient.repository.codePackage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.squareup.picasso.Picasso;
import com.usth.group10.githubclient.R;
import com.usth.group10.githubclient.others.MySingleton;
import com.usth.group10.githubclient.profile.ProfileActivity;
import com.usth.group10.githubclient.repository.RepoActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommitsFragment extends Fragment {
    private static final String TAG = "CommitsFragment";
    private static final String KEY_REPO_URL = "repo_url";

    private FrameLayout mProgressBarLayout;
    private RecyclerView mCommitsRecyclerView;
    private RecyclerView.Adapter mCommitsAdapter;

    public static CommitsFragment newInstance(String repoUrl) {
        CommitsFragment CommitsFragment = new CommitsFragment();
        Bundle args = new Bundle();
        args.putString(KEY_REPO_URL, repoUrl);
        CommitsFragment.setArguments(args);
        return CommitsFragment;
    }

    public CommitsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_commits, container, false);

        mProgressBarLayout = view.findViewById(R.id.progress_bar_layout_feeds);
        mProgressBarLayout.setVisibility(View.VISIBLE);

        mCommitsRecyclerView = view.findViewById(R.id.recycler_view_commits);
        mCommitsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateCommitsList();
        return view;
    }

    private class CommitsFeedAdapter extends RecyclerView.Adapter<CommitsViewHolder>{
        private ArrayList<CommitsFeed> mCommitsList;

        private CommitsFeedAdapter(ArrayList<CommitsFeed> commitsList){
            mCommitsList = commitsList;}

        @NonNull
        @Override
        public CommitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CommitsViewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CommitsViewHolder holder, int position) {
            holder.bind(mCommitsList.get(position));
        }

        @Override
        public int getItemCount() {
            return mCommitsList.size();
        }
    }



    private class CommitsViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView mImageUserAvatar;
        private TextView mTextViewTitle;
        private TextView mTextViewUsername;
        private TextView mTextViewTime;

        private CommitsViewHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.item_feeds_list, parent, false));

            mImageUserAvatar = itemView.findViewById(R.id.image_avatar_feeds);
            mTextViewTitle = itemView.findViewById(R.id.text_title_feeds);
            mTextViewTime = itemView.findViewById(R.id.text_time_feeds);
            mTextViewUsername = itemView.findViewById(R.id.text_content_feeds);
        }

        private void bind(final CommitsFeed commitsFeed){
            mTextViewTitle.setText(commitsFeed.getTitle());
            mTextViewTime.setText(commitsFeed.getTime());
            mTextViewUsername.setText(commitsFeed.getUsername());
            Picasso.get().load(commitsFeed.getUserAvatarUrl()).into(mImageUserAvatar);

            mImageUserAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = ProfileActivity.newIntent(getActivity(), commitsFeed.getUserUrl());
                            startActivity(intent);
                }
            });
        }
    }

    private void updateCommitsList() {
        String url = getArguments().getString(KEY_REPO_URL) + "/commits";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<CommitsFeed> commitsFeedsList = processRawJson(response);

                        mCommitsAdapter = new CommitsFeedAdapter(commitsFeedsList);
                        mCommitsRecyclerView.setAdapter(mCommitsAdapter);
                        mProgressBarLayout.setVisibility(View.GONE);
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Loading commits failed", Toast.LENGTH_LONG).show();
                    }
                });
        MySingleton.getInstance(getActivity()).addToRequestQueue(jsonArrayRequest);
    }



    private ArrayList<CommitsFeed> processRawJson(JSONArray response){
        JSONObject currentItem;
        ArrayList<CommitsFeed> commitsFeedsList = new ArrayList<>();

        String title, userAvatarUrl, time;
        String username, userUrl;

        for (int i = 0; i < response.length(); i++){
            try {
                currentItem = response.getJSONObject(i);
                username = currentItem.getJSONObject("committer").getString("login");
                title = currentItem.getJSONObject("commit").getString("message");
                userAvatarUrl = currentItem.getJSONObject("committer").getString("avatar_url");
                time = currentItem.getJSONObject("commit").getJSONObject("author").getString("date");
                userUrl = currentItem.getJSONObject("author").getString("url");

                commitsFeedsList.add(new CommitsFeed(title, username, userAvatarUrl, time, userUrl));

            } catch (JSONException e){
                e.printStackTrace();
            }

        }
        return commitsFeedsList;
    }

    private class CommitsFeed {
        private String mTitle;
        private String mUsername;
        private String mUserAvatarUrl;
        private String mTime;
        private String mUserUrl;

        private CommitsFeed(String title, String username, String userAvatarUrl, String time, String userUrl){
            mTitle = title;
            mUsername = username;
            mUserAvatarUrl = userAvatarUrl;
            mUserUrl = userUrl;
            setTime(time);
        }
        public String getTitle() { return mTitle; }

        public String getUserAvatarUrl() { return mUserAvatarUrl; }

        public String getUsername() { return mUsername; }

        public String getTime() { return mTime; }

        public String getUserUrl() { return mUserUrl; }

        public void setTime(String time){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
            try {
                Date d = formatter.parse(time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(d);
                calendar.add(Calendar.HOUR,7);

                mTime = DateUtils.getRelativeTimeSpanString(calendar.getTime().getTime(), new Date().getTime(),
                                                            DateUtils.MINUTE_IN_MILLIS,
                                                            DateUtils.FORMAT_ABBREV_RELATIVE).toString();

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}


