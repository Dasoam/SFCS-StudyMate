package com.dadash.sfcsnotes.User_Profile;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.dadash.sfcsnotes.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class profile_upload_id extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 1;
    private File storedImageFile;
    public profile_upload_id() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_upload_id, container, false);
        LinearLayout uploadContainer = view.findViewById(R.id.profile_upload_container);
        LinearLayout viewContainer = view.findViewById(R.id.profile_view_id_container);
        storedImageFile = new File(requireContext().getFilesDir(), "id_card.png");
        if (storedImageFile.exists()) {
            uploadContainer.setEnabled(false);
            uploadContainer.setAlpha(0.5f);
            Toast.makeText(requireContext(), "ID card already uploaded!", Toast.LENGTH_SHORT).show();
        } else {
            uploadContainer.setOnClickListener(v -> openImagePicker());
        }
        viewContainer.setOnClickListener(v -> {
            if (storedImageFile.exists()) {
                openImagePreviewFragment(storedImageFile.getAbsolutePath());
            } else {
                Toast.makeText(requireContext(), "No ID card uploaded yet!", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                    storedImageFile = saveImageToLocalStorage(bitmap);
                    Toast.makeText(requireContext(), "ID card uploaded successfully!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to upload ID card!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private File saveImageToLocalStorage(Bitmap bitmap) throws IOException {
        File storageDir = requireContext().getFilesDir();
        File file = new File(storageDir, "id_card.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        return file;
    }
    private void openImagePreviewFragment(String imagePath) {
        profile_view_id fragment = profile_view_id.newInstance(imagePath);
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
