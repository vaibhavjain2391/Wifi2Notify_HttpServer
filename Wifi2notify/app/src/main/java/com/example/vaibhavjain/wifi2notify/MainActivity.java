package com.example.vaibhavjain.wifi2notify;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends Activity {

	ShareExternalServer appUtil;
	String regId;
	String userName;
	AsyncTask<Void, Void, String> shareRegidTask;

	EditText toUser;
	EditText message;
	Button btnSendMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		appUtil = new ShareExternalServer();

		regId = getIntent().getStringExtra("regId");
		Log.d("MainActivity", "regId: " + regId);

		userName = getIntent().getStringExtra(Config.REGISTER_NAME);
		Log.d("MainActivity", "userName: " + userName);

		shareRegidTask = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String result = appUtil
						.shareRegIdWithAppServer(regId, userName);
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				shareRegidTask = null;
				Toast.makeText(getApplicationContext(), result,
						Toast.LENGTH_LONG).show();
			}

		};

		// to send message to another device via Google GCM
		btnSendMessage = (Button) findViewById(R.id.sendMessage);
		btnSendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				toUser = (EditText) findViewById(R.id.toUser);
				String toUserName = toUser.getText().toString();

				message = (EditText) findViewById(R.id.message);
				String messageToSend = message.getText().toString();

				if (TextUtils.isEmpty(toUserName)) {
					Toast.makeText(getApplicationContext(),
							"To User is empty!", Toast.LENGTH_LONG).show();
				} else if (TextUtils.isEmpty(messageToSend)) {
					Toast.makeText(getApplicationContext(),
							"Message is empty!", Toast.LENGTH_LONG).show();
				} else {

					Log.d("MainActivity", "Sending message to user: "
							+ toUserName);
					sendMessageToGCMAppServer(toUserName, messageToSend);

				}
			}
		});

		shareRegidTask.execute(null, null, null);
	}

	private void sendMessageToGCMAppServer(final String toUserName,
			final String messageToSend) {
		appendLog(System.currentTimeMillis() + " Sending_ping_to_:"+toUserName , "sdcard/winot_PingSent.file");
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {

				String result = appUtil.sendMessage(userName, toUserName,
						messageToSend);
				Log.d("MainActivity", "Result: " + result);
				return result;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.d("MainActivity", "Result: " + msg);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		}.execute(null, null, null);
	}

	public boolean disableCellular(View v){

		//Settings.Global.putInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 1);

		try{
			Process su = Runtime.getRuntime().exec("su");
			DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

			outputStream.writeBytes("settings put global airplane_mode_on 1\n");
			outputStream.flush();
			outputStream.writeBytes("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true\n");
			outputStream.flush();
			outputStream.writeBytes("exit\n");
			outputStream.flush();
			su.waitFor();
		}catch(IOException e){
			//throw new Exception(e);
			Log.d(CallReceiver.TAG, "Error in executing shell command\n");
		}catch(InterruptedException e){
			//throw new Exception(e);
			Log.d(CallReceiver.TAG,"Error in executing shell command\n");
		}

		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);

		//startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
		return true;

	}

	public void appendLog(String text,String fileName)
	{
		//"sdcard/winot.file"
		File logFile = new File(fileName);
		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			buf.append(text);
			buf.newLine();
			buf.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
