package de.invees.portal.event.event;

import de.invees.portal.event.configuration.Configuration;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/")
public class DefaultServlet extends HttpServlet {

  private final Configuration configuration;

  public DefaultServlet(Configuration configuration) {
    this.configuration = configuration;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws  IOException {
    response.getWriter().println("Invees/Event");
    response.setHeader("Access-Control-Allow-Origin", configuration.getAccessControlAllowOrigin());
  }

  protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
    response.setHeader("Access-Control-Allow-Origin", configuration.getAccessControlAllowOrigin());
  }

}
