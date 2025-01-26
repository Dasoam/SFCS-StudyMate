package com.dadash.sfcsnotes.User_Profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.dadash.sfcsnotes.R;

import java.io.File;

public class profile_view_id extends Fragment {

    private static final String ARG_IMAGE_PATH = "imagePath";
    private String imagePath;
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private ImageView imageView;

    public profile_view_id() {
        // Required empty public constructor
    }

    public static profile_view_id newInstance(String imagePath) {
        profile_view_id fragment = new profile_view_id();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_view_id, container, false);

        imageView = view.findViewById(R.id.profile_id_view);

        // Set up ScaleGestureDetector
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        // Load image
        if (getArguments() != null) {
            imagePath = getArguments().getString(ARG_IMAGE_PATH);
            loadAndCenterImage();
        }

        // Set touch listener for zoom
        imageView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });

        return view;
    }

    private void loadAndCenterImage() {
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        centerImage(bitmap);
                        imageView.setImageBitmap(bitmap);
                    }
                });
            } else {
                Log.e("profile_view_id", "Image file does not exist: " + imagePath);
            }
        } else {
            Log.e("profile_view_id", "Image path is null.");
        }
    }

    private void centerImage(Bitmap bitmap) {
        int imageViewWidth = imageView.getWidth();
        int imageViewHeight = imageView.getHeight();

        if (imageViewWidth == 0 || imageViewHeight == 0) {
            Log.e("profile_view_id", "ImageView dimensions are zero.");
            return;
        }

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        float scaleX = (float) imageViewWidth / bitmapWidth;
        float scaleY = (float) imageViewHeight / bitmapHeight;
        float scale = Math.min(scaleX, scaleY);

        float dx = (imageViewWidth - bitmapWidth * scale) / 2;
        float dy = (imageViewHeight - bitmapHeight * scale) / 2;

        matrix.reset();
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);

        imageView.setImageMatrix(matrix);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            imageView.setImageMatrix(matrix);
            return true;
        }
    }
}
