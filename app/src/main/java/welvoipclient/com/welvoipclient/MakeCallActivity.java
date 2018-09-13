package welvoipclient.com.welvoipclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telecom.Call;
import android.util.Log;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static java.security.AccessController.getContext;

/**
 * Created by Krystiano on 2016-12-23.
 */

public class MakeCallActivity {


    Activity activity;
    Socket socket;
    ServerSocket serverSocket;
    public final double BandwidthG711u = 0.0232;
    public final double BandwidthGSM = 0.010839;
    public final double BandwidthGSMEFR = 0.0105;
    private AudioStream audioStream;
    private AudioGroup audioGroup;
    private int codecs;

    Boolean zmiana= false;
    public MakeCallActivity(Activity context) {activity=context;

    }


    void answer(final String incommingIp) //funkcja odbierająca połączenie
    {
        Thread accept = new Thread() {
            public void run()
            {

                try {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    dos.writeUTF("ACCEPT");
                    String data = dis.readUTF();


                    String code = data.substring(1);
                    codecs = Integer.valueOf(data.substring(0, 1));

                    Log.d("TAG", "port: " + code + " kodek: " + codecs);

                    AudioManager audioManager =  (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
                   audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                   audioGroup = new AudioGroup();
                    audioGroup.setMode(AudioGroup.MODE_NORMAL);
                   audioStream = new AudioStream(InetAddress.getByName(MainActivity.localIP));

                    switch (codecs)
                    {
                        case 0:
                            audioStream.setCodec(AudioCodec.GSM);
                            break;
                        case 1:
                            audioStream.setCodec(AudioCodec.PCMU);
                            break;
                        case 2:
                            audioStream.setCodec(AudioCodec.AMR);
                            break;
                        case 3:
                            audioStream.setCodec(AudioCodec.GSM_EFR);
                            break;
                    }

                    audioStream.setMode(RtpStream.MODE_NORMAL);
                    audioStream.associate(InetAddress.getByName(incommingIp), Integer.valueOf(code)); //asocjacja z drugim użytkownikiem
                    audioStream.join(audioGroup);
                      if(zmiana==true)
                      {
                          UpdateStatus("Wznowiono połączenie");
                          zmiana=false;
                      }
                }
                catch (Exception e) {e.printStackTrace();}

            }
        };
        accept.start();
    }

    void makeCall(final String ip,final int codec) //funkcja wykonująca połączenie
    {
        Thread makeCall = new Thread() {
            public void run()
            {
                try {
                    Socket client = new Socket(ip, 8888);
                    DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                    DataInputStream dis = new DataInputStream(client.getInputStream());
                    dos.writeUTF("MAKECALL");
                    String code = dis.readUTF();
                    Log.d("TAG", "odebrano: "+code);
                    if(code.equals("ACCEPT"))
                    {
                        AudioManager audioManager =  (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        audioGroup = new AudioGroup();
                        audioGroup.setMode(AudioGroup.MODE_NORMAL);
                        audioStream = new AudioStream(InetAddress.getByName(MainActivity.localIP));
                        Log.d("TAG", InetAddress.getByName(MainActivity.localIP).toString());
                        switch (codec)
                        {
                            case 0:
                                audioStream.setCodec(AudioCodec.GSM);
                                break;
                            case 1:
                                audioStream.setCodec(AudioCodec.PCMU);
                                break;
                            case 2:
                                audioStream.setCodec(AudioCodec.AMR);
                                break;
                            case 3:
                                audioStream.setCodec(AudioCodec.GSM_EFR);
                                break;
                        }
                        audioStream.setMode(RtpStream.MODE_NORMAL);
                        audioStream.associate(InetAddress.getByName(ip), 2000); //asoccjacja z drugim hostem
                        audioStream.join(audioGroup);
                        dos.writeUTF(String.valueOf(codec)+String.valueOf(audioStream.getLocalPort())); //wysłanie informacji o kodeku i porcie
                        Intent i = new Intent(activity, CallActivity.class);
                        activity.startActivityForResult(i, 0);
                    }
                    client.close();
                }
                catch (Exception e) {e.printStackTrace();}
            }
        };
        makeCall.start();
    }

    void waitForConnection() //funkcja oczejująca na nadchodzące połączenie
    {
        Thread lis = new Thread()
        {
            public void run()
            {
                try
                {

                    serverSocket = new ServerSocket(8888); //otwarcie gniazda
                    Log.d("TAG", "oczekiwanie na połączenie");
                    while(true) {
                        socket = serverSocket.accept(); //czekamy na nadchodzące połączenie
                        Log.d("TAG", "połączenie zaakceptowane");
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        String signal = dis.readUTF();
                        Log.d("TAG", "Odebrano:" + signal);
                        if (signal.equals("MAKECALL")) {
                            Intent i = new Intent(activity, IncomingCallActivity.class);
                            i.putExtra("ip", ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().replace("/", ""));

                            activity.startActivityForResult(i, 0);
                        }
                        else if(signal.equals("CHANGE")){
                            zmiana=true;
                            answer(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().replace("/", ""));
                        }
                    }
                }
                catch(Exception e) {e.printStackTrace();}
            }
        };
        lis.start();
    }

    void checkBandwith(final String ip) //funkcja określają przepustowość
    {
        Thread check = new Thread()
        {
            public void run()
            {
                Log.d("TAG", "wysylanie danych testowwych podczas połączenia");
                String bandwidth = null;
                Socket socket1 = null;
                Socket socket2=null;
                OutputStream os = null;
                Boolean test=true;

                try {
//
                    byte[] bytes = new byte[1024 * 100];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = 1;
                    }
                    Thread.sleep(5000);
                    int codec=codecs;
                    while(test==true) {
                        socket1 = new Socket(ip, 8080);
                        os = socket1.getOutputStream();

                        os.write(bytes);
                        Log.d("TAG","Wysłano: "+bytes.length );
                        os.flush();
                        DataInputStream dis1 = new DataInputStream(socket1.getInputStream());
                        bandwidth = dis1.readUTF();
                        Log.d("TAG", "Przepustowśc podczas połączenia wynosi:" + bandwidth);
                        Log.d("TAG", "kodek to:" +codec);
                        double parsingBandwidth = Double.parseDouble(bandwidth);

                        switch (codec){
                            case 2: //AMR
                                if(parsingBandwidth>BandwidthGSMEFR && parsingBandwidth<BandwidthGSM) {
                                    Log.d("TAG","zmiana kodeka na GSM-EFR");
                                    codec=3;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else if(parsingBandwidth>BandwidthGSM && parsingBandwidth<BandwidthG711u)
                                {
                                    Log.d("TAG","zmiana kodeka na GSM");
                                    codec=0;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else if(parsingBandwidth>BandwidthG711u)
                                {
                                    Log.d("TAG","zmiana kodeka na G711u");
                                    codec=1;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else
                                {
                                    Thread.sleep(10000);
                                }
                                break;
                            case 1: //G711u

                            if(BandwidthG711u> parsingBandwidth && parsingBandwidth > BandwidthGSM )

                                {
                                    Log.d("TAG","zmiana kodeka na GSM");
                                    codec=0;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                    changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else if(BandwidthGSM>parsingBandwidth && parsingBandwidth > BandwidthGSMEFR) {
                                    Log.d("TAG", "zmiana kodeka na GSM-EFR");
                                    codec=3;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                  socket2.close();
                              Thread.sleep(1000);
                                }
                                else if(BandwidthGSMEFR>parsingBandwidth)
                                {
                                      Log.d("TAG","Zmiana kodeka na AMR");
                                      codec=2;
                                      UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                      endCall();
                                      socket2 = new Socket(ip, 8880);
                                      DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                      DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                      dos.writeUTF("END");
                                      String code=dis.readUTF();
                                      if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else
                                {
                                    Thread.sleep(10000);
                                }
                                break;
                            case 0: // GSM
                              if(BandwidthGSM > parsingBandwidth && parsingBandwidth> BandwidthGSMEFR)
                                {
                                    Log.d("TAH","zmiana kodeka na GSM-EFR ");
                                    codec=3;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else if(BandwidthGSMEFR> parsingBandwidth) {
                                    Log.d("TAH", "zmiana kodeka na AMR");
                                    codec = 2;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                  else if(parsingBandwidth>BandwidthG711u)
                                  {
                                      Log.d("TAG","zmiana kodeka na G711u");
                                      codec=1;
                                      UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                      endCall();
                                      socket2 = new Socket(ip, 8880);
                                      DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                      DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                      dos.writeUTF("END");
                                      String code=dis.readUTF();
                                      if(code.equals("DONE")){
                                          changeCodec(ip,codec);
                                      }
                                      socket2.close();
                                      Thread.sleep(10000);
                                  }
                                else
                                {
                                    Thread.sleep(10000);
                                }
                                break;
                            case 3://GSM-EFR
                                if(BandwidthGSMEFR > parsingBandwidth)
                                {
                                    Log.d("TAG","zmiana kodeka na AMR");
                                    codec=2;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else if(parsingBandwidth>BandwidthGSM && parsingBandwidth<BandwidthG711u)
                                {
                                    Log.d("TAG","zmiana kodeka na GSM");
                                    codec=0;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else if(parsingBandwidth>BandwidthG711u)
                                {
                                    Log.d("TAG","zmiana kodeka na G711u");
                                    codec=1;
                                    UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                                    endCall();
                                    socket2 = new Socket(ip, 8880);
                                    DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                    DataInputStream dis=new DataInputStream(socket2.getInputStream());
                                    dos.writeUTF("END");
                                    String code=dis.readUTF();
                                    if(code.equals("DONE")){
                                        changeCodec(ip,codec);
                                    }
                                    socket2.close();
                                    Thread.sleep(10000);
                                }
                                else
                                {
                                    Thread.sleep(10000);
                                }
                                break;
                        }

                        socket1.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        check.start();
    }
    public void endCall()
    {
        if (audioStream != null) {
            audioStream.join(null);
            audioStream.release();
            audioStream = null;
        }
    }

    public void UpdateStatus(final String mystr) { //zmiana statusu
        EventBus.getDefault().post(new MessageEvent(mystr));
    }

    void listen ()
    {
        Thread listen = new Thread()
        {
            public void run()
            {
                Log.d("TAG", "oczekiwanie na dane testowe");
                    ServerSocket server;
                    Socket socket2;
                    InputStream input;
                    try {
                        server = new ServerSocket(8080);    //otwarcie gniazda

                        while (true) {  //rozpoczynamy pomiar
                            socket2 = server.accept();  //funkcja czekająca na nadchodzące połączenie
                            input = socket2.getInputStream();
                            long size = 0;
                            long start = System.currentTimeMillis();
                            byte[] data = new byte[1024 * 100];
                            int read = input.read(data);
                            Log.d("TAG","Odebrano: "+ data[3]);
                            size += read;
                            long roundTripDelay = System.currentTimeMillis() - start; // znakowanie czasowe

                                double MBytes = (size / (1024.0 * 1024)); //zamiana na MB
                                double time = (roundTripDelay / 1000.0); // zamiana na sek
                                double bandwidth = MBytes / time;
                                Log.d("TAG", "Odebrano " + size + " bajtow z predkoscia: " + MBytes / time + " MB/s");
                                DataOutputStream dos = new DataOutputStream(socket2.getOutputStream());
                                dos.writeUTF(String.valueOf(bandwidth));
                            dos.flush();

                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
            }

        };
        listen.start();
    }
    public void changeCodec(final String ip,final int codec){
        Thread changeCodec = new Thread() {
            public void run()
            {
                try {
                    Socket client = new Socket(ip, 8888);
                    DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                    DataInputStream dis = new DataInputStream(client.getInputStream());
                    dos.writeUTF("CHANGE");
                    dos.flush();
                    String code = dis.readUTF();
                    Log.d("TAG", "odebrano: "+code);
                    if(code.equals("ACCEPT"))
                    {
                        AudioManager audioManager =  (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        audioGroup = new AudioGroup();
                        audioGroup.setMode(AudioGroup.MODE_NORMAL);
                        audioStream = new AudioStream(InetAddress.getByName(MainActivity.localIP));
                        Log.d("TAG", InetAddress.getByName(MainActivity.localIP).toString());

                        switch (codec)
                        {
                            case 0:
                                audioStream.setCodec(AudioCodec.GSM);
                                break;
                            case 1:
                                audioStream.setCodec(AudioCodec.PCMU);
                                break;
                            case 2:
                                audioStream.setCodec(AudioCodec.AMR);
                                break;
                            case 3:
                                audioStream.setCodec(AudioCodec.GSM_EFR);
                                break;
                        }
                        audioStream.setMode(RtpStream.MODE_NORMAL);
                        audioStream.associate(InetAddress.getByName(ip), 2000); //asoccjacja z drugim hostem
                        audioStream.join(audioGroup);
                        dos.writeUTF(String.valueOf(codec)+String.valueOf(audioStream.getLocalPort())); //wysłanie informacji o kodeku i porcie
                        dos.flush();
                        UpdateStatus("Wznowiono połączenie");
                    }
                    client.close();
                }
                catch (Exception e) {e.printStackTrace();}
            }
        };
        changeCodec.start();

    }
    void endCalllisten() //funkcja serwera
    {
        Thread lis = new Thread()
        {
            public void run()
            {
                try{
                ServerSocket server;
                Socket socket;
                    server = new ServerSocket(8880); //otwarcie gniazda
                    Log.d("TAG", "oczekiwanie na połączenie");
                    while(true) {
                        socket = server.accept(); //czekamy na nadchodzące połączenie
                        Log.d("TAG", "połączenie zaakceptowane");
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        String signal = dis.readUTF();
                        Log.d("TAG", "Odebrano:" + signal);
                        if (signal.equals("END")) {
                           endCall();
                            UpdateStatus("Zmiana kodeka, proszę czekać na wznowienie połączenia...");
                        }
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("DONE");
                        dos.flush();

                    }
                }
                catch(Exception e) {e.printStackTrace();}
            }
        };
        lis.start();
    }
}
