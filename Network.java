package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {  //отдельный класс для socket клиента
	private Socket socket;
	private static final String SERVER_ADRESS = "localhost";
	private static final int SERVER_PORT = 8189;

	private final String host;
	private final int port;

	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;

	public Network()
	{
		this(SERVER_ADRESS, SERVER_PORT); // пустой конструктор с перегрузкой
	} 									// т.е вызываем дефолтный конструктор с константными значениями

	public Network(String host, int port) { // обязательный конструктор т.к у нас два поля private final, которые
		this.host = host;					// нужно обязательно принять на вход
		this.port = port;
	}

	//getterы чтобы получать сообщение за пределами контроллера
	public DataInputStream getDataInputStream() {
		return dataInputStream;
	}
	public DataOutputStream getDataOutputStream() {
		return dataOutputStream;
	}

	public boolean connect(){
		try {
			socket = new Socket("localhost", 8189);
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
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

	public void waitMessage(Controller controller) {
		Thread thread = new Thread(() -> {
			try {
				while (true) {
					String message = dataInputStream.readUTF();
					controller.appendMessage(message);
				}

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Соединение потеряно!");
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
