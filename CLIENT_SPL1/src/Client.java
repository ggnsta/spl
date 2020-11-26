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
            System.out.println("waiting");
            String msg = dis.readUTF();
            System.out.println("Server:"+msg);

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
                    dos.writeUTF(fileName+" isFileExsist?\\n");
                    Long ans = dis.readLong();
                    if (ans!=-1){
                        dos.writeUTF(fileName+" download\\n");
                        FileTransmitter.requestDownload(dis,dos,0,fileName,ans);
                        while(dis.available()>0)dis.skipBytes(1);
                        send();
                        break;
                    }
                    else System.out.println("No such file");
                    send();
                    break;
                }
                case "upload":{
                    System.out.print("Input file name:");
                    String fileName = in.nextLine();
                    Long fileSize = cmd_checkFileExsist(fileName);
                    if(fileSize!=-1){
                        dos.writeUTF(fileName+ " upload\\n");
                        dos.writeLong(fileSize);
                        FileTransmitter.requestUpload(dis,dos,0,fileName);
                        System.out.println("uploaded finished");
                        break;
                    }
                    else break;

                }
                default: {
                    dos.writeUTF(" "+mes);
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private long cmd_checkFileExsist(String filename)
    {
        File file = new File(filename);
        if (!filename.trim().isEmpty() && file.exists())
        {
            System.out.println("file est");
            return file.length();
        }
        else{
            System.out.println("no such file");
            return -1;
        }
    }
}
