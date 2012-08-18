package ch.spacebase.openclassic.server.network;

import java.util.HashMap;
import java.util.Map;

import ch.spacebase.openclassic.api.network.msg.*;
import ch.spacebase.openclassic.api.network.msg.custom.*;
import ch.spacebase.openclassic.server.network.handler.*;
import ch.spacebase.openclassic.server.network.handler.custom.*;

public final class HandlerLookupService {

	private static final Map<Class<? extends Message>, MessageHandler<?>> handlers = new HashMap<Class<? extends Message>, MessageHandler<?>>();
	
	static {
		try {
			bind(IdentificationMessage.class, IdentificationMessageHandler.class);
			bind(PlayerSetBlockMessage.class, PlayerSetBlockMessageHandler.class);
			bind(PlayerTeleportMessage.class, PlayerTeleportMessageHandler.class);
			bind(PlayerChatMessage.class, PlayerChatMessageHandler.class);
			
			// Custom
			bind(GameInfoMessage.class, ClientInfoMessageHandler.class);
			bind(KeyChangeMessage.class, KeyChangeMessageHandler.class);
			bind(PluginMessage.class, PluginMessageHandler.class);
			bind(CustomMessage.class, CustomMessageHandler.class);
		} catch(Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static <T extends Message> void bind(Class<T> clazz, Class<? extends MessageHandler<T>> handlerClass) throws InstantiationException, IllegalAccessException {
		MessageHandler<T> handler = handlerClass.newInstance();
		handlers.put(clazz, handler);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Message> MessageHandler<T> find(Class<T> clazz) {
		return (MessageHandler<T>) handlers.get(clazz);
	}

	private HandlerLookupService() {
	}
	
}
