package com.tonyzyc.thread;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// thread to send message
public class SendThread extends Thread {
    private String msg;
    private Socket socket;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public SendThread(Socket socket) {
        this.socket = socket;
    }

    public SendThread(Socket socket, String msg) {
        this.socket = socket;
        this.msg = msg;
    }

    public void run() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            while (true) {
                Thread.sleep(50);
                if (msg != null) {
                    dataOutputStream.writeUTF(msg);
                    msg = null;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
