package com.example.exoplayerdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.widget.Toast;
import com.google.android.exoplayer.*;

/**
 * Created by lz on 2015/1/23.
 * <p/>
 * DoWhat:
 */

public class SimplePlayerActivity extends Activity implements
//        SurfaceHolder.Callback,
        TextureView.SurfaceTextureListener,
        ExoPlayer.Listener,
        MediaCodecVideoTrackRenderer.EventListener {

    private static final String TAG = "SimplePlayerActivity";

    public static final int RENDERER_COUNT = 2;

    private Uri contentUri;
    private int contentType;

    private ExoPlayer player;
    private RendererBuilder builder;
    private RendererBuilderCallback callback;
    private MediaCodecVideoTrackRenderer videoRenderer;

//    private VideoSurfaceView surfaceView;
    private VideoTextureView textureView;
    private Handler mainHandler;


    private boolean autoPlay = true;




    /**
     * Builds renderers for the player.
     */
    public interface RendererBuilder {

        void buildRenderers(RendererBuilderCallback callback);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        initData();

        mainHandler = new Handler(getMainLooper());
        builder = getRendererBuilder();

        setContentView(R.layout.player_activity_simple);
        initView();

    }

    private void initView() {
//        surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
//        surfaceView.getHolder().addCallback(this);

        textureView = (VideoTextureView) findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);

    }

    private void initData() {
        Intent intent = getIntent();
        contentUri = intent.getData();
        contentType = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup the player
        player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
        player.addListener(this);
//        player.seekTo(0);
        // Build the player controls
//        mediaController.setMediaPlayer(new PlayerControl(player));
//        mediaController.setEnabled(true);
        // Request the renderers
        callback = new RendererBuilderCallback();
        builder.buildRenderers(callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Release the player
        if (player != null) {
            player.release();
            player = null;
        }
        callback = null;
        videoRenderer = null;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }


    private RendererBuilder getRendererBuilder() {
//        String userAgent = DemoUtil.getUserAgent(this);
        switch (contentType) {
//            case DemoUtil.TYPE_SS:
//                return new SmoothStreamingRendererBuilder(this, userAgent, contentUri.toString(),
//                        contentId);
//            case DemoUtil.TYPE_DASH:
//                return new DashRendererBuilder(this, userAgent, contentUri.toString(), contentId);
            default:
                return new DefaultRendererBuilder(this, contentUri);
//                return new SmoothStreamingRendererBuilder(this, "", contentUri.toString(), "");
        }
    }


    private void onRenderers(RendererBuilderCallback callback,
                             MediaCodecVideoTrackRenderer videoRenderer, MediaCodecAudioTrackRenderer audioRenderer) {
        if (this.callback != callback) {
            return;
        }
        this.callback = null;
        this.videoRenderer = videoRenderer;
        player.prepare(videoRenderer, audioRenderer);
        maybeStartPlayback();
    }

    private void maybeStartPlayback() {
        Surface surface = surfaceView.getHolder().getSurface();
        if (videoRenderer == null || surface == null || !surface.isValid()) {
            // We're not ready yet.
            return;
        }
        player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        if (autoPlay) {
            player.setPlayWhenReady(true);
            autoPlay = false;
        }
    }

    private void onRenderersError(RendererBuilderCallback callback, Exception e) {
        if (this.callback != callback) {
            return;
        }
        this.callback = null;
        onError(e);
    }

    private void onError(Exception e) {
        Log.e(TAG, "Playback failed", e);
        Toast.makeText(this, R.string.failed, Toast.LENGTH_SHORT).show();
        finish();
    }

    //----------------SurfaceHolder.Callback
//    @Override
//    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        Log.d("0-0","----------surfaceCreated");
//        maybeStartPlayback();
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//        Log.d("0-0","----------surfaceChanged");
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//        Log.d("0-0","----------surfaceDestroyed");
//        if (videoRenderer != null) {
//            player.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
//        }
//
//    }

    //------------------SurfaceTextureListener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("0-0","----------onSurfaceTextureAvailable");
        maybeStartPlayback();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("0-0","----------onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d("0-0","----------onSurfaceTextureDestroyed");
        if (videoRenderer != null) {
            player.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Log.d("0-0","----------onSurfaceTextureUpdated");
    }


    //---------------MediaCodecVideoTrackRenderer.EventListener
    @Override
    public void onDroppedFrames(int i, long l) {
        Log.d("0-0","----------onDroppedFrames");
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {
        Log.d("0-0","----------onVideoSizeChanged");
        textureView.setVideoWidthHeightRatio(
                height == 0 ? 1 : (pixelWidthHeightRatio * width) / height);

    }

    @Override
    public void onDrawnToSurface(Surface surface) {
        Log.d("0-0","----------onDrawnToSurface");
    }

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
        Log.d("0-0","----------onDecoderInitializationError");
    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
        Log.d("0-0","----------onCryptoError");
    }

    //---------exoplayer listener
    @Override
    public void onPlayerStateChanged(boolean b, int i) {
        Log.d("0-0","----------onPlayerStateChanged");
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        Log.d("0-0","----------onPlayWhenReadyCommitted");
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        Log.d("0-0","----------onPlayerError");
    }


    /* package */ final class RendererBuilderCallback {

        public void onRenderers(MediaCodecVideoTrackRenderer videoRenderer,
                                MediaCodecAudioTrackRenderer audioRenderer) {
            SimplePlayerActivity.this.onRenderers(this, videoRenderer, audioRenderer);
        }

        public void onRenderersError(Exception e) {
            SimplePlayerActivity.this.onRenderersError(this, e);
        }

    }


}
