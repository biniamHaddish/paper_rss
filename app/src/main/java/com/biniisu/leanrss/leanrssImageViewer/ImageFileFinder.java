package com.biniisu.leanrss.leanrssImageViewer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by biniam on 6/3/17.
 */

public class ImageFileFinder {

    public static final String TAG=ImageFileFinder.class.getSimpleName();
    private File[]   listFile;
    private String[] imageFilePath;
    private String[] imageFileName;
    private Context context;

    public File[] getListFile() {
        return listFile;
    }

    public String[] getImageFilePath() {
        return imageFilePath;
    }

    public String[] getImageFileName() {
        return imageFileName;
    }


    public  void getImageInfo(Context context,File imageFileDir){
          this.context=context;
        Log.d("imageDir", String.valueOf(imageFileDir));

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
           // Log.d("DataInfo","No SD Card Mounted");
        }else{
            if (isExternalStorageReadable())
                Log.d(TAG, "isReadable: \t"+isExternalStorageReadable());
                Log.d("DataInfo","Directory Created "+ imageFileDir);
        }

        if (imageFileDir.isDirectory()) {
            Log.d("DataInfo","Confirmed that the Dir is real dir\t"+ imageFileDir);
            if (!imageFileDir.canRead()){
                Log.d("DataInfo ","ImageFeileDir Car read Dir\t"+imageFileDir.canRead());
               return;
            }
            Log.d("DataInfo","Confirmed that the Dir can read\t"+ imageFileDir.canRead());

            listFile= imageFileDir.listFiles();
         Log.d("DataInfo","List of Image files\t"+ imageFileDir.listFiles().toString());
            if (listFile!=null&& imageFileDir !=null) {  // check the file list and image file vars
                imageFilePath = new String[listFile.length];
                imageFileName = new String[listFile.length];
                Log.d("DataInfo","image file names \t"+imageFilePath+"File name\t"+imageFileName);
                for (int i = 0; i < listFile.length; i++) {
                    Log.d("ImageInfo", imageFilePath[i] = listFile[i].getAbsolutePath());
                    Log.d("ImageInfo", listFile[i].getName());
                    // Dont' add .bin files to the list
                    imageFilePath[i] = listFile[i].getAbsolutePath();
                    imageFileName[i] = listFile[i].getName();

                }
            }
        }
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
