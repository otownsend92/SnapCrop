package com.cs291a.snapcrop;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    public static boolean tracking = true;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private final String TAG = "MainActivity";

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tab = mViewPager.getCurrentItem();
                Log.e(TAG, "Currently in tab " + tab);

                if (tab == 0) {
                    ImageView imageView = (ImageView) findViewById(R.id.chosen_image_view);
                    int[] dimens = getBitmapPositionInsideImageView(imageView);
                    int imvW = dimens[2];
                    int imvH = dimens[3];

                    Bitmap bmOverlay = Bitmap.createBitmap(
                            imvW,
                            imvH,
                            Bitmap.Config.ARGB_8888);

                    Bitmap scaledImg = Bitmap.createScaledBitmap(
                            PictureSelectFragment.globalBitmap,
                            imvW,
                            imvH,
                            false);

                    Canvas canvas = new Canvas(bmOverlay);
                    canvas.drawARGB(0x00, 0, 0, 0);
                    canvas.drawBitmap(scaledImg, 0, 0, null);
                    canvas.drawBitmap(DrawingView.mBitmap, 0, 0, null);

                    BitmapDrawable dr = new BitmapDrawable(getResources(), bmOverlay);
                    dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());

                    imageView.setImageDrawable(dr);
                    //mViewPager.setCurrentItem(tab + 1);
                }
                else if (tab == 1) {
                    Log.e(TAG, "In tab 1, trying to get new aspect ratio");
                    String aspectW = ((EditText) findViewById(R.id.aspect_width_value)).getText().toString();
                    String aspectH = ((EditText) findViewById(R.id.aspect_height_value)).getText().toString();
                    Log.e(TAG, "aspectW " + aspectW);
                    Log.e(TAG, "aspectH " + aspectH);

                    ImageRetarget.retargetImage(Integer.parseInt(aspectW), Integer.parseInt(aspectH));
                    ImageView imageView = (ImageView) findViewById(R.id.result_image_view);

                    if (ImageRetarget.globalNewImg != null) {
                        Log.e(TAG, "Setting the new globalNewImage!");
                        imageView.setImageBitmap(ImageRetarget.globalNewImg);
                    } else {
                        Log.e(TAG, "globalNewImage is null!");
                    }

                    mViewPager.setCurrentItem(tab+1);
                }
            }
        });

    }

    // Rendre OPEN CV utilisable
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCVManager setup", "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
                mLoaderCallback);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        int imgViewWL = imageView.getWidth();
        int imgViewHL = imageView.getHeight();

        int top = (int) (imgViewHL - actH)/2;
        int left = (int) (imgViewWL - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }

}
