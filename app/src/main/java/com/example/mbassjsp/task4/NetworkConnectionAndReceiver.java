package com.example.mbassjsp.task4;

// Created by A Leeming
// Modified JSP
// Date 17-1-2018
// See https://developer.android.com ,for android classes, methods, etc
// Code snippets from http://examples.javacodegeeks.com/android/core/socket-core/android-socket-example

// import classes
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;



public class NetworkConnectionAndReceiver extends Thread{
    //Declare class variables
    private Socket socket = null;
    private  int timeForSound = 3;
    private long help = 0;
    int limitPacketNumber = 42;
    private short [] Sound = new short[250*timeForSound*limitPacketNumber];
    private int index = 0;



    private static final int SERVERPORT = 9999; // This is the port that we are connecting to
    // Channel simulator is 9998
    private static final String SERVERIP = "10.0.2.2";  // This is the host's loopback address
    private static final String LOGTAG = "Network and receiver"; // Identify logcat messages

    private boolean terminated = false; // When FALSE keep thread alive and polling

    private PrintWriter streamOut = null; // Transmitter stream
    private BufferedReader streamIn = null; // Receiver stream
    private AppCompatActivity parentRef; // Reference to main user interface(UI)
    private TextView receiverDisplay; // Receiver display
    private String message = null; //Received message
    private String isRegistered = "@@@@@";
    //class constructor
    public NetworkConnectionAndReceiver(AppCompatActivity parentRef)
    {
        this.parentRef=parentRef; // Get reference to UI
    }
    // Start new thread method
    public void run()
    {
        Log.i(LOGTAG,"Running in new thread");

        //Create socket and input output streams
        try {   //Create socket
            //InetAddress svrAddr = InetAddress.getByName(SERVERIP);
            InetAddress svrAddr = InetAddress.getLocalHost();
            socket = new Socket(svrAddr, SERVERPORT);

            //Setup i/o streams
            streamOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException uhe) {
            Log.e(LOGTAG, "Unknownhost\n" + uhe.getStackTrace().toString());
            terminated = true;
        }
        catch (Exception e) {
            Log.e(LOGTAG, "Socket failed\n" + e.getMessage());
            e.printStackTrace();
            terminated = true;
        }

        //receiver
        while(!terminated) // Keep thread running
        {
            try {
                message = streamIn.readLine(); // Read a line of text from the input stream
                // If the message has text then display it
                if (message != null && message != "") {
                    help = help + 1;
                    Log.i(LOGTAG, "helpp : " + index);
                    Log.i(LOGTAG, "MSG recv : " + message);
                    if(message.length() > 936)
                    {
                        String[] result = message.split(" ");
                        Log.i(LOGTAG, "help<<" + result[result.length - 1] + "end");
                        int[] tempBitString = new int[result[result.length - 1].length() / 16];
                        Log.i(LOGTAG, "help<<" + tempBitString.length);
                        Log.i(LOGTAG, "help<<" + result[result.length - 1].length());
                        bitS2IntA(result[result.length - 1], result[result.length - 1].length() / 16, tempBitString);
                        //String codeMessage = new String(tempBitString);
                        //Log.i(LOGTAG, "help<<" + codeMessage);
                        //result[result.length - 1] = codeMessage;
                        int indexForPack;
                        Log.i(LOGTAG, "AUDIO");
                        message = "AAAHHH BARRY";
                        for(indexForPack = 0; indexForPack < tempBitString.length; indexForPack++)
                        {
                            tempBitString[indexForPack] -= 16384;
                            tempBitString[indexForPack] *= 2;
                            Sound[indexForPack+index] = (short)tempBitString[indexForPack];
                            //message = message.concat("" + Sound[indexForPack + index]);
                            //Log.i(LOGTAG, "this is the index" + index);

                        }

                        index += indexForPack;
                        if(index >= Sound.length)
                        {
                                        /*
                                        Log.i(LOGTAG, "AUDIO");
                                        for(int x = 0; x < index; x++)
                                        {
                                            Log.i(LOGTAG, "" + Sound[x]);

                                        }

                                        int CONF = AudioFormat.CHANNEL_OUT_MONO;
                                        int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
                                        int MDE = AudioTrack.MODE_STATIC; //Need static mode.
                                        int STRMTYP = AudioManager.STREAM_MUSIC;
                                        AudioTrack track = new AudioTrack(STRMTYP, 8000, CONF, FORMAT, 250*1*limitPacketNumber*2, MDE);

                                        //fillRandom(); // Fill data array with 16-bit samples of random noise
                                        track.write(Sound, 0, 250*1*42);
                                        //track.setPlaybackRate(8000);
                                        //while(track.getPlaybackRate() != 8000) { } // wait for new sample rate to be established
                                        track.play();
                                        while(track.getPlaybackHeadPosition() != 250*1*42) {}; //Wait before playing more
                                        track.stop(); track.setPlaybackHeadPosition(0);

                                        while(track.getPlaybackHeadPosition() != 0) {}; // wait for head position
                                        track.write(Sound, 0, (int) 250*1*limitPacketNumber); track.play();
                                        while(track.getPlaybackHeadPosition() < 250*1*limitPacketNumber) { }; //Wait before playing more
                                        track.stop(); track.setPlaybackHeadPosition(0);
                                        while(track.getPlaybackHeadPosition() != 0) { }; // wait for head position
                                        Log.i(LOGTAG, "worked");

                                        */


                            int CONF = AudioFormat.CHANNEL_OUT_MONO;
                            int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
                            int MDE = AudioTrack.MODE_STATIC; //Need static mode.
                            int STRMTYP = AudioManager.STREAM_ALARM;
                            AudioTrack track = new AudioTrack(STRMTYP, 22050, CONF, FORMAT, 250*timeForSound*limitPacketNumber*2, MDE);
                            //fillRandom(); // Fill data array with 16-bit samples of random noise
                            track.write(Sound, 0,  250*timeForSound*limitPacketNumber);
                            track.setPlaybackRate(8000);
                            while(track.getPlaybackRate() != 8000) { } // wait for new sample rate to be established
                            track.play();
                            while(track.getPlaybackHeadPosition() !=  250*timeForSound*limitPacketNumber) {}; //Wait before playing more
                            track.stop(); track.setPlaybackHeadPosition(0);
                            while(track.getPlaybackHeadPosition() != 0) {}; // wait for head position
                            index = 0;
                        }
                    }
                    //Get the receiving text area as defined in the Res dir xml code
                    receiverDisplay = parentRef.findViewById(R.id.txtServerResponse);
                    //Run code in run() method on UI thread
                    parentRef.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Display message, and old text in the receiving text area
                            //we will do something fun in her
                            String borders = "\n**************************************************************\n";
                            String kaomoji;
                            if(message.contains("ERROR"))
                            {
                                if(message.contains("You have already registered"))
                                    message = message.concat(" Please press start");
                                kaomoji = "(ʘ言ʘ╬)\n";
                            }
                            else if(message.contains("INFO Welcome") )
                            {
                                isRegistered = "nice";
                                kaomoji = "⊂(◉‿◉)つ\n";
                                receiverDisplay.setText("");
                            }
                            else if(message.contains("WHO"))
                            {
                                String[] result = message.split("'");
                                message = "List of users:";
                                for(int i = 1; i < result.length; i++)
                                { Log.i(LOGTAG, "help" + result[i]+"end");
                                   if(!result[i].equals(", ") && !result[i].equals("]"))
                                     message = message.concat("\n->" + result[i]);
                                }
                                kaomoji = "( ͡° ͜ʖ ͡°)\n";
                            }
                            else if(message.contains("BarryBot") && (message.contains("0")
                                                                    || message.contains("1"))) {
                                kaomoji = "⊂(◉‿◉)つ\n";
                                if(message.length() < 939) {
                                    String[] result = message.split(" ");
                                    Log.i(LOGTAG, "help<<" + result[result.length - 1] + "end");
                                    char[] tempBitString = new char[result[result.length - 1].length() / 16];
                                    Log.i(LOGTAG, "help<<" + tempBitString.length);
                                    Log.i(LOGTAG, "help<<" + result[result.length - 1].length());
                                    bitS2Text(result[result.length - 1], result[result.length - 1].length() / 16, tempBitString);
                                    String codeMessage = new String(tempBitString);
                                    Log.i(LOGTAG, "help<<" + codeMessage);
                                    result[result.length - 1] = codeMessage;
                                    message = "";
                                    for (int i = 0; i < result.length; i++) {
                                        message = message.concat(result[i] + " ");
                                    }
                                } else {
/*
                                    String[] result = message.split(" ");
                                    Log.i(LOGTAG, "help<<" + result[result.length - 1] + "end");
                                    int[] tempBitString = new int[result[result.length - 1].length() / 16];
                                    Log.i(LOGTAG, "help<<" + tempBitString.length);
                                    Log.i(LOGTAG, "help<<" + result[result.length - 1].length());
                                    bitS2IntA(result[result.length - 1], result[result.length - 1].length() / 16, tempBitString);
                                    //String codeMessage = new String(tempBitString);
                                    //Log.i(LOGTAG, "help<<" + codeMessage);
                                    //result[result.length - 1] = codeMessage;
                                    int indexForPack;
                                    Log.i(LOGTAG, "AUDIO");
                                    message = "AAAHHH BARRY";
                                    for(indexForPack = 0; indexForPack < tempBitString.length; indexForPack++)
                                    {
                                        tempBitString[indexForPack] -= 16384;
                                        tempBitString[indexForPack] *= 2;
                                        Sound[indexForPack+index] = (short)tempBitString[indexForPack];
                                        //message = message.concat("" + Sound[indexForPack + index]);
                                        //Log.i(LOGTAG, "this is the index" + index);

                                    }

                                    //message = message.concat("the index is:" + help);
                                    index += indexForPack;
                                    //Log.i(LOGTAG, "this is the index" + index);
                                    //message = message.concat("the index is:" + index + " and the index for pack" + indexForPack );
*/

                                }
                            }
                            else
                                kaomoji =  "⊂(◉‿◉)つ\n";

                            if(isRegistered.equals("nice"))
                            {
                                receiverDisplay.setText(kaomoji
                                        + message
                                        + borders
                                        + receiverDisplay.getText());
                            }
                            else
                            {
                                receiverDisplay.setText(kaomoji
                                        + message
                                        + borders);
                            }

                        }
                    });
                }
            }
            catch (Exception e) {
                Log.e(LOGTAG, "Receiver failed\n" + e.getMessage());
                e.printStackTrace();
            }

        }
        //Call disconnect method to close i/o streams and socket
        disconnect();
        Log.i(LOGTAG,"Thread now closing");
    }

    public static void bitS2Text(String bitS, int L, char[] charA)
    {// Converts a bit string bitS representing 16*L bits to L positive 16-bit integers
        for (int i = 0; i < L; i++)
        { int d=0;
            for (int j = 0; j < 16; j++)
            { int k = i*16+j; d = d + (Character.getNumericValue(bitS.charAt(k))<< j); } charA[i]=(char)d; }
    } // end of method
    //  Method for closing socket and i/o streams
    public void disconnect()
    {
        Log.i(LOGTAG, "Closing socket and io streams");
        try {
            streamIn.close();
            streamOut.close();
        }
        catch(Exception e)
        {/*do nothing*/}

        try {
            socket.close();
        }
        catch(Exception e)
        {/*do nothing*/}
    }
    // Getter method for returning the output stream for the transmitter to use
    public PrintWriter getStreamOut() {return this.streamOut;}
    // Setter method for terminating this thread
    // Set value to true to close thread
    public void closeThread(boolean value) {this.terminated = value;}
    public  String getIsRegistered()
    {
        return isRegistered;
    }
    public static void bitS2IntA(String bitS, int L, int[ ] IntA)
    {// Converts a bit string bitS representing 16*L bits to L positive 16-bit integers
        for (int i = 0; i < L; i++)
        { int d=0;
            for (int j = 0; j < 16; j++)
            { int k = i*16+j; d = d + (Character.getNumericValue(bitS.charAt(k))<< j); } IntA[i]=d; }
    } // end of method

}
