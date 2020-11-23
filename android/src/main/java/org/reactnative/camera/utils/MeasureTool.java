package org.reactnative.camera.utils;

public class MeasureTool {
   private static long before;

   public static void setBefore() {
      before = System.currentTimeMillis();
   }

   public static long getMeasureFromBefore(){
      return System.currentTimeMillis() - before;
   }
}
