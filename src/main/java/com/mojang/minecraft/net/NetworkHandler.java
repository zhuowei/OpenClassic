package com.mojang.minecraft.net;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Minecraft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public final class NetworkHandler {

	public volatile boolean connected;
	public SocketChannel channel;
	public ByteBuffer in = ByteBuffer.allocate(1048576);
	public ByteBuffer out = ByteBuffer.allocate(1048576);
	public NetworkManager netManager;
	private Socket sock;

	public NetworkHandler(String server, int port, Minecraft mc) {
		try {
			this.channel = SocketChannel.open();
			this.channel.connect(new InetSocketAddress(server, port));
			this.channel.configureBlocking(false);
			this.sock = this.channel.socket();
		} catch(IOException e) {
			throw new RuntimeException("Failed to connect!", e);
		}
		
		this.connected = true;
		this.in.clear();
		this.out.clear();
		
		if(this.sock != null) {
			try {
				this.sock.setTcpNoDelay(true);
				this.sock.setTrafficClass(24);
				this.sock.setKeepAlive(false);
				this.sock.setReuseAddress(false);
				this.sock.setSoTimeout(100);
			} catch(IOException e) {
				throw new RuntimeException("Failed to connect!", e);
			}
		}
	}

	public void close() {
		if(this.netManager.identified) {
			for(BlockType block : Blocks.getBlocks()) {
				if(block != null && block instanceof CustomBlock) {
					Blocks.unregister(block.getId());
				}
			}
		}
		
		GeneralUtils.getMinecraft().serverPlugins.clear();
		
		try {
			if (this.out.position() > 0) {
				this.out.flip();
				this.channel.write(this.out);
				this.out.compact();
			}
		} catch (Exception e) {
		}

		this.connected = false;

		try {
			this.channel.close();
		} catch (Exception e) {
		}

		this.sock = null;
		this.channel = null;
	}

	public void send(PacketType packet, Object... params) {
		if (this.connected) {
			this.out.put(packet.opcode);

			for (int count = 0; count < params.length; ++count) {
				Class<?> type = packet.params[count];
				Object param = params[count];
				if (this.connected) {
					try {
						if (type == Long.TYPE) {
							this.out.putLong((Long) param);
						} else if (type == Integer.TYPE) {
							this.out.putInt((Integer) param);
						} else if (type == Short.TYPE) {
							this.out.putShort((Short) param);
						} else if (type == Byte.TYPE) {
							this.out.put((Byte) param);
						} else if (type == Double.TYPE) {
							this.out.putDouble((Double) param);
						} else if (type == Float.TYPE) {
							this.out.putFloat((Float) param);
						} else {
							byte[] output;
							if (type != String.class) {
								if (type == byte[].class) {
									output = (byte[]) param;
									
									if (output.length < 1024) {
										output = Arrays.copyOf(output, 1024);
									}

									this.out.put(output);
								}
							} else {
								output = ((String) param).getBytes("UTF-8");
								byte data[] = new byte[64];
								Arrays.fill(data, (byte) 32);

								for (int index = 0; index < 64 && index < output.length; index++) {
									data[index] = output[index];
								}

								for (int index = output.length; index < 64; index++) {
									data[index] = 32;
								}

								this.out.put(data);
							}
						}
					} catch (Exception e) {
						this.netManager.error(e);
					}
				}
			}

		}
	}

	public Object recieveData(Class<?> type) {
		if (!this.connected) {
			return null;
		} else {
			try {
				if (type == Long.TYPE) {
					return this.in.getLong();
				} else if (type == Integer.TYPE) {
					return this.in.getInt();
				} else if (type == Short.TYPE) {
					return this.in.getShort();
				} else if (type == Byte.TYPE) {
					return this.in.get();
				} else if (type == Double.TYPE) {
					return this.in.getDouble();
				} else if (type == Float.TYPE) {
					return this.in.getFloat();
				} else if (type == String.class) {
					byte data[] = new byte[64];
					this.in.get(data);
					
					return (new String(data, "UTF-8")).trim();
				} else if (type == byte[].class) {
					byte[] var3 = new byte[1024];
					this.in.get(var3);
					return var3;
				} else {
					return null;
				}
			} catch (Exception e) {
				this.netManager.error(e);
				return null;
			}
		}
	}
}
