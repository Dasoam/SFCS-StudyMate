package com.dadash.sfcsnotes;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
public class clearUserData {
    public static void clearDefaultData(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    public static void clearCustomData(Context context, String prefsName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    public static void clearAllData(Context context) {
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor defaultEditor = defaultPrefs.edit();
        defaultEditor.clear();
        defaultEditor.apply();
        String[] prefsNames = {"UserProfilePrefs", "OtherPrefs"};
        for (String prefsName : prefsNames) {
            SharedPreferences customPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
            SharedPreferences.Editor customEditor = customPrefs.edit();
            customEditor.clear();
            customEditor.apply();
        }
    }
    public static void clearFirestoreCache(Context context,FirebaseFirestore firestore) {
        firestore.clearPersistence()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                    } else {
                    }
                });
    }
    public static void deleteLocalFiles(Context context) {
        File dir = context.getFilesDir();
        if (dir != null && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }
    }
}
