package com.biniisu.leanrss.leanrssImageViewer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewPropertyAnimator;
import android.widget.Toast;

import com.biniisu.leanrss.R;

import java.io.File;
import java.net.URI;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener,LeanImageViewerPagerAdapter.ImageTapped{

    public static final String TAG = ImageViewerActivity.class.getSimpleName();
    public static final String FILE_PROVIDER = "com.biniisu.leanrss.fileprovider";
    public static final String IMAGE_PATH = "IMAGE_PATH";
    public static final String IMG_ELEMENTS = "IMG_ELEMENTS";
    public static final String IMG_START_POS = "IMG_START_POS";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Context context;
    private Toolbar toolbar;
    private Uri mImageUri;
    private ViewPropertyAnimator toolbarAnimator;
    private ViewPager viewPager;
    private LeanImageViewerPagerAdapter leanImageViewerPagerAdapter;
    private List<String> images;
    private int startPosition;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        context=ImageViewerActivity.this;
        //toolbar setup

        Log.d(TAG, "onCreate: opening image viewer activity");

        //Check if we have received a valid image path
        if (!getIntent().hasExtra(IMG_START_POS) && !getIntent().hasExtra(IMG_ELEMENTS)) {
            throw new IllegalArgumentException("Needs starting position and list of images!");
        }

        images = (List<String>) getIntent().getSerializableExtra(IMG_ELEMENTS);
        startPosition = getIntent().getIntExtra(IMG_START_POS, 0);

        toolbar = findViewById(R.id.appbar_toolbar);
        toolbar.inflateMenu(R.menu.image_viewer_menu);
        toolbar.setTitle("");
        toolbar.setOnMenuItemClickListener((Toolbar.OnMenuItemClickListener) context);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> finish());

        /*ViewPager setup*/
        viewPager = (ImageViewPager) findViewById(R.id.view_pager);
        viewPager.setPageMargin(25);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
               //onPageScrolled
            }
            @Override
            public void onPageSelected(int position) {
                mImageUri = FileProvider.getUriForFile(context, FILE_PROVIDER, new File(URI.create(images.get(startPosition))));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("ViewPagerState", String.valueOf(state));
            }
        });


        // Code for above or equal 23 API Oriented Device
        leanImageViewerPagerAdapter = new LeanImageViewerPagerAdapter(images, this);
        viewPager.setAdapter(leanImageViewerPagerAdapter);
        viewPager.setCurrentItem(startPosition);
        leanImageViewerPagerAdapter.setImageTapped(this);
        mImageUri = FileProvider.getUriForFile(context, FILE_PROVIDER, new File(URI.create(images.get(startPosition))));


        hideToolBarMomentarily();
    }

    /**
     *
     * @return
     */
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(ImageViewerActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * ask for Permission
     */
    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(ImageViewerActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(ImageViewerActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(ImageViewerActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Permission Granted, Now we can use local drive .");
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else {
                    Log.d("Permission", "Permission Denied, we cannot use local drive .");
                }
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        hideToolBarMomentarily();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideToolBarMomentarily();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share: {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, mImageUri);
                context.startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_using)));

            }
        }
        return false;
    }
    @Override
    public void ImageTapped() {
            giveToolBarAnim();
            new Handler()
                    .postDelayed(() -> {
                        if (toolbar != null) {
                            if (toolbarAnimator != null)
                                toolbarAnimator.cancel();
                        }

                    }, 2000);

    }

    public  void giveToolBarAnim(){
        if (toolbar == null)
            return;
        else if (toolbarAnimator != null)
            toolbarAnimator.cancel();
        //this might get weird now
        toolbarAnimator = toolbar.animate();
        if (toolbar.getAlpha() > 0f) {
            toolbarAnimator.alpha(0f);
        } else {
            toolbarAnimator.alpha(1f);
        }
        toolbarAnimator.setDuration(100);
        toolbarAnimator.start();
    }

    private void hideToolBarMomentarily(){
        new Handler()
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (toolbar != null) {
                          giveToolBarAnim();
                        }

                    }},2000 );
           }
}
