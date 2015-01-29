package com.hack40.streamhanddrawingappserver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;




public class MainActivity extends Activity implements View.OnClickListener {

    private final String tag = "MainActivity";

    private ImageButton btnClear, btnSave,btnShare;

    private DrawingView drawingView;

    ArrayList<WebSocket> mSockets;

    AsyncHttpServer mAsyncHttpServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            setContentView(R.layout.activity_main);

            drawingView = (DrawingView) findViewById(R.id.drawing);

            btnClear = (ImageButton) findViewById(R.id.btnClear);
            btnClear.setOnClickListener(this);

            btnSave = (ImageButton) findViewById(R.id.btnSave);
            btnSave.setOnClickListener(this);

            btnShare = (ImageButton) findViewById(R.id.btnShare);
            btnShare.setOnClickListener(this);
        }

        mSockets = new ArrayList<WebSocket>();
        mAsyncHttpServer = new AsyncHttpServer();
        AsyncHttpServer.WebSocketRequestCallback mWebSocketCallback = new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
                mSockets.add(webSocket);
                webSocket.send("Welcome Client");
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        try {
                            if (ex != null)
                                Log.e("WebSocket", "Error");
                        } finally {
                            mSockets.remove(webSocket);
                        }
                    }
                });
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        Log.d("SERVERTAG", s);
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        mAsyncHttpServer.websocket("/", mWebSocketCallback);
        mAsyncHttpServer.listen(8888);

        drawingView.parent(this);

        for (WebSocket socket : mSockets) {
            socket.send("Server sent a string");
        }
    }

    @Override
    public void onClick(View v) {

       if (v == btnClear) {

            drawingView.reset();
            drawingView.setBackground(null);

        } else if (v == btnSave) {

            saveImage();

        } else if (v == btnShare) {

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/png");

            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(saveImage().getAbsolutePath())); //"file:///sdcard/temporary_file.jpg"
            startActivity(Intent.createChooser(share, "Share Image"));
        }
    }

    public File saveImage() {
        drawingView.setDrawingCacheEnabled(true);
        Bitmap bm = drawingView.getDrawingCache();

        File fPath = Environment.getExternalStorageDirectory();

        File f = null;

        f = new File(fPath, UUID.randomUUID().toString() + ".png");

        try {
            FileOutputStream strm = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 80, strm);
            strm.close();

            Toast.makeText(getApplicationContext(), "Image is saved successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
