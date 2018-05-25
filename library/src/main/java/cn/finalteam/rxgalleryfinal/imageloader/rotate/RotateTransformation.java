package cn.finalteam.rxgalleryfinal.imageloader.rotate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * Created by pengjianbo  Dujinyang on 2016/8/16 0016.
 */
public class RotateTransformation extends BitmapTransformation {

//    private float rotateRotationAngle = 0f;
//
//    public RotateTransformation(Context context, float rotateRotationAngle) {
//        super(context);
//
//        this.rotateRotationAngle = rotateRotationAngle;
//    }
//
//    @Override
//    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(rotateRotationAngle);
//        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
//    }
//
//    @Override
//    public String getId() {
//        return "rotate" + rotateRotationAngle;
//    }


    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return null;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
