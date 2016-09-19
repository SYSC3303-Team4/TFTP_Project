/**
*Class:             Client.java
*Project:           Assignment1
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    13/09/2016                                              
*Version:           1.0.0                                                      
*                                                                                    
*Purpose:           Generates a datagram following the format of [0,R/W,STR1,0,STR2,0],
					in which R/W signifies read (1) or write (2), STR1 is a filename,
					and STR2 is the mode. Sends this datagram to the IntermediateHost
					and waits for response from intermediateHost. Repeats this
					process ten times, then sends a datagram packet that DOES NOT
					follow the expected format stated above. Waits for response from
					IntermediateHost. We DO NOT expect a response to the badly formated
					packet.
* 
* 
*Update Log:        v1.0.0
*                       - null
*/


//import external libraries
import java.io.*;
import java.net.*;


public class Client 
{
	//declaring local instance variables
	private DatagramPacket sentPacket;
	private DatagramPacket recievedPacket;
	private DatagramSocket generalSocket;
	
	//declaring local class constants
	private static final int PORT = 23;
	private static final int MAX_SIZE = 100;
	
	
	//generic constructor
	public Client()
	{
		//construct a socket, bind to any local port
		try
		{
			generalSocket = new DatagramSocket();
		}
		//enter if socket creation results in failure
		catch (SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	
	//generic accessors and mutators
	public DatagramPacket getSentPacket()
	{
		return sentPacket;
	}
	public DatagramPacket getRecievedPacket()
	{
		return recievedPacket;
	}
	public DatagramSocket getGeneralSocket()
	{
		return generalSocket;
	}
	public void setSentPacket(DatagramPacket dp)
	{
		sentPacket = dp;
	}
	public void setRecievedPacket(DatagramPacket dp)
	{
		recievedPacket = dp;
	}
	public void setGeneralSocket(DatagramSocket gs)
	{
		generalSocket = gs;
	}
	
	
	//generate DatagramPacket, save as sentPacket 
	public void generateDatagram(String fileName, String mode, byte RWval)
	{
		//generate the data to be sent in datagram packet
		System.out.println("Client: Prepping packet containing '" + fileName + "'...");
			
		//convert various strings to Byte arrays
		byte[] fileNameBA = fileName.getBytes();
		byte[] modeBA = mode.getBytes();
			
		//compute length of data being sent (metadata include) and create byte array
		byte[] data = new byte[fileNameBA.length + modeBA.length + 4];
		int i = 2;
			
		//add first 2 bytes of metadata
		data[0] = 0x00;
		data[1] = RWval;
		//add text
		for(int c=0; c<fileNameBA.length; c++, i++)
		{
			data[i] = fileNameBA[c];
		}
		//add pesky 0x00
		data[i] = 0x00;
		i++;
		//add mode
		for(int c=0; c<modeBA.length; c++, i++)
		{
			data[i] = modeBA[c];
		}
		//add end metadata
		data[i] = 0x00;
			
		
		//generate and return datagram packet
		try
		{
			sentPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), PORT);
			System.out.println("Client: Packet successfully created");
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	//send and echo the datagram
	public void sendAndEcho()
	{
		//print packet info
		byte[] data = sentPacket.getData();
		int packetSize = sentPacket.getLength();
		System.out.println("Client: Sending packet...");
		System.out.println("        Host:  " + sentPacket.getAddress());
		System.out.println("        Port:  " + sentPacket.getPort());
		System.out.println("        Bytes: " + sentPacket.getLength());
		System.out.println("        Cntn:  " + (new String(data,0,packetSize)));
		System.out.printf("%s", "        Cntn:  ");
		for(int i = 0; i < packetSize; i++)
		{
			System.out.printf("0x%02X", data[i]);
			System.out.printf("%-2c", ' ');
		}
				
		//send packet
		try
		{
			generalSocket.send(sentPacket);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("\nClient: Packet Sent");
	}
	
	
	//receive and echo received packet
	public void receiveAndEcho()
	{
		//prep for response
		byte[] response = new byte[MAX_SIZE];
		recievedPacket = new DatagramPacket(response, response.length);
				
		//wait for response
		System.out.println("Client: Waiting for response...");
		try
		{
			generalSocket.receive(recievedPacket);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		//Process the response
		byte[] data = recievedPacket.getData();
		int packetSize = recievedPacket.getLength();
		System.out.println("Client: Packet received");
		System.out.println("        Source: " + recievedPacket.getAddress());
		System.out.println("        Port:   " + recievedPacket.getPort());
		System.out.println("        Bytes:  " + packetSize);
		System.out.println("        Cntn:  " + (new String(data,0,packetSize)));
		System.out.printf("%s", "        Cntn:  ");
		for(int i = 0; i < packetSize; i++)
		{
			System.out.printf("0x%02X", data[i]);
			System.out.printf("%-2c", ' ');
		}
	}
	
	
	public static void main (String[] args) 
	{
		//declaring local variables
		Client client = new Client();
		byte flipFlop = 0x01;
		
		for(int i=0; i<10; i++)
		{
			//generate datagram
			client.generateDatagram("DatagramsOutForHarambe.txt","octet", flipFlop);
		
			//send and echo outgoing datagram
			client.sendAndEcho();
		
			//idle until packet is received, echo and and save
			client.receiveAndEcho();
			
			//flip R/W byte
			if (flipFlop == 0x01)
			{
				flipFlop = 0x02;
			}
			else
			{
				flipFlop = 0x01;
			}
			
			System.out.println("\n----------------------------------------\n");
		}
		
		//generate and send bad datagram
		client.generateDatagram("gArBaGe.trash","trascii", (byte)0x05);
		client.sendAndEcho();
		client.receiveAndEcho();
	}
}
