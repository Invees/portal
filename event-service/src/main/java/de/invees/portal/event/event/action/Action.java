package de.invees.portal.event.event.action;

import com.google.gson.JsonObject;
import de.invees.portal.event.event.EventHandler;

public interface Action {

  JsonObject execute(EventHandler handler, JsonObject message);

}
