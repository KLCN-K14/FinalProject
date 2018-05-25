package com.klcn.xuant.transporter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class Base64Utils {

    private Context mContext;
    public Base64Utils(Context context){
        this.mContext = context;
    }
    // get dic chi hinh anh sau khi chon tu diwn thoai

    public Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                mContext.getContentResolver().openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                mContext.getContentResolver().openInputStream(selectedImage), null, o2);
    }
    // chuyen hinh anh sang nhi phan: blob
    private byte[] getByteArrayFromBitmap(Bitmap bm){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    // chuyen hinh anh sang nhi phan: blob
    public String getStringFromBitmap(Bitmap bm){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String encodeImage = Base64.encodeToString(byteArray,Base64.DEFAULT);
        return encodeImage;
    }
}