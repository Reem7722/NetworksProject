import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;


public class TCPServer {

    public static void main(String[] args) throws IOException {

        int port = 1423; // Set the port number for the server to listen on
        ServerSocket SVSocket = new ServerSocket(port); // Create a server socket that listens on the specified port
        System.out.println("Server is now running on port " + port);

        // Accept client connections (the server waits until a client connects)

        Socket CSocket = SVSocket.accept(); // The server waits for a client to establish a connection
        BufferedReader inClient = new BufferedReader(new InputStreamReader(CSocket.getInputStream())); // Create a reader to receive data from the client
        DataOutputStream outClient = new DataOutputStream(CSocket.getOutputStream()); // Create a stream to send responses back to the client

        // Enter a loop to continuously receive messages from the client

        while (true) {
            // قراءة الرسالة المرسلة من العميل
            String clientMessage = inClient.readLine(); // قراءة الرسالة الواردة من العميل
        
            // إذا أرسل العميل "Quit"، الخروج من الحلقة وإنهاء الاتصال
            if (clientMessage.equalsIgnoreCase("Quit")) { // التحقق مما إذا كان العميل قد اتصل
                System.out.println("Client has disconnected."); 
                break; // الخروج من حلقة الخادم
            }
        
            // تقسيم الرسالة والتحقق من تنسيقها (التنسيق: رسالة/تأكيد)
            String[] messageParts = clientMessage.split("/");
        
            switch (messageParts.length) {
                case 2: // إذا كان التنسيق صحيحًا
                    String Message = messageParts[0]; // استخراج الرسالة الفعلية 
                    String recChecksum = messageParts[1]; // استخراج التأكيد المستلم
                    System.out.println("Received Message: " + Message + " with checksum: " + recChecksum);
                    
                    String calculatedChecksum = calculateChecksum(Message);
                    
                    if (!calculatedChecksum.equals(recChecksum)) {
                        outClient.writeBytes("Error: The received message is incorrect\n");
                    } else {
                        outClient.writeBytes("Confirmation: The received message is correct\n");
                    }
                    break;
                    
                default: // إذا كان التنسيق غير صحيح
                    outClient.writeBytes("Error: Message format is invalid\n"); // إرسال استجابة خطأ
                    break;
            }
        } // End of loop
        CSocket.close();
        SVSocket.close();
        
    } // End of main method
    
    private static String calculateChecksum(String message) { 
        int checksum = 0;

        // Iterate through the message in 16-bit (2-character) segments
        for (int i = 0; i < message.length(); i += 2) {

            int word = 0;
            // Get the first character (8 bits)
            word = message.charAt(i) << 8; // Shift the first character 8 bits to the left (upper byte)

            // Get the second character if it exists
            if (i + 1 < message.length()) {
                word += message.charAt(i + 1); // Add the second character (lower byte)
            }

            // Add the 16-bit word to the checksum
            checksum += word; 
            // Handle carry (wrap around) if the sum exceeds 16 bits
            if ((checksum & 0xF0000) > 0) { // Check for carry beyond 16 bits 
                checksum &= 0xFFFF; // Keep only the lower 16 bits 
                checksum++; // Add the carry 
            }
        }

        checksum = checksum & 0xFFFF;
        // Return the checksum as a hexadecimal string
        return Integer.toHexString(checksum).toUpperCase();
    }   
    }// End of class

            

