package ch.spacebase.openclassic.server.network;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.StaticChannelPipeline;

public class ClassicPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		return new StaticChannelPipeline(
			new ClassicDecoder(),
			new ClassicEncoder(),
			new ClassicHandler()
		);
	}

}
