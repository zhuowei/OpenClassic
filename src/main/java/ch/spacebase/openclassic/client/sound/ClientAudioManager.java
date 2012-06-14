package ch.spacebase.openclassic.client.sound;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.sound.AudioManager;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.player.Player;

import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class ClientAudioManager implements AudioManager {
	
	private static final Random rand = new Random();
	private static int nextSoundId = 0;
	
	private final Map<String, List<URL>> sounds = new HashMap<String, List<URL>>();
	private final Map<String, List<URL>> music = new HashMap<String, List<URL>>();
	
	private SoundSystem system;
	private Minecraft mc;
	public long lastBGM = System.currentTimeMillis();
	
	public ClientAudioManager(Minecraft mc) {
		this.mc = mc;
		
		Class<? extends Library> lib = Library.class;
		
		if(SoundSystem.libraryCompatible(LibraryLWJGLOpenAL.class)) {
			lib = LibraryLWJGLOpenAL.class;
		} else if(SoundSystem.libraryCompatible(LibraryJavaSound.class)) {
			lib = LibraryJavaSound.class;
		}
		
		try {
			this.system = new SoundSystem(lib);
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
		} catch(SoundSystemException e) {
			e.printStackTrace();
		}
	}
	
	public void update(Player player) {
		if(player != null && OpenClassic.getClient().isInGame()) {
			this.system.setListenerPosition(player.x, player.y, player.z);
			// TODO: Orientation?
		} else {
			this.system.setListenerPosition(0, 0, 0);
			this.system.setListenerOrientation(0, 0, -1, 0, 1, 0);
		}
	}
	
	public void cleanup() {
		this.system.cleanup();
	}
	
	public void registerSound(String sound, URL file) {
		if(!this.sounds.containsKey(sound)) this.sounds.put(sound, new ArrayList<URL>());
		this.sounds.get(sound).add(file);
	}
	
	public void registerMusic(String music, URL file) {
		if(!this.music.containsKey(music)) this.music.put(music, new ArrayList<URL>());
		this.music.get(music).add(file);
	}
	
	public boolean playSound(String sound, float x, float y, float z, float volume, float pitch) {
		if(!this.mc.settings.sound) return true;
		
		List<URL> files = this.sounds.get(sound);
		if(files != null) {
			URL file = files.get(rand.nextInt(files.size()));
			
			nextSoundId = (nextSoundId + 1) % 256;
			String source = "sound_" + nextSoundId;

			float attenuation = 16;
			if(volume > 1) attenuation = volume * 16;
			
			this.system.newSource(volume > 1, source, file, file.getFile(), false, x, y, z, SoundSystemConfig.ATTENUATION_LINEAR, attenuation);
			
			if(volume > 1) volume = 1;
			this.system.setVolume(source, volume);
			this.system.setPitch(source, pitch);
			
			this.system.play(source);
			return true;
		}
		
		return false;
	}
	
	public boolean playSound(String sound, float volume, float pitch) {
		return this.playSound(sound, this.system.getListenerData().position.x, this.system.getListenerData().position.y, this.system.getListenerData().position.z, volume, pitch);
	}
	
	public boolean playMusic(String music) {
		return this.playMusic(music, false);
	}
	
	public boolean playMusic(String music, boolean loop) {
		if(!this.mc.settings.music) return true;
		
		List<URL> files = this.music.get(music);
		if(files != null) {
			if(this.isPlaying(music)) return true;
			if(this.isPlayingMusic()) {
				this.stopMusic();
			}
			
			URL file = files.get(rand.nextInt(files.size()));
			this.system.backgroundMusic(music, file, file.getFile(), loop);
			this.system.setTemporary(music, true);
			this.system.play(music);
			return true;
		}
		
		return false;
	}
	
	public boolean isPlayingMusic() {
		for(String music : this.music.keySet()) {
			if(this.system.playing(music)) return true;
		}
		
		return false;
	}
	
	public void stopMusic() {
		for(String music : this.music.keySet()) {
			if(this.isPlaying(music)) this.stop(music);
		}
	}
	
	public boolean isPlaying(String music) {
		return this.system.playing(music);
	}
	
	public void stop(String music) {
		this.system.stop(music);
	}
	
}