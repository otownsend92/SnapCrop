package com.cs291a.snapcrop;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.

        if (position == 0) return PictureSelectFragment.newInstance();
        else if (position == 1) return AspectRatioFragment.newInstance();
        else if (position == 2) return ResultImageFragment.newInstance();

        else return PlaceholderFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Select Picture";
            case 1:
                return "Aspect Ratio";
            case 2:
                return "Result";
        }
        return null;
    }
}
