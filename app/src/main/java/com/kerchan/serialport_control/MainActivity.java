package com.kerchan.serialport_control;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img1,img2,img3;
    protected SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    private ReadThread mReadThread;

    private class ReadThread extends Thread
    {
        @Override
        public void run()
        {
            super.run();

            while(!isInterrupted())
            {
                int size;
                Log.v("debug", "接收线程已经开启");
                try
                {
                    byte[] buffer = new byte[64];

                    if (mInputStream == null)
                        return;

                    size = mInputStream.read(buffer);

                    if (size > 0)
                    {
                        onDataReceived(buffer, size);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable(){
            public void run(){
                String recinfo = new String(buffer, 0, size);
                Toast.makeText(MainActivity.this,"onDataReceived: " + recinfo,
                        Toast.LENGTH_SHORT).show();
                Log.v("debug", "接收到串口信息======>" + recinfo + "/");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);

        img1.setOnClickListener(this);
        img2.setOnClickListener(this);
        img3.setOnClickListener(this);
        initSerialPort();

    }

    private void initSerialPort() {
        try {
            mSerialPort = new SerialPort(new File("/dev/ttyS2"), 115200, 0);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("test", "启动失败");
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img1:
                sendCmds("PU001");
                break;
            case R.id.img2:
                sendCmds("PU002");
                break;
            case R.id.img3:
                sendCmds("PU003");
                break;
            default:
                break;
        }
    }

    /**
     * 发送指令到串口
     *
     * @param cmd
     * @return
     */
    public boolean sendCmds(String cmd) {
        boolean result = true;
        byte[] mBuffer = cmd.getBytes();
        //注意：我得项目中需要在每次发送后面加\r\n，大家根据项目项目做修改，也可以去掉，直接发送mBuffer
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBuffer);
                mOutputStream.flush();
            } else {
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

}
