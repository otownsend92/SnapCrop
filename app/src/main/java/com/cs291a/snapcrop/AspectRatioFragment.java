package com.cs291a.snapcrop;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by olivertownsend on 11/9/16.
 */
public class AspectRatioFragment extends Fragment {
    public AspectRatioFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AspectRatioFragment newInstance() {
        AspectRatioFragment fragment = new AspectRatioFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_aspect_ratio, container, false);



        return rootView;
    }
}
