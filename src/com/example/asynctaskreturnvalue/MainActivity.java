package com.example.asynctaskreturnvalue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/***
 * @author Lalit
 *
 * Used two ways to return value from AsyncTask 
 * 1.) BroadCastReceiver
 * 2.) Interface
 * 
 * The use of Interface(second) way is much faster in terms of response.
 *
 */

public class MainActivity extends Activity implements onTaskCompletion{

	onTaskCompletion mCompletion;
	TextView textView;
	final String INTENT_ACTION = "content";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        EncodeDecodeURL();
        
        mCompletion = this;

        textView = (TextView) findViewById(R.id.textView);
        Toast.makeText(MainActivity.this, "Activity Started", Toast.LENGTH_LONG).show();
        
        String url = "https://www.google.co.in/";
        try {
			new DownloadBitmapTask().execute(url);
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }
    
    private void EncodeDecodeURL() {
    	try {  
    		  String url = "http://www.yoursite.com/blah";
    		  String queryString = "param=1&value=2";  
    		 
    		  // Encode
    		  String encodedQueryString = URLEncoder.encode(queryString,"UTF-8");
    		  String encodedUrl = url + "?" + encodedQueryString;
    		  Log.d("Encode URL: ", encodedUrl);
    		   
    		   // Decode 
    		   String decodeUrl = URLDecoder.decode(encodedUrl, "UTF-8");
    		   Log.d("Decode URL: ", decodeUrl);
    		 }
    		 catch (UnsupportedEncodingException e)
    		 {
    		  Log.e("Exception: " , e.getMessage());
    		 
    		 }
    }
    
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(MainActivity.this, "BroadCast Download Complete " +intent.getStringExtra("content"), Toast.LENGTH_LONG).show();
		}
	};
    
	private String convertStreamToString(InputStream is) {
		
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
    
    class DownloadBitmapTask extends AsyncTask<String, Void, String>
    {
    	public DownloadBitmapTask() {
			registerReceiver(mReceiver, new IntentFilter(INTENT_ACTION));
		}
    	@Override
    	protected void onPreExecute() {
    		textView.append("started downloading...");
    	}
		@Override
		protected String doInBackground(String... params) {
			String content = null;
			 try {
		        	URL url = new URL(params[0]);
		        	URLConnection urlConnection = url.openConnection();
		        	InputStream is = urlConnection.getInputStream();
		        	content = convertStreamToString(is);
				} catch (Exception e) {
					e.printStackTrace();
				}
			return content;
		}
		
		@Override
		protected void onPostExecute(String result) {
			//textView.append(result);
			textView.append("downloading completed...");
			
			Intent intent = new Intent(INTENT_ACTION);
			intent.putExtra("content", result);
			sendBroadcast(intent);
			
			// receiver
			unregisterReceiver(mReceiver);
			
			// interface
			mCompletion.onTaskCompleted(result);
			
			// interface
			mTaskDone.onTaskFinished(result);
		}
    }

	public void onTaskCompleted(String value) {
		Toast.makeText(MainActivity.this, "Download Complete Interface "+value, Toast.LENGTH_LONG).show();
	}
	
	interface onTaskDone{
		void onTaskFinished(String result);
	}
	
	onTaskDone mTaskDone = new onTaskDone() {
		
		public void onTaskFinished(String result) {
			Toast.makeText(MainActivity.this, "mTaskDone Download Finish Interface "+result, Toast.LENGTH_LONG).show();
		}
	}; 
}
