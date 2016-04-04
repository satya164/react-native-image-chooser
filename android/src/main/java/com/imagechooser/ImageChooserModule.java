package com.imagechooser;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.io.File;


public class ImageChooserModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final int PICK_IMAGE = 3500;
    private static final String ERR_PICKER_CANCELLED = "ERR_PICKER_CANCELLED";
    private static final String ERR_FAILED_TO_PICK = "ERR_FAILED_TO_PICK";

    private Promise mPickerPromise;

    public ImageChooserModule(ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ImageChooserModule";
    }

    private void resolvePromise(WritableMap map) {
        if (mPickerPromise != null) {
            mPickerPromise.resolve(map);
            mPickerPromise = null;
        }
    }

    private void rejectPromise(String code, String reason) {
        if (mPickerPromise != null) {
            mPickerPromise.reject(code, reason);
            mPickerPromise = null;
        }
    }

    private void rejectPromise(Exception reason) {
        if (mPickerPromise != null) {
            mPickerPromise.reject(reason);
            mPickerPromise = null;
        }
    }

    @Nullable
    private String getPathFromUri(Uri contentUri) {
        if (contentUri.getScheme().equals("file")) {
            return contentUri.getPath();
        }

        String[] projection = {MediaStore.Images.Media.DATA};

        CursorLoader loader = new CursorLoader(getReactApplicationContext(), contentUri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();

        try {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();

            return cursor.getString(column_index);
        } catch (RuntimeException e) {
            return null;
        } finally {
            cursor.close();
        }
    }

    private String getNameFromUri(Uri contentUri) {
        if (contentUri.getScheme().equals("file")) {
            return contentUri.getLastPathSegment();
        }

        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};

        Cursor metaCursor = getReactApplicationContext().getContentResolver().query(contentUri, projection, null, null, null);

        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    return metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }

        return contentUri.getLastPathSegment();
    }

    private long getSizeFromUri(Uri contentUri) {
        if (contentUri.getScheme().equals("file")) {
            return new File(contentUri.getPath()).length();
        }

        Cursor cursor = getReactApplicationContext().getContentResolver().query(contentUri, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));

            cursor.close();

            return size;
        }

        return 0;
    }

    @Nullable
    private WritableMap getImageData(@Nullable  Uri uri) {
        if (uri == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        String path = getPathFromUri(uri);

        if (path != null) {
            BitmapFactory.decodeFile(path, options);

            WritableMap map = Arguments.createMap();

            map.putInt("height", options.outHeight);
            map.putInt("width", options.outWidth);
            map.putDouble("size", getSizeFromUri(uri));
            map.putString("name", getNameFromUri(uri));
            map.putString("uri", uri.toString());

            return map;
        } else {
            return null;
        }
    }

    @ReactMethod
    public void pickImage(final Promise promise) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            mPickerPromise = promise;
        } else {
            promise.reject("Activity doesn't exist");
            return;
        }

        try {
            final Intent galleryIntent = new Intent(Intent.ACTION_PICK);

            galleryIntent.setType("image/*");

            final Intent chooserIntent = Intent.createChooser(galleryIntent, "Pick an image");

            currentActivity.startActivityForResult(chooserIntent, PICK_IMAGE);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == PICK_IMAGE) {
            if (mPickerPromise != null) {
                if (resultCode == Activity.RESULT_CANCELED) {
                    rejectPromise(ERR_PICKER_CANCELLED, "Image picker was cancelled");
                } else if (resultCode == Activity.RESULT_OK) {
                    try {
                        Uri uri = intent.getData();
                        WritableMap map = getImageData(uri);

                        if (map != null) {
                            resolvePromise(map);
                        } else {
                            rejectPromise(ERR_FAILED_TO_PICK, "Failed to pick image: " + uri);
                        }
                    } catch (Exception e) {
                        rejectPromise(e);
                    }
                }
            }
        }
    }
}
