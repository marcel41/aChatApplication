package com.example.mbassjsp.task4;

// Created by A Leeming
// Modified JSP
// Date 17-1-2018
// See https://developer.android.com ,for android classes, methods, etc
// Default android imports

//*******************************************************
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import android.view.Menu; import android.view.MenuItem; import android.view.View;
import android.widget.Button; import android.widget.EditText;
import android.widget.TextView;


// Added for playing sound
import android.media.AudioManager; import android.media.MediaPlayer;
import android.media.AudioTrack; import android.media.AudioFormat;
import java.io.File;
import java.io.InputStream; import java.io.BufferedInputStream;
import java.io.DataInputStream; import java.io.*;
import android.content.res.* ;
import java.util.Random;
//*******************************************************


//added for micro
import android.media.MediaRecorder; import android.media.AudioRecord;
import android.media.AudioFormat; import android.media.AudioTrack;
import android.media.AudioManager; import android.os.Bundle;
import android.support.v4.app.ActivityCompat; import android.support.v7.app.AppCompatActivity;
import android.util.Log; import android.view.View;
import android.widget.Button;
import java.io.File; import java.io.FileOutputStream; import java.io.DataOutputStream;
import java.io.IOException; import android.os.Environment;


import android.app.Activity; import android.app.ActionBar;
import android.app.Fragment; import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater; import android.view.Menu; import android.view.MenuItem;
import android.view.View; import android.view.ViewGroup;
import android.os.Build;
//Added imports for this app
import android.widget.Button; import android.widget.ImageView;
import java.io.File; import java.io.FileNotFoundException;
import java.io.FileOutputStream; import java.io.IOException;
import java.io.InputStream; import java.io.OutputStream;
import android.graphics.Bitmap; import android.graphics.BitmapFactory;
// Import classes
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.method.ScrollingMovementMethod;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.inputmethod.EditorInfo;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        //new libraries for lab5
        import android.media.AudioManager;
        import android.media.MediaPlayer;
// Android apps must have a MainActivity class that extends Activity or AppCompatActivity class
public class MainActivity extends AppCompatActivity {
    //using microphone


    private String wavFileName = null;
    private int Fs2 = 8000; // Sampling freq (Hz)
    private int NS = 12000*10; // Number of samples
    private short soundSamples[] = new short [(int) NS];
    private int recBufferSize;
    private int recChunks;
    private AudioRecord recorder;
    private AudioTrack track;
    private int[] wavHeader = {0x46464952, 44+NS*2, 0x45564157, 0x20746D66,16, 0x00010001,
            Fs2, Fs2*2, 0x00100002, 0x61746164, NS*2};





    //playSound
    boolean fixbutton = false;
    private int Fs = 22050; // Samping rate (Hz)
    private int length = Fs*10; // length of array for 10 seconds
    private short[ ] data = new short[length]; // Array of 16-bit samples
    private int i;
    // Method for filling data array with random noise:
    void fillRandom()
    { Random rand = new Random();
        for ( i = 0 ; i < length ; i++ ) { data[i] = (short) rand.nextInt();} //Fill data array
    } // end of method fillRandom
    // Method for playing sound from an array:
    void arrayplay()
    { int CONF = AudioFormat.CHANNEL_OUT_MONO;
        int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        int MDE = AudioTrack.MODE_STATIC; //Need static mode.
        int STRMTYP = AudioManager.STREAM_ALARM;
        AudioTrack track = new AudioTrack(STRMTYP, Fs, CONF, FORMAT, length*2, MDE);
        //fillRandom(); // Fill data array with 16-bit samples of random noise
        track.write(data, 0, length);
        //track.setPlaybackRate(8000);
        //while(track.getPlaybackRate() != 8000) { } // wait for new sample rate to be established
        track.play();
        while(track.getPlaybackHeadPosition() != length) {}; //Wait before playing more
        track.stop(); track.setPlaybackHeadPosition(0);
        while(track.getPlaybackHeadPosition() != 0) {}; // wait for head position
    } // end of arrayplay method
    void arrayplaySpeech()
    { int CONF = AudioFormat.CHANNEL_OUT_MONO;
        int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        int MDE = AudioTrack.MODE_STATIC; //Need static mode.
        int STRMTYP = AudioManager.STREAM_ALARM;
        AudioTrack track = new AudioTrack(STRMTYP, Fs, CONF, FORMAT, length*2, MDE);
        //fillRandom(); // Fill data array with 16-bit samples of random noise
        track.write(data, 0, length);
        track.setPlaybackRate(8000);
        while(track.getPlaybackRate() != 8000) { } // wait for new sample rate to be established
        track.play();
        while(track.getPlaybackHeadPosition() != length) {}; //Wait before playing more
        track.stop(); track.setPlaybackHeadPosition(0);
        while(track.getPlaybackHeadPosition() != 0) {}; // wait for head position
    } // end of arrayplay method


