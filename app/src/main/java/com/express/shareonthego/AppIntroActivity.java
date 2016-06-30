package com.express.shareonthego;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro2;

public class AppIntroActivity extends AppIntro2 {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(SampleSlide1.newInstance());
        addSlide(SampleSlide2.newInstance());
        addSlide(SampleSlide3.newInstance());
        setIndicatorColor(getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorAccent));
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void getStarted(View v) {
        loadMainActivity();
    }
}