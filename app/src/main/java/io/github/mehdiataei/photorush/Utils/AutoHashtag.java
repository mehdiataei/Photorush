package io.github.mehdiataei.photorush.Utils;


import android.graphics.Bitmap;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;

import java.util.List;

public class AutoHashtag {

    private FirebaseVisionImage image;
    private List<FirebaseVisionImageLabel> generatedHashtagInfo;
    private List<String> hashtags;
    public FirebaseVisionImageLabeler labeler;

    public AutoHashtag(Bitmap bitmap) {

        image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionOnDeviceImageLabelerOptions options =
                new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.7f)
                        .build();
        labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler(options);

    }

    public FirebaseVisionImage getImage() {
        return image;
    }


}
