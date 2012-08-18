package ch.spacebase.openclassic.server.network;

import java.util.HashMap;
import java.util.Map;

import ch.spacebase.openclassic.api.network.msg.Message;
import ch.spacebase.openclassic.server.network.codec.*;
import ch.spacebase.openclassic.server.network.codec.custom.*;

public final class CodecLookupService {

	private static MessageCodec<?>[] opcodeTable = new MessageCodec<?>[256];

	private static Map<Class<? extends Message>, MessageCodec<?>> classTable = new HashMap<Class<? extends Message>, MessageCodec<?>>();
	
	static {
		try {
			bind(IdentificationCodec.class);
			bind(PingCodec.class);
			bind(LevelInitializeCodec.class);
			bind(LevelDataCodec.class);
			bind(LevelFinalizeCodec.class);
			bind(PlayerSetBlockCodec.class);
			bind(BlockChangeCodec.class);
			bind(PlayerSpawnCodec.class);
			bind(PlayerTeleportCodec.class);
			bind(PlayerPositionRotationCodec.class);
			bind(PlayerPositionCodec.class);
			bind(PlayerRotationCodec.class);
			bind(PlayerDespawnCodec.class);
			bind(PlayerChatCodec.class);
			bind(PlayerDisconnectCodec.class);
			bind(PlayerOpCodec.class);
			
			// Custom
			bind(GameInfoCodec.class);
			bind(CustomBlockCodec.class);
			bind(BlockModelCodec.class);
			bind(QuadCodec.class);
			bind(KeyChangeCodec.class);
			bind(LevelColorCodec.class);
			bind(AudioRegisterCodec.class);
			bind(AudioPlayCodec.class);
			bind(MusicStopCodec.class);
			bind(PluginCodec.class);
			bind(CustomCodec.class);
		} catch(Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private static <T extends Message, C extends MessageCodec<T>> void bind(Class<C> clazz) throws InstantiationException, IllegalAccessException {
		MessageCodec<T> codec = clazz.newInstance();
		opcodeTable[codec.getOpcode()] = codec;
		classTable.put(codec.getType(), codec);
	}

	public static MessageCodec<?> find(int opcode) {
		return opcodeTable[opcode];
	}

	@SuppressWarnings("unchecked")
	public static <T extends Message> MessageCodec<T> find(Class<T> clazz) {
		return (MessageCodec<T>) classTable.get(clazz);
	}

	private CodecLookupService() {
	}
	
}
