package ru.techpark.agregator.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import ru.techpark.agregator.R;
import ru.techpark.agregator.viewmodels.ApiSingleViewModel;

public class ApiDetailedFragment extends DetailedFragment {

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

    @Override
    void hideLoading() {
        super.hideLoading();
        likeEvent.setVisibility(View.VISIBLE);
    }

    @Override
    void handleErrorInObserver() {
        super.handleErrorInObserver();
        errorText.setText(R.string.network_error);
        errorImage.setImageResource(R.drawable.ic_network_error);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //обработка нажатия лайка
        likeEvent.setOnClickListener((v) -> {
            detailedViewModel.insertEventBD(event);
            Toast.makeText(getContext(), R.string.added, Toast.LENGTH_SHORT).show();
            likeEvent.setVisibility(View.GONE);
        });
    }
}
