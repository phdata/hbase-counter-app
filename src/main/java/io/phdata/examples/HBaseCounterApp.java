package io.phdata.examples;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletMapping;


public class HBaseCounterApp {
  public static void main(String[] args) throws Exception {
    int port = 8091;
    if (args.length > 0) {
      Integer.parseInt(args[0]);
    }
    String webDir = HBaseCounterApp.class.getClassLoader().getResource("io/phdata/examples/webapp").toExternalForm();
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setWelcomeFiles(new String[]{"index.html"});
    resourceHandler.setResourceBase(webDir);
    Server server = new Server();
    Connector connector = new SocketConnector();
    connector.setPort(port);
    server.setConnectors(new Connector[]
        {connector});
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(HBaseCounterServlet.class.getName(), "/UpdateCounter");
    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[]{resourceHandler, handler});
    server.setHandler(handlers);
    server.start();
    server.join();
  }
}
