package de.pd.lesedatenbank;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.github.appintro.AppIntro2;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.model.SliderPage;

public class Intro extends AppIntro2 {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle(getString(R.string.app_name));
        sliderPage1.setDescription(getString(R.string.introText1));
        sliderPage1.setBackgroundColorRes(R.color.primary);
        sliderPage1.setImageDrawable(R.mipmap.ic_launcher_round_inverted);

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle(getString(R.string.app_name));
        sliderPage2.setDescription(getString(R.string.introText2));
        sliderPage2.setBackgroundColorRes(R.color.primary);

        SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle(getString(R.string.app_name));
        sliderPage3.setDescription(getString(R.string.introText3));
        sliderPage3.setBackgroundColorRes(R.color.primary);

        addSlide(AppIntroFragment.createInstance(sliderPage1));
        addSlide(AppIntroFragment.createInstance(sliderPage2));
        addSlide(AppIntroFragment.createInstance(sliderPage3));

        showStatusBar(true);
        setSystemBackButtonLocked(true);
        setSkipButtonEnabled(true);
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}