    void fixSpeech() {
        //check all the samples and look at the previous sample if there is a rapid increase
        //then we have a spike
        int spikeThreshold = 1000;
        int difference;
        //fix the first samples

        for (i = 45; i < 200; i++) {
            difference = Math.abs(data[i - 1] - data[i]);
            if (difference > spikeThreshold) {
                //if we need to smothed over this sample then take the previous example and the next one
                //add both of them and get the average
                data[i] = (short) ((data[i - 1] + data[i + 1]) / 2);
            }
        }
/*
        for(i = 45; i < length-1; i++)
        {
            difference =  Math.abs(data[i - 1] - data[i]);
            if(difference > spikeThreshold)
            {
                //if we need to smothed over this sample then take the previous example and the next one
                //add both of them and get the average
                data[i] = (short) ((data[i - 1] + data[i + 1])/2);
            }
        }
*/

        //every pack it is sent 200 times then
        //we can skip first package since there is no way to correct this one
        boolean isPackageLost = true;
        int numberMulti = 400;
        int indexOfCopy;
        for(i = 200; i  < length;)
        {
            if (!(data[i] == 0))
            {
                isPackageLost = false;
                i = numberMulti - 1;
            }
            if(numberMulti <= i || !isPackageLost) {

                if(isPackageLost)
                {
                    for (indexOfCopy = numberMulti - 200; indexOfCopy < length && indexOfCopy < numberMulti; indexOfCopy++) {
                        //Log.i(LOGTAG,  " yes" + data[indexOfCopy]); // Report to Logcat
                        data[indexOfCopy] = data[indexOfCopy - 200];
                        //Log.i(LOGTAG,  " no" + data[indexOfCopy - 200]); // Report to Logcat
                    }

                }
                else
                {
                    for (indexOfCopy = numberMulti - 200; indexOfCopy < length-1 && indexOfCopy < numberMulti; indexOfCopy++) {

                        difference = Math.abs(data[indexOfCopy - 1] - data[indexOfCopy]);
                        if (difference > spikeThreshold) {
                            data[indexOfCopy] = (short) ((data[indexOfCopy - 1] + data[indexOfCopy + 1]) / 2);
                            //Log.i(LOGTAG,  " yes" + data[indexOfCopy]); // Report to Logcat
                        }
                    }

                }
                numberMulti = numberMulti + 200;
                isPackageLost = true;

            }
            i++;


        }
/*
        boolean isPackageLost;
        for (i = 200; i < length; i += 200) {
            isPackageLost = true;
            for (int index = 0; index < 200 && i + index < length; index++) {
                if (!(data[i + index] == 0)) {
                    isPackageLost = false;
                    break;

                }
            }
            if (isPackageLost) {
                //copy the content from the last sample
                for (int indexOfCopy = 0; indexOfCopy < length && i + indexOfCopy < length; indexOfCopy++) {
                    data[i + indexOfCopy] = data[i + indexOfCopy - 200];
                }

            } else {
                //check that the package has the correct bits
                for (int indexOfCopy = 0; indexOfCopy < length && i + indexOfCopy < length; indexOfCopy++) {
                    if (!(i + indexOfCopy == length - 1)) {
                        difference = Math.abs(data[i + indexOfCopy - 1] - data[i + indexOfCopy]);
                        if (difference > spikeThreshold)
                            data[i + indexOfCopy] = (short) ((data[i + indexOfCopy - 1] + data[i + indexOfCopy + 1]) / 2);
                    }
                }
            }

        }*/
    }


