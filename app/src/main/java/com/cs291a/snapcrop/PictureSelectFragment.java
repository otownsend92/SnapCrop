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
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;

/**
 * Created by olivertownsend on 11/8/16.
 */
public class PictureSelectFragment extends Fragment {

    private final String TAG = "PictureSelectFragment";
    private final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri mImageUri;
    private boolean imageSet = false;

    String sfp;

    public static Bitmap globalBitmap;
    public static int imgViewW;
    public static int imgViewH;
    public static boolean drawing = false;


    public PictureSelectFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PictureSelectFragment newInstance() {
        PictureSelectFragment fragment = new PictureSelectFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_picture, container, false);

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
                try {
                    dispatchTakePictureIntent();
                } catch (IOException e) {
                    Log.e(TAG, "Can't take picture");
                    e.printStackTrace();
                }
            }
        });

        final ToggleButton markImportantButton = (ToggleButton) rootView.findViewById(R.id.draw_button);
        markImportantButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "onClick markImportantButton");
                if (imageSet)
                    setLayoutParams();
                drawing = !drawing;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap imgBitmap = null;

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            imgBitmap = populateFromGallery(uri);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imgBitmap = populateFromCamera();
        }
        imageSet = true;
    }

    private void setLayoutParams() {
        RelativeLayout imgRelLayout = (RelativeLayout) getActivity().findViewById(R.id.canvas_relative_layout);
        ViewGroup.LayoutParams params = imgRelLayout.getLayoutParams();

        ImageView imageView = (ImageView) getActivity().findViewById(R.id.chosen_image_view);
        int[] dimens = MainActivity.getBitmapPositionInsideImageView(imageView);

        params.width = dimens[2];
        params.height = dimens[3];
        imgRelLayout.setLayoutParams(params);

        DrawingView mDrawingView = new DrawingView(getActivity());
        imgRelLayout.addView(mDrawingView);
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

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = getOutputMediaFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mImageUri = FileProvider.getUriForFile(getActivity(),
                        "com.cs291a.snapcrop.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File getOutputMediaFile() throws IOException {
        // Create an image file name
        Long timeStamp = System.currentTimeMillis() / 1000;
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    private Bitmap populateFromGallery(Uri uri) {
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            ImageView imageView = (ImageView) getActivity().findViewById(R.id.chosen_image_view);

            imageView.setImageBitmap(imageBitmap);
            globalBitmap = imageBitmap;
            return imageBitmap;

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to load image", e);
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap populateFromCamera() {
        getActivity().getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = getActivity().getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
            return bitmap;
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to capture image", e);
            e.printStackTrace();
        }
        return null;
    }
}