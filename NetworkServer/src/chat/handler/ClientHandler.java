package chat.handler;

import chat.MyServer;
import chat.auth.AuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

	private static final String AUTH_CMD_PREFIX = "/auth";
	private static final String AUTHOK_CMD_PREFIX = "/authok";
	private static final String AUTHOERR_CMD_PREFIX = "/autherr";

	private final MyServer myServer;
	private final Socket clientSocket;
	private DataInputStream io;
	private DataOutputStream out;
	private String clientUsername;

	public ClientHandler(MyServer myServer, Socket clientSocket) {
		this.myServer = myServer;
		this.clientSocket = clientSocket;
	}

	public void handle() throws IOException {
		//из сокета клиента достаем потоки
		io = new DataInputStream(clientSocket.getInputStream());
		out = new DataOutputStream(clientSocket.getOutputStream());

		//каждому польз-лю создается свой поток для связи с сервером
		//процесс авторизации и процесс чтения всех сообщений
		new Thread(() -> {
			try {
				//авторизация польз-ля (происходит один раз, но внутри много попыток подключиться)
				authentication();
				//как только авторизация прошла, то подвисаем на ожидании сообщений
				readMessage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void authentication() throws IOException {
		//считываем что передает объект
		String message = io.readUTF();
		//бесконечное количество раз просим польз-ля ввести корректные данные
		while(true){
			//проверка - начинается ли сообщение с указанной строки
			// если да - делим строку на части (3 штуки) по пробелам
			if (message.startsWith(AUTH_CMD_PREFIX)){
				String[] parts = message.split("\\s+", 3);
				String login = parts[1];
				String password = parts[2];
				//обращение к сервису авторизации
				//сравнить пароли и логины с тем, что есть у нас в списке BaseAuthService
				AuthService authService = myServer.getAuthService();
				this.clientUsername = authService.getUsernameByLoginAndPassword(login, password);
				//если польз-ль есть в базе, то вернется String с именем польз-ля
				if(clientUsername != null){
					//проверка на уже имеющееся подключение на сервере
					if(myServer.isUsernameBusy(clientUsername)){
						out.writeUTF(AUTHOERR_CMD_PREFIX + " Логин уже используется");
					}
					//если успешно и пользователь не подключался ранее
					out.writeUTF(AUTHOK_CMD_PREFIX + " " + clientUsername);
					//уведомить всех польз-ей, об успешном подключении нового польз-ля
					myServer.broadcastMessage(clientUsername + " присоединился к чату", this);
					//подписываем нового польз-ля на получение всех сообщений
					myServer.subscribe(this);
					break;
				}
				//если польз-ля нет в базе
				else{
					out.writeUTF(AUTHOERR_CMD_PREFIX + "Логин или пароль не соответствует действительности");
				}
			}
			else{
				out.writeUTF(AUTHOERR_CMD_PREFIX + " Ошибка авторизации");
			}
		}
	}

	private void readMessage() throws IOException {
		while (true){
			String message = io.readUTF();
			System.out.println("message | " + clientUsername + ": " + message);
			if(message.startsWith("/end")){
				return;
			}
			//уведомить всех польз-ей, что кто-то отправил сообщение
			myServer.broadcastMessage(message, this);
		}
	}

	public String getClientUsername() {
		return clientUsername;
	}

	public void sendMessage(String s, String senderName) throws IOException {
		out.writeUTF(clientUsername + ": " + s);
	}
}
