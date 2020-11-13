package com.me;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.google.android.cameraview.CameraViewImpl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class Utils implements Handler.Callback {
   private Handler handler = null;
   private HandlerThread backgroundThread;
   private static final int MSG_SAVE_IMAGE = 0x00001;

   private static Utils instance = null;

   private Utils() {

   }
   public static Utils getInstance() {
      if (instance == null) {
         instance = new Utils();
      }

      return instance;
   }

   public void saveImage(Context context, byte[] data, Camera camera,
                                int rotateAngle, CameraViewImpl.Callback mCallback) {
      SavePhotoAsyncTask task = new SavePhotoAsyncTask(context, data, camera, rotateAngle,
                                                       mCallback);
      task.execute();
   }

   public void saveImage2(WeakReference<Context> context, byte[] data, Camera camera,
                          int rotateAngle, CameraViewImpl.Callback mCallback) {
      sendMessageToHandler(MSG_SAVE_IMAGE,
                           new SaveImageHolder(context, data, camera, rotateAngle, mCallback));
   }


   public static void clearImageCache(Context context) {
      File directory = getCacheDir(context);
      deleteDir(directory);
   }


   @Override
   public boolean handleMessage(Message msg) {
      int what = msg.what;
      switch (what) {
         case MSG_SAVE_IMAGE:
            Log.w("DUY_TAG", "onPreviewFrame in Camera1.java " + System.currentTimeMillis());
            WeakReference<Bitmap> bitmap;
            SaveImageHolder saveData = (SaveImageHolder) msg.obj;

            WeakReference<Context> context = saveData.context;
            byte[] data = saveData.data;
            Camera camera = saveData.camera;
            int rotateAngle = saveData.rotateAngle;
            CameraViewImpl.Callback mCallback = saveData.mCallback;
            if (data == null || context.get() == null) return false;

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
            image.compressToJpeg(rectangle, 100, out2);

            bitmap = rotateBitmap2(out2, rotateAngle);

            File directory = getCacheDir(context.get());
            File photo = new File(directory, System.currentTimeMillis() + ".jpg");

            try {
               FileOutputStream fos = new FileOutputStream(photo.getPath());
               bitmap.get().compress(Bitmap.CompressFormat.JPEG, 100, fos);

               fos.flush();
               fos.close();

               out2.flush();
               out2.close();
            } catch (java.io.IOException e) {
               Log.e("PictureDemo", "Exception in photoCallback", e);
            }

            mCallback.onCameraCapture(photo.getPath());
            bitmap.clear();

            break;
      }
      return true;
   }

   private void sendMessageToHandler(int message, Object obj) {
      if (checkThreadIsAlive()) {
         handler.obtainMessage(message, obj).sendToTarget();
      }
   }

   private boolean checkThreadIsAlive() {
      if (backgroundThread == null || !backgroundThread.isAlive()) {
         backgroundThread = new HandlerThread(Utils.class.getName());
         backgroundThread.setPriority(Thread.MIN_PRIORITY);
         backgroundThread.start();
         handler = new Handler(backgroundThread.getLooper(), this);
      }
      return true;
   }

   private static class SavePhotoAsyncTask extends AsyncTask<byte[], String, Void> {
      private final byte[] data;
      private final Camera camera;
      private final WeakReference<Context> context;
      private final CameraViewImpl.Callback mCallback;
      private Bitmap bitmap;
      int rotateAngle;

      public SavePhotoAsyncTask(Context ctx, byte[] d, Camera c,
                                int rotateAngle, CameraViewImpl.Callback cb) {
         this.data = d;
         this.camera = c;
         this.context = new WeakReference<>(ctx);
         this.mCallback = cb;
         this.rotateAngle = rotateAngle;
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
         image.compressToJpeg(rectangle, 10, out2);
         byte[] imageBytes = out2.toByteArray();

//         bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//         bitmap = rotateBitmap(bitmap, rotateAngle);
//
//         File directory = getCacheDir(context.get());
//         File photo = new File(directory, System.currentTimeMillis() + ".jpg");
//
//         try {
//            FileOutputStream fos = new FileOutputStream(photo.getPath());
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//
//            fos.flush();
//            fos.close();
//
//            out2.flush();
//            out2.close();
//         } catch (java.io.IOException e) {
//            Log.e("PictureDemo", "Exception in photoCallback", e);
//         }
//
//         mCallback.onCameraCapture(photo.getPath());
//
//
//         bitmap.recycle();
//         bitmap = null;
         String imgString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
         mCallback.onCameraCapture(imgString);

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
      } else if (dir != null && dir.isFile()) {
         return dir.delete();
      } else {
         return false;
      }
   }

   private static Bitmap rotateBitmap(Bitmap source, float angle) {
      Matrix matrix = new Matrix();
      matrix.postRotate(angle);
      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                                 true);
   }
   private static WeakReference<Bitmap> rotateBitmap2(ByteArrayOutputStream out2, float angle) {
      byte[] imageBytes = out2.toByteArray();
      Bitmap source = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
      Matrix matrix = new Matrix();
      matrix.postRotate(angle);
      return new WeakReference<>(Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                                                     true));
   }




}
