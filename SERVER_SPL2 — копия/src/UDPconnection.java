import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

public class UDPconnection {


    protected int bufSize = 16384;
    protected boolean UDPStatus = false;
    protected DatagramSocket socket=null;
    protected  InetAddress senderAddress;
    protected int senderPort;

    public UDPconnection(DatagramSocket socket)
    {
        this.socket=socket;
        this.UDPStatus = true;
    }

    public String get ()
    {
        try {
            byte[] receivingDataBuffer = new byte[ bufSize];
            DatagramPacket inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
            socket.receive(inputPacket);
            senderAddress=inputPacket.getAddress();
            senderPort = inputPacket.getPort();
            String msg = new String(receivingDataBuffer);

            return  msg;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    public void  send(String msg)
    {
        byte[] sendingDataBuffer = new byte[ bufSize];
        sendingDataBuffer=msg.getBytes();
        DatagramPacket outputPacket = new DatagramPacket(
                sendingDataBuffer, sendingDataBuffer.length,
                senderAddress,senderPort
        );
        try {
            socket.send(outputPacket);
            //zakrut socket gde-nibud
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public String cutOfNull (String msg)
    {
        msg=msg.substring(0,msg.indexOf(0));
        return msg;
    }

    public void chechExecCmd(String message) throws IOException {
        String mes = " ";
        if (message.startsWith("\\n") ||message.startsWith("\\r\\n")){
            if (message.startsWith("\\n")) mes = message.substring(2, message.indexOf(0));
            if (message.startsWith("\\r\\n")) mes = message.substring(4, message.length() - 4);

            String []cmd= mes.split(" ",2); // cmd[0] filesize, cmd[1]- команда, только в случае загрузки/выгрузки/проверки существования файла

            switch (cmd[1]) {
                case "download": {
                    String fileName = cutOfNull(get());
                    UDPTransmitter.requestUpload(socket,senderAddress,senderPort,0,fileName);
                    send("Server finished downloading");
                    break;
                }
                case "upload": {
                    String fs =get();
                    fs = fs.substring(0, fs.indexOf(0));// срезаем лишние null
                    Long fileSize = Long.parseLong(fs);

                    String fn = get();
                    fn = fn.substring(0,fn.indexOf(0));
                    String fileName = fn;
                //    int startAt = dis.readInt();
                    cmd_accept_upload(fileName,fileSize,0);
                    send("server finished uploading");
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
                case "TCP MODE ON":
                {
                    closeUDP();
                    break;
                }
                case "isFileExsist?": {
                    Long fileSize = cmd_checkFileExsist(cutOfNull(get()));
                    send(fileSize.toString());
                    break;
                }
                default: {
                    System.out.println("Client:" + message);
                    send("Unknown command");
                    break;
                }
            }
        } else{
            System.out.println("Client [UDP]:" + message);
            send("...");
        }
    }

    private void cmd_accept_upload(String fileName, Long fileSize,int startAt) {
        UDPTransmitter.requestDowload(socket,senderAddress,senderPort,startAt,fileName,(fileSize));

    }

    private long cmd_checkFileExsist(String filename) {
        File file = new File(filename);
        if (!filename.trim().isEmpty() && file.exists())
        {
            System.out.println("file is exist");
            return file.length();
        }
        else{
            System.out.println("no such file");
            return -1;
        }
    }

    private void cmd_time()
    {
        Date date = new Date();
        send(date.toString());

    }
    private void cmd_echo()
    {
        send("Your echo:");
        String buf = get();
        buf.trim();
        send(buf);
    }
    private void cmd_stop() {
        try {
            send("Server closed connection.");
            socket.close();
            System.out.println("stop command");
        } catch (Exception e) {
            throw new RuntimeException("SERVER:Error closing server", e);
        }
    }
    protected void closeUDP()
    {
        this.UDPStatus=false;
        this.socket.close();
    }
}

