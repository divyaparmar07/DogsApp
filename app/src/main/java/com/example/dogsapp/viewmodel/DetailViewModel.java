package com.example.dogsapp.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.dogsapp.model.DogBreed;
import com.example.dogsapp.model.DogDatabase;

public class DetailViewModel extends AndroidViewModel {
    public MutableLiveData<DogBreed> dogLiveData = new MutableLiveData<>();
    private RetrieveDogTask retrieveDogTask;

    public DetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void fetch(int uuid) {
        retrieveDogTask = new RetrieveDogTask();
        retrieveDogTask.execute(uuid);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (retrieveDogTask != null) {
            retrieveDogTask.cancel(true);
            retrieveDogTask = null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class RetrieveDogTask extends AsyncTask<Integer, Void, DogBreed> {

        @Override
        protected DogBreed doInBackground(Integer... integers) {
            int uuid = integers[0];
            return DogDatabase.getInstance(getApplication()).dogDao().getDog(uuid);
        }

        @Override
        protected void onPostExecute(DogBreed dogBreed) {
            dogLiveData.setValue(dogBreed);
        }
    }
}
