package com.example.vaibhavjain.wifi2notify;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.example.vaibhavjain.wifi2notify.R;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GCMNotificationIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GCMNotificationIntentService() {
		super("GcmIntentService");
	}

	public static final String TAG = "GCMNotificationIntentService";

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				/*for (int i = 0; i < 3; i++) {
					Log.i(TAG,
							"Working... " + (i + 1) + "/5 @ "
									+ SystemClock.elapsedRealtime());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}

				}
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());*/

				appendLog(System.currentTimeMillis() + " Call_Notification","sdcard/winot_pingIn.file" );

				try{
					Process su = Runtime.getRuntime().exec("su");
					DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

					outputStream.writeBytes("settings put global airplane_mode_on 0\n");
					outputStream.flush();
					outputStream.writeBytes("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false\n");
					outputStream.flush();
					outputStream.writeBytes("exit\n");
					outputStream.flush();
					su.waitFor();

				}catch(IOException e){
					//throw new Exception(e);
					Log.d(TAG,"Error in executing shell command\n");
				}catch(InterruptedException e){
					//throw new Exception(e);
					Log.d(TAG,"Error in executing shell command\n");
				}

				Context context=getApplicationContext();
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				int networkType = tm.getNetworkType();
				Log.i(CallReceiver.TAG, "enabling cellular");
				while(networkType == TelephonyManager.NETWORK_TYPE_UNKNOWN)
				{
					networkType = tm.getNetworkType();
					Log.d(TAG,"Network not connected\n");
				}

				Log.i(CallReceiver.TAG, "cellular enabled");
				appendLog(System.currentTimeMillis() + " Cellular_Enabled ", "sdcard/winot_cellEn.file");
				//comment out for proxy test
				//sendNotification(callerPhoneNo);

				String callerPhoneNo  = "+1765-491-8467"; //hardcoding
			    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callerPhoneNo));
				callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(callIntent);

				/*sendNotification("Message via Google GCM Server! Sender: "
						+ extras.get(Config.REGISTER_NAME) + ". Message: "
						+ extras.get(Config.MESSAGE_KEY));*/
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String msg) {
		Log.d(TAG, "Preparing to send notification...: " + msg);
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.gcm_cloud)
				.setContentTitle("GCM Notification")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		Log.d(TAG, "Notification sent successfully.");
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
