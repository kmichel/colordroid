package info.kmichel.camera;

public interface CameraListener {
    void onCameraStart();

    void onCameraStartRunning();

    void onFirstImage();

    void onImageChange(NV21Buffer buffer);

    void onCameraStopRunning();
}