    void msSpeech()
    { // Method for reading 16-bit samples from a wav file into array data
        int i; int b1 = 0; int b2 = 0;
        try
        { // Make speechis an input stream object for accessing a wav file placed in R.raw
            InputStream speechis=getResources().openRawResource(R.raw.damagedspeech);
            // Read & discard the 44 byte header of the wav file:
            for ( i = 0 ; i < 44 ; i++ ) { b1 = speechis.read(); }
            // Read rest of 16-bit samples from wav file byte-by-byte:
            for ( i = 0 ; i < length ; i++ )
            { b1 = speechis.read(); // Get first byte of 16-bit sample in least sig 8 bits of b1
                if (b1 == -1) {b1 = 0;} // b1 becomes -1 if we try to read past End of File
                b2 = speechis.read(); // Get second byte of sample value in b2
                if (b2 == -1) {b2 = 0;} // trying to read past EOF
                b2 = b2<<8 ; // shift b2 left by 8 binary places
                data[i] = (short) (b1 | b2); //Concat 2 bytes to make 16-bit sample value
                //Log.i(LOGTAG,  " "+ data[i]); // Report to Logcat
            } // end of for loop
            speechis.close();
        } catch (FileNotFoundException e) {Log.e("tag", "wav file not found", e);}
        catch (IOException e) { Log.e("tag", "Failed to close input stream", e);}
    } // end of msSpeech method




    //display image
    void msImage(String filename)
    { // Reads from an image file into an array for image processing in Java
        try { int res_id = getResources().getIdentifier(filename, "raw", getPackageName() );
            InputStream image_is = getResources().openRawResource(res_id);
            int filesize = image_is.available(); //Get image file size in bytes
            byte[ ] image_array = new byte[filesize]; //Create array to hold image
            image_is.read(image_array); //Load image into array
            image_is.close(); // Close in-out file stream
            // Add your code here to process image_array & save processed version to a file.
            File newImageFile = new File(getFilesDir(), filename);
            OutputStream image_os = new FileOutputStream(newImageFile);
            image_os.write(image_array, 0, filesize);
            image_os.flush();
            image_os.close();
            //Display the processed image-file
            Bitmap newBitmap = BitmapFactory.decodeFile(newImageFile.getAbsolutePath());
            // Create ImageView object
            ImageView image = (ImageView) findViewById(R.id.imageView1);
            image.setImageBitmap(newBitmap);
        } // end of try
        catch (FileNotFoundException e) { Log.e("tag", "File not found ", e);}
        catch (IOException e) { Log.e("tag", "Failed for stream", e); }
    } //end of msImage method
    void msImage3(String filename)
    { // Reads from an image file into an array for image processing in Java
        try { int res_id = getResources().getIdentifier(filename, "raw", getPackageName() );
            InputStream image_is = getResources().openRawResource(res_id);
            int filesize = image_is.available(); //Get image file size in bytes
            byte[ ] image_array = new byte[filesize]; //Create array to hold image
            image_is.read(image_array); //Load image into array
            image_is.close(); // Close in-out file stream
            // Add your code here to process image_array & save processed version to a file.
            File newImageFile = new File(getFilesDir(), filename);
            OutputStream image_os = new FileOutputStream(newImageFile);
            image_os.write(image_array, 0, filesize);
            image_os.flush();
            image_os.close();
            //Display the processed image-file
            Bitmap newBitmap = BitmapFactory.decodeFile(newImageFile.getAbsolutePath());
            // Create ImageView object
            ImageView image = (ImageView) findViewById(R.id.imageView5);
            image.setImageBitmap(newBitmap);
        } // end of try
        catch (FileNotFoundException e) { Log.e("tag", "File not found ", e);}
        catch (IOException e) { Log.e("tag", "Failed for stream", e); }
    } //end of msImage method

