package edu.harvard.seas.synthesis;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class HTTPServer {
	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);	
		
		ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase("../front-end/");
        
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(PauseListener.class);
            }
        };
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {wsHandler, resource_handler, new DefaultHandler() });
        server.setHandler(handlers);
        
        server.start();
        server.join();
	}
}
