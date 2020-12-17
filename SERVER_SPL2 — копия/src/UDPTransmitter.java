import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class UDPTransmitter {

    protected static final int defaultPacketSize = 65507;//65507;
    private static final Integer TIME_TO_WAIT_IN_MILLIS =2000; //16384;
   

    public static int calculatePacketSize(Long FileSize)//высчитавает размер массива,который будет передаватсья за раз
    {
        int packetSize = (int) Math.min(FileSize, defaultPacketSize);
        return packetSize;
    }

    public static int calculateNumOfPacket(Long FileSize) {
        int numOfPacket = ((int) Math.ceil((double) FileSize / calculatePacketSize(FileSize)));
        return numOfPacket;

    }

    public static int requestDowload (DatagramSocket socket, InetAddress inetAddress, int port,int startAt, String fileName, long fileSize)
    {
        int i = 0;
        try {
            socket.setSoTimeout(TIME_TO_WAIT_IN_MILLIS);
            File file = new File(fileName);
            int packetSize = calculatePacketSize(fileSize);
            int numOfPacket = calculateNumOfPacket(fileSize);
            byte[][]fileByteArray = new byte[window_size][(int)packetSize+8];
            FileOutputStream fos = new FileOutputStream(file,true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            long startTime = System.currentTimeMillis();

            for (i=startAt; i<numOfPacket;i++)
            {
                byte[] receivingDataBuffer = new byte[packetSize];
                DatagramPacket receivingPacket = new DatagramPacket(receivingDataBuffer,receivingDataBuffer.length);
                socket.receive(receivingPacket);
                sendMessage(i,socket,inetAddress,port);
                bos.write(receivingDataBuffer,0,packetSize);
                System.out.println("D Send: " + (100 * i / numOfPacket) + "%" + i);
            }
            bos.close();
            fos.close();
            FileTransmitter.calclulateSpeed(startTime,fileSize);
        }catch (Exception ex){
            return i;
            //ex.printStackTrace();
        }
        return -1;//
    }


    public static int requestUpload (DatagramSocket socket, InetAddress inetAddress, int port,int startAt, String fileName)
    {
        int i = 0;
        try{
            File file = new File(fileName);
            socket.setSoTimeout(TIME_TO_WAIT_IN_MILLIS);
            if (!fileName.trim().isEmpty() && file.exists()) {
                int packetSize = calculatePacketSize(file.length());
                int numOfPacket= calculateNumOfPacket(file.length());
                byte[]fileByteArray = new byte[(int)packetSize];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                if(startAt!=0)
                {
                    bis.skipNBytes(startAt*defaultPacketSize);
                }

                for (i=startAt;i<numOfPacket;i++)
                {

                    bis.read(fileByteArray, 0, packetSize);//
                    createAndSendPacket(fileByteArray,socket,inetAddress,port);
                    System.out.println("u Send: " + (100 * i / numOfPacket) + "%" + i);
                    if (!waitForUpload(socket,i)){
                        System.out.println("oi oi oi oi");
                        return i;
                    }

                }
                bis.close();
                System.out.println("finish");
            }
        }catch (Exception x){
            return i;
        }
        return -1;
    }

    private  static void createAndSendPacket(byte[] fileByteArray,DatagramSocket socket, InetAddress inetAddress, int port) {
        try {
            DatagramPacket packet = new DatagramPacket(fileByteArray, fileByteArray.length, inetAddress, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  static void sendMessage(int msg,DatagramSocket socket, InetAddress inetAddress, int port) {
        try {
            byte[] fileByteArray= ByteBuffer.allocate(4).putInt(msg).array();
            DatagramPacket packet = new DatagramPacket(fileByteArray, fileByteArray.length, inetAddress, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  static int getMessage(DatagramSocket socket) {
        try {
            byte[] fileByteArray= new byte[4];
            DatagramPacket packet = new DatagramPacket(fileByteArray, fileByteArray.length);
            socket.receive(packet);
            int num = ByteBuffer.wrap(fileByteArray).getInt();
            return num;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static boolean waitForUpload(DatagramSocket socket, int i)  {
        try {
            long startTime = System.currentTimeMillis();
            long endTime = startTime;


            while (getMessage(socket)!=i) {
                endTime = System.currentTimeMillis();
                if (endTime - startTime >= TIME_TO_WAIT_IN_MILLIS) {
                    System.out.println("OUT OF TIME WAITING (" + Thread.currentThread().getId() + ")");
                    return false;
                }
            }
        }catch (Exception ex){
            System.out.println("Обрыв соединения");
            return false;
        }
        return true;
    }

}
