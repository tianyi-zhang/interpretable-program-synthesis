package edu.harvard.seas.synthesis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import edu.harvard.seas.synthesis.logging.SynthesisLogger;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class PauseListener {
	@OnWebSocketMessage
	public void onMessage(String message) {
		System.out.println(message);
		SynthesisLogger.getSynthesisLogger().logString("Synthesizer: " + message);
		if (message.equals("Pause")) {
			// write the signal to the pause file
			File f = new File("pause");
			try {
				String counter = readCounter();
				if(counter != null) {
					FileUtils.writeStringToFile(f, "pause-" + counter, Charset.defaultCharset(), false);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (message.equals("Stop")) {
			File f1 = new File("pause");
			File f2 = new File("stop");
			try {
				String counter = readCounter();
				if(counter != null) {
					// write a pause signal first so the synthesizer will spill out the satisfying programs it has found so far
					FileUtils.writeStringToFile(f1, "pause-" + counter, Charset.defaultCharset(), false);
					// sleep for 1 second
					Thread.sleep(1000);
					// write the stop signal so the resnax runner will kill the synthesis process
					FileUtils.writeStringToFile(f2, "stop-" + counter, Charset.defaultCharset(), false);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String readCounter() {
		String counter = "0";
		
		try {
			File in = new File("input");
			if(!in.exists()) {
				return null;
			}
			List<String> lines = FileUtils.readLines(in, Charset.defaultCharset());
			// read from the end
			for(int i = lines.size() - 1; i >= 0; i--) {
				String line = lines.get(i);
				if(line.startsWith("READY-")) {
					counter = line.split("-")[1]; 
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return counter;
	}
}