    void msImage4(String filename)
    { // Reads from an image file into an array for image processing in Java
        try { int res_id = getResources().getIdentifier(filename, "raw", getPackageName() );
            InputStream image_is = getResources().openRawResource(res_id);
            int filesize = image_is.available(); //Get image file size in bytes
            byte[ ] image_array = new byte[filesize]; //Create array to hold image
            image_is.read(image_array); //Load image into array
            image_is.close(); // Close in-out file stream
            // Add your code here to process image_array & save processed version to a file.
            File newImageFile = new File(getFilesDir(), filename);
            OutputStream image_os = new FileOutputStream(newImageFile);
            image_os.write(image_array, 0, filesize);
            image_os.flush();
            image_os.close();
            //Display the processed image-file
            Bitmap newBitmap = BitmapFactory.decodeFile(newImageFile.getAbsolutePath());
            // Create ImageView object
            ImageView image = (ImageView) findViewById(R.id.imageView7);
            image.setImageBitmap(newBitmap);
        } // end of try
        catch (FileNotFoundException e) { Log.e("tag", "File not found ", e);}
        catch (IOException e) { Log.e("tag", "Failed for stream", e); }
    } //end of msImage method

    void msImage5(String filename)
    { // Reads from an image file into an array for image processing in Java
        try { int res_id = getResources().getIdentifier(filename, "raw", getPackageName() );
            InputStream image_is = getResources().openRawResource(res_id);
            int filesize = image_is.available(); //Get image file size in bytes
            byte[ ] image_array = new byte[filesize]; //Create array to hold image
            image_is.read(image_array); //Load image into array
            image_is.close(); // Close in-out file stream
            // Add your code here to process image_array & save processed version to a file.
            File newImageFile = new File(getFilesDir(), filename);
            OutputStream image_os = new FileOutputStream(newImageFile);
            image_os.write(image_array, 0, filesize);
            image_os.flush();
            image_os.close();
            //Display the processed image-file
            Bitmap newBitmap = BitmapFactory.decodeFile(newImageFile.getAbsolutePath());
            // Create ImageView object
            ImageView image = (ImageView) findViewById(R.id.imageView9);
            image.setImageBitmap(newBitmap);
        } // end of try
        catch (FileNotFoundException e) { Log.e("tag", "File not found ", e);}
        catch (IOException e) { Log.e("tag", "Failed for stream", e); }
    } //end of msImage method


    void msImage2(String filename)
    { // Reads from an image file into an array for image processing in Java
        try { int res_id = getResources().getIdentifier(filename, "raw", getPackageName() );
            InputStream image_is = getResources().openRawResource(res_id);
            int filesize = image_is.available(); //Get image file size in bytes
            byte[ ] image_array = new byte[filesize]; //Create array to hold image
            image_is.read(image_array); //Load image into array
            image_is.close(); // Close in-out file stream
            // Add your code here to process image_array & save processed version to a file.
            File newImageFile = new File(getFilesDir(), filename);
            OutputStream image_os = new FileOutputStream(newImageFile);
            image_os.write(image_array, 0, filesize);
            image_os.flush();
            image_os.close();
            //Display the processed image-file
            Bitmap newBitmap = BitmapFactory.decodeFile(newImageFile.getAbsolutePath());
            // Create ImageView object
            ImageView image = (ImageView) findViewById(R.id.imageView3);
            image.setImageBitmap(newBitmap);
        } // end of try
        catch (FileNotFoundException e) { Log.e("tag", "File not found ", e);}
        catch (IOException e) { Log.e("tag", "Failed for stream", e); }
    } //end of msImage method
    //Declare class variables
    private MediaPlayer mySound;
    private int imagePick = 0;
    private int dmgImagePick = 0;
    private static final String LOGTAG = "Main UI"; //Logcat messages from UI are identified
    private NetworkConnectionAndReceiver networkConnectionAndReceiver = null;
    private String transmitterText; //Transmitter data variable
    public EditText user;
    // Class methods

