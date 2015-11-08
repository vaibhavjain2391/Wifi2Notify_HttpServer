package com.example.vaibhavjain.wifi2notify;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by abhilash on 20/09/15.
 */
public class CallReceiver extends PhonecallReceiver {
    static String TAG = "chup";

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        Log.i(TAG, "Incoming call started");
        Log.i(TAG, "From Phone Number: " + number);
        //boolean sendPing = ((MyApplication)ctx).fwdPing();
        boolean sendPing = true;
        Log.i(TAG, "Forward call ? " + sendPing);
        appendLog(System.currentTimeMillis() + " Incoming_call_from_number:"+number , "sdcard/winot_CallIn.file");
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.i(TAG, "Outgoing call started");
        appendLog(System.currentTimeMillis() + " Outgoing_Call_started", "sdcard/winot_CallOut.file");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.i(TAG, "Incoming call ended");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.i(TAG, "Outgoing call ended");
        //appendLog(System.currentTimeMillis() + " Outgoing call ended ");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
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
