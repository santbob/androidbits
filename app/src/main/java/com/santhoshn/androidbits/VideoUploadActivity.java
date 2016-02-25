package com.santhoshn.androidbits;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.santhoshn.androidbits.utils.ResponseHandler;
import com.santhoshn.androidbits.utils.VolleyRequestHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by santhosh on 23/01/16.
 * Activity to Upload Video
 */
public class VideoUploadActivity extends AppCompatActivity {
    Button uploadBtn;
    TextView tv = null;
    private static final int SELECT_VIDEO = 1;
    private static final String VIDEO_UPLOAD_URL = "https://0ef378b0.ngrok.io/upload/video";
    String mimeType;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String boundary = "apiclient-" + System.currentTimeMillis();
    String twoHyphens = "--";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 50 * 1024 * 1024;

    private EditText title;
    private EditText channel;
    private EditText context_type;
    private EditText context_resource_id;
    private EditText isPublic;
    private EditText isUserVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_video_upload);
        uploadBtn = (Button) findViewById(R.id.upload);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_VIDEO);
            }
        });
        tv = (TextView) findViewById(R.id.tv);
        title = (EditText) findViewById(R.id.title);
        channel = (EditText) findViewById(R.id.channel);
        context_type = (EditText) findViewById(R.id.context_type);
        context_resource_id = (EditText) findViewById(R.id.context_resource_id);
        isPublic = (EditText) findViewById(R.id.isPublic);
        isUserVideo = (EditText) findViewById(R.id.isUserVideo);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_VIDEO) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    try {
                        doFileUpload(data.getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doFileUpload(Uri uri) {
        VideoParams params = new VideoParams();
        params.setChannel(channel.getText().toString());
        params.setContextResourceId(context_resource_id.getText().toString());
        params.setTitle(title.getText().toString());
        params.setContextType(context_type.getText().toString());
        params.setUri(uri);
        params.setIsPublicVideo(Integer.parseInt(isPublic.getText().toString()));
        params.setIsUserVideo(Integer.parseInt(isUserVideo.getText().toString()));

        uploadVideo(params);
//        UploadVideoAsyncTask asyncTask = new UploadVideoAsyncTask();
//        asyncTask.execute(params);
    }

    private String getPath(Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            cursor = getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void uploadVideo(VideoParams videoParams) {
        Map<String, String> params = new HashMap<String, String>();
        try {
            final InputStream iptStream;
            iptStream = getContentResolver().openInputStream(videoParams.getUri());
            if (iptStream == null) {
                return;
            }
            params.put("file", convertFileToStream(videoParams.getUri()));
            params.put("title", videoParams.getTitle());
            params.put("duration", "15");
            params.put("orientation", "portrait");
            params.put("channel", videoParams.getChannel());
            params.put("context_type", videoParams.getContextType());
            params.put("context_resource_id", videoParams.getContextResourceId());
            params.put("isPublic", Integer.toString(videoParams.getIsPublicVideo()));
            params.put("isUserVideo", Integer.toString(videoParams.getIsUserVideo()));

            VolleyRequestHandler.getInstance(this).post(VIDEO_UPLOAD_URL, params, new ResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d("VideoUpload", "Video upload successfully " + response.toString());
                }

                @Override
                public void onError(JSONObject error) {
                    Log.d("VideoUpload", "Error Uploading Video " + error.toString());
                }
            }, 0);

        } catch (Exception ex) {
            Log.d("VideoUpload", "Exception " + ex.toString());
        }
    }

    private class UploadVideoAsyncTask extends AsyncTask<VideoParams, Void, String> {
        private final String LOG_TAG = UploadVideoAsyncTask.class.getSimpleName();

        private String getParamKeyValue(String paramName, String paramValue) {
            return ";" + paramName + "=\"" + paramValue + "\"";
        }

        private String getParamKeyIntValue(String paramName, int paramValue) {
            return ";" + paramName + "=" + paramValue;
        }


        protected String doInBackground(VideoParams... params) {
            String fileData = null;
            // If there's no search string, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return fileData;
            }
            VideoParams videoParams = params[0];
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;


            // Is this the place are you doing something wrong.

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";


            int bytesRead, bytesAvailable, bufferSize;

            byte[] buffer;

            int maxBufferSize = 50 * 1024 * 1024;

            try {


                Log.e("MediaPlayer", "Inside second Method");

                InputStream iptStream = getContentResolver().openInputStream(videoParams.getUri());


                URL url = new URL(VIDEO_UPLOAD_URL);

                conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true);

                // Allow Outputs
                conn.setDoOutput(true);

                // Don't use a cached copy.
                conn.setUseCaches(false);

                // Use a post method.
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection", "Keep-Alive");
                //conn.setRequestProperty("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6OCwidXNlcklkIjoiel84MDU4NTI5MyJ9.kW9xKW5q2XgAzRDT_6cj3VxlEpn4tNGCUIYCRRq0YUI");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);


                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                //dos.writeBytes("Content-Disposition: form-data; name=\"file\"" + getParamKeyValue("filename", "test.mp4") + getParamKeyIntValue("duration", 15) + getParamKeyValue("orientation", "portrait") + getParamKeyValue("channel", videoParams.getChannel()) + getParamKeyValue("context_resource_id", videoParams.getContextResourceId()) + getParamKeyValue("context_type", videoParams.getContextType()) + getParamKeyValue("title", videoParams.getTitle()) + getParamKeyIntValue("isPublic", videoParams.getIsPublicVideo()) + getParamKeyIntValue("isUserVideo", videoParams.getIsUserVideo()) + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"" + ";filename=\"test.mp4 \"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.e("MediaPlayer", "Headers are written");

                bytesAvailable = iptStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = iptStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = iptStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = iptStream.read(buffer, 0, bufferSize);
                }


                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    fileData += inputLine;
                }


                // close streams
                Log.e("MediaPlayer", "File is written");
                iptStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("IOException", "error: " + ioe.getMessage(), ioe);
            }


            //------------------ read the SERVER RESPONSE


            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;

                while ((str = inStream.readLine()) != null) {
                    Log.e("MediaPlayer", "Server Response" + str);
                }
                /*while((str = inStream.readLine()) !=null ){

                }*/
                inStream.close();

            } catch (IOException ioex) {
                Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
            }

            return fileData;
        }

        @Override
        protected void onPostExecute(String fileData) {
            if (fileData != null) {
                tv.setText(fileData);
            }
        }
    }

    private String convertFileToStream(Uri uri) {
        InputStream inputStream = null;
        String result = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);

            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            // handling
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception exc) {
                //do nothing
            }
        }
        return result;
    }

    private class VideoParams {
        Uri uri;
        String title;
        String channel;
        String contextType;
        String contextResourceId;
        int isPublicVideo;
        int isUserVideo;

        public Uri getUri() {
            return uri;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getContextType() {
            return contextType;
        }

        public void setContextType(String contextType) {
            this.contextType = contextType;
        }

        public String getContextResourceId() {
            return contextResourceId;
        }

        public void setContextResourceId(String contextResourceId) {
            this.contextResourceId = contextResourceId;
        }

        public int getIsPublicVideo() {
            return isPublicVideo;
        }

        public void setIsPublicVideo(int isPublicVideo) {
            this.isPublicVideo = isPublicVideo;
        }

        public int getIsUserVideo() {
            return isUserVideo;
        }

        public void setIsUserVideo(int isUserVideo) {
            this.isUserVideo = isUserVideo;
        }
    }
}
