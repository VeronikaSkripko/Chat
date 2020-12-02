package chat.auth;

import chat.User;

import java.util.List;

public class BaseAuthService implements AuthService {

	//список всех польз-ей
	private static final List<User> clients = List.of(
			new User("user1", "1111", "Борис"),
			new User("user2", "2222", "Тимофей"),
			new User("user3", "3333", "Мартин")
	);
	//проверка на соответсвие нового польз-ля списку польз-лям
	public String getUsernameByLoginAndPassword(String login, String password){
		for (User client : clients) {
			if(client.getLogin().equals(login) & client.getPassword().equals(password)){
				return client.getUsername();
			}
		}
		return null;
	}

}
