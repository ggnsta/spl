import java.io.*;

public class FileTransmitter {

    private static final int SLEEP_TIME_IN_MILLIS = 10;
    private static final int TIME_TO_WAIT_IN_MILLIS = 5000000;
    protected static final int defaultPacketSize = 65535;


    public static int calculatePacketSize(Long FileSize)//высчитавает размер массива,который будет передаватсья за раз
    {
        int packetSize = (int) Math.min(FileSize, defaultPacketSize);
        System.out.println("Real Packet Size" + packetSize);
        return packetSize;
    }

    public static int calculateNumOfPacket(Long FileSize) {
        int numOfPacket = ((int) Math.ceil((double) FileSize / calculatePacketSize(FileSize)));
        System.out.println("NUM OF PACKET" + numOfPacket);
        return numOfPacket;

    }

    public static void RequestTCPUpload(DataInputStream dis, DataOutputStream dos, String fileName, int startByte) {
        try {
            File file = new File(fileName);
            if (!fileName.trim().isEmpty() && file.exists()) {

                int packetSize = calculatePacketSize(file.length());
                byte[] fileByteArray = new byte[(int) packetSize];
                dos.writeUTF("1 " + file.length()); // отправляем всего размер файла

                int num_of_packet = calculateNumOfPacket(file.length());

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

                TCPUpload(dis, dos, fileByteArray, packetSize,bis, num_of_packet );


                // if (fileByteArray.length != current) {
                //    memoryService.saveDownloadCommandInfo(clientId, CommandName.DOWNLOAD, fileName, current, fileByteArray);
                //  }

            } else {
                dos.writeUTF("0"); // если нет файла с запрашиваемым именем
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private static void TCPUpload (DataInputStream dis, DataOutputStream dos, byte[] fileByteArray,int packetSize, BufferedInputStream bis, int num_of_packet)  {

        try{
            for (int i =0; i<num_of_packet;i++ ) {
                bis.read(fileByteArray, 0, packetSize);
                dos.write(fileByteArray);
                dos.flush();
                if (!waitForBytes(dis)) {
                    break;
                }
                dis.skipBytes(1);
                System.out.println("U Send: " + (100*i/num_of_packet) + "%" + i);
            }
        }catch (Exception x){
            x.printStackTrace();
        }
        // dos.flush();

    }

    private static boolean waitForBytes(DataInputStream fromClient) {
        try {
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            while (fromClient.available() <= 0) {
                endTime = System.currentTimeMillis();
                if (endTime - startTime >= TIME_TO_WAIT_IN_MILLIS) {
                    System.out.println("OUT OF TIME WAITING (" + Thread.currentThread().getId() + ")");
                    return false;
                }
            }
        }catch (Exception x){
            x.printStackTrace();
        }
        return true;

    }

    public static void RequestTCPDowload(DataInputStream dis, DataOutputStream dos, String filename,  int startByte)  {

        try {
            String[] answer = dis.readUTF().split(" ", 2);
            if (answer[0].equalsIgnoreCase("1")) {

                long fileSize = Long.parseLong(answer[1]);
                int packetSize = calculatePacketSize(fileSize);

                byte[] fileByteArray = new byte[(int) packetSize];
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filename)));

                TCPDowload(dis, dos, fileByteArray, packetSize, bos, calculateNumOfPacket(fileSize));

        /*if (current == fileSize) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filename)))) {
                bos.write(fileByteArray, 0, fileSize);
                bos.flush();
            }

            dos.writeUTF("The file has been gotten fully");
        } else {
            //   memoryService.saveDownloadCommandInfo(clientId, CommandName.UPLOAD, filename, current, fileByteArray);
            dos.writeUTF("The file has not been gotten fully");
        }*/
            } else {
                System.out.println("pizsa1");
                dos.writeUTF("There is no such file");
            }
        }catch (Exception x){
            x.printStackTrace();
        }
    }

    private static void TCPDowload(DataInputStream dis, DataOutputStream dos, byte[] fileByteArray,int packetSize, BufferedOutputStream bos, int num_of_packet) {
        try {
            for (int i = 0; i < num_of_packet; i++) {
                if (!waitForBytes(dis)) {
                    break;
                }
                dis.read(fileByteArray);
                bos.write(fileByteArray);
                bos.flush();
                System.out.println("D Send: " + (100 * i / num_of_packet) + "%" + i);
                dos.write(1);
            }
            System.out.println("tcp download finished");

        } catch (Exception x){
            x.printStackTrace();
        }
    }

}
