import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import static java.lang.Math.toIntExact;
public class Client {
    protected Socket clientSocket = null;
    protected DataOutputStream dos = null;//выходной
    protected DataInputStream dis = null;//входной поток
    protected String address;
    protected int port;
    protected int startAt=0;
    protected int lastOperation = 0;
    protected long fileSize=0;
    protected String fileName = "";

    public Client(String address, int port) {
        try {
            clientSocket = new Socket(address, port);
            this.port=port;
            this.address=address;
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
                    this.fileName = in.nextLine();
                    this.fileSize = cmd_checkFileExsist(fileName);
                    this.startAt = 0;
                    if(fileSize!=-1){
                        if(cmd_upload()) break;
                        else {
                            reconnect();
                            askContinueDOwnloading();
                        }
                    }
                    else break;
                }
                case "UDP MODE ON":{
                    dos.writeUTF("1"+" UDP MODE ON\\n");
                    UDPconnection udp = new UDPconnection(this.port, InetAddress.getByName(address));
                    udp.run();
                    break;
                }
                default: {
                    dos.writeUTF(" "+mes);
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
    private boolean cmd_upload() throws IOException {
        int answer;
        dos.writeUTF(this.fileName+ " upload\\n");
        dos.writeLong(this.fileSize);
        dos.writeInt(startAt);
        answer= FileTransmitter.requestUpload(dis,dos,this.startAt,this.fileName);
        if (answer==-1) {
            return true;
        }
        else{
            this.startAt=answer;
            this.lastOperation = -1;
            return false;
        }
    }
    public static long cmd_checkFileExsist(String filename)
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
    private void reconnect() throws IOException, InterruptedException {
        System.out.println("Reconnectuing...");
        clientSocket = new Socket(this.address, this.port);
        dis = new DataInputStream(clientSocket.getInputStream());
        dos = new DataOutputStream(clientSocket.getOutputStream());
    }
    private void askContinueDOwnloading() throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Do you want to continue downloading?(y/n)");
        String ans = in.nextLine();
        if(ans.equals("y")||ans.equals("yes"))
        {
            System.out.println("check");
            if (lastOperation==-1)
            {
                cmd_upload();
            }
        }
    }
}

