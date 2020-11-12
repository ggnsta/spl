import java.io.*;
import java.net.Socket;
import java.net.InetAddress;

public class ClientDescriptor {

    protected Socket clientSocket = null;
    protected DataOutputStream dos = null;//выходной
    protected DataInputStream dis = null;//входной поток

    protected String ClientIP="";
    protected boolean status;



    public ClientDescriptor(Socket clientSocket) {
        try {
                this.status=true;
                this.clientSocket = clientSocket;
                this.ClientIP=clientSocket.getInetAddress().toString();
                this.run();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void run(){
        try{
            dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.flush();
            System.out.println("DataOutputStream  created");
            dis=new DataInputStream(clientSocket.getInputStream());
            System.out.println("DataInputStream created");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    ////метод отправки сообщений
    public void send_message(String message) {
        try {
            dos.writeUTF(message);//пишем в поток
            dos.flush();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    ////метод отправки сообщений
    public String get_message( ) {
        String message="";
        try {
           message= dis.readUTF();
        } catch (Exception x) {
            x.printStackTrace();
            x.getMessage();
        }
        return message;
    }

    public void start_download_file(String fileName)  {
        FileTransmitter.RequestTCPDowload(dis,dos,fileName,0);

    }
    public void start_upload_file (String fileName)
    {
        FileTransmitter.RequestTCPUpload(dis,dos,fileName,0);
    }

    public void closeStreams()
    {
        try {
            this.dos.close();
            this.dis.close();
           // this.fis.close();
          //  this.fos.close();
          //  this.bos.close();
           // this.bis.close();
        } catch (Exception x) {
            x.printStackTrace();
        }

    }



}
