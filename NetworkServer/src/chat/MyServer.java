package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import chat.handler.*;
import chat.auth.*;

public class MyServer {
	private final ServerSocket serverSocket;
	private final AuthService authService;
	private final List<ClientHandler> clients = new ArrayList<>();

	//принимаем порт из мэйна
	public MyServer(int port) throws IOException {
		//создание сервер сокета
		this.serverSocket = new ServerSocket(port);
		//создание авторизации
		this.authService = new BaseAuthService();
	}

	public void start() throws IOException {
		System.out.println("Сервер запущен!");
		try {
			while (true) {
				//сервер запущен и ожидает новых пользователей (постоянно)
				waitAndProcessNewClientConnection();
			}
		} catch (IOException e){
			System.out.println("Ошибка создания нового подключения");
			e.printStackTrace();
		} finally {
			serverSocket.close();
		}

	}

	private void waitAndProcessNewClientConnection() throws IOException {
		System.out.println("Ожидание пользователя...");

		//польз-ль подключился, мы получили его clientSocket
		Socket clientSocket = serverSocket.accept();
		System.out.println("Клиент подключился!");

		//создать подключение (свой client handler) для нового пользователя (передаем нового польз-ля)
		processClientConnection(clientSocket);
	}

	private void processClientConnection(Socket clientSocket) throws IOException {
		//как только новый пользователь подключился мы реагируем новым handler
		//для работы ему нужно знать клиентский сокет, с которым он будет работать,
		//и текущий объект сервера (MyServer), чтобы он мог работать и в обратную сторону
		ClientHandler clientHandler = new ClientHandler(this, clientSocket);
		//метод, отвечающий за все действия с текущим польз-ем
		clientHandler.handle();
	}

	public AuthService getAuthService() {
		return authService;
	}

	public boolean isUsernameBusy(String clientUsername) {
		for (ClientHandler client : clients) {
			//если пришедший клиент равен тому, который лежит в списке на сервере, то true
			if(client.getClientUsername().equals(clientUsername)){
				return true;
			}
		}
		return false;
	}

	public void	subscribe(ClientHandler clientHandler){
		clients.add(clientHandler);
	}
	public void unsubscribe(ClientHandler clientHandler){
		clients.remove(clientHandler);
	}

	public void broadcastMessage(String s, ClientHandler sender) throws IOException {
		for (ClientHandler client : clients) {
			//чтобы не отпраавлять сообщение о новом польз-ле самому новому польз-лю
			if(client == sender){
				continue;
			}
			//отправляем всем сообщение о новом пользователе
			client.sendMessage(s, sender.getClientUsername());
		}
	}
}
