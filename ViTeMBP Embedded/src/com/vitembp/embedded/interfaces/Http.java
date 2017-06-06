/*
 * Video Telemetry for Mountain Bike Platform back-end services.
 * Copyright (C) 2017 Kyle Grund
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vitembp.embedded.interfaces;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vitembp.embedded.hardware.Platform;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;

/**
 * Provides a user interface accepting HTTP requests.
 */
public class Http {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The HTTP server instance this object operates.
     */
    private final HttpServer server;
    
    /**
     * Initializes a new instance of the Http class.
     * @param port The port to run the HTTP server on.
     * @param functions The API functions to provide an interface for.
     * @throws IOException If there is an exception creating the HTTP server.
     */
    public Http(int port, Platform functions) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/setsynclight", this.getHandler(functions));
        this.server.start();
    }
    
    /**
     * Returns a HttpHandler instance which can process HTTP service requests.
     * @param functions 
     * @return 
     */
    private HttpHandler getHandler(final Platform functions) {
        return (HttpExchange he) -> {
            // get path url
            Headers headers = he.getRequestHeaders();
            Boolean enabled = null;
            
            System.out.println("Request from: " + he.getRemoteAddress().getHostString());
            for(String key : headers.keySet()) {
                System.out.println("Header: (" + key + "," + headers.getFirst(key) +")");
            }
            
            if (headers.containsKey("enabled")) {
                enabled = Boolean.parseBoolean(headers.getFirst("enabled"));
            }
            
            StringBuilder request = new StringBuilder();
            InputStream requestStream = he.getRequestBody();
            int requestChar = requestStream.read();
            while (requestChar >= 0) {
                request.append((char)requestChar);
                requestChar = requestStream.read();
            }
            
            System.out.println(request.toString());
            
            if (enabled == null) {
                URI req = he.getRequestURI();
                String decoded = req.getQuery();
                if (decoded != null) {
                    List<String> query = Arrays.asList(req.getQuery().split("&"));
                    Map<String, String> posts = new HashMap<>();
                    for (String qry : query) {
                        String[] elms = qry.split("=");
                        if (elms.length == 2) {
                            posts.put(elms[0].toLowerCase(), elms[1]);
                        }
                    }

                    String path = null;

                    if (posts.containsKey("enabled")) {
                        enabled = Boolean.parseBoolean(posts.get("enabled"));
                    }
                }
            }
            
            if (enabled == null) {
                //todo: Only sends success response, must send error resp as well.
                byte[] toSend = "Failed, no enabled state specified.".getBytes();
                he.sendResponseHeaders(400, toSend.length);
                try (OutputStream os = he.getResponseBody()){
                    os.write(toSend);
                }
            } else {
                System.out.println("Request to set sync light state to: " + enabled.toString());
                
                try {
                    // set the sync light
                    functions.getSetSyncLightTarget().accept(enabled);
                } catch (IOException ex) {
                    LOGGER.error("IO error setting sync light.", ex);
                    byte[] toSend = "Error processing request".getBytes();
                    he.sendResponseHeaders(500, toSend.length);
                    try (OutputStream os = he.getResponseBody()){
                        os.write(toSend);
                    }
                    return;
                }                
                
                // holds response to send to caller
                StringBuilder toReturn = new StringBuilder();
                toReturn.append("<p>Set synchronization light state to: ");
                toReturn.append(Boolean.toString(enabled));
                toReturn.append("</p>");
                
                byte[] toSend = toReturn.toString().getBytes();
                he.sendResponseHeaders(200, toSend.length);
                try (OutputStream os = he.getResponseBody()){
                    os.write(toSend);
                }
                return;
            }
            
            LOGGER.error("Unknown error processing request.");
            byte[] toSend = "Invalid request".getBytes();
            he.sendResponseHeaders(500, toSend.length);
            try (OutputStream os = he.getResponseBody()){
                os.write(toSend);
            }
        };
    }
}
