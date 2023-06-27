package io.kavith.tomato_app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import io.kavith.tomato_app.databinding.ActivityMainBinding;
import retrofit2.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    ImageView image;
    TextView imgPath;
    TextView resultTextView;
    Bitmap bitmap;

    Uri selectedImage;
    String part_image;

    Button selectImageBtn;
    ProgressBar progressBar;
    Button predictBtn;
    private static final int PICK_IMAGE_REQUEST = 9544;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectImageBtn = findViewById(R.id.selectImageBtn);
        imgPath = findViewById(R.id.selectedImageName);
        progressBar = findViewById(R.id.progressBar);
        image = findViewById(R.id.selectImagePreviewImageView);
        predictBtn = findViewById(R.id.predictBtn);
        predictBtn.setEnabled(false);
        resultTextView = findViewById(R.id.resultTextView);
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image.setImageBitmap(null);
                pick(v);
            }
        });

        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    predictBtn.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    resultTextView.setText("");
                    uploadImage(v);
                } catch (URISyntaxException e) {
                    predictBtn.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    throw new RuntimeException(e);
                }
            }
        });

    }

    // Method for starting the activity for selecting image from phone storage
    public void pick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select Picture"),PICK_IMAGE_REQUEST);
    }

    private String getImagePathFromUri(Uri uri) {
        String imagePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return imagePath;
    }

    // Method to get the absolute path of the selected image from its URI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    image.setImageBitmap(bitmap);
                    String imagePath = getPath(imageUri);// Get the image file URI
                    imgPath.setText(imagePath);
                    System.out.println("Image Path "+ imagePath);
                    part_image = imagePath;
                    predictBtn.setEnabled(true);
                } catch (IOException e) {
                    predictBtn.setEnabled(false);
                    throw new RuntimeException(e);

                }

            }
        }
    }

    private String getPath(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
        }

        return path;
    }

    // Upload the image to the remote database
    public void uploadImage(View view) throws URISyntaxException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageBytes);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody);

        APIMethods methods = RetrofitClient.getRetrofitInstance().create(APIMethods.class);

        Call<TomatoAPIResponse> upload = methods.getTomatoAPIResponse(imagePart);
        upload.enqueue(new Callback<TomatoAPIResponse>() {
            @Override
            public void onResponse(Call<TomatoAPIResponse> call, Response<TomatoAPIResponse> response) {
                if(response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    TomatoAPIResponse tomatoAPIResponse = response.body();
                    resultTextView.setText(tomatoAPIResponse.disease_name);
                    progressBar.setVisibility(View.INVISIBLE);
                    predictBtn.setEnabled(true);
                }else {
                    Toast.makeText(MainActivity.this, "Image Upload Unsuccessful"+response.message(), Toast.LENGTH_SHORT).show();
                    System.out.println(response.message());
                    System.out.println(response.errorBody());
                    progressBar.setVisibility(View.INVISIBLE);
                    predictBtn.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<TomatoAPIResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MainActivity.this, "Request failed", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                predictBtn.setEnabled(true);
            }
        });
    }


}