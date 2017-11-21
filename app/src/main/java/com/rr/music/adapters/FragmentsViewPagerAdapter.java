package com.rr.music.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.rr.music.fragments.AlphabeticalFragment;
import com.rr.music.fragments.FoldersFragment;

public class FragmentsViewPagerAdapter extends FragmentStatePagerAdapter {
    private int numbOfTabs;
    private AlphabeticalFragment alphabeticalFragment;

    public FragmentsViewPagerAdapter(FragmentManager fm, int numbOfTabs) {
        super(fm);
        this.numbOfTabs = numbOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                alphabeticalFragment = new AlphabeticalFragment();
                return alphabeticalFragment;
            case 1:
                return new FoldersFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numbOfTabs;
    }

    public void updateAlphabeticalFragment () {
        alphabeticalFragment.updateList();
    }

}