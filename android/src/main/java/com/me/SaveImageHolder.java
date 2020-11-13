package com.me;

import android.content.Context;
import android.hardware.Camera;

import com.google.android.cameraview.CameraViewImpl;

import java.lang.ref.WeakReference;

public class SaveImageHolder {
   WeakReference<Context> context;
   byte[] data;
   Camera camera;
   int rotateAngle;
   CameraViewImpl.Callback mCallback;
   public SaveImageHolder(WeakReference<Context> ctx,  byte[] d, Camera c,
                          int angle, CameraViewImpl.Callback cb) {
      this.data = d;
      this.context = ctx;
      this.camera = c;
      this.rotateAngle = angle;
      this.mCallback = cb;
   }
}
