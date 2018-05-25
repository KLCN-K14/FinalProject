package com.klcn.xuant.transporter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * Created by xuant on 18/05/2017.
 */

public class ConvertBitmap {
    private Context mContext;
    public ConvertBitmap(Context context){
        this.mContext = context;
    }
    // mã hóa bitmap theo   đường dẫn tới ảnh
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
    // chuyển bit map thành chuỗi byte
    private byte[] getByteArrayFromBitmap(Bitmap bm){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    // Chuyển bitmap thành chuỗi 64bit
    public String getStringFromBitmap(Bitmap bm){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String encodeImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encodeImage;
    }
}