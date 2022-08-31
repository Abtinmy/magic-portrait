package com.example.magicportrait;

import static java.lang.Math.max;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.magicportrait.ml.Segmentor;
import com.example.magicportrait.ml.Stylemodel;
import com.example.magicportrait.ml.Transformer;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TransformerActivity extends AppCompatActivity {

    private Bitmap content, style, output;
    private ImageView imgView;
    private LoadingDialog loadingDialog;

    public static final int imageSizeSegmentation = 128;
    public static final int imageSizeStyle = 256;
    public static final int imageSizeContent = 384;
    public static final int imageSizeBlend = 512;

    ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transformer);

        Uri contentURI = (Uri) getIntent().getParcelableExtra("contentURI");

        imgView = findViewById(R.id.imageViewTransformer);
        imgView.setImageURI(contentURI);

        content = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        output = content;

        loadingDialog = new LoadingDialog(TransformerActivity.this);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null){
                            Uri selectedImage = result.getData().getData();

                            ContentResolver contentResolver = getContentResolver();
                            try {
                                if(Build.VERSION.SDK_INT < 28) {
                                    style = MediaStore.Images.Media.getBitmap(contentResolver,
                                            selectedImage);
                                } else {
                                    ImageDecoder.Source source = ImageDecoder
                                            .createSource(contentResolver, selectedImage);
                                    style = ImageDecoder.decodeBitmap(source);
                                    style = style.copy(Bitmap.Config.ARGB_8888, true);
                                    styleOnClick();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        initImageViews();
    }

    public void saveOutput(View view){
        Boolean res = storeImage(this.output);
        if (res)
            Toast.makeText(TransformerActivity.this, "Successfuly Saved",
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(TransformerActivity.this, "Save Failed",
                    Toast.LENGTH_SHORT).show();
    }

    public void doSegmentation(View view){
        Bitmap contentScaled = Bitmap.createScaledBitmap(content, imageSizeSegmentation,
                imageSizeSegmentation, false);
        float[] segmentedImage = segmentImage(contentScaled);
        Bitmap mask = getMask(segmentedImage);
        output = Bitmap.createScaledBitmap(mask, content.getWidth(), content.getHeight(), false);
        imgView.setImageBitmap(output);
    }

    public void doStyleTransfer(View view){
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.startLoadingDialog();
                    }
                });

                if (style == null)
                    style = ((BitmapDrawable) ((ImageView) findViewById(R.id.style1)).getDrawable())
                            .getBitmap();

                Bitmap contentScaled = Bitmap.createScaledBitmap(content, imageSizeContent,
                        imageSizeContent, false);
                Bitmap styleScaled = Bitmap.createScaledBitmap(style, imageSizeStyle, imageSizeStyle,
                        false);

                output = Bitmap.createScaledBitmap(styleTransfer(contentScaled, styleScaled),
                        content.getWidth(), content.getHeight(), false);
                imgView.setImageBitmap(output);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();

                    }
                });
            }
        });
    }

    private void initImageViews(){
        ImageView style1 = findViewById(R.id.style1);
        ImageView style2 = findViewById(R.id.style2);
        ImageView style3 = findViewById(R.id.style3);
        ImageView style4 = findViewById(R.id.style4);
        ImageView style5 = findViewById(R.id.style5);
        ImageView style6 = findViewById(R.id.style6);
        ImageView style7 = findViewById(R.id.style7);
        ImageView style8 = findViewById(R.id.style8);
        ImageView style9 = findViewById(R.id.style9);
        ImageView style10 = findViewById(R.id.style10);
        ImageView addStyle = findViewById(R.id.btnAddStyle);


        style1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style1);
            }
        });

        style2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style2);
            }
        });

        style3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style3);
            }
        });

        style4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style4);
            }
        });

        style5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style5);
            }
        });

        style6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style6);
            }
        });

        style7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style7);
            }
        });

        style8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style8);
            }
        });

        style9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style9);
            }
        });

        style10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                styleOnClick(style10);
            }
        });

        addStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGallery = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(intentGallery);
            }
        });

    }

    private void styleOnClick(ImageView style) {
        this.style = ((BitmapDrawable) style.getDrawable()).getBitmap();

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.startLoadingDialog();
                    }
                });

                output = objectTransfer();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                        imgView.setImageBitmap(output);
                    }
                });
            }
        });
    }

    private void styleOnClick() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.startLoadingDialog();
                    }
                });

                output = objectTransfer();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismissDialog();
                        imgView.setImageBitmap(output);
                    }
                });
            }
        });
    }

    private Bitmap objectTransfer(){
        int divider = (int) Math.ceil((double) max(content.getWidth(),
                                                   content.getHeight()) / imageSizeBlend);
        int dstWidth = (int) content.getWidth() / divider,
                dstHeight = (int) content.getHeight() / divider;

        // segmentation
        Bitmap contentScaledSegmentation = Bitmap.createScaledBitmap(content, imageSizeSegmentation,
                imageSizeSegmentation, false);
        float[] segmentedImage = segmentImage(contentScaledSegmentation);
        Bitmap mask = getMask(segmentedImage);

        // style transfer
        Bitmap contentScaledST = Bitmap.createScaledBitmap(content, imageSizeContent,
                imageSizeContent, false);
        Bitmap styleScaled = Bitmap.createScaledBitmap(style, imageSizeStyle, imageSizeStyle,
                false);
        Bitmap styledImage = styleTransfer(contentScaledST, styleScaled);

        // change size of mask and content images to be compatible with style transfer image output
        Bitmap contentResized = Bitmap.createScaledBitmap(content, dstWidth, dstHeight, false);
        Bitmap maskResized = Bitmap.createScaledBitmap(mask, dstWidth, dstHeight, false);
        Bitmap styleResized = Bitmap.createScaledBitmap(styledImage, dstWidth, dstHeight, false);

        Bitmap output = contentResized.copy(contentResized.getConfig(), true);
        blend(styleResized, contentResized, maskResized, output);
        return Bitmap.createScaledBitmap(output, content.getWidth(), content.getHeight(), false);
    }

    private float[] segmentImage(Bitmap image) {
        try {
            Segmentor model = Segmentor.newInstance(getApplicationContext());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3},
                    DataType.FLOAT32);

            inputFeature0.loadArray(bitmapToFloatArray(image, imageSizeSegmentation));
            Segmentor.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            model.close();
            return outputFeature0.getFloatArray();
        } catch (IOException e) {
            // TODO Handle the exceptiond
        }
        return null;
    }

    private Bitmap getMask(float[] segmentedImage){
        float[] target = new float[imageSizeSegmentation * imageSizeSegmentation * 3];

        for (int i = 0; i < imageSizeSegmentation * imageSizeSegmentation; ++i) {
            float maxValue = 0, maxArg = 1;
            for (int j = 0; j < 3; ++j){
                int index = i * 3 + j;
                if (j == 0){
                    maxValue = segmentedImage[index];
                    maxArg = j;
                }
                else if (maxValue < segmentedImage[index]){
                    maxValue = segmentedImage[index];
                    maxArg = j;
                }
            }

            if (maxArg == 1)
                maxArg = 0;
            else
                maxArg = 1;

            for (int j = 0; j < 3; j++){
                target[i * 3 + j] = maxArg * 255;
            }
        }

        return floatArrayToBitmap(target, imageSizeSegmentation);
    }

    private Bitmap styleTransfer(Bitmap content, Bitmap style){
        try {
            Transformer model = Transformer.newInstance(getApplicationContext());
            TensorImage contentImage = TensorImage.fromBitmap(content);
            TensorBuffer styleBottleneck = styleToFeature(style);

            Transformer.Outputs outputs = model.process(contentImage, styleBottleneck);
            TensorImage styledImage = outputs.getStyledImageAsTensorImage();
            Bitmap styledImageBitmap = styledImage.getBitmap();

            model.close();
            return styledImageBitmap;
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return null;
    }

    private TensorBuffer styleToFeature(Bitmap image){
        try {
            Stylemodel model = Stylemodel.newInstance(getApplicationContext());
            TensorImage styleImage = TensorImage.fromBitmap(image);

            Stylemodel.Outputs outputs = model.process(styleImage);
            TensorBuffer styleBottleneck = outputs.getStyleBottleneckAsTensorBuffer();

            model.close();
            return  styleBottleneck;
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return null;
    }

    private float[] bitmapToFloatArray(Bitmap input, int size){
        int[] intValues = new int[size * size];
        float[] floatValues = new float[size * size * 3];
        input.getPixels(intValues, 0, input.getWidth(), 0, 0, input.getWidth(),
                input.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }

        return floatValues;
    }

    private Bitmap floatArrayToBitmap(float[] input, int size){
        int[] intValues = new int[size * size];

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (input[i * 3] * 1)) << 16)
                            | (((int) (input[i * 3 + 1] * 1)) << 8)
                            | ((int) (input[i * 3 + 2] * 1));
        }

        Bitmap outputBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        outputBitmap.setPixels(intValues, 0, size, 0, 0, size, size);
        return outputBitmap;
    }

    private Boolean storeImage(Bitmap bitmap){
        OutputStream fos;
        try{
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "image.jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos);
                fos.flush();
                fos.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public native void blend(Bitmap bitmapInSrc, Bitmap bitmapInDst, Bitmap mask, Bitmap bitmapOut);

}