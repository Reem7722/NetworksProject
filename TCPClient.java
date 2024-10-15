import java.io.BufferedReader;
  import java.io.DataOutputStream;
  import java.io.IOException;
  import java.io.InputStreamReader;
  import java.net.*;
  import java.util.Random;
  


  public class TCPClient {

    private static int ReqCount =0; 
    private static int errorReqCount= 0; // Tracks how many requests should have errors



  public static void main(String[] args) throws IOException {
  
  String serverIP = "172.20.10.5"; // IP of the server 
  int port= 1423; // Server port
  
  Socket CSocket = null;
  

  //--------------------------------------------------------------------------------



  // Try to establish a connection to the server
  try {
  
  CSocket = new Socket (serverIP, port); // Connect to the server
  
  } catch (IOException e)
  {
    System.out.println("Server is down, please try later."); // Print error if connection fails 
    return; // Exit if the server is not available
  }
  
  

  //--------------------------------------------------------------------------------



  // Create input/output streams for communication with the server
  BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); // To get user input
  DataOutputStream outToServer= new DataOutputStream (CSocket.getOutputStream()); // To send messages to the server
  BufferedReader inFromServer= new BufferedReader(new InputStreamReader(CSocket.getInputStream())); // To receive
  


  //--------------------------------------------------------------------------------



  // Run a loop to continuously read from user and send messages to the server 
  while (true) {
  
  System.out.print("Enter a message: "); 
  String message = inFromUser.readLine(); // Read user input
  
  //Message validation
  
  if (message.isEmpty()) { // If the message is empty
  
  System.out.println("Error: Empty message is not valid."); // Print error and skip sending continue; // Go to the next iteration
  
  }
  //Check for Quit command
  
  if (message.equalsIgnoreCase ("Quit")) { // If the user types "Quit"
  
  outToServer.writeBytes (message + "\n"); // Send the "Quit" message to the server
  
  break; // Exit the loop to terminate the program
  }



  //--------------------------------------------------------------------------------



  // Generate checksum for the message 
    String checksum = calculateChecksum (message);
  
    message =simulateError(message,0.3);
  
      // Send message with checksum to the server (Format: message:checksum)
      outToServer.writeBytes (message + "/" + checksum + "\n");
  
   // Read server response
  
      String SResponse = inFromServer.readLine(); // Receive response from the server
  
      System.out.println("Server: " + SResponse); // Print server response
  
  
     }// end while 
  CSocket.close();
  }//end main 
  


  //--------------------------------------------------------------------------------



  //method to calculate 16-bit one's complement checksum
  
  private static String calculateChecksum (String message) { 
    int checksum = 0;
  
  //Loop through the message 16-bits (2 characters) at a time 
    for (int i = 0; i < message.length(); i += 2) {
  
      int word = 0;
  
  //Get the first character (8 bits) 
      word = message.charAt(i) << 8; // Shift the first character 8 bits to the left (upper byte)
  
  //Get the second character if it exists
  
  if (i + 1 < message.length()) {
  
  word += message.charAt(i + 1); // Add the second character (lower byte)
  
  }



  //--------------------------------------------------------------------------------
  //Add the 16-bit word to the checksum
  
  checksum += word; //sh
  
  //Handle carry (wrap around) if the sum exceeds 16 bits 
  if ((checksum & 0xF0000) > 0) { // If there's a carry beyond 16 bits 
    checksum &= 0xFFFF; // Keep only the lower 16 bits 
    checksum++; // Add the carry
  
  }
    }
  
  checksum = checksum & 0xFFFF;
  
  return Integer.toHexString(checksum).toUpperCase();
  
    }
    
  
    
  //--------------------------------------------------------------------------------



  //Helper method to simulate errors in the message
  private static String simulateError (String message, double errorProbability) {
  
  //Increment total request count
  
  ReqCount++;
  
  //Calculate how many requests should have errors based on probability
  int expectedErrors = (int) (ReqCount* errorProbability);
  
  if (errorReqCount < expectedErrors) { // If the number of error requests is less than expected
    errorReqCount++;
    return introduceRandomError(message);
  
  }
  
  //Introduce error into the message
  //If the number of error requests has reached the expected amount, return the original message
  return message;
  
  }
  


  //--------------------------------------------------------------------------------



  private static String introduceRandomError (String message) {
  
  Random Ran = new Random();
  
  StringBuilder corruptedMessage = new StringBuilder (message);
  
  //Introduce an error at a random position in the message
  
  int errorPosition = Ran.nextInt (message.length());
  
  corruptedMessage.setCharAt(errorPosition, (char) (Ran.nextInt (26) + 'a')); // Random lowercase letter
  
  return corruptedMessage.toString();
  
  }
  
  }//end class