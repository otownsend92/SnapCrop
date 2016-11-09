package com.cs291a.snapcrop;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by olivertownsend on 11/8/16.
 */
public class PictureSelectFragment extends Fragment {

    private final String TAG = "PictureSelectFragment";
    private final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private String _path;
    private Uri mImageUri;


    public PictureSelectFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PictureSelectFragment newInstance() {
        PictureSelectFragment fragment = new PictureSelectFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_picture, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText("Choose from gallery, or take a new photo");

        final Button loadGalleryButton = (Button) rootView.findViewById(R.id.load_from_gallery_button);
        loadGalleryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "onClick loadGalleryButton");
                loadFromGallery(v);
            }
        });

        final Button takePictureButton = (Button) rootView.findViewById(R.id.take_picture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "onClick takePictureButton");
                dispatchTakePictureIntent();
            }
        });

        return rootView;
    }

    private void loadFromGallery(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
    }

    private void dispatchTakePictureIntent() {
        _path = Environment.getExternalStorageDirectory() + File.separator +  "retarget_image.jpg";
        File file = new File( _path );
        mImageUri = Uri.fromFile( file );

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, mImageUri );

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                Log.e(TAG, String.valueOf(imageBitmap));

                ImageView imageView = (ImageView) getActivity().findViewById(R.id.chosen_image_view);
                imageView.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            getActivity().getContentResolver().notifyChange(mImageUri, null);
            ContentResolver cr = getActivity().getContentResolver();
            Bitmap bitmap;
            try
            {
                bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
                ImageView imageView = (ImageView) getActivity().findViewById(R.id.chosen_image_view);
                imageView.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Failed to load", e);
            }
        }
    }

}