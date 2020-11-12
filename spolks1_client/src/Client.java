import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import static java.lang.Math.toIntExact;

public class Client {

    protected Socket clientSocket = null;
    protected DataOutputStream dos = null;//выходной
    protected DataInputStream dis = null;//входной поток



    public Client(String address, int port) {
        try {
            clientSocket = new Socket(address, port);
            this.run();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }


    public void run() {
        try {
            dis = new DataInputStream(clientSocket.getInputStream());
            dos = new DataOutputStream(clientSocket.getOutputStream());


            while (true) {
                this.send();
                this.get();
               // собственно эти потоки создаются только для того, чтобы постоянно получать сообщения
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void get() {
        try {
            //System.out.println("waiting");
            String msg = dis.readUTF();
            System.out.println("Server: "+msg);

        } catch (Exception x) {
           // x.printStackTrace();
        }
    }

    public void send() {
        try {
            System.out.print("You>");
            Scanner in = new Scanner(System.in);
            String mes = in.nextLine();
            String cmd="";
            if (mes.endsWith("\\n")) cmd = mes.substring(0, mes.length() - 2);
            if (mes.endsWith("\\r\\n")) cmd = mes.substring(0, mes.length() - 4);

            switch (cmd){

                case "download":{
                    System.out.print("Input file name:");
                    String fileName = in.nextLine();
                    dos.writeUTF("download\\n");
                    dos.writeUTF(fileName);
                    FileTransmitter.RequestTCPDowload(dis,dos,fileName,0);
                    break;
                }
                case "upload":{
                    System.out.print("Input file name:");
                    String fileName = in.nextLine();
                    dos.writeUTF("upload\\n");
                    dos.writeUTF(fileName);
                    FileTransmitter.RequestTCPUpload(dis,dos,fileName,0);
                    dos.writeUTF("checna kruto");
                    dos.flush();
                    break;
                }
                default: {
                    dos.writeUTF(mes);
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }


}

