package com.example.library;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

// פונקציות עזר לטיפול בתמונות (כריכות ספרים ותמונת פרופיל)
public final class ImageUtils {

    private static final String TAG = "ImageUtils";

    private ImageUtils() {}

    // מציגה תמונה לפי Uri. אם אין הרשאה לקרוא אותה (למשל תמונה מהגלריה שההרשאה אליה פגה),
    // במקום שהאפליקציה תקרוס מוצגת תמונת ברירת המחדל ומוחזר false.
    public static boolean showImage(ImageView imageView, Uri uri, int placeholderRes) {
        if (uri == null) {
            imageView.setImageResource(placeholderRes);
            return false;
        }
        try {
            imageView.setImageURI(uri);
            return true;
        } catch (SecurityException e) {
            Log.w(TAG, "No permission to read image: " + uri, e);
            imageView.setImageResource(placeholderRes);
            return false;
        }
    }

    // מעתיקה תמונה שנבחרה מהגלריה לתיקייה פרטית של האפליקציה ומחזירה Uri לקובץ החדש.
    // כך אפשר לפתוח את התמונה גם בפעם הבאה, בלי לתלות בהרשאה של הגלריה.
    // במקרה של כישלון מוחזר null.
    public static Uri copyToAppStorage(Context context, Uri source, String folderName) {
        File folder = new File(context.getFilesDir(), folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File target = new File(folder, "img_" + System.currentTimeMillis() + ".jpg");
        try (InputStream in = context.getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            if (in == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return Uri.fromFile(target);
        } catch (Exception e) {
            Log.w(TAG, "Could not copy image: " + source, e);
            return null;
        }
    }
}