    @Override
    //Extend the onCreate method, called whenever an activity is started
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Extend the onCreate method
        // Set up the view using xml description res>layout>activity_main.xml
        setContentView(R.layout.activity_main);
        //include the the over-rided 'onCreate'
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }

        createRecord();

        wavFileName = getExternalFilesDir(null) + "/Barry.wav";
        Log.e(LOGTAG, "Wav file is " + wavFileName);
        // Get the following buttons as defined in Res dir xml code
        //final Button stopButton = findViewById(R.id.btnstopRecord);
        final Button recButton = findViewById(R.id.btnRecord);
        final Button playButton = findViewById(R.id.btnPlay);

        recButton.setOnClickListener
                (new View.OnClickListener()
                { public void onClick(View v)
                { recButton.setText("RECORDING"); recorder.startRecording();
                    int i;
                    for(i = 0; i < recChunks; i++ )
                    { recorder.read(soundSamples, i*recBufferSize, (int) recBufferSize);}
                    recorder.read(soundSamples, i*recBufferSize, NS - i*recBufferSize);
                    recorder.stop(); Log.e(LOGTAG, "Finished recording");
                    try { File wavFile = new File(wavFileName);
                        FileOutputStream wavOutputStream = new FileOutputStream(wavFile);
                        DataOutputStream wavDataOutputStream = new DataOutputStream(wavOutputStream);
                        for (i = 0; i < wavHeader.length; i++)
                        { wavDataOutputStream.writeInt(Integer.reverseBytes(wavHeader[i])); }
                        for (i = 0 ; i < soundSamples.length ; i++ )
                        { wavDataOutputStream.writeShort(Short.reverseBytes(soundSamples[i])); }
                        wavOutputStream.close(); Log.e(LOGTAG, "Wav file saved");
/*
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(new
                                File(getFilesDir()+File.separator+"Barry.wav")));

                        int read;
                        //StringBuilder builder = new StringBuilder("");
                        int m = 0;
                        while(m < 3){
                            //builder.append(read);
                            read = bufferedReader.read();
                            m++;
                            Log.i(LOGTAG, "Output" + (char)read); // Report to Logcat

                        }
                        //Log.i(LOGTAG, "Output" + builder.toString()); // Report to Logcat
                        //Log.d();
                        bufferedReader.close();

*/
                    } catch (IOException e) { Log.e(LOGTAG, "Wavfile write error");}
                    recButton.setText("DONE");
                } // end of onClick method
                }); // end of recButton.setOnClickListener
        playButton.setOnClickListener
                ( new View.OnClickListener()
                { public void onClick(View v)
                { playButton.setText("PLAYING");
                    track.write(soundSamples, 0, (int) NS); track.play();
                    while(track.getPlaybackHeadPosition() < NS) { }; //Wait before playing more
                    track.stop(); track.setPlaybackHeadPosition(0);
                    while(track.getPlaybackHeadPosition() != 0) { }; // wait for head position
                    playButton.setText("PLAY"); // for next time
                } //end of onClick method
                } ); //end of playButton.setOnClickListener

        msSpeech();
        //arrayplaySpeech();
        mySound = MediaPlayer.create(this, R.raw.damagedspeech);
        ImageView image = (ImageView) findViewById(R.id.imageView1);
        ImageView image2 = (ImageView) findViewById(R.id.imageView3);
        //mySound.start();
        //mySound.release();
        Log.i(LOGTAG, "Starting task4 app"); // Report to Logcat

        // Instantiate the network connection and receiver object
        networkConnectionAndReceiver = new NetworkConnectionAndReceiver(this);
        networkConnectionAndReceiver.start();    // Start socket-receiver thread


        // Get the receiving text area as defined in the Res dir xml code
        final TextView receiverTextArea = findViewById(R.id.txtServerResponse);

        EditText editText = (EditText) findViewById(R.id.userName);
        // Make the receiving text area scrollable
        receiverTextArea.setMovementMethod(new ScrollingMovementMethod());


        // Get the kill button as defined in the Res dir xml code
        Button killButton = findViewById(R.id.btnKill);
        // Make the kill button receptive to being clicked
        // Button click handler
        killButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the killButton object
            public void onClick(View v) {
                // OnClick actions here
                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                transmitterText = "DISCONNECT";
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup

                    Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                    transmitter.start();        // Run on its own thread
                    Log.i(LOGTAG, "help" + networkConnectionAndReceiver.getIsRegistered()); // Report to Logcat
                }
                System.exit(0);  // Exit app


            }
        });

        // Get the text area for commands to be transmitted as defined in the Res dir xml code
        user = (EditText) findViewById(R.id.userName);


        //transmitterText = "lol";
        // Get the send button as defined in the Res dir xml code
        final Button sendButton =  findViewById(R.id.btnSendCmd);
        final Button whoButton = findViewById(R.id.listOfUsers);
        final Button inviteButton = findViewById(R.id.button_invite);
        final Button acceptButton = findViewById(R.id.button_accept);
        final Button endButton = findViewById(R.id.button_end);
        final Button declineButton = findViewById(R.id.button_decline);
        final Button msgButton = findViewById(R.id.button_msg);
        final Button startButton = findViewById(R.id.button_start);
        final Button msg_protocol = findViewById(R.id.button_send_protocol);
        final Button invite_protocol = findViewById(R.id.button_invite_protocol);
        final Button msgSND = findViewById(R.id.msgSound);
        // Make the kill button receptive to being clicked
        // Button click handler
        sendButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                transmitterText = "REGISTER " + ((EditText)findViewById(R.id.userName)).getText().toString();
                ((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                    transmitter.start();        // Run on its own thread
                         Log.i(LOGTAG, "check the message: " + transmitter); // Report to Logcat
                }
                //((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    if (networkConnectionAndReceiver.getIsRegistered().equals("nice"))
                    {
                        //acceptButton.setVisibility(View.VISIBLE);
                        //endButton.setVisibility(View.VISIBLE);
                        //declineButton.setVisibility(View.VISIBLE);
                        whoButton.setVisibility(View.VISIBLE);
                        startButton.setVisibility(View.INVISIBLE);
                        //inviteButton.setVisibility(View.VISIBLE);
                        //msgButton.setVisibility(View.VISIBLE);
                        sendButton.setVisibility(View.INVISIBLE);
                        invite_protocol.setVisibility(View.VISIBLE);
                        msg_protocol.setVisibility(View.VISIBLE);
                        user.setVisibility(View.INVISIBLE);
                    }

                }
                //((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        invite_protocol.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                        findViewById(R.id.userto_name).setVisibility(View.INVISIBLE);
                        findViewById(R.id.message_id).setVisibility(View.INVISIBLE);
                        acceptButton.setVisibility(View.VISIBLE);
                        endButton.setVisibility(View.VISIBLE);
                        declineButton.setVisibility(View.VISIBLE);
                        //whoButton.setVisibility(View.INVISIBLE);
                        //startButton.setVisibility(View.INVISIBLE);
                        inviteButton.setVisibility(View.VISIBLE);
                        msgButton.setVisibility(View.INVISIBLE);
                        sendButton.setVisibility(View.INVISIBLE);
                        invite_protocol.setVisibility(View.INVISIBLE);
                        msg_protocol.setVisibility(View.VISIBLE);
                        user.setVisibility(View.VISIBLE);
                        findViewById(R.id.pickMode).setVisibility(View.INVISIBLE);
                }
                //((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
        msg_protocol.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    findViewById(R.id.userto_name).setVisibility(View.VISIBLE);
                    findViewById(R.id.message_id).setVisibility(View.VISIBLE);
                    acceptButton.setVisibility(View.INVISIBLE);
                    endButton.setVisibility(View.INVISIBLE);
                    declineButton.setVisibility(View.INVISIBLE);
                    //whoButton.setVisibility(View.INVISIBLE);
                    //startButton.setVisibility(View.INVISIBLE);
                    inviteButton.setVisibility(View.INVISIBLE);
                    msgButton.setVisibility(View.VISIBLE);
                    //sendButton.setVisibility(View.INVISIBLE);
                    invite_protocol.setVisibility(View.VISIBLE);
                    msg_protocol.setVisibility(View.INVISIBLE);
                    findViewById(R.id.pickMode).setVisibility(View.VISIBLE);
                    user.setVisibility(View.INVISIBLE);
                }
                //((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });

        //second button for getting the list of user in the server
        whoButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                transmitterText = "WHO"  + ((EditText)findViewById(R.id.userName)).getText().toString();
                ((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                    Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                    transmitter.start();        // Run on its own thread
                }
            }
        });
        //the following are the rest of the buttons that will be used for creating a link with barry bot
        inviteButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                transmitterText = "INVITE "  + ((EditText)findViewById(R.id.userName)).getText().toString();
                ((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                    Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                    transmitter.start();        // Run on its own thread
                }
            }
        });
        //
        acceptButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                transmitterText = "ACCEPT " + ((EditText)findViewById(R.id.userName)).getText().toString();
                ((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                    Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                    transmitter.start();        // Run on its own thread
                }
            }
        });
        //
        endButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                transmitterText = "END " + ((EditText)findViewById(R.id.userName)).getText().toString();
                ((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                    Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                    transmitter.start();        // Run on its own thread
                }
            }
        });
        //
        declineButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {

                transmitterText = "DECLINE " + ((EditText)findViewById(R.id.userName)).getText().toString();
                ((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                    Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                    transmitter.start();        // Run on its own thread
                }
            }
        });
        //
        msgButton.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {
                //transmitterText = (EditText)findViewById(R.id.pickMode)).getText().toString());
                transmitterText = "";
                transmitterText = transmitterText.concat(((EditText)findViewById(R.id.pickMode)).getText().toString());
                transmitterText = transmitterText.concat("MSG ");
                transmitterText = transmitterText.concat(((EditText)findViewById(R.id.message_id)).getText().toString());
                transmitterText = transmitterText.concat(" ");
                String bitString = ((EditText)findViewById(R.id.userto_name)).getText().toString();
                if(!bitString.equals("ENCRYPT"))
                 bitString = text2bitS(((EditText)findViewById(R.id.userto_name)).getText().toString(), ((EditText)findViewById(R.id.userto_name)).getText().toString().length());
                transmitterText = transmitterText.concat(bitString);
                Log.i(LOGTAG, "help >>> " + transmitterText); // Report to Logcat
                //Log.i(LOGTAG, "Translate " + transmitterText); // Report to Logcat
                //transmitterText = "MSG " + ((EditText)findViewById(R.id.message_id)).getText().toString() + " " + ((EditText)findViewById(R.id.userto_name)).getText().toString();
                //transmitterText.concat((EditText)findViewById(R.id.pickMode)).getText().toString())
                ((EditText)findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
                // OnClick actions here
                // Instantiate the transmitter passing the output stream and text to it
                if(networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                    Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                    Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                    transmitter.start();        // Run on its own thread
                }
            }
        });
        msgSND.setOnClickListener(new View.OnClickListener() {
            // onClick method implementation for the sendButton object
            public void onClick(View v) {
                //transmitterText = (EditText)findViewById(R.id.pickMode)).getText().toString());
                transmitterText = "";
                transmitterText = transmitterText.concat(((EditText)findViewById(R.id.pickMode)).getText().toString());
                transmitterText = transmitterText.concat("MSG ");
                transmitterText = transmitterText.concat("BarryBot");
                transmitterText = transmitterText.concat(" ");
                String tempText = transmitterText;
                Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                //we will send 3 seconds of audio to barrybot
                int limitSecond = 3;
                int [] packet = new int[250];
                int limitPacketNumber = 42;
                int packerOffSet = 250;
                int packetForSecond = 250 * limitPacketNumber;
                String bitString;
                for(int second = 0; second < limitSecond; second++)
                {
                    //42 packet for sending one second
                    for(int packetNumber = 0; packetNumber < limitPacketNumber; packetNumber++) {
                        Log.i(LOGTAG, "this is packet : " + packetNumber + "\n"); // Report to Logcat
                        for (int indexPacket = 0; indexPacket < packet.length; indexPacket++) {
                            //Log.i(LOGTAG, "" + data[indexPacket + (packerOffSet * packetNumber) + (second * packetForSecond)]); // Report to Logcat
                            packet[indexPacket] = soundSamples[indexPacket + (packerOffSet * packetNumber) + (second * packetForSecond)];
                            //divide by 2
                            packet[indexPacket] /= 2;
                            packet[indexPacket] += 16384;

                           // Log.i(LOGTAG, "" + packet[indexPacket]); // Report to Logcat
                        }

                        bitString = IntA2bitS(packet, packet.length);
                        transmitterText = transmitterText.concat(bitString);
                        //Log.i(LOGTAG, "help >>> " + transmitterText); // Report to Logcat

                        ((EditText) findViewById(R.id.userName)).onEditorAction(EditorInfo.IME_ACTION_DONE);
                        // OnClick actions here
                        // Instantiate the transmitter passing the output stream and text to it
                        //Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                        if (networkConnectionAndReceiver.getStreamOut() != null) { // Check that output stream has be setup
                           // Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                            //Log.i(LOGTAG, "check the message: " + transmitterText); // Report to Logcat
                            Transmitter transmitter = new Transmitter(networkConnectionAndReceiver.getStreamOut(), transmitterText);
                            transmitter.start();        // Run on its own thread
                        }

                        transmitterText = tempText;
                    }

                }



                //transmitterText = transmitterText.concat(bitString);

            }
        });
    } //End of app onCreate method

    public void playSound(View view) {
            mySound.start();

    }
    public static String text2bitS(String textString, int L)
    {
        //Converts an array charA of L positive 16-bit integers to a bit-string bits
        char[] charA = textString.toCharArray();
        String bitS = "";
        for(int i=0; i < L; i++)
        {
            int d = charA[i];
            for(int j = 0; j < 16; j++)
            {
                int k=i*16+j; bitS = bitS + String.valueOf(d & 1); d = d >> 1;
            }
        }
        return bitS;
    }//end of method

    public void stopFile(View view) {
        mySound.release();
    }

    public void displayImg(View view) {
        switch (imagePick)
        {
            case 0: msImage("peppers");
                    imagePick++;
                    break;
            case 1: msImage3("peppersjpg");
                    imagePick++;
                    break;
            case 2: break;
        }

    }

    public void displayBadImg(View view) {
        switch (dmgImagePick)
        {
            case 0: msImage2("damagedpeppers");
                dmgImagePick++;
                break;
            case 1: msImage4("damagedpeppersjpg");
                dmgImagePick++;
                break;
            case 2: msImage5("verydamagedpeppers");
                dmgImagePick++;
                break;
            case 3:
                break;

        }
    }

    public void playDmgSound(View view) {
        arrayplaySpeech();
    }

    public void fixSound(View view) {
        if(!fixbutton)
        {
            fixSpeech();
            fixbutton = true;
        }
        arrayplaySpeech();
    }

    public void createRecord()
    {

        recBufferSize = AudioRecord.getMinBufferSize
                (Fs2, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recChunks = ((int) (NS/recBufferSize));

        recorder = new AudioRecord ( MediaRecorder.AudioSource.MIC, (int) Fs2,
                AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT, recBufferSize*2 );
        track = new AudioTrack
                ( AudioManager.STREAM_MUSIC, Fs2, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, NS*2, AudioTrack.MODE_STATIC );

    }
    public static String IntA2bitS(int[ ] IntA, int L)
    {// Converts an array IntA of L positive 16-bit integers to a bit-string bitS
        String bitS = "";
        for (int i = 0; i < L; i++)
        { int d = IntA[i];
            for (int j = 0; j < 16; j++)
            { int k=i*16+j; bitS = bitS + String.valueOf(d & 1); d = d >> 1; } }
        return bitS;
    } // end of method

/*  // Following code used when using basic activity
    @Override
    //Create an options menu
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // Uses res>menu>main.xml
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    //Called when an item is selected from the options menu
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    } //End of app onOptionsItemSelected method
*/

}//End of app MainActivity class


