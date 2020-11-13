package org.reactnative.camera.events;

import androidx.core.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.reactnative.camera.CameraViewManager;


public class CameraCaptureEvent extends Event<CameraCaptureEvent> {
   private static final Pools.SynchronizedPool<CameraCaptureEvent> EVENTS_POOL = new Pools.SynchronizedPool<>(3);
   private String mPath;

   private CameraCaptureEvent() {
   }

   public static CameraCaptureEvent obtain(int viewTag, String path) {
      CameraCaptureEvent event = EVENTS_POOL.acquire();
      if (event == null) {
         event = new CameraCaptureEvent();
      }
      event.init(viewTag, path);
      return event;
   }

   private void init(int viewTag, String path) {
      super.init(viewTag);
      mPath = path;
   }

   @Override
   public short getCoalescingKey() {
      return 0;
   }

   @Override
   public String getEventName() {
      return CameraViewManager.Events.EVENT_ON_CAMERA_CAPTURE.toString();
   }

   @Override
   public void dispatch(RCTEventEmitter rctEventEmitter) {
      rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
   }

   private WritableMap serializeEventData() {
      WritableMap arguments = Arguments.createMap();
      arguments.putString("uri", mPath);
      return arguments;
   }
}