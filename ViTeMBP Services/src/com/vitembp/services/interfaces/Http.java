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
package com.vitembp.services.interfaces;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vitembp.services.ApiFunctions;
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
    public Http(int port, ApiFunctions functions) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/findsyncframes", this.getHandler(functions));
        this.server.createContext("/getHashes", this.getHandler(functions));
        this.server.createContext("/healthcheck", this.getHealthCheckHandler());
        this.server.start();
    }
    
    /**
     * Returns a HttpHandler instance which can process HTTP service requests.
     * @param functions 
     * @return 
     */
    private HttpHandler getHandler(final ApiFunctions functions) {
        return (HttpExchange he) -> {
            // get path url
            Headers headers = he.getRequestHeaders();
            URI uri = null;
            
            System.out.println("Request from: " + he.getRemoteAddress().getHostString());
            for(String key : headers.keySet()) {
                System.out.println("Header: (" + key + "," + headers.getFirst(key) +")");
            }
            
            if (headers.containsKey("path")) {
                String path = headers.getFirst("path");
                try {                    
                    uri = new URI(path);
                } catch (URISyntaxException ex) {
                    LOGGER.warn("Unable to parse path: " + path, ex);
                }
            }
            
            boolean outputDebug = false;
            if (headers.containsKey("debug")) {
                outputDebug = Boolean.parseBoolean(headers.getFirst("debug"));
            }
            
            StringBuilder request = new StringBuilder();
            InputStream requestStream = he.getRequestBody();
            int requestChar = requestStream.read();
            while (requestChar >= 0) {
                request.append((char)requestChar);
                requestChar = requestStream.read();
            }
            
            System.out.println(request.toString());
            
            if (uri == null) {
                URI req = he.getRequestURI();
                List<String> query = Arrays.asList(req.getQuery().split("&"));
                Map<String, String> posts = new HashMap<>();
                for (String qry : query) {
                    String[] elms = qry.split("=");
                    if (elms.length == 2) {
                        posts.put(elms[0].toLowerCase(), elms[1]);
                    }
                }
                
                String path = null;
                
                if (posts.containsKey("path")) {
                    String pathStr = posts.get("path");
                    try {
                        uri = new URI(pathStr);
                    } catch (URISyntaxException ex) {
                        LOGGER.warn("Bad path found in request: " + pathStr, ex);
                    }
                }
                
                if (posts.containsKey("debug")) {
                    outputDebug = Boolean.parseBoolean(posts.get("debug"));
                }
            }
            
            if (uri == null) {
                //todo: Only sends success response, must send error resp as well.
                byte[] toSend = "Failed, no path specified.".getBytes();
                he.sendResponseHeaders(400, toSend.length);
                try (OutputStream os = he.getResponseBody()){
                    os.write(toSend);
                }
            } else if (!"https".equals(uri.getScheme())) {
                //todo: Only sends success response, must send error resp as well.
                byte[] toSend = "Failed, invalid path.".getBytes();
                he.sendResponseHeaders(400, toSend.length);
                try (OutputStream os = he.getResponseBody()){
                    os.write(toSend);
                }
            } else {
                System.out.println("Request to process URI: " + uri.toString());
                // call to functions
                List<Integer> result = null;
                Path debugOutFile = null;
                try {
                    if (outputDebug) {
                        // create a temporary file name
                        debugOutFile = Files.createTempFile("vitembp", ".mp4");
                        Files.deleteIfExists(debugOutFile);
                        
                        result = functions.findChannelSyncFramesDiag(uri.toString(), ApiFunctions.COLOR_CHANNELS.GREEN, debugOutFile);
                        
                        if (!Files.deleteIfExists(debugOutFile)) {
                            LOGGER.error("Could not delete temporary debug output video file.");
                        }
                    } else {
                        result = functions.findChannelSyncFrames(uri.toString(), ApiFunctions.COLOR_CHANNELS.GREEN);
                    }
                } catch (Exception ex) {
                    LOGGER.error("IO error processing URI.", ex);
                    byte[] toSend = "Error processing request".getBytes();
                    he.sendResponseHeaders(500, toSend.length);
                    try (OutputStream os = he.getResponseBody()){
                        os.write(toSend);
                    }
                    return;
                }

                System.out.println("Result: " + Arrays.toString(result.toArray()));
                
                //todo: Only sends success response, must send error resp as well.
                StringBuilder toReturn = new StringBuilder();
                
                toReturn.append("<p>Synchronization Frames: ");
                toReturn.append(Arrays.toString(result.toArray()));
                toReturn.append("</p>");

                if (debugOutFile != null) {
                    toReturn.append("<p>Debug output: <a href=\"http://www.vitembp.com/debug/");
                    toReturn.append(debugOutFile.getFileName().toString());
                    toReturn.append("\">");
                    toReturn.append(debugOutFile.getFileName().toString());
                    toReturn.append("</a></p>");
                }
                
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
    
    /**
     * Returns a HttpHandler instance which can process HTTP service requests.
     * @return 
     */
    private HttpHandler getHealthCheckHandler() {
        return (HttpExchange he) -> {
            //todo: Sends success response, must send error resp as well.
            byte[] toSend = "Services Healthy.".getBytes();
            he.sendResponseHeaders(200, toSend.length);
            try (OutputStream os = he.getResponseBody()){
                os.write(toSend);
            }
        };
    }
}
