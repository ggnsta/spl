import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Input address and port (ip:port):");
        String address = in.nextLine();
        int del = address.indexOf(":");
        int port=Integer.parseInt(address.substring(del+1));
        address=address.substring(0,del);
        Client client = new Client(address,port);
    }
}


