package com.example.magicportrait;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.example.magicportrait.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'opencvnativec' library on application startup.
    static {
        System.loadLibrary("opencvnativec");
    }

    private Bitmap content = null;
    private Uri contentURI = null;

    ActivityResultLauncher<Intent> galleryLauncher, cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        content = BitmapFactory.decodeResource(this.getResources(), R.drawable.content);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null){
                            contentURI = result.getData().getData();
                            changeActivity();
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null){
                            Bundle bundle = result.getData().getExtras();
                            Bitmap cameraImage = (Bitmap) bundle.get("data");
                            contentURI = getImageUri(getApplicationContext(), cameraImage);
                            changeActivity();
                        }
                    }
                });
    }

    public void pickImageFromGallery(View view){
        Intent intentGallery = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intentGallery);
    }

    public void getImageFromCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null)
            cameraLauncher.launch(intent);
    }

    public void runSample(View view){
        content = BitmapFactory.decodeResource(this.getResources(), R.drawable.content);
        contentURI = getImageUri(getApplicationContext(), content);
        changeActivity();
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,
                "Title", null);
        return Uri.parse(path);
    }

    private void changeActivity(){
        Intent intentActivity = new Intent(getApplicationContext(), TransformerActivity.class);
        intentActivity.putExtra("contentURI", contentURI);
        startActivity(intentActivity);
    }

}