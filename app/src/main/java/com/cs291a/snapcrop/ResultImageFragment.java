package com.cs291a.snapcrop;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class ResultImageFragment extends Fragment {

    private static final String TAG = "ResultImageFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    public ResultImageFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ResultImageFragment newInstance() {
        ResultImageFragment fragment = new ResultImageFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result_img, container, false);

        return rootView;
    }

}
