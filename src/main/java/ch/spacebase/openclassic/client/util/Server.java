package ch.spacebase.openclassic.client.util;

public class Server {

	public String username = "";
	public String name = "Unnamed";
	public String ip = "";
	public int port = 25565;
	public int users = 0;
	public int max = 0;
	public String flags = "";
	public int order = 10;
	public String mppass = "";
	public String description = "";
	public String serverId = "";

	public Server(String username, String data) {
		this.username = username;
		String[] elements = data.trim().split("\t", 10);
		
		if (elements.length > 0)
			this.name = elements[0];
		if (elements.length > 1)
			this.ip = elements[1];
		if (elements.length > 2)
			this.port = (elements[2].length() > 0 ? Integer.parseInt(elements[2]) : 0);
		if (elements.length > 3)
			this.users = (elements[3].length() > 0 ? Integer.parseInt(elements[3]) : -1);
		if (elements.length > 4)
			this.max = (elements[4].length() > 0 ? Integer.parseInt(elements[4]) : 0);
		if (elements.length > 5)
			this.mppass = elements[5];
		if (elements.length > 6)
			this.serverId = elements[6];
		if (elements.length > 7)
			this.order = (elements[7].length() > 0 ? Integer.parseInt(elements[7]) : 10);
		if (elements.length > 8)
			this.flags = elements[8];
		if (elements.length > 9)
			this.description = elements[9];

		if (this.serverId.length() == 0)
			this.serverId = (this.ip + ":" + this.port);
	}

	public Server(String name, int users, int max, String id) {
		this.name = name;
		this.users = users;
		this.max = max;
		this.serverId = id;
	}

	public String getUrl() {
		return "http://www.minecraft.net/classic/play/" + this.serverId;
	}

}
