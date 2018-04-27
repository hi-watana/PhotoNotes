package jp.ac.titech.itpro.sdl.phototaker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static int REQ_PHOTO = 1234;
    private Bitmap photoImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button photoButton = findViewById(R.id.photo_button);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // TODO: You should setup appropriate parameters for the intent
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                PackageManager packageManager = getPackageManager();
                List activities = packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (activities.size() > 0) {
                    startActivityForResult(intent, REQ_PHOTO);
                }
                else {
                    Toast.makeText(MainActivity.this,
                            R.string.toast_no_activities, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showPhoto() {
        if (photoImg == null) return;
        ImageView photoView = findViewById(R.id.photo_view);
        photoView.setImageBitmap(photoImg);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        switch (reqCode) {
        case REQ_PHOTO:
            if (resCode == RESULT_OK) {
                // TODO: You should implement the code that retrieve a bitmap image
                Bundle extras = data.getExtras();
                photoImg = (Bitmap) extras.get("data");
                showPhoto();
            }
            break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPhoto();
    }
}
