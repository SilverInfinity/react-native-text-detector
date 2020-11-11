
package com.fetchsky.RNTextDetector;

import android.graphics.Rect;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;

public class RNTextDetectorModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private TextRecognizer detector;
  private InputImage image;

  public RNTextDetectorModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    try {
        detector = TextRecognition.getClient();
    }
    catch (IllegalStateException e) {
        e.printStackTrace();
    }
  }

  @ReactMethod
    public void detectFromUri(String uri, final Promise promise) {
        try {
            image = InputImage.fromFilePath(this.reactContext, android.net.Uri.parse(uri));
            Task<Text> result =
                    detector.process(image) // need to close this detector?
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text firebaseVisionText) {
                                    promise.resolve(getDataAsArray(firebaseVisionText));
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                            promise.reject(e);
                                        }
                                    });;
        } catch (IOException e) {
            promise.reject(e);
            e.printStackTrace();
        }
    }

    /**
     * Converts firebaseVisionText into a map
     *
     * @param firebaseVisionText
     * @return
     */
    private WritableArray getDataAsArray(Text firebaseVisionText) {
        WritableArray data = Arguments.createArray();
        WritableMap info = Arguments.createMap();
        WritableMap coordinates = Arguments.createMap();

        for (Text.TextBlock block: firebaseVisionText.getTextBlocks()) {
            info = Arguments.createMap();
            coordinates = Arguments.createMap();

            Rect boundingBox = block.getBoundingBox();

            coordinates.putInt("top", boundingBox.top);
            coordinates.putInt("left", boundingBox.left);
            coordinates.putInt("width", boundingBox.width());
            coordinates.putInt("height", boundingBox.height());

            info.putMap("bounding", coordinates);
            info.putString("text", block.getText());
            data.pushMap(info);
        }

        return data;
    }


  @Override
  public String getName() {
    return "RNTextDetector";
  }
}