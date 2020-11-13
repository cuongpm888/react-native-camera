package com.me;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.android.cameraview.CameraViewImpl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class Utils {
   private Utils() {

   }

   public static void saveImage(Context context, byte[] data, Camera camera,
                                CameraViewImpl.Callback mCallback) {
      SavePhotoAsyncTask task = new SavePhotoAsyncTask(context,data, camera,mCallback);
      task.execute();
   }

   public static void clearImageCache(Context context) {
      File directory = getCacheDir(context);
      deleteDir(directory);
   }


   private static class SavePhotoAsyncTask extends AsyncTask<byte[], String, Void> {
      private final byte[] data;
      private final Camera camera;
      private final WeakReference<Context> context;
      private final  CameraViewImpl.Callback mCallback;

      public SavePhotoAsyncTask(Context ctx, byte[] d, Camera c,
                                CameraViewImpl.Callback cb) {
         this.data = d;
         this.camera = c;
         this.context = new WeakReference<>(ctx);
         this.mCallback = cb;
      }

      @Override
      protected Void doInBackground(byte[]... jpeg) {

         if (data == null || context.get() == null) return null;

         Camera.Parameters parameters = camera.getParameters();
         Camera.Size size = parameters.getPreviewSize();
         YuvImage image = new YuvImage(data, ImageFormat.NV21,
                                       size.width, size.height, null);
         Rect rectangle = new Rect();
         rectangle.bottom = size.height;
         rectangle.top = 0;
         rectangle.left = 0; 
         rectangle.right = size.width;
         ByteArrayOutputStream out2 = new ByteArrayOutputStream();
         image.compressToJpeg(rectangle, 20, out2);
         byte[] imageBytes = out2.toByteArray();
         Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);


         File directory = getCacheDir(context.get());
         File photo = new File(directory, System.currentTimeMillis() + ".jpg");

         try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
         } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
         }

         mCallback.onCameraCapture(photo.getPath());
         return null;
      }
   }

   private static File getCacheDir(Context context) {
      ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
      return cw.getDir("camera_cache", Context.MODE_PRIVATE);
   }

   private static boolean deleteDir(File dir) {
      if (dir != null && dir.isDirectory()) {
         String[] children = dir.list();
         for (int i = 0; i < children.length; i++) {
            boolean success = deleteDir(new File(dir, children[i]));
            if (!success) {
               return false;
            }
         }
         return dir.delete();
      } else if(dir!= null && dir.isFile()) {
         return dir.delete();
      } else {
         return false;
      }
   }
}
