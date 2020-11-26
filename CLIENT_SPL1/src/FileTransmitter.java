import java.io.*;

public class FileTransmitter {
    protected static final int defaultPacketSize = 8192;
    private static final Integer TIME_TO_WAIT_IN_MILLIS =100; //16384 8192;

    public static int calculatePacketSize(Long FileSize)//высчитавает размер массива,который будет передаватсья за раз
    {
        int packetSize = (int) Math.min(FileSize, defaultPacketSize);
        return packetSize;
    }

    public static int calculateNumOfPacket(Long FileSize) {
        int numOfPacket = ((int) Math.ceil((double) FileSize / calculatePacketSize(FileSize)));
        return numOfPacket;

    }
    public static void requestDownload(DataInputStream from, DataOutputStream to, int startAt, String fileName, Long fileSize)
    {
        try{
            File file = new File (fileName);
            int packetSize= calculatePacketSize(fileSize);
            int numOfPacket = calculateNumOfPacket(fileSize);
            byte[] fileByteArray = new byte[packetSize];

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            long startTime = System.currentTimeMillis();
            for(int i=startAt; i <numOfPacket;i++)
            {
                if (!waitForBytes(from)) {
                    break;
                }
                if (i==numOfPacket-1)
                {
                    packetSize=adaptPacketSize(numOfPacket, fileSize);
                }
                from.read(fileByteArray,0,packetSize);
                bos.write(fileByteArray,0,packetSize);
                bos.flush();
                System.out.println("D Send: " + (100 * i / numOfPacket) + "%" + i);
                to.writeInt(i);
            }
            long endTime = System.currentTimeMillis();
            System.out.println(endTime-startTime + " ms");
            System.out.println((fileSize/1000000)/(((endTime-startTime))/1000)+"Mb/s");

        }catch (Exception x){
            x.printStackTrace();
        }

    }
    public static void requestUpload (DataInputStream from, DataOutputStream to, int startAt, String fileName)
    {
        try{
            File file = new File(fileName);
            if (!fileName.trim().isEmpty() && file.exists()) {
                int packetSize = calculatePacketSize(file.length());
                int numOfPacket= calculateNumOfPacket(file.length());
                byte[]fileByteArray = new byte[(int)packetSize];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

                for(int i=startAt; i <numOfPacket;i++)
                {
                    if (i==numOfPacket-1)
                    {
                        packetSize=adaptPacketSize(numOfPacket, file.length());
                    }
                    bis.read(fileByteArray,0,packetSize);
                    to.write(fileByteArray);
                    to.flush();
                    System.out.println("u Send: " + (100 * i / numOfPacket) + "%" + i);
                    if (!waitForBytes(from)) {
                        break;
                    }

                    int ans = from.readInt();
                    if (ans==i)
                    {
                        System.out.println("ok"+ans);
                    }
                    else i=-1;

                }

            }

        }catch (Exception x){
            x.printStackTrace();
        }

    }
    private static int adaptPacketSize(int NumOfPacket,Long fileSize)
    {
        int newByteArraySize = (int) (fileSize - (defaultPacketSize*(NumOfPacket-1)));
        return newByteArraySize;
    }
    private static boolean waitForBytes(DataInputStream fromClient) throws IOException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        while (fromClient.available() <= 0) {
            endTime = System.currentTimeMillis();
            if (endTime - startTime >= TIME_TO_WAIT_IN_MILLIS) {
                System.out.println("OUT OF TIME WAITING (" + Thread.currentThread().getId() + ")");
                return false;
            }
        }
        return true;
    }

}
