package jp.ac.titech.itpro.sdl.photonotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PhotoViewActivity extends AppCompatActivity {
    public static final String PHOTOPOS_EXTRA = "photo_pos";

    private final static int FILE_DELETED = 100;
    private Uri fileUri = null;
    private String filename = null;

    private final static String KEY_NAME = "PhotoViewActivity.filename";

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){
        return FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                getOutputMediaFile(MEDIA_TYPE_IMAGE));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        File mediaStorageDir = this.getFilesDir();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            filename = "IMG_"+ timeStamp + ".jpg";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    filename);
        } else if(type == MEDIA_TYPE_VIDEO) {
            filename = "VID_"+ timeStamp + ".mp4";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    filename);
        } else {
            return null;
        }
        return mediaFile;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        if (savedInstanceState != null) {
            filename = savedInstanceState.getString(KEY_NAME);
            if (filename != null) {
                fileUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        new File(this.getFilesDir(), filename));
            }
        }

        Button photoButton = findViewById(R.id.delete_button);
        photoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(PhotoViewActivity.this)
                    .setTitle("CONFIRM")
                    .setMessage("Do you really delete this note?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // OK button pressed
                        //Intent intent = new Intent();
                        //intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        //PackageManager packageManager = getPackageManager();
                        //List activities = packageManager
                        //        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        //if (activities.size() > 0) {
                        //    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                        //    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        //    startActivityForResult(intent, REQ_PHOTO);
                        //}
                        //else {
                        //    Toast.makeText(PhotoViewActivity.this,
                        //            R.string.toast_no_activities, Toast.LENGTH_LONG).show();
                        //}
                        deleteFile(filename);
                        setResult(FILE_DELETED);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        filename = getIntent().getStringExtra(PHOTOPOS_EXTRA);
        if (filename != null) {
            fileUri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    new File(this.getFilesDir(), filename));
        } else {
            Toast.makeText(this, R.string.toast_no_such_photo, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showPhoto() {
        if (fileUri == null) {
            Log.d("debug", "fileUri == null");
            return;
        }
        ImageView photoView = findViewById(R.id.photo_view);
        photoView.setImageURI(fileUri);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPhoto();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (filename != null)
            outState.putString(KEY_NAME, filename);
    }
}
