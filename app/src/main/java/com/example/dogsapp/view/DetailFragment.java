package com.example.dogsapp.view;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.palette.graphics.Palette;

import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.dogsapp.R;
import com.example.dogsapp.databinding.FragmentDetailBinding;
import com.example.dogsapp.databinding.SendSmsDialogBinding;
import com.example.dogsapp.model.DogBreed;
import com.example.dogsapp.model.DogPalette;
import com.example.dogsapp.model.SmsInfo;
import com.example.dogsapp.viewmodel.DetailViewModel;

import java.util.Objects;

public class DetailFragment extends Fragment {

    private DetailViewModel viewModel;
    private final int dogUuid;
    private FragmentDetailBinding binding;
    private Boolean sendSmsStarted = false;
    private DogBreed currentDog;

    public DetailFragment(int dogUuid) {
        this.dogUuid = dogUuid;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        this.binding = binding;
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(DetailViewModel.class);
        viewModel.fetch(dogUuid);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.dogLiveData.observe(getViewLifecycleOwner(), dogBreed -> {
            if (dogBreed != null && getContext() != null) {
                currentDog = dogBreed;
                binding.setDog(dogBreed);
                if (dogBreed.imageUrl != null) {
                    setupBackgroundColor(dogBreed.imageUrl);
                }
            }
        });
    }

    private void setupBackgroundColor(String url) {
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Palette.from(resource)
                                .generate(palette -> {
                                    assert palette != null;
                                    int intColor = Objects.requireNonNull(palette.getLightMutedSwatch()).getRgb();
                                    DogPalette myPalette = new DogPalette(intColor);
                                    binding.setPalette(myPalette);
                                });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_send_sms) {
            if (!sendSmsStarted) {
                sendSmsStarted = true;
                ((MainActivity) requireActivity()).checkSmsPermission();
            }
        }
        if (item.getItemId() == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed");
            intent.putExtra(Intent.EXTRA_TEXT, currentDog.dogBreed + " bred for " + currentDog.bredFor);
            intent.putExtra(Intent.EXTRA_STREAM, currentDog.imageUrl);
            startActivity(Intent.createChooser(intent, "Share with"));
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPermissionResult(Boolean permissionGranted) {
        if (isAdded() && sendSmsStarted && permissionGranted) {
            SmsInfo smsInfo = new SmsInfo("", currentDog.dogBreed + "bred for " + currentDog.bredFor, currentDog.imageUrl);

            SendSmsDialogBinding dialogBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.send_sms_dialog,
                    null,
                    false
            );
            new AlertDialog.Builder(requireContext())
                    .setView(dialogBinding.getRoot())
                    .setPositiveButton("Send SMS", (dialogInterface, i) -> {
                        if (!dialogBinding.smsDestination.getText().toString().isEmpty()) {
                            smsInfo.to = dialogBinding.smsDestination.getText().toString();
                            sendSms(smsInfo);
                        }
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    })
                    .show();
            sendSmsStarted = false;

            dialogBinding.setSmsInfo(smsInfo);
        }
    }

    private void sendSms(SmsInfo smsInfo) {
        Intent intent = new Intent(getContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(smsInfo.to, null, smsInfo.text, pi, null);
    }
}