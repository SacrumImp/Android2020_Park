package ru.techpark.agregator.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ru.techpark.agregator.FragmentNavigator;
import ru.techpark.agregator.NotificationWorker;
import ru.techpark.agregator.R;
import ru.techpark.agregator.event.Event;
import ru.techpark.agregator.viewmodels.SingleViewModel;

public abstract class DetailedFragment extends Fragment {

    public final static String KEY_DES = "KEY_DES";
    public final static String KEY_ID = "KEY_ID";
    public final static String KEY_TITLE = "KEY_TITLE";
    public final static String KEY_DATE = "KEY_DATE";
    public final static String KEY_TIME = "KEY_TIME";
    static final String NUM_CURR = "CURRENT";
    private static final String TAG = "DetailedFragment";
    protected int id;
    protected Event event;
    ProgressBar loading_progress;
    ImageButton button_go;
    FloatingActionButton likeEvent;
    SingleViewModel detailedViewModel;
    private TextView title;
    private TextView description;
    private TextView body_text;
    private TextView price;
    protected TextView date_start;
    protected TextView time_start;
    private TextView location;
    private TextView location_label;
    private ImageView image;
    private TextView price_label;
    private TextView description_label;
    private TextView phone_label;
    protected TextView time_label;
    private TextView place_title_label;
    private TextView place_title;
    private TextView place_address;
    private TextView place_address_label;
    protected ImageButton calendar_button;


    private TextView phone;
    private Toolbar toolbar;

