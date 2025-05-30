package edu.usfca.cs272.templates.sockets;

import java.io.BufferedReader;
import java.io.IOException;

public class SimpleClient {
	public static void main(String[] args) throws IOException {
		System.out.println("Client: Started...");
		System.out.println("Client: Ending client.");
		System.out.println("Client: Shutting down server.");
		System.out.println("Client: Client disconnected.");

		try (
				BufferedReader reader = new BufferedReader(null); // TODO
		) {
			String input = null;

			while ((input = reader.readLine()) != null) {
				// TODO
			}
		}
	}
}
