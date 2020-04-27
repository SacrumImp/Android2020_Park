
package ru.techpark.agregator;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ru.techpark.agregator.fragments.ApiDetailedFragment;
import ru.techpark.agregator.fragments.ApiFeedFragment;
import ru.techpark.agregator.fragments.BdDetailedFragment;
import ru.techpark.agregator.fragments.BdFeedFragment;
import ru.techpark.agregator.viewmodels.ApiViewModel;
import ru.techpark.agregator.viewmodels.BdSingleViewModel;
import ru.techpark.agregator.viewmodels.BdViewModel;

public class MainActivity extends AppCompatActivity implements FragmentNavigator{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            new ApiViewModel(getApplication()).addFeedNextPage(1);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, new ApiFeedFragment());
            transaction.commit();
        }
        Intent intent = getIntent();
        if (intent.getAction().equals(NotificationWorker.ACTION_TO_OPEN)){
            int id = intent.getIntExtra(NotificationWorker.OPEN_FRAGMENT_ID, 0);
            navigateToAnotherFragment(id);
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent1;
            switch(item.getItemId()){
                case R.id.action_feed:
                    if (savedInstanceState == null) {
                        new ApiViewModel(getApplication()).addFeedNextPage(1);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new ApiFeedFragment());
                        transaction.commit();
                    }
                    intent1 = getIntent();
                    if (intent1.getAction().equals(NotificationWorker.ACTION_TO_OPEN)){
                        int id = intent1.getIntExtra(NotificationWorker.OPEN_FRAGMENT_ID, 0);
                        navigateToAnotherFragment(id);
                    }
                    break;
                case R.id.action_liked:
                    if (savedInstanceState == null) {
                        new BdViewModel(getApplication()).addFeedNextPage(1);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new BdFeedFragment());
                        transaction.commit();
                    }
                    intent1 = getIntent();
                    if (intent1.getAction().equals(NotificationWorker.ACTION_TO_OPEN)){
                        int id = intent1.getIntExtra(NotificationWorker.OPEN_FRAGMENT_ID, 0);
                        navigateToAnotherBdFragment(id);
                    }

                    break;
                case R.id.action_settings:
                    Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });
    }



    @Override
    public void navigateToAnotherFragment(int id) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, ApiDetailedFragment.newInstance(id))
                .addToBackStack(null)
                .commit();// all transactions before commit are added to backstack
    }

    @Override
    public void navigateToAnotherBdFragment(int id) {
        new BdSingleViewModel(getApplication()).getDetailedEvent(id);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, BdDetailedFragment.newInstance(id))
                .addToBackStack(null)
                .commit();
    }


}
