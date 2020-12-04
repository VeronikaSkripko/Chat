package chat.handler;

import chat.MyServer;
import chat.auth.AuthService;
import clientserver.Command;
import clientserver.CommandType;
import clientserver.commands.AuthCommandData;
import clientserver.commands.PrivateMessageCommandData;
import clientserver.commands.PublicMessageCommandData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler {

	private final MyServer myServer;
	private final Socket clientSocket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String clientUsername;

	public ClientHandler(MyServer myServer, Socket clientSocket) {
		this.myServer = myServer;
		this.clientSocket = clientSocket;
		//clientSocket.setSoTimeout(120000);
//        new Timer().schedule();
	}

	public void handle() throws IOException {
		//из сокета клиента достаем потоки
		in = new ObjectInputStream(clientSocket.getInputStream());
		out = new ObjectOutputStream(clientSocket.getOutputStream());

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
		while (true) {
			Command command = readCommand();
			if (command == null) {
				continue;
//                Set set = new TreeSet();
			}
			if (command.getType() == CommandType.AUTH) {
				boolean isSuccessAuth = processAuthCommand(command);
				if (isSuccessAuth) {
					break;
				}
			} else {
				sendMessage(Command.authErrorCommand("Ошибка авторизации"));
			}
		}
	}

	private boolean processAuthCommand(Command command) throws IOException {
		AuthCommandData cmdData = (AuthCommandData) command.getData();
		String login = cmdData.getLogin();
		String password = cmdData.getPassword();

		AuthService authService = myServer.getAuthService();
		this.clientUsername = authService.getUsernameByLoginAndPassword(login, password);
		if (clientUsername != null) {
			if (myServer.isUsernameBusy(clientUsername)) {
				sendMessage(Command.authErrorCommand("Логин уже используется"));
				return false;
			}

			sendMessage(Command.authOkCommand(clientUsername));
			String message = String.format(">>> %s присоединился к чату", clientUsername);
			myServer.broadcastMessage(this, Command.messageInfoCommand(message, null));
			myServer.subscribe(this);
			return true;
		} else {
			sendMessage(Command.authErrorCommand("Логин или пароль не соответствуют действительности"));
			return false;
		}
	}

	private Command readCommand() throws IOException {
		try {
			return (Command) in.readObject();
		} catch (ClassNotFoundException e) {
			String errorMessage = "Получен неизвестный объект";
			System.err.println(errorMessage);
			e.printStackTrace();
			return null;
		}
	}

	private void readMessage() throws IOException {
		while (true) {
			Command command = readCommand();
			if (command == null) {
				continue;
			}
			switch (command.getType()) {
				case END:
					return;
				case PUBLIC_MESSAGE: {
					PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
					String message = data.getMessage();
					String sender = data.getSender();
					myServer.broadcastMessage(this, Command.messageInfoCommand(message, sender));
					break;
				}
				case PRIVATE_MESSAGE:
					PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
					String recipient = data.getReceiver();
					String message = data.getMessage();
					myServer.sendPrivateMessage(recipient, Command.messageInfoCommand(message, recipient));
					break;
				default:
					String errorMessage = "Неизвестный тип команды" + command.getType();
					System.err.println(errorMessage);
					sendMessage(Command.errorCommand(errorMessage));
			}
		}
	}

	public String getClientUsername() {
		return clientUsername;
	}

	public void sendMessage(Command command) throws IOException {
		out.writeObject(command);
	}
}
