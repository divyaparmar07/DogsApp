package com.example.dogsapp.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dogsapp.R;
import com.example.dogsapp.model.DogBreed;
import com.example.dogsapp.viewmodel.ListViewModel;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class ListFragment extends Fragment {

    private ListViewModel viewModel;
    private final DogsListAdapter dogsListAdapter = new DogsListAdapter(new ArrayList<>());

    RecyclerView dogsList;
    TextView listError;
    ProgressBar loadingView;
    SwipeRefreshLayout refreshLayout;


    public ListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        dogsList = view.findViewById(R.id.dogsList);
        listError = view.findViewById(R.id.listError);
        loadingView = view.findViewById(R.id.loadingView);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true); //for menu list
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ListViewModel.class);
        viewModel.refresh(); //To retrieve information

        dogsList.setLayoutManager(new LinearLayoutManager(getContext()));
        dogsList.setAdapter(dogsListAdapter);

        refreshLayout.setOnRefreshListener(() -> {
            dogsList.setVisibility(View.GONE);
            listError.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            viewModel.refreshBypassCache();
            refreshLayout.setRefreshing(false);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.dogs.observe(getViewLifecycleOwner(), dogs -> {
            if (dogs != null) {
                dogsList.setVisibility(View.VISIBLE);
                dogsListAdapter.UpdateDogsList((ArrayList<DogBreed>) dogs);
            }
        });

        viewModel.dogLoadError.observe(getViewLifecycleOwner(), isError -> {
            if (isError != null) {
                listError.setVisibility(isError ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    listError.setVisibility(View.GONE);
                    dogsList.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.actionSettings) {
            if (isAdded()) {
                Navigation.findNavController(requireView()).navigate(R.id.actionSetting);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}