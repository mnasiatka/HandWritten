package com.ythogh.handwritten;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CHOOSE_MULTIPLE_PHOTOS = 0;
    private static final int TAKE_PHOTO = 1;

    Button reset, save, takePhoto, choosePhoto;
    ImageView imageView;
    LinearLayout drawing_layout;

    ArrayList<Bitmap> dirImages;
    Bitmap bmp, sigBmp;
    File imageFile;
    String imageFilename;

    CaptureSignatureView mSig;
    signature mSignature;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dirImages = new ArrayList<>();

        drawing_layout = (LinearLayout) findViewById(R.id.drawing_view);
        imageView = (ImageView) findViewById(R.id.imageview);
        reset = (Button) findViewById(R.id.reset);
        save = (Button) findViewById(R.id.save);
        takePhoto = (Button) findViewById(R.id.take_photo);
        choosePhoto = (Button) findViewById(R.id.choose_photo);

        reset.setOnClickListener(this);
        save.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        choosePhoto.setOnClickListener(this);

        mSignature = new signature(this, null);
        mSignature.setBackgroundColor(Color.BLACK);
        //drawing_layout.addView(mSignature);

        mSig = new CaptureSignatureView(this, null);
        drawing_layout.addView(mSig);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.reset) {
            mSig.ClearCanvas();
        } else if (id == R.id.save) {
            sigBmp = mSig.getBitmap();
            imageView.setImageBitmap(sigBmp);
        } else if (id == R.id.take_photo) {
            takePhoto();
        } else if (id == R.id.choose_photo) {
            choosePhotos();
        }
    }

    private void onGottenImagesFromGallery() {
        Log.e("Count", dirImages.size() + " images chosen");
    }

    private void onTakenPhoto() {
        Log.e("Count", dirImages.size() + " images chosen");
    }

    private void choosePhotos() {
        Intent intentPick = new Intent(Intent.ACTION_PICK);
        intentPick.setType("image/*");
        intentPick.setAction(Intent.ACTION_GET_CONTENT);
        intentPick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intentPick, CHOOSE_MULTIPLE_PHOTOS);
    }

    private void takePhoto() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                imageFilename = "JPEG_" + "itemPic" + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(imageFilename, ".jpg", storageDir);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                imageFilename = image.getAbsolutePath();
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_MULTIPLE_PHOTOS) {
            dirImages.clear();
            Bitmap bmp;
            ClipData cp = data.getClipData();
            if (cp != null) {
                for (int i = 0; i < cp.getItemCount(); i++) {
                    try {
                        ClipData.Item item = cp.getItemAt(i);
                        bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), item
                                .getUri());
                        dirImages.add(bmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else {
                if (data.getData() != null) {
                    try {
                        bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                        dirImages.add(bmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            onGottenImagesFromGallery();
        } else if (requestCode == TAKE_PHOTO) {
            try {
                dirImages.clear();
                imageFile = new File(imageFilename);
                bmp = BitmapFactory.decodeFile(imageFilename);
                System.out.println("base image size in main activity 1: " + bmp.getHeight() + ", " + bmp.getWidth());
                int o = resolveBitmapOrientation(new File(imageFilename));
                bmp = applyOrientation(bmp, o);
//                    width = imageBitmap.getWidth();
//                    height = imageBitmap.getHeight();
//
//                    scaleWidth = ((float) newWidth) / width;
//                    scaleHeight = ((float) newHeight) / height;
//                    scale = Math.min(scaleHeight,scaleWidth);
//
//                    matrix = new Matrix();
//                    matrix.postScale(scale, scale);
//                    matrix.postRotate(0);
//
//                    imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix, true);
//                    Log.e("SIZE", "Width: " + imageBitmap.getWidth() + ", Height: " + imageBitmap
//                            .getHeight());
//                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, false);

                dirImages.add(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int resolveBitmapOrientation(File file) throws IOException {
        ExifInterface exif = null;
        exif = new ExifInterface(file.getAbsolutePath());

        return exif
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }

    private Bitmap applyOrientation(Bitmap bitmap, int orientation) {
        int rotate = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            default:
                return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }
}
