package jp.ac.titech.itpro.sdl.photonotes;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class PhotoViewActivity extends AppCompatActivity {
    public static final String PHOTOPOS_EXTRA = "photo_pos";

    private final static int FILE_DELETED = 100;
    private Uri fileUri = null;
    private String filename = null;

    private final static String KEY_NAME = "PhotoViewActivity.filename";


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
        photoButton.setOnClickListener(v -> new AlertDialog.Builder(PhotoViewActivity.this)
                .setTitle("CONFIRM")
                .setMessage("Do you really delete this note?")
                .setPositiveButton("OK", (dialog, which) -> {
                    deleteFile(filename);
                    setResult(FILE_DELETED);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show());

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
