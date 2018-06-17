package jp.ac.titech.itpro.sdl.photonotes;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static int REQ_PHOTO = 1234;
    private final static int REQ_VIEW = 2345;

    private final static int BACK = 100;
    private final static int FILE_DELETED = 123;

    private Uri fileUri = null;
    private String filename = null;

    private ArrayAdapter<String> photoListAdapter = null;
    private ListView photoListView = null;
    private List<String> filenames = null;

    private String EXTENSION_JPG = "jpg";
    private int EXTENSION_JPG_LENGTH = 3;
    private String EXTENSION_TXT = "txt";

    // Create a file Uri for saving an image
    private Uri getOutputMediaFileUri() {
        return FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                getOutputMediaFile(EXTENSION_JPG));
    }

    private String getDateFormatFromFilename(String filename) {
        return new StringBuffer().append(filename.substring(0, 4))
                .append("/")
                .append(filename.substring(4, 6))
                .append("/")
                .append(filename.substring(6, 8))
                .append(" ")
                .append(filename.substring(9, 11))
                .append(":")
                .append(filename.substring(11, 13))
                .append(":")
                .append(filename.substring(13, 15)).toString();
    }

    private List<String> getPhotoFilenames() {
        return Arrays.stream(
                new File(getFilesDir().getPath())
                        .listFiles())
                        .filter(File::isFile)
                        .map(File::getName)
                        .filter(s -> s.endsWith(EXTENSION_JPG))
                        .sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    private String getTopOfNotes(String filename) {
        Log.d("debug", "Read");
        File noteFile = new File(this.getFilesDir(), filename);
        String result = "(Empty)";
        String str;
        if (!noteFile.exists())
            return result;
        try (BufferedReader br = new BufferedReader(new FileReader(noteFile))) {
            str = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
        if (str != null) {
            result = str;
        }
        return result;
    }

    private void updatePhotoListAdapter() {
        photoListAdapter.clear();
        photoListAdapter.addAll(filenames);
    }

    // Create a File for saving an image
    private File getOutputMediaFile(String extension) {
        File mediaStorageDir = this.getFilesDir();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        filename = timeStamp + "." + extension;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                filename);
        return mediaFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        photoListView = findViewById(R.id.photo_list_view);

        photoListAdapter =
                new ArrayAdapter<String>(this, 0, new ArrayList<>()) {
                    @Override
                    public @NonNull
                    View getView(int pos, @Nullable View view, @NonNull ViewGroup parent) {
                        if (view == null) {
                            LayoutInflater inflater = LayoutInflater.from(getContext());
                            view = inflater.inflate(android.R.layout.simple_list_item_2,
                                    parent, false);
                        }
                        String filename = getItem(pos);
                        if (filename != null) {
                            File photoFile = new File(MainActivity.this.getFilesDir(), filename);
                            String noteFileName = filename.substring(0, filename.length() - EXTENSION_JPG_LENGTH) + EXTENSION_TXT;
                            if (photoFile.exists()) {
                                TextView noteHeadText = view.findViewById(android.R.id.text1);
                                TextView photoNameText = view.findViewById(android.R.id.text2);
                                noteHeadText.setText(getTopOfNotes(noteFileName));
                                photoNameText.setText("Created on " + getDateFormatFromFilename(photoFile.getName()) + ".");
                            }
                        }
                        return view;
                    }
                };

        photoListView.setAdapter(photoListAdapter);
        photoListView.setOnItemClickListener((parent, view, pos, id) -> {
            Intent intent = new Intent(MainActivity.this, PhotoViewActivity.class);
            intent.putExtra(PhotoViewActivity.PHOTOPOS_EXTRA, photoListAdapter.getItem(pos));
            startActivityForResult(intent, REQ_VIEW);
        });

        filenames = getPhotoFilenames();
        photoListAdapter.addAll(filenames);


        Button photoButton = findViewById(R.id.new_button);
        photoButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            PackageManager packageManager = getPackageManager();
            List activities = packageManager
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (activities.size() > 0) {
                fileUri = getOutputMediaFileUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, REQ_PHOTO);
            }
            else {
                Toast.makeText(MainActivity.this,
                        R.string.toast_no_activities, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        switch (reqCode) {
            case REQ_PHOTO:
                if (resCode == RESULT_OK) {
                    if (fileUri != null) {
                        // Rename filename (Time when NEW button was pressed -> Time when a photo was created)
                        File oldFile = new File(this.getFilesDir(), filename);
                        File newFile = getOutputMediaFile(EXTENSION_JPG);
                        if (!newFile.exists()) {
                            if (!oldFile.renameTo(newFile)) {
                                Log.d("debug", "Rename failed.");
                            }
                        }

                        Intent intent = new Intent(MainActivity.this, PhotoViewActivity.class);
                        intent.putExtra(PhotoViewActivity.PHOTOPOS_EXTRA, filename);
                        startActivityForResult(intent, REQ_VIEW);
                    } else {
                        Log.d("debug", "fileUri == null");
                    }
                }
                break;
            case REQ_VIEW:
                switch (resCode) {
                    case BACK:
                    case FILE_DELETED:
                        filenames = getPhotoFilenames();
                        updatePhotoListAdapter();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
}
