package jp.ac.titech.itpro.sdl.photonotes;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PhotoViewActivity extends AppCompatActivity {
    public static final String PHOTOPOS_EXTRA = "photo_pos";

    private final static int BACK = 100;
    private final static int FILE_DELETED = 123;
    private Uri fileUri = null;
    private String filename = null;
    private EditText noteText = null;

    private int EXTENSION_JPG_LENGTH = 3;
    private String EXTENSION_TXT = "txt";
    private final static String KEY_NAME = "PhotoViewActivity.filename";

    private File getTextFile() {
        return new File(this.getFilesDir(), filename.substring(0, filename.length() - EXTENSION_JPG_LENGTH) + EXTENSION_TXT);
    }

    private String getTextFromFile() {
        StringBuffer sb;
        try (BufferedReader br = new BufferedReader(new FileReader(getTextFile()))) {
            String str = br.readLine();
            if (str != null)
                sb = new StringBuffer(str);
            else
                sb = new StringBuffer();

            str = br.readLine();
            while (str != null) {
                sb.append("\n").append(str);
                str = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return sb.toString();
    }

    private void setTextToFile(String str) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(getTextFile()))) {
            pw.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (noteText != null) {
                String str = noteText.getText().toString();
                setTextToFile(str);
            }
            Log.d("debug", "Write");

            setResult(BACK);
            finish();
        });

        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> new AlertDialog.Builder(PhotoViewActivity.this)
                .setTitle("CONFIRM")
                .setMessage("Do you really delete this note?")
                .setPositiveButton("OK", (dialog, which) -> {
                    deleteFile(filename);
                    deleteFile(getTextFile().getName());
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

        noteText = findViewById(R.id.note_text);
        noteText.setText(getTextFromFile());

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
