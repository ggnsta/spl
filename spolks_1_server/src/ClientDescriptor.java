import java.io.*;
import java.net.Socket;
import java.net.InetAddress;

public class ClientDescriptor {

    protected Socket clientSocket = null;
    protected DataOutputStream dos = null;//выходной
    protected DataInputStream dis = null;//входной поток

    protected BufferedInputStream bis = null;
    protected BufferedOutputStream bos =null;

    protected FileInputStream fis = null;
    protected FileOutputStream fos =null;


    protected int windows_size = 65535;
    protected String ClientIP="";
    protected boolean status;
    protected int multiplier = 4;


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
            confSocket();
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
        }
        return message;
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

    public void acceptDownload(String fileName)
    {
        try {
        File file = new File(fileName);
        boolean isExist = file.exists();
        if (isExist==true) {
            this.send_message("true");
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(clientSocket.getOutputStream());
            dos.writeLong(file.length());
            uploadFile(file);
        }
        else this.send_message("false");

        } catch (Exception x) {
            x.printStackTrace();
        }
    }
    public void uploadFile(File file)
    {
        long file_size = file.length(); // размер файла в байтах
        long count_windows = calclulateNumOfWindows(file_size); // количество передаваемых пакетов
        long count_buf = (int)Math.ceil((double)file_size/(windows_size*multiplier));// количество обращений к файлу
        try {
        byte[] buf = new byte[windows_size*multiplier];


        for(int k=0;k<count_buf;k++){
            bis.read(buf, 0, windows_size*multiplier);
          //  if(isConnectionAlive()) {
                for (int i = 0; i < multiplier; i++) {
                    bos.write(buf, i * windows_size, windows_size);
                    bos.flush();
                }
                System.out.println(countTransmissionProgress(file_size, k * windows_size * multiplier));
           // }
          //  else System.out.println("pizdec");
        }
        closeFileBufStreams();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void acceptUpload()
    {
        try {
        bis= new BufferedInputStream(clientSocket.getInputStream());
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void downloadFile(String path, long file_size)
    {
        File file=new File(path);
        long count_windows=calclulateNumOfWindows(file_size);
        long count_buf = (int)Math.ceil((double)file_size/(windows_size*multiplier));;
        byte[] buf = new byte[windows_size*multiplier];
        //дописать если файл меньше размера буфера, обрезать файл нахуй.
        if (bis!=null){
            try {
                fos=new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                for(int k=0; k<count_buf;k++) {
                    for (int i = 0; i < multiplier; i++) {
                        bis.read(buf, i * windows_size, windows_size);
                        bos.flush();
                    }
                    bos.write(buf,0,windows_size*multiplier);
                    System.out.println(countTransmissionProgress(file_size, k * windows_size*multiplier));
                }
                closeFileBufStreams();
            } catch (Exception x) {
                x.printStackTrace();
            }
        }

    }

    public int countTransmissionProgress(long filesize, long transmitedSize){
        int percent = (int)((100 * transmitedSize)/filesize);
        return percent;
    }
    public int calclulateNumOfWindows(long file_size)
    {
        if (file_size<windows_size)return 1;
        else return ((int)Math.ceil((double)file_size/windows_size));
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    public void closeFileBufStreams()
    {
        try {
            if (fis!=null)this.fis=null;
            if (fos!=null)this.fos=null;
            if (bis!=null)this.bis=null;
            if (bos!=null)this.bos=null;
        } catch (Exception x) {
            x.printStackTrace();
        }

    }

    public void confSocket() {
        try {
         clientSocket.setKeepAlive(true);
        } catch (Exception x) {
        x.printStackTrace(); }
    }

    public boolean isConnectionAlive() {
        String ans="";
        try {
            ans=dis.readUTF();
            if (ans.equals("Alive?"))dos.writeUTF("Alive!");
            return true;
        } catch (Exception x) {
            x.printStackTrace();
        }
      return false;
    }
}
