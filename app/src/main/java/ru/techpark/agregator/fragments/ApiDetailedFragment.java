package ru.techpark.agregator.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import ru.techpark.agregator.R;
import ru.techpark.agregator.viewmodels.ApiSingleViewModel;
import ru.techpark.agregator.viewmodels.BdViewModel;
import ru.techpark.agregator.viewmodels.FeedViewModel;

public class ApiDetailedFragment extends DetailedFragment {

    private static final String ERROR_IN_OBSERVER = "Нет соеинения с интернетом";

    public static ApiDetailedFragment newInstance(int num) {
        ApiDetailedFragment frag = new ApiDetailedFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(NUM_CURR, num);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detailedViewModel = new ViewModelProvider(this).get(ApiSingleViewModel.class);
        Bundle arguments = getArguments();
        if (arguments != null) {
            id = arguments.getInt(NUM_CURR);
            detailedViewModel.getDetailedEvent(id);
        }
    }

    void hideLoading() {
        loading_progress.setVisibility(View.GONE);
        likeEvent.setVisibility(View.VISIBLE);
    }

    void handleErrorInObserver() {
        loading_progress.setVisibility(View.GONE);
        Toast.makeText(getContext(), ERROR_IN_OBSERVER, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        likeEvent = view.findViewById(R.id.likeUnlike);

        button_go.setVisibility(View.GONE);
        likeEvent.setVisibility(View.GONE);

        //обработка нажатия лайка
        FeedViewModel feedViewModel = new ViewModelProvider(this).get(BdViewModel.class);
        likeEvent.setOnClickListener((v) -> feedViewModel.insertEventBD(event));
    }
}