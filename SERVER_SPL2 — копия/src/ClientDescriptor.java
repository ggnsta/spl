import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Date;

public class ClientDescriptor {

    protected Socket clientSocket = null;
    protected DataOutputStream dos = null;//выходной
    protected DataInputStream dis = null;//входной поток
    protected String ClientIP = "";
    protected boolean status;
    protected Server server=null;



    public ClientDescriptor(Socket clientSocket, Server server) {
        try {
            this.server=server;
            this.status = true;
            this.clientSocket = clientSocket;
            this.ClientIP = clientSocket.getInetAddress().toString();
            this.run();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void run() {
        try {
            dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.flush();
            System.out.println("DataOutputStream  created");
            dis = new DataInputStream(clientSocket.getInputStream());
            System.out.println("DataInputStream created");
        } catch (IOException e) {
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
    public String get_message() {
        String message = "";
        try {
            message = dis.readUTF();
        } catch (Exception x) {
            x.printStackTrace();
            x.getMessage();
        }
        return message;
    }

    public void chechExecCmd(String message) throws IOException {
        String mes = " ";
        if (message.endsWith("\\n") || message.endsWith("\\r\\n")) {
            if (message.endsWith("\\n")) mes = message.substring(0, message.length() - 2);
            if (message.endsWith("\\r\\n")) mes = message.substring(0, message.length() - 4);

            String []cmd= mes.split(" ",2); // cmd[0] filesize, cmd[1]- команда, только в случае загрузки/выгрузки/проверки существования файла

            switch (cmd[1]) {
                case "isFileExsist?": {
                    Long fileSize = cmd_checkFileExsist(cmd[0]);
                    dos.writeLong(fileSize);
                    break;
                }
                case "download": {
                    //int startAt = dis.readInt();
                    cmd_accept_download(cmd[0], 0);
                    break;
                }
                case "upload": {
                    Long fileSize = dis.readLong();
                    int startAt = dis.readInt();
                    cmd_accept_upload(cmd[0],fileSize,startAt);
                    System.out.println(dis.available());
                    while(dis.available()>0)dis.skipBytes(1);
                    dos.writeUTF("Server finished uploading");
                    break;
                }
                case "time":{
                    cmd_time();
                    break;
                }
                case "echo":{
                    cmd_echo();
                    break;
                }
                case "stop":{
                    cmd_stop();
                    break;
                }
                case "UDP MODE ON":
                {
                    server.UDPMODE=true;
                    server.workInUDPMode();
                    break;
                }
                default: {
                    System.out.println("Client:" + message);
                    send_message(" ");
                    break;
                }
            }
        } else{
            System.out.println("Client:" + message);
            send_message(" ");
        }
    }

    private void cmd_accept_upload(String fileName, Long fileSize,int startAt) {
        FileTransmitter.requestDownload(dis,dos,startAt,fileName,(fileSize));

    }

    private long cmd_checkFileExsist(String filename) {
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
    private void cmd_accept_download(String fileName,int startAt)  {

        System.out.println("cmd_accept_download");
        FileTransmitter.requestUpload(dis,dos,startAt,fileName);


        // client_descriptor.send_message("File was successfully downloaded");
    }
    private void closeStream () throws IOException {
        dos.close();
        dis.close();
    }
    private void cmd_time()
    {
        Date date = new Date();
        send_message(date.toString());

    }
    private void cmd_echo()
    {
        send_message("Your echo:");
        String buf = get_message();
        buf.trim();
        send_message(buf);
    }
    private void cmd_stop() {
        try {
            send_message("Server closed connection.");
            closeStream();
            this.clientSocket.close();
            System.out.println("stop command");
        } catch (Exception e) {
            throw new RuntimeException("SERVER:Error closing server", e);
        }
    }

}