    FragmentNavigator navigator;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        navigator = (FragmentNavigator) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailed_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        time_label = view.findViewById(R.id.label_date);
        description_label = view.findViewById(R.id.label_description);
        body_text = view.findViewById(R.id.body_text);
        price = view.findViewById(R.id.price);
        image = view.findViewById(R.id.image);
        description = view.findViewById(R.id.description);
        date_start = view.findViewById(R.id.date_start);
        time_start = view.findViewById(R.id.time_start);
        price_label = view.findViewById(R.id.price_label);
        location = view.findViewById(R.id.location);
        location_label = view.findViewById(R.id.location_label);
        phone_label = view.findViewById(R.id.phone_label);
        place_address = view.findViewById(R.id.place_address);
        place_address_label = view.findViewById(R.id.place_address_label);
        place_title = view.findViewById(R.id.place_title);
        place_title_label = view.findViewById(R.id.place_title_label);
        phone = view.findViewById(R.id.phone);
        button_go = view.findViewById(R.id.notify_button);
        loading_progress = view.findViewById(R.id.loading_progress);
        title = view.findViewById(R.id.title);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.detailed_event_toolbar_menu);
        likeEvent = view.findViewById(R.id.likeUnlike);
        calendar_button = view.findViewById(R.id.calendar_button);

        description_label.setVisibility(View.INVISIBLE);
        time_label.setVisibility(View.GONE);
        price_label.setVisibility(View.INVISIBLE);
        phone_label.setVisibility(View.GONE);
        time_start.setVisibility(View.GONE);
        date_start.setVisibility(View.GONE);
        place_address.setVisibility(View.GONE);
        place_address_label.setVisibility(View.GONE);
        place_title.setVisibility(View.GONE);
        place_title_label.setVisibility(View.GONE);
        phone.setVisibility(View.GONE);
        price_label.setVisibility(View.GONE);
        price.setVisibility(View.GONE);
        loading_progress.setVisibility(View.VISIBLE);

        body_text.setMovementMethod(LinkMovementMethod.getInstance());

        Observer<Event> observer = event -> {
            if (event != null) {
                setEventData(event);
            } else {
                handleErrorInObserver();
            }
            Log.d(TAG, "observer worked");
        };

        detailedViewModel
                .getEvent()
                .observe(getViewLifecycleOwner(), observer);

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_share:
                    if (event != null) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, event.getSite_url());
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share_event)));
                        return true;
                    } else return false;
                case R.id.action_open_in_browser:
                    if (event != null) {
                        Intent openIntent = new Intent();
                        openIntent.setAction(Intent.ACTION_VIEW);
                        openIntent.setData(Uri.parse(event.getSite_url()));
                        startActivity(Intent.createChooser(openIntent, getResources().getString(R.string.open_in_browser)));
                        return true;
                    } else return false;
                default:
                    return false;
            }
        });
    }
 //todo поддержка остальных кнопок
    private void setEventData(Event event) {
        this.event = event;
        hideLoading();
        title.setText(event.getTitle());
        description_label.setVisibility(View.VISIBLE);
        description.setText(Html.fromHtml(event.getDescription()));
        if (event.getImages().size() > 0)
            Glide.with(image.getContext())
                    .load(event.getImages().get(0).getImageUrl())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .error(R.drawable.ic_image_placeholder)
                    .into(image);
        body_text.setText(Html.fromHtml(event.getBody_text()));
        if (event.getPrice().length() != 0) {
            price_label.setVisibility(View.VISIBLE);
            price.setVisibility(View.VISIBLE);
            price.setText(event.getPrice());
        }
        if (event.getLocation().getSlug().equals("online")) {
            location_label.setText(R.string.way_to_go);
        } else {
            location_label.setText(R.string.city);
        }
        location.setText(event.getLocation().getName());
        Log.d(TAG, "\"" + event.getDates().get(0).getStart_date() + "\" \"" + event.getDates().get(0).getStart_time() + "\" " + (event.getDates().get(0).getStart_date() != null));
        if (!event.getDates().get(0).getStart_date().equals("null") && event.getDates().get(0).getStart_date().length() > 0) {
            time_label.setVisibility(View.VISIBLE);
            date_start.setText(event.getDates().get(0).getStart_date());
            time_start.setText(event.getDates().get(0).getStart_time());
        }

        if (event.getPlace() != null) {
            if (event.getPlace().getTitle().length() != 0) {
                place_title_label.setVisibility(View.VISIBLE);
                place_title.setVisibility(View.VISIBLE);
                place_title.setText(event.getPlace().getTitle());
            }
            if (event.getPlace().getAddress().length() != 0) {
                place_address.setVisibility(View.VISIBLE);
                place_address_label.setVisibility(View.VISIBLE);
                place_address.setText(event.getPlace().getAddress());
            }
            if (event.getPlace().getPhone().length() != 0) {
                phone_label.setVisibility(View.VISIBLE);
                phone.setVisibility(View.VISIBLE);
                phone.setText(event.getPlace().getPhone());
            }
        }
        button_go.setOnClickListener(v -> {
            String workTag = event.getId() + "";
            Data put = new Data.Builder().putInt(KEY_ID, event.getId())
                    .putString(KEY_DATE, event.getDates().get(0).getStart_date())
                    .putString(KEY_TIME, event.getDates().get(0).getStart_time())
                    .putString(KEY_TITLE, event.getTitle())
                    .putString(KEY_DES, event.getDescription()).build();
            long difference;
            Date eventDate = new Date(event.getDates().get(0).getStart() * 1000l + 10800000l);
            long extra_time = 18000000; // 5 часов.
            difference = eventDate.getTime() - System.currentTimeMillis() - extra_time; // за 5 часов до события
            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    //          .setInputData(put).build();
                    .setInputData(put).setInitialDelay(difference, TimeUnit.MILLISECONDS).addTag(workTag).build();
            WorkManager.getInstance(getContext()).enqueue(notificationWork);
        });
        calendar_button.setOnClickListener(v ->{
            Calendar beginTime = Calendar.getInstance();
            beginTime.setTimeInMillis(event.getDates().get(0).getStart()*1000l + 10800000l);
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(event.getDates().get(0).getEnd()*1000l + 10800000l);
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                    .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            startActivity(intent);
        });
    }

    abstract void hideLoading();

    abstract void handleErrorInObserver();
}