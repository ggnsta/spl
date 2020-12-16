import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class UDPconnection {

   protected int port;
   protected InetAddress ip;
   protected DatagramSocket socket;
   protected int bufSize = 16384;
   protected boolean UDPStatus=false;
   protected long fileSize=0;
   protected String fileName = "";
   protected int startLoadAt;
   protected int last_op;


   public UDPconnection (int port, InetAddress ip)
   {

       this.port=port;
       this.ip = ip;
       try{
           socket= new DatagramSocket();
       }catch (Exception ex){
           ex.printStackTrace();
       }
       this.UDPStatus=true;
   }
    public void run() {
        try {
            while (UDPStatus) {
                this.send();
                if (UDPStatus==false)break;
                this.getAndPrint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAndPrint() {
        try {
            byte[] receivingDataBuffer = new byte[ bufSize];
            System.out.println("waiting");
            DatagramPacket receivingPacket = new DatagramPacket(receivingDataBuffer,receivingDataBuffer.length);
            socket.receive(receivingPacket);
            String msg = new String (receivingPacket.getData());
            System.out.println("Server [UDP]:"+msg);

        } catch (Exception x) {
             x.printStackTrace();
        }
    }
    public String get() {
        try {
            byte[] receivingDataBuffer = new byte[ bufSize];
            DatagramPacket receivingPacket = new DatagramPacket(receivingDataBuffer,receivingDataBuffer.length);
            socket.receive(receivingPacket);
            String msg = new String (receivingPacket.getData());
            return msg;

        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }
    public void send_packet (String msg)
    {
        try {
            byte[] sendingDataBuffer = new byte[ bufSize];
            sendingDataBuffer=msg.getBytes();
            DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer,sendingDataBuffer.length,this.ip, this.port);
            socket.send(sendingPacket);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void send( ) {
        System.out.print("You [UDP]>");
        Scanner in = new Scanner(System.in);
        String msg = in.nextLine();

        if(msg.endsWith("\\n")) {
            msg=msg.substring(0,msg.length()-2);
            msg= "\\n "+msg;
        }

        switch (msg)
        {
            case "\\n TCP MODE ON":{
                send_packet(msg);
                closeUDP();
                break;
            }
            case "\\n upload":{
                System.out.print("Input file name:");
                this.fileName = in.nextLine();
                this.fileSize = Client.cmd_checkFileExsist(fileName);
                if (fileSize!=-1)cmd_upload();
                break;
            }
            case "\\n download":
            {
                send_packet("\\n isFileExsist?");
                System.out.print("Input file name:");
                this.fileName = in.nextLine();
                send_packet(fileName);
                this.fileSize=Integer.parseInt(cutOfNull(get()));
                if(this.fileSize==-1){
                    System.out.println("no such file on server");
                    send();
                    break;
                }
                else{
                    cmd_download(fileName,socket,ip,port,this.fileSize,0);
                }
                break;
            }
            default:send_packet(msg);
        }

    }
    protected void cmd_download(String fileName,DatagramSocket socket, InetAddress inetAddress, int port,long fileSize,int startAt)
    {
        send_packet("\\n download");
        send_packet(fileName);
        UDPTransmitter.requestDowload(socket,inetAddress,port,startAt,fileName,fileSize);
    }

    private boolean cmd_upload()
    {
        send_packet("\\n upload");
        send_packet(Long.toString(this.fileSize));
        send_packet(fileName);
        int result=UDPTransmitter.requestUpload(socket,ip,port,0,fileName);
        if (result==-1)return true;
        else{
            this.startLoadAt=result;
            this.last_op=-1;
            return false;
        }
    }

    private void reconnect() throws IOException, InterruptedException {
        System.out.println("Reconnectuing...");

    }
    private void askContinueDOwnloading() throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Do you want to continue downloading?(y/n)");
        String ans = in.nextLine();
        if(ans.equals("y")||ans.equals("yes"))
        {
            System.out.println("check");
            if (last_op==-1)
            {
                cmd_upload();
            }
        }
    }

    protected void closeUDP()
    {
        this.UDPStatus=false;
        this.socket.close();
    }

    public String cutOfNull (String msg)
    {
        msg=msg.substring(0,msg.indexOf(0));
        return msg;
    }
}
