/**
*Class:             Server.java
*Project:           Assignment1
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    15/09/2016                                              
*Version:           1.0.0                                                      
*                                                                                    
*Purpose:           Receives packet from IntermediateHost, proceeds to check
					packet for validity (ie proper format, proper R/W byte).
					If valid, generates and sends a packet to the IntermediateHost.
					Note that the contents of the packet depend on if the R/W byte in
					the received packet is set to read or write. If non-valid, the
					server shuts down. Loops this process indefinitely.
* 
* 
*Update Log:        v1.0.0
*                       - null
*/

//import stuff
import java.io.*;
import java.net.*;


public class Server 
{

	//declaring local instance variables
	DatagramPacket sentPacket;
	DatagramPacket receivedPacket;
	DatagramSocket inSocket;
	DatagramSocket outSocket;
	
	//declaring local class constants
	private static final int IN_PORT = 69;
	private static final int MAX_SIZE = 100;
	
	
	//generic constructor
	public Server()
	{
		//construct socket for incoming client communication, bind to IN_PORT
		try
		{
			inSocket = new DatagramSocket(IN_PORT);
		}
		//enter if socket creation results in failure
		catch (SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	
	//receive and echo
	public void receiveAndEcho()
	{
		//construct an empty datagram packet for receiving purposes
		byte[] arrayholder = new byte[MAX_SIZE];
		receivedPacket = new DatagramPacket(arrayholder, arrayholder.length);
				
		//wait for incoming data
		System.out.println("Server: Waiting for data...");
		try
		{
			inSocket.receive(receivedPacket);
		}
		catch (IOException e)
		{
			System.out.print("Incoming socket timed out\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		//print
		byte[] data = receivedPacket.getData();
		int packetSize = receivedPacket.getLength();
		System.out.println("Server: Packet received");
		System.out.println("        Source: " + receivedPacket.getAddress());
		System.out.println("        Port:   " + receivedPacket.getPort());
		System.out.println("        Bytes:  " + receivedPacket.getLength());
		System.out.println("        Cntn:  " + (new String(data,0,packetSize)));
		System.out.printf("%s", "        Cntn:  ");
		for(int i = 0; i < packetSize; i++)
		{
			System.out.printf("0x%02X", data[i]);
			System.out.printf("%-2c", ' ');
		}
		
		//check data for validity
		int i = 2;
		int txtLength = 0;
		try
		{
			System.out.println("\nServer: Parsing data...");
			if (data[0] == 0x00 && (data[1] == 0x01 || data[1] == 0x02))
			{
				System.out.println("        0x00 (1) OK") ;
				System.out.println("        R/W byte OK") ;
			}
			else
			{
				//error
				System.out.println("        BAD R/W BYTE / METADATA") ;
				throw new Exception("BAD PACKET");
			}
			while(data[i] != 0x00 && i < packetSize)
			{
				txtLength++;
				i++;
			}
			if(txtLength >= 1 && i < packetSize)
			{
				System.out.println("        Filename  OK");
				System.out.println("        0x00 (2) OK");
				txtLength = 0;
				i++;
			}
			else
			{
				//error
				System.out.println("        BAD FILENAME");
				throw new Exception("BAD PACKET");
			}
			while(data[i] != 0x00 && i < packetSize)
			{
				txtLength++;
				i++;
			}
			if(txtLength >= 1 && i < packetSize)
			{
				System.out.println("        Mode  OK");
				System.out.println("        0x00 (3) OK");
			}
			else
			{
				//error
				System.out.println("        BAD MODE/TERMINATIOR");
				throw new Exception("BAD PACKET");
			}
		}
		catch(Exception e)
		{
			System.out.println("Server: BAD PACKET - EXITING SERVER");
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server: Packet Acceptable");
	}
	
	
	//send and echo using procedurally generated socket
	//delete socket after use
	public void sendAndEcho()
	{
		//prep packet to send
		System.out.println("Server: Sending packet...");
		byte[] inData = receivedPacket.getData();
		byte[] outData = new byte[4];
		int port = receivedPacket.getPort();
		//read
		if (inData[1] == 0x01)
		{
			outData[0] = 0;
			outData[1] = 3;
			outData[2] = 0;
			outData[3] = 1;
		}
		//write
		else if (inData[1] == 0x02)
		{
			outData[0] = 0;
			outData[1] = 4;
			outData[2] = 0;
			outData[3] = 0;
		}
		
		//print contents
		try
		{
			sentPacket = new DatagramPacket(outData, outData.length, InetAddress.getLocalHost(), port);
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		int packetSize = outData.length;
		System.out.println("        Source: " + sentPacket.getAddress());
		System.out.println("        Port:   " + sentPacket.getPort());
		System.out.println("        Bytes:  " + packetSize);
		System.out.println("        Cntn:  " + (new String(outData,0,packetSize)));
		System.out.printf("%s", "        Cntn:  ");
		for(int i = 0; i < packetSize; i++)
		{
			System.out.printf("0x%02X", outData[i]);
			System.out.printf("%-2c", ' ');
		}
		
		//create temp socket, random port
		try
		{
			outSocket = new DatagramSocket();
		}
		catch(SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		//send packet
		try
		{
			outSocket.send(sentPacket);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("\nServer: Packet successfully sent");	
		//close socket
		outSocket.close();
	}
	
	
	public static void main(String[] args) 
	{
		//create instance of server
		Server server = new Server();
		
		while(true)// work
		{
			//idle until data is received from host
			//receive and save and echo
			server.receiveAndEcho();
		
			//send packet to host
			server.sendAndEcho();
			System.out.println("----------------------------------------\n");
		}
	}

}
