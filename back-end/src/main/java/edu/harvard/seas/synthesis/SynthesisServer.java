package edu.harvard.seas.synthesis;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import edu.harvard.seas.synthesis.sample.RegexSampler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class SynthesisServer {
	public static final String session_id = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());

	public static void main(String[] args) throws Exception {
		
		ServerCommandLineParser cmdParser = new ServerCommandLineParser();
		boolean b = cmdParser.parse(args);
		if(!b) {
			return;
		}
		
		// config the synthesizer and the input generator
		ResnaxRunner.resnax_path = cmdParser.resnax_path;
		ResnaxRunner.timeout = cmdParser.timeout;
//		ExampleBasedInputGenerator.input_generator_path = cmdParser.input_generator_path;
		ExampleBasedInputGenerator.num_of_examples_per_cluster = cmdParser.num_of_examples_per_cluster;
//		ExampleBasedInputGenerator.python3_path = cmdParser.python3_path;
		RegexSampler.python_cmd = cmdParser.python3_path;
		
		Server server = new Server(8070);				
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(SynthesisServerHandler.class);
            }
        };
        
        server.setHandler(wsHandler);
        server.start();
        server.join();
	}
}
