package ru.techpark.agregator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import ru.techpark.agregator.FragmentNavigator;
import ru.techpark.agregator.R;
import ru.techpark.agregator.event.Event;
import ru.techpark.agregator.viewmodels.FeedViewModel;

public abstract class FeedFragment extends Fragment {
    private static final String TAG = "FeedFragment";
    private static final String SEARCH_STATE = "SEARCH_STATE";
    private static final String SEARCH_QUERY = "SEARCH_QUERY";
    private static final String PAGE = "PAGE";
    protected String searchQuery;
    FragmentNavigator navigator;
    FeedViewModel feedViewModel;

    boolean isSearch = false;
    int pageCounter = 1;
    boolean isAllEvents = false;
    EditText searchField;
    ImageButton startSearch;
    ImageButton exitSearch;
    ImageButton setFilter;
    RecyclerView feed;
    ChipGroup chipsLayout;
    LinearLayout errorLayout;
    TextView errorText;
    ImageView errorImage;
    private FeedFragment.FeedAdapter adapter;
    private ProgressBar loadingProgress;
    private boolean isFirstCalled = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        navigator = (FragmentNavigator) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            isSearch = savedInstanceState.getBoolean(SEARCH_STATE);
            searchQuery = savedInstanceState.getString(SEARCH_QUERY);
            pageCounter = savedInstanceState.getInt(PAGE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SEARCH_STATE, isSearch);
        outState.putString(SEARCH_QUERY, searchQuery);
        outState.putInt(PAGE, pageCounter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        feed = view.findViewById(R.id.list_of_events);
        loadingProgress = view.findViewById(R.id.loading_progress);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        startSearch = toolbar.findViewById(R.id.start_search);
        searchField = toolbar.findViewById(R.id.search_query);
        exitSearch = toolbar.findViewById(R.id.exit_search);
        setFilter = toolbar.findViewById(R.id.filters_button);
        chipsLayout = view.findViewById(R.id.chip_group);
        errorLayout = view.findViewById(R.id.error_layout);
        errorText = errorLayout.findViewById(R.id.error_text);
        errorImage = errorLayout.findViewById(R.id.error_image);
        Button refreshButton = errorLayout.findViewById(R.id.refresh);


        if (savedInstanceState == null)
            showLoadingProgress();
        setHomeState();
        if (isSearch) {
            setSearchState();
        }

        exitSearch.setOnClickListener((l) -> {
            if (isSearch) {
                isSearch = false;
                isAllEvents = false;
                pageCounter = 1;
                loadNextPage();
            }
            setHomeState();
        });

        refreshButton.setOnClickListener((l) -> {
            loadNextPage();
            Log.d(TAG, "refresh");
        });

        startSearch.setOnClickListener(l -> setSearchState());

        searchField.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchQuery = searchField.getText().toString();
                if (searchQuery.length() > 1) {
                    pageCounter = 1;
                    isAllEvents = false;
                    isSearch = true;
                    loadNextPage();
                    handled = true;
                } else {
                    Toast.makeText(getContext(), R.string.search_toast_empty_field, Toast.LENGTH_SHORT).show();
                }
            }
            return handled;
        });

        adapter = new FeedFragment.FeedAdapter();
        feed.setAdapter(adapter);
        feed.setLayoutManager(new LinearLayoutManager(view.getContext()));
        Observer<List<Event>> observer = Events -> {
            if (!isFirstCalled) {
                isFirstCalled = true;
                return;
            }
            if (Events != null) {
                Log.d(TAG, "in observer");
                hideLoadingProgress();
                adapter.setEvents(Events);
            } else {
                handleObserverError();
            }
        };
        feedViewModel.getEvents()
                .observe(getViewLifecycleOwner(), observer);
    }

    void setHomeState() {
        exitSearch.setVisibility(View.GONE);
        setFilter.setVisibility(View.VISIBLE);
        startSearch.setVisibility(View.VISIBLE);
        searchField.setVisibility(View.GONE);
        chipsLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        feed.setVisibility(View.VISIBLE);
        searchField.setText("");
    }

    private void setSearchState() {
        searchField.setVisibility(View.VISIBLE);
        setFilter.setVisibility(View.GONE);
        exitSearch.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        startSearch.setVisibility(View.GONE);
        feed.setVisibility(View.VISIBLE);
        chipsLayout.setVisibility(View.GONE);
    }


    abstract void handleObserverError();

    abstract void getFromAdapter(int id);

    void hideLoadingProgress() {
        Log.d(TAG, "progress hidden, " + isSearch);
        loadingProgress.setVisibility(View.GONE);
        feed.setVisibility(View.VISIBLE);
    }

    void showLoadingProgress() {
        Log.d(TAG, "progress shown");
        loadingProgress.setVisibility(View.VISIBLE);
        feed.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    void loadNextPage() {
        showLoadingProgress();
        if (isSearch) {
            feedViewModel.addSearchNextPage(searchQuery, pageCounter);
        } else {
            feedViewModel.addFeedNextPage(pageCounter);
        }
    }


    protected abstract void showEmptyState();

class FeedViewHolder extends RecyclerView.ViewHolder {
    ImageView eventImage;
    TextView title;
    TextView description;
    TextView date;
    TextView time;
    TextView dateLabel;
    LinearLayout timeLayout;

    FeedViewHolder(@NonNull View itemView) {
        super(itemView);
        eventImage = itemView.findViewById(R.id.image_in_feed);
        title = itemView.findViewById(R.id.title_in_feed);
        description = itemView.findViewById(R.id.description_in_feed);
        date = itemView.findViewById(R.id.date_in_feed);
        time = itemView.findViewById(R.id.time_in_feed);
        dateLabel = itemView.findViewById(R.id.date_label_in_feed);
        timeLayout = itemView.findViewById(R.id.time_layout);
        itemView.setOnClickListener(v -> {
            int id = adapter.getIdOfEvent(getAbsoluteAdapterPosition());
            getFromAdapter(id);
            Log.d(TAG, "id " + id);
        });
    }
}

protected class FeedAdapter extends RecyclerView.Adapter<FeedFragment.FeedViewHolder> {

    List<Event> events = new ArrayList<>();

    void setEvents(List<Event> events) {
        int EVENTS_ON_PAGE = 20;

        if (events.size() % EVENTS_ON_PAGE != 0)
            isAllEvents = true;
        this.events = events;
        if (pageCounter == 1)
            notifyDataSetChanged();
        else {
            notifyItemRangeInserted(EVENTS_ON_PAGE * (pageCounter - 1), EVENTS_ON_PAGE);
        }
        if (events.size() == 0)
            showEmptyState();
    }

    int getIdOfEvent(int position) {
        return events.get(position).getId();
    }

    @NonNull
    @Override
    public FeedFragment.FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FeedFragment.FeedViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_elem, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull FeedFragment.FeedViewHolder holder, int position) {

        Event event = events.get(position);
        holder.timeLayout.setVisibility(View.GONE);
        holder.title.setText(event.getTitle());
        holder.description.setText(Html.fromHtml(event.getDescription()));

        if (UIutils.hasTime(event)) {
            holder.timeLayout.setVisibility(View.VISIBLE);
            UIutils.setTimeInformation(event, holder.time, holder.date);
        }

        if (event.getImages().size() > 0)
            Glide.with(holder.eventImage.getContext())
                    .load(event.getImages().get(0).getImageUrl())
                    .error(R.drawable.ic_image_placeholder)
                    .into(holder.eventImage);
        if (position == getItemCount() - 1 && !isAllEvents) {
            Log.d(TAG, "page " + pageCounter);
            ++pageCounter;
            loadNextPage();
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
}