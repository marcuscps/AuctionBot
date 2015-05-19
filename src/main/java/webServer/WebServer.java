package webServer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class WebServer extends Thread {

	private int port; //port we are going to listen to
	private volatile boolean shutdownFlag;
	private ServerSocket serverSocket = null;
	
	public WebServer(int listen_port) {
		port = listen_port;
		shutdownFlag = false;
		this.start();
	}

	public void shutdown() {
		shutdownFlag = true;
		if (serverSocket != null) {
			try {
				System.out.println("[WebServer]: " + "Closing socket...");
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[WebServer]: " + "Joining thread...");
		try { this.join(); } catch (InterruptedException e) { e.printStackTrace(); }
		System.out.println("[WebServer]: " + "DONE!");
	}
	
	private void s(String s2) {
		//an alias to avoid typing so much!
//		message_to.send_message_to_window(s2);
		System.out.println("[WebServer]: " + s2);
	}

	public void run() {
		s("HTTP server starting localy on port " + Integer.toString(port) + " ...");
		try (
				ServerSocket serversocket = new ServerSocket(port);
		) {
			serverSocket = serversocket;
			s("Ready, Waiting for requests...");
			while (shutdownFlag == false) {
				acceptConnection();
			}
		} catch (Exception e) {
			s("Fatal Error:" + e.getMessage());
		}
	}

	private void acceptConnection() {
		try (
			Socket connectionsocket = serverSocket.accept();
			BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
			DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
		) {
			InetAddress client = connectionsocket.getInetAddress();
			s(client.getHostName() + " connected to server.");
			handleRequest(input, output);
		} catch (SocketException e) {
			if (shutdownFlag != true) {
				s("Fatal Error:" + e.getMessage());
			}
		} catch (Exception e) {
			s("Error:" + e.getMessage());
		}
	}

	private void handleRequest(BufferedReader input, DataOutputStream output) {
		int method = 0; // 1 get, 2 head, 0 not supported
//		String http = ""; // a bunch of strings to hold
		String path = ""; // the various things, what http v, what path,
//		String file = ""; // what file
//		String userAgent = ""; // what user_agent
		
		Map<String, String> getParams = null;
		
		try {
			// This is the two types of request we can handle
			// GET /index.html HTTP/1.0
			// HEAD /index.html HTTP/1.0
			String tmp = input.readLine();
			String tmp2 = new String(tmp);
			tmp.toUpperCase();

			s("Request:" + tmp2);
			
			if (tmp.startsWith("GET"))  { method = 1; }
			if (tmp.startsWith("HEAD")) { method = 2; }

			// Not supported
			if (method == 0) {
				try {
					output.writeBytes(construct_http_header(501, 0));
					output.close();
					return;
				}
				catch (Exception e3) {
					s("error:" + e3.getMessage());
				}
			}

			// tmp contains "GET /index.html HTTP/1.0 ......."
			// find first space
			// find next space
			// copy whats between minus slash, then you get "index.html"
			int start = tmp2.indexOf(' ');
			if (start == -1) {
				s("error: " + "Could not filter path (start).");
			} else {
				int end = -1;
				int params = tmp2.indexOf('&', start + 1);
				if (params == -1) {
					end = tmp2.indexOf(' ', start + 1);
				} else {
					end = tmp2.indexOf(' ', params + 1);
				}

				if (end == -1) {
					s("error: " + "Could not filter path (end).");
				} else if (params != -1) {
					path = tmp2.substring(start + 2, params);
					getParams = queryToMap(tmp2.substring(params + 1, end));
				} else {
					path = tmp2.substring(start + 2, end);
				}
			}
		} catch (Exception e) {
			s("error: " + e.getMessage());
		}

		s("Client requested: \"" + path + "\"");
		if (path.equals("update.html")) {
			String parsed = "";
			for (Entry<String, String> entry: getParams.entrySet()) {
				parsed += entry.getKey() + " = " + entry.getValue() + ", ";
			}
			
			s("Update received: " + parsed);
			// write out the header, 200 ->everything is ok we are all happy.
			try {
				output.writeBytes(construct_http_header(200, 10));
			} catch (IOException e) {
				s("error: " + e.getMessage());
			}
			return;
		}
		
		//path do now have the filename to what to the file it wants to open
		s("Client requested:" + new File(path).getAbsolutePath());

		// NOTE that there are several security consideration when passing the untrusted string "path" to FileInputStream.
		// You can access all files the current user has read access to!!!
		// current user is the user running the javaprogram.
		// you can do this by passing "../" in the url or specify absoulute path or change drive (win)
		
		try (FileInputStream requestedfile = new FileInputStream(path)) {
			int type_is = 0;
			// find out what the filename ends with, so you can construct a the right content type
			if (path.endsWith(".zip")) { type_is = 3; }
			if (path.endsWith(".jpg") || path.endsWith(".jpeg")) { type_is = 1; }
			if (path.endsWith(".gif")) { type_is = 2; }

			// write out the header, 200 ->everything is ok we are all happy.
			output.writeBytes(construct_http_header(200, type_is));

			// If it was a HEAD request, we don't print any BODY (1 is GET, 2 is head and skips the body)
			if (method == 1) {
				while (true) {
					int b = requestedfile.read();
					if (b == -1) {
						break;
					}
					output.write(b);
				}
			}
		} catch (FileNotFoundException e) {
			s("error: " + e.getMessage());
			try {
				output.writeBytes(construct_http_header(404, 0));
			} catch (Exception e2) {
			}
		} catch (IOException e) {
			s("error: " + e.getMessage());
			try {
				output.writeBytes(construct_http_header(404, 0));
			} catch (Exception e2) {
			}
		}
	}

	private Map<String, String> queryToMap(String query){
	    Map<String, String> result = new HashMap<String, String>();
	    for (String param : query.split("&")) {
	        String pair[] = param.split("=");
	        if (pair.length>1) {
	            result.put(pair[0], pair[1]);
	        }else{
	            result.put(pair[0], "");
	        }
	    }
	    return result;
	}
	
	// This method makes the HTTP header for the response the headers job is to tell the browser the result
	// of the request among if it was successful or not.
	private String construct_http_header(int return_code, int file_type) {
		StringBuilder s = new StringBuilder("HTTP/1.0 ");

		//you probably have seen these if you have been surfing the web a while
		switch (return_code) {
		case 200: s.append("200 OK");						break;
		case 400: s.append("400 Bad Request");				break;
		case 403: s.append("403 Forbidden");				break;
		case 404: s.append("404 Not Found");				break;
		case 500: s.append("500 Internal Server Error");	break;
		case 501: s.append("501 Not Implemented");			break;
		}

		// Other header fields,
		s.append("\r\n");
		// Can't handle persistent connections
		s.append("Connection: close\r\n");
		// Server name
		s.append("Server: Internal server v1\r\n");

		// Construct the right Content-Type for the header.
		// This is so the browser knows what to do with the file, you may know the browser dosen't look on the file
		// extension, it is the servers job to let the browser know what kind of file is being transmitted.
		// You may have experienced if the server is miss configured it may result in pictures displayed as text!
		switch (file_type) {
		case 0: break;
		case 1:	 s.append("Content-Type: image/jpeg\r\n");						break;
		case 2:	 s.append("Content-Type: image/gif\r\n");						break;
		case 3:  s.append("Content-Type: application/x-zip-compressed\r\n");	break;
		default: s.append("Content-Type: text/html\r\n");						break;
		}

		// This marks the end of the httpheader and the start of the body
		s.append("\r\n");
		// ok return our newly created header!
		return s.toString();
	}

}
