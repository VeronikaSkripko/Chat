import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {  //отдельный класс для socket клиента

	private static final String AUTH_CMD_PREFIX = "/auth";
	private static final String AUTHOK_CMD_PREFIX = "/authok";
	private static final String AUTHOERR_CMD_PREFIX = "/autherr";

	private Socket socket;

	//возвращается от сервера, нетворк сохраняет себе и дальше переиспользует
	private String username;

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
			socket = new Socket(host, port);
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
			try { while (true) {
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

	public String sendAuthCommand(String login, String password) {
		try {
			dataOutputStream.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));

			//от clientHalder придет ответ в виде сообщения об ошибке или AuthOk + имя пользователя
			//читаем ответ
			String response = dataInputStream.readUTF();
			//если сообщение начинается с Окей
			if(response.startsWith(AUTHOK_CMD_PREFIX)){
				//то делим ответ на 2 части по пробелам и сохраняем первое значение в username
				this.username = response.split("\\s+", 2)[1];
				//null перехватывается в AuthDIalogController
				return null;
			}
			else {
				//если пришло сообщение об ошибке, то его и возвращаем
				return response.split("\\s+", 2)[1];
			}
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public String getUsername() {
		return username;
	}
}
