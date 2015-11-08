package com.javapapers.java.gcm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.AbstractDocument.Content;

import org.json.simple.JSONObject;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;

@WebServlet("/GCMNotification")
public class GCMNotification extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Put your Google API Server Key here
	private static final String GOOGLE_SERVER_KEY = "AIzaSyDzITUnrcjkv7yTKSErLd6MbfOasbgKjNw";
	static final String REGISTER_NAME = "name";
	static final String MESSAGE_KEY = "message";
	static final String TO_NAME = "toName";
	static final String REG_ID_STORE = "GCMRegId.txt";

	public GCMNotification() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("hii");
		String action = request.getParameter("action");

		if ("shareRegId".equalsIgnoreCase(action)) {

			writeToFile(request.getParameter("name"),
					request.getParameter("regId"));
			request.setAttribute("pushStatus",
					"GCM Name and corresponding RegId Received.");
			request.getRequestDispatcher("index.jsp")
					.forward(request, response);

		} else if ("sendMessage".equalsIgnoreCase(action)) {

			try {
				System.out.println("got call");
				String fromName = request.getParameter(REGISTER_NAME);
				String toName = request.getParameter(TO_NAME);
				String userMessage = request.getParameter(MESSAGE_KEY);

				Message message = new Message.Builder().timeToLive(30)
						.delayWhileIdle(true).addData(MESSAGE_KEY, userMessage)
						.addData(REGISTER_NAME, fromName).build();
				Map<String, String> regIdMap = readFromFile();
				String regId = regIdMap.get(toName);
				/*
				 * Sender sender = new Sender(GOOGLE_SERVER_KEY); Result result
				 * = sender.send(message, regId, 1);
				 * request.setAttribute("pushStatus", result.toString());
				 */

				URL url = new URL("https://android.googleapis.com/gcm/send");
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Authorization", "key="
						+ GOOGLE_SERVER_KEY);
				conn.setRequestProperty("priority","high");
				conn.setDoOutput(true);

				String payload = "{ \"to\": \"" + regId
						+ "\",\"data\" : {\"hello\":\"hello\"}}";
				System.out.println(payload);
				/*
				 * Gson gson = new Gson(); Type type = new TypeToken<Data>()
				 * {}.getType(); String json = gson.toJson(payload, type);
				 * System.out.println(json); DataOutputStream wr = new
				 * DataOutputStream(conn.getOutputStream());
				 * wr.write(json.getBytes("UTF-8")); wr.flush(); wr.close();
				 */

				// DataOutputStream wr = new
				// DataOutputStream(conn.getOutputStream());
				OutputStreamWriter writer = new OutputStreamWriter(
						conn.getOutputStream(), "UTF-8");
				writer.write(payload);
				writer.close();

				int responseCode = conn.getResponseCode();
				System.out.println("\nSending 'POST' request to URL : " + url);
				System.out.println("Response Code : " + responseCode);
				// ObjectMapper mapper = new ObjectMapper();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				String inputLine;
				StringBuffer res = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					res.append(inputLine);
				}
				in.close();

				// 7. Print result
				System.out.println(res.toString());

			} catch (IOException ioe) {
				ioe.printStackTrace();
				request.setAttribute("pushStatus",
						"RegId required: " + ioe.toString());
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("pushStatus", e.toString());
			}
			request.getRequestDispatcher("index.jsp")
					.forward(request, response);
		} else if ("multicast".equalsIgnoreCase(action)) {

			try {
				String fromName = request.getParameter(REGISTER_NAME);
				String userMessage = request.getParameter(MESSAGE_KEY);
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				Message message = new Message.Builder().timeToLive(30)
						.delayWhileIdle(true).addData(MESSAGE_KEY, userMessage)
						.addData(REGISTER_NAME, fromName).build();
				Map<String, String> regIdMap = readFromFile();

				List<String> regIdList = new ArrayList<String>();

				for (Entry<String, String> entry : regIdMap.entrySet()) {
					regIdList.add(entry.getValue());
				}

				MulticastResult multiResult = sender
						.send(message, regIdList, 1);
				request.setAttribute("pushStatus", multiResult.toString());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				request.setAttribute("pushStatus",
						"RegId required: " + ioe.toString());
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("pushStatus", e.toString());
			}
			request.getRequestDispatcher("index.jsp")
					.forward(request, response);
		}
	}

	private void writeToFile(String name, String regId) throws IOException {

		File file = new File(REG_ID_STORE);

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		Map<String, String> regIdMap = readFromFile();
		regIdMap.put(name, regId);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				REG_ID_STORE, false)));
		for (Map.Entry<String, String> entry : regIdMap.entrySet()) {
			out.println(entry.getKey() + "," + entry.getValue());
		}
		out.println(name + "," + regId);
		out.close();

	}

	private Map<String, String> readFromFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(REG_ID_STORE));
		String regIdLine = "";
		Map<String, String> regIdMap = new HashMap<String, String>();
		while ((regIdLine = br.readLine()) != null) {
			String[] regArr = regIdLine.split(",");
			regIdMap.put(regArr[0], regArr[1]);
		}
		br.close();
		return regIdMap;
	}
}
