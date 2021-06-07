package com.ashunevich.qrscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.util.Size;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.ashunevich.qrscanner.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    ActivityMainBinding binding;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate (getLayoutInflater ());
        setContentView(binding.getRoot());
        setClickTener();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
        setTextChangeListenere();
    }

    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera ();
            } else {
                Toast.makeText (this, "Camera Permission Denied", Toast.LENGTH_SHORT).show ();
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future
                // This should never be reached
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void setClickTener(){

        binding.textView2.setOnClickListener (view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(binding.textView2.getText ().toString ()));
            startActivity(browserIntent);
        });

    }

    private void setTextChangeListenere(){
        binding.textView2.addTextChangedListener (new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkHyperlink(editable.toString ());
            }
        });
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        binding.previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(binding.previewView.createSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution (new Size (350,450))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String _qrCode) {
                if(!_qrCode.equals (binding.textView2.getText ().toString ())){
                    binding.textView2.setText("");
                    binding.textView2.setText (_qrCode);
                }

            }

            @Override
            public void qrCodeNotFound() {

            }
        }));
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }

    private void checkHyperlink(String s){
        boolean isValid = URLUtil.isValidUrl(s) && Patterns.WEB_URL.matcher(s).matches();
        if(isValid){
           binding.textView2.setEnabled (true);
            binding.textView2.setClickable (true);
            binding.textView2.setTextColor (Color.BLUE);
           binding.textView2.setTypeface (null, Typeface.ITALIC);
        }
        else{
            binding.textView2.setEnabled (false);
            binding.textView2.setClickable (false);
            binding.textView2.setTextColor (binding.textView.getCurrentTextColor ());
            binding.textView2.setTypeface (null, Typeface.NORMAL);
        }
    }
    }
