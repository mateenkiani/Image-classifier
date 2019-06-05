package com.example.machinelearning;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class BarCodeScannerFragment extends Fragment {


    public static final int PICK_IMAGE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;
    public static String tag = "ML-APP";
    FirebaseVisionImage image;
    Bitmap bitmap;
    FirebaseVisionBarcodeDetector detector;
    TextView resultTextView;
    ImageView chosenImage;
    ProgressBar pb;
    Button scanImage;
    ImageButton openCameraButton;
    ImageButton openPhotosButton;
    String currentPhotoPath;
    Uri photoURI;
    private View view;
    TextView openPhotosTv;
    TextView openCameraTv;
    String s = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bar_code_scanner, container, false);
        chosenImage = view.findViewById(R.id.barcode_scanner_selected_image);
        scanImage = view.findViewById(R.id.barcode_scanner_scan_image_button);
        resultTextView = view.findViewById(R.id.barcode_scanner_scanned_text);
        pb = view.findViewById(R.id.barcode_scanner_progressbar);
        openCameraTv = view.findViewById(R.id.barcode_scanner_textView2);
        openPhotosTv = view.findViewById(R.id.barcode_scanner_textView3);
        openPhotosButton = view.findViewById(R.id.barcode_scanner_open_photos_image_view);
        openCameraButton = view.findViewById(R.id.barcode_scanner_open_camera_image_view);
        FirebaseApp.initializeApp(getContext());
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        photoURI = FileProvider.getUriForFile(getContext(),
                                "com.example.machinelearning.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        openPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                } catch (Exception e) {
                    //Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        scanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImage(v);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            openPhotosButton.setVisibility(View.GONE);
            openCameraButton.setVisibility(View.GONE);
            openCameraTv.setVisibility(View.GONE);
            openPhotosTv.setVisibility(View.GONE);
            scanImage.setVisibility(View.VISIBLE);
            chosenImage.setVisibility(View.VISIBLE);

        }
        switch (requestCode) {
            case PICK_IMAGE:
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    chosenImage.setImageBitmap(bitmap);
                    chosenImage.setVisibility(View.VISIBLE);
                    image = FirebaseVisionImage.fromBitmap(bitmap);
                    detector = FirebaseVision.getInstance()
                            .getVisionBarcodeDetector();
                } catch (Exception e) {
                    Log.i(tag, e.getMessage());
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_IMAGE_CAPTURE:
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoURI);
                    chosenImage.setImageBitmap(bitmap);
                    chosenImage.setVisibility(View.VISIBLE);
                    image = FirebaseVisionImage.fromBitmap(bitmap);
                    FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                            .getVisionBarcodeDetector();
                } catch (Exception e) {
                    Log.i(tag, e.getMessage());
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    void processImage(final View view) {

        chosenImage.setVisibility(view.INVISIBLE);
        scanImage.setVisibility(View.INVISIBLE);
        pb.setVisibility(view.VISIBLE);


        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {


                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        for (FirebaseVisionBarcode barcode : barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();
                            Toast.makeText(getContext(), rawValue, Toast.LENGTH_LONG).show();
                            int valueType = barcode.getValueType();
                            //Toast.makeText(getContext(), valueType, Toast.LENGTH_LONG).show();
                            // See API reference for complete list of supported types
                            switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_WIFI:
                                    String ssid = barcode.getWifi().getSsid();
                                    s=s+"SSID"+ssid+"\n";
                                    String password = barcode.getWifi().getPassword();
                                    s=s+"password"+password+"\n";
                                    int type = barcode.getWifi().getEncryptionType();
                                    s=s+"Type"+type+"\n";
                                    break;
                                case FirebaseVisionBarcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    s=s+"Title"+title+"\n";
                                    String url = barcode.getUrl().getUrl();
                                    s=s+"URL"+url+"\n";
                                    break;
                            }

                        }
                        //Toast.makeText(getContext(),s, Toast.LENGTH_LONG).show();
                        if (s.equals("")) {
                            s = "Couldn't Find any Object";
                        }
                        resultTextView.setVisibility(view.VISIBLE);
                        resultTextView.setText(s);
                        pb.setVisibility(view.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Failed",Toast.LENGTH_LONG ).show();
                    }
                });
    }

}
