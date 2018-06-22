package com.biniam.rss.leanrssImageViewer;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.List;

/**
 * Created by biniam on 6/5/17.
 */

public class LeanImageViewerPagerAdapter extends PagerAdapter {
    public static final String TAG = LeanImageViewerPagerAdapter.class.getSimpleName();
    ImageFileFinder imageFileFinder = new ImageFileFinder();

    private File imagepath;
    private String[] imagefilePath;
    private String[] imagefilename;
    private int position;
    private ImageTapped mimageTapped;

    private File imageDir;


    private List<String> images;
    private Context context;


    public LeanImageViewerPagerAdapter(List<String> images, Context context) {
        this.images = images;
        this.context = context;
    }



    /**
     * @param context
     */
    LeanImageViewerPagerAdapter(Context context, File imageDir, File imagePath) {

        this.context = context;
        this.imageDir = imageDir;
        this.imagepath = imagePath;
        imageFileFinder.getImageInfo(context, imageDir);
        imagefilePath = imageFileFinder.getImageFilePath();
        imagefilename = imageFileFinder.getImageFileName();
        Log.d("Count", "Count is=\t" + String.valueOf(imagefilePath.length));
        // feedItemsActivity.setImageClicked(this);
    }

    public String[] getImagefilePath() {
        return imagefilePath;

    }

    public String[] getImagefilename() {
        return imagefilename;
    }

    /**
     * An Interface to monitor image tap event
     * @param mimageTapped
     */
    public void setImageTapped(ImageTapped mimageTapped){
        this.mimageTapped = mimageTapped;
    }

    public int spitImagePosition(){
        //Log.d("ClickedImagePath:\t",getImagepath());
        for (int i = 0; i <imagefilePath.length ; i++) {
            if (imagefilePath[i].equals(getImagepath())){
                return i;
            }
        }
        return -1;
    }

    public String getImagepath() {
        return imagepath.toString();
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getCount() {
        return images.size();

    }

    @Override
    public View instantiateItem(final ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                // you have to send the event to the main Activity inorder to handle the Toolbar movement properly
                Log.d("Tap", "You Tapped the image ");
                mimageTapped.ImageTapped();

            }
        });
        //Log.d(TAG,"clickedImage"+spitImagePosition());
//            photoView.setImageURI(Uri.parse(imagefilePath[position]));
        //Glide.with(context).load(Uri.parse(imagefilePath[position])).into(photoView);
        //RequestBuilder requestBuilder = new RequestBuilder();
        //requestBuilder.load(Uri.parse(imagefilePath[position]));

        Glide.with(context).load(Uri.parse(images.get(position))).into(photoView);
        photoView.setOnOutsidePhotoTapListener(new OnOutsidePhotoTapListener() {
            @Override
            public void onOutsidePhotoTap(ImageView imageView) {
                mimageTapped.ImageTapped();
            }
        });

        // Now just add PhotoView to ViewPager and return it
        container.addView(
                photoView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    /**
     *
     */
    public interface ImageTapped {
        void ImageTapped();
    }
}