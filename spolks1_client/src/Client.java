import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import static java.lang.Math.toIntExact;

public class Client {

    protected Socket clientSocket = null;
    protected BufferedOutputStream bos;
    protected BufferedInputStream bis;
    protected DataOutputStream dos = null;//выходной
    protected DataInputStream dis = null;//входной поток
    protected FileOutputStream fos;
    protected FileInputStream fis;
    protected int windows_size = 65535;
    protected int multiplier = 4;


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
            String msg = dis.readUTF();
            System.out.println("Server:"+msg);

        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void send() {
        try {
            System.out.print("You>");
            Scanner in = new Scanner(System.in);
            String mes = in.nextLine();
            if(mes.equals("upload\\n")||mes.equals("upload\\r\\n"))
            {
                UploadFile();
            }
            if(mes.equals("download\\n")||mes.equals("download\\r\\n"))
            {
                RequestDownloadFile();
            }
            else dos.writeUTF(mes);

        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void UploadFile()
    {
        System.out.print("Input file name:");
        Scanner in = new Scanner(System.in);
        String mes = in.nextLine();
        try {
            File file = new File(mes);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(clientSocket.getOutputStream());
            long file_size = file.length(); // размер файла в байтах
            long count_windows = calclulateNumOfWindows(file_size); // количество передаваемых пакетов
            long count_buf = (int)Math.ceil((double)file_size/(windows_size*multiplier));// количество обращений к файлу

            dos.writeUTF("upload\\n");//собщаем серверу о загрузке файла
            dos.writeUTF(mes);//передаем имя файла
            dos.writeUTF(Long.toString(file_size));//размер файла
            byte[] buf = new byte[windows_size*multiplier];

            for(int k=0;k<count_buf;k++){
                bis.read(buf, 0, windows_size*multiplier);

                 for (int i = 0; i < multiplier; i++) {
                      bos.write(buf, i*windows_size, windows_size);
                      bos.flush();
                 }
                System.out.println(countTransmissionProgress(file_size, k * windows_size*multiplier));
            }
            closeFileBufStreams();

        } catch (Exception x) {
            x.printStackTrace();
        }

    }
    public void RequestDownloadFile()
    {
        System.out.print("Input file name:");
        Scanner in = new Scanner(System.in);
        String mes = in.nextLine();
        try {
            dos.writeUTF("download\\n");//запрос на скачивание
            dos.writeUTF(mes);//передаем имя файла
            String serverAns = dis.readUTF();
            if (serverAns.contains("true")) {
                Long file_size = dis.readLong();
                bis= new BufferedInputStream(clientSocket.getInputStream());
                downloadFile(mes,file_size);
            }
            else {
                System.out.println("File not found");
                ErrorNotification.fileNotFound();
            }

        }catch (Exception x) {
            x.printStackTrace();
        }

    }

    public void downloadFile(String path,long file_size)
    {
        System.out.println(file_size);
        System.out.println("downloadin file");
        File file=new File(path);
        long count_windows=calclulateNumOfWindows(file_size);
        long count_buf = (int)Math.ceil((double)file_size/(windows_size*multiplier));;
        byte[] buf = new byte[windows_size*multiplier];
        if (bis!=null){
            try {
                fos=new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                for(int k=0; k<count_buf;k++) {
                    if (isConnectionAlive()) {
                        for (int i = 0; i < multiplier; i++) {
                            bis.read(buf, i * windows_size, windows_size);
                            bos.flush();
                        }
                        bos.write(buf, 0, windows_size * multiplier);
                        System.out.println(countTransmissionProgress(file_size, k * windows_size * multiplier));
                    }
                    else System.out.println("pizdec");
                }
                System.out.println("downloading finished");
                dos.writeUTF("THANKS");
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
    public boolean isConnectionAlive() {
        String ans="";
        try {
            dos.writeUTF("Alive?");
            ans= dis.readUTF();
        } catch (Exception x) {
            x.printStackTrace();
        }
            if(ans.equals("Alive!"))return true;
            else return false;
    }
}