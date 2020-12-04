package client.models;

import client.controllers.ChatController;
import clientserver.Command;
import clientserver.commands.*;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Network {

	private Socket socket;

	//возвращается от сервера, нетворк сохраняет себе и дальше переиспользует
	private String username;

	private static final String SERVER_ADRESS = "localhost";
	private static final int SERVER_PORT = 8189;

	private final String host;
	private final int port;

	private ObjectOutputStream dataOutputStream;
	private ObjectInputStream dataInputStream;

	public Network()
	{
		this(SERVER_ADRESS, SERVER_PORT); // пустой конструктор с перегрузкой
	} 									// т.е вызываем дефолтный конструктор с константными значениями

	public Network(String host, int port) { // обязательный конструктор т.к у нас два поля private final, которые
		this.host = host;					// нужно обязательно принять на вход
		this.port = port;
	}

	//getterы чтобы получать сообщение за пределами контроллера
	public ObjectInputStream getDataInputStream() {
		return dataInputStream;
	}
	public ObjectOutputStream getDataOutputStream() {
		return dataOutputStream;
	}

	public boolean connect(){
		try {
			socket = new Socket(host, port);
			dataInputStream = new ObjectInputStream(socket.getInputStream());
			dataOutputStream = new ObjectOutputStream(socket.getOutputStream());
			return true;
		} catch (IOException e) {
			System.out.println("Соединение не было установлено");
			e.printStackTrace();
			return false;
		}
	}

	// общий метод закрытия соединения
	public void close(){
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void waitMessage(ChatController chatController) {
		Thread thread = new Thread( () -> {
			try { while (true) {
				Command command = readCommand();
				if(command == null) {
					chatController.showError("Ошибка серверва", "Получена неверная команда");
					continue;
				}
				switch (command.getType()) {
					case INFO_MESSAGE: {
						MessageInfoCommandData data = (MessageInfoCommandData) command.getData();
						String message = data.getMessage();
						String sender = data.getSender();
						String formattedMessage = sender != null ? String.format("%s: %s", sender, message) : message;
						Platform.runLater(() -> {
							chatController.appendMessage(formattedMessage);
						});
						break;
					}
					case ERROR: {
						ErrorCommandData data = (ErrorCommandData) command.getData();
						String errorMessage = data.getErrorMessage();
						Platform.runLater(() -> {
							chatController.showError("Server error", errorMessage);
						});
						break;
					}
					case UPDATE_USERS_LIST: {
						UpdateUsersListCommandData data = (UpdateUsersListCommandData) command.getData();
						Platform.runLater(() -> chatController.updateUsers(data.getUsers()));
						break;
					}
					default:
						Platform.runLater(() -> {
							chatController.showError("Unknown command from server!", command.getType().toString());
						});
				}
			}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Соединение потеряно!");
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public String sendAuthCommand(String login, String password) {
		try {
			Command authCommand = Command.authCommand(login, password);
			dataOutputStream.writeObject(authCommand);

			Command command = readCommand();
			if (command == null) {
				return "Ошибка чтения команды с сервера";
			}

			switch (command.getType()) {
				case AUTH_OK: {
					AuthOkCommandData data = (AuthOkCommandData) command.getData();
					this.username = data.getUsername();
					return null;
				}

				case AUTH_ERROR:
				case ERROR: {
					AuthErrorCommandData data = (AuthErrorCommandData) command.getData();
					return data.getErrorMessage();
				}
				default:
					return "Unknown type of command: " + command.getType();

			}
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public String getUsername() {
		return username;
	}

	public void sendMessage(String message) throws IOException {
		sendMessage(Command.publicMessageCommand(username, message));
	}

	public void sendMessage(Command command) throws IOException {
		dataOutputStream.writeObject(command);
	}

	public void sendPrivateMessage(String message, String recipient) throws IOException {
//        String command = String.format("%s %s %s",PRIVATE_MSG_CMD_PREFIX, recipient, message);
		Command command = Command.privateMessageCommand(recipient, message);
		sendMessage(command);
	}

	private Command readCommand() throws IOException {
		try {
			return (Command) dataInputStream.readObject();
		} catch (ClassNotFoundException e) {
			String errorMessage = "Получен неизвестный объект";
			System.err.println(errorMessage);
			e.printStackTrace();
			sendMessage(Command.errorCommand(errorMessage));
			return null;
		}
	}

}
