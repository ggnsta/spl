import java.io.*;
import java.nio.channels.FileChannel;

public class FileTransmitter {
    protected static final int defaultPacketSize = 65507;
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
    public static int requestDownload(DataInputStream from, DataOutputStream to, int startAt, String fileName, Long fileSize)
    {
        int i = 0;
        try{
            File file = new File (fileName);
            int packetSize= calculatePacketSize(fileSize);
            int numOfPacket = calculateNumOfPacket(fileSize);
            byte[] fileByteArray = new byte[packetSize];
            FileOutputStream fos = new FileOutputStream(file,true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            long startTime = System.currentTimeMillis();

            for( i=startAt; i <numOfPacket;i++)
            {
                if(startAt!=0)
                {
                    FileChannel fc = null;
                    fc=fos.getChannel();
                    fc.position(startAt*defaultPacketSize);
                }


                if (!waitForDownload(from)) {
                    return i;
                    //     break;
                }
                if (i==numOfPacket-1)
                {
                    packetSize=adaptPacketSize(numOfPacket, fileSize);
                }
                from.read(fileByteArray,0,packetSize);
                to.writeInt(i);
                bos.write(fileByteArray,0,packetSize);
                bos.flush();
                System.out.println("D Send: " + (100 * i / numOfPacket) + "%" + i);

            }
            bos.close();
            fos.close();
            calclulateSpeed(startTime,fileSize);


        }catch (Exception x){

            return i;
        }
        return -1;
    }
    public static int requestUpload (DataInputStream from, DataOutputStream to, int startAt, String fileName)
    {
        int i = 0;
        try{
            File file = new File(fileName);
            if (!fileName.trim().isEmpty() && file.exists()) {
                int packetSize = calculatePacketSize(file.length());
                int numOfPacket= calculateNumOfPacket(file.length());
                byte[]fileByteArray = new byte[(int)packetSize];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                if(startAt!=0)
                {
                    bis.skipNBytes(startAt*defaultPacketSize);
                }

                for( i=startAt; i <numOfPacket;i++)
                {
                    if (i==numOfPacket-1)
                    {
                        packetSize=adaptPacketSize(numOfPacket, file.length());
                    }
                    bis.read(fileByteArray,0,packetSize);
                    to.write(fileByteArray);
                    to.flush();
                    System.out.println("u Send: " + (100 * i / numOfPacket) + "%" + i);
                    if (!waitForUpload(from,i)) {
                        return i;
                        //  break;
                    }

                }
                bis.close();
            }

        }catch (Exception x){
            return i;
        }
        return -1;
    }
    private static int adaptPacketSize(int NumOfPacket,Long fileSize)
    {
        int newByteArraySize = (int) (fileSize - (defaultPacketSize*(NumOfPacket-1)));
        return newByteArraySize;
    }

    private static boolean waitForDownload(DataInputStream fromClient) {
        try {
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            while (fromClient.available() < defaultPacketSize) {
                endTime = System.currentTimeMillis();
                if (endTime - startTime >= TIME_TO_WAIT_IN_MILLIS) {
                    System.out.println("OUT OF TIME WAITING (" + Thread.currentThread().getId() + ")");
                    return false;
                }
            }
        }catch (Exception ex)
        {
            System.out.println("Обрыв соединения");
            return false;
        }
        return true;
    }
    private static boolean waitForUpload(DataInputStream fromClient,int i)  {
        try {
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            while (fromClient.readInt() != i) {
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
    public static void calclulateSpeed(long startTime, long fileSize)
    {
        long endTime = System.currentTimeMillis();
        System.out.println("downloaded in" + (endTime-startTime) + " ms");
        long fileSizeInMb = fileSize/1000000;
        long timeInSec = (endTime-startTime)/1000;
        if(timeInSec<1)timeInSec=1;
        System.out.println((fileSizeInMb/timeInSec)+"Mb/s");
    }

}
