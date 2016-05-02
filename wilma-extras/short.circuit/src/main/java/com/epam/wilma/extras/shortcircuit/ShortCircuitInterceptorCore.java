package com.epam.wilma.extras.shortcircuit;
/*==========================================================================
Copyright 2013-2016 EPAM Systems

This file is part of Wilma.

Wilma is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Wilma is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wilma.  If not, see <http://www.gnu.org/licenses/>.
===========================================================================*/

import com.epam.wilma.common.helper.FileFactory;
import com.epam.wilma.common.helper.LogFilePathProvider;
import com.epam.wilma.common.helper.UniqueIdGenerator;
import com.epam.wilma.domain.http.WilmaHttpRequest;
import com.epam.wilma.domain.http.WilmaHttpResponse;
import com.epam.wilma.domain.stubconfig.parameter.ParameterList;
import com.epam.wilma.webapp.config.servlet.stub.upload.helper.FileOutputStreamFactory;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides the Short Circuit Service calls.
 *
 * @author tkohegyi
 */
public class ShortCircuitInterceptorCore {

    private static Map<String, ShortCircuitResponseInformation> shortCircuitMap = ShortCircuitChecker.getShortCircuitMap();
    private static Object shortCircuitMapGuard = ShortCircuitChecker.getShortCircuitMapGuard();
    private final Logger logger = LoggerFactory.getLogger(ShortCircuitInterceptorCore.class);

    @Autowired
    private LogFilePathProvider logFilePathProvider;
    @Autowired
    private FileFactory fileFactory;
    @Autowired
    private FileOutputStreamFactory fileOutputStreamFactory;

    /**
     * Method that generates the general hash code for a request in SHort Circuit.
     *
     * @param wilmaHttpRequest is the request, that is the base of the hash code
     * @return with the generated hash code
     */
    protected String generateKeyForMap(final WilmaHttpRequest wilmaHttpRequest) {
        String hashString = wilmaHttpRequest.getRequestLine() + " - " + wilmaHttpRequest.getBody().hashCode();
        //ensure that it is ok for a header
        //CHECKSTYLE OFF - we must use "new String" here
        hashString = new String(Base64.encodeBase64(hashString.getBytes()));
        //CHECKSTYLE ON
        return hashString;
    }

    /**
     * Method that handles GET (all) and DELETE (all) methods on the actual Short Circuit Map.
     *
     * @param myMethod            is expected as either GET or DELETE
     * @param httpServletResponse is the response object
     * @param path                is the request path
     * @return with the response body (and with the updated httpServletResponse object
     */
    protected String handleBasicCall(String myMethod, HttpServletResponse httpServletResponse, String path) {
        String response = null;
        if ("get".equalsIgnoreCase(myMethod)) {
            //list the map (circuits + get)
            response = getShortCircuitMap(httpServletResponse);
        }
        if ("delete".equalsIgnoreCase(myMethod)) {
            //first detect if we have basic path or we have a specified id in it
            int index = path.lastIndexOf("/");
            String idStr = path.substring(index + 1);
            int id;
            try {
                id = Integer.parseInt(idStr);
                //remove a specific map entry
                String[] keySet = shortCircuitMap.keySet().toArray(new String[shortCircuitMap.size()]);
                if (keySet.length > id) {
                    //we can delete it
                    String entryKey = keySet[id];
                    shortCircuitMap.remove(entryKey);
                    response = getShortCircuitMap(httpServletResponse);
                } else {
                    //resource cannot be deleted
                    response = "{ \"Cannot found specified entry in Short Circuit Cache\": \"" + id + "\" }";
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                //it is not a problem, we should delete all
                //invalidate map (remove all from map) (circuits + delete)
                shortCircuitMap.clear();
                response = getShortCircuitMap(httpServletResponse);
            }
        }
        return response;
    }

    /**
     * Method that handles request to save and load the Short Circuit Map.
     *
     * @param myMethod            POST (for Save) and GET (for Load) and DELETE for a selected
     * @param folder              is the folder to be used, or the identifier of the entry to be deleted
     * @param httpServletResponse is the response object
     * @return with the response body (and with the updated httpServletResponse object
     */
    protected String handleComplexCall(String myMethod, String folder, HttpServletResponse httpServletResponse) {
        String response = null;
        if ("post".equalsIgnoreCase(myMethod)) {
            //save map (to files) (post + circuits?folder=...)
            response = savePreservedMessagesFromMap(httpServletResponse, folder);
        }
        if ("get".equalsIgnoreCase(myMethod)) {
            //load map (from files) (get + circuits?folder=....)
            response = loadPreservedMessagesToMap(httpServletResponse, folder);
        }
        return response;
    }

    /**
     * Saves the map to a folder, to preserve it for later use.
     *
     * @param httpServletResponse is the response object
     * @param folder              is the folder to be used, under Wilma'S message folder.
     * @return with the response body - that is a json info about the result of the call
     */
    protected String savePreservedMessagesFromMap(HttpServletResponse httpServletResponse, String folder) {
        String response = null;
        String path = logFilePathProvider.getLogFilePath() + "/" + folder + "/";
        String filenamePrefix = "sc" + UniqueIdGenerator.getNextUniqueId() + "_";
        if (!shortCircuitMap.isEmpty()) {
            String[] keySet = shortCircuitMap.keySet().toArray(new String[shortCircuitMap.size()]);
            for (String entryKey : keySet) {
                ShortCircuitResponseInformation information = shortCircuitMap.get(entryKey);
                if (information != null) { //save only the cached files
                    //save this into file, folder is in folder variable
                    String filename = path + filenamePrefix + UniqueIdGenerator.getNextUniqueId() + ".json";
                    WilmaHttpResponse wilmaHttpResponse = information.getWilmaHttpResponse();
                    File file = fileFactory.createFile(filename);
                    try {
                        saveMapObject(file, entryKey, wilmaHttpResponse);
                    } catch (IOException e) {
                        String message = "Cache save failed at file: " + filename + ", with message: " + e.getLocalizedMessage();
                        logger.info("ShortCircuit: " + message);
                        response = "{ \"resultsFailure\": \"" + message + "\" }";
                        httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        break;
                    }
                }
            }
        }
        if (response == null) {
            String message = "Cache saved as: " + path + filenamePrefix + "*.json files";
            response = "{ \"resultsSuccess\": \"" + message + "\" }";
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            logger.info("ShortCircuit: " + message);
        }
        return response;
    }

    /**
     * Load the map from a folder, and create a new map from it.
     *
     * @param httpServletResponse is the response object
     * @param folder              is the folder to be used, under Wilma'S message folder.
     * @return with the response body - that is a json info about the result of the call
     */
    private String loadPreservedMessagesToMap(HttpServletResponse httpServletResponse, String folder) {
        Map<String, ShortCircuitResponseInformation> newShortCircuitMap = new HashMap<>();

        String path = logFilePathProvider.getLogFilePath() + "/" + folder;
        File folderFile = new File(path);
        File[] listOfFiles = folderFile.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String pattern = "sc*.json";
                // Create a Pattern object
                Pattern r = Pattern.compile(pattern);
                // Now create matcher object.
                Matcher m = r.matcher(listOfFiles[i].getName());
                if (m.find()) {
                    WilmaHttpResponse mapObject = loadMapObject(listOfFiles[i].getName());
                    if (mapObject != null) {
                        ShortCircuitResponseInformation info = new ShortCircuitResponseInformation(mapObject, Long.MAX_VALUE);
                        newShortCircuitMap.put(Integer.toString(i), info);
                    }
                }
            }
        }
        //adding the new map object
        synchronized (shortCircuitMapGuard) {
            shortCircuitMap.putAll(newShortCircuitMap);
        }
        return getShortCircuitMap(httpServletResponse);
    }

    private WilmaHttpResponse loadMapObject(String fileName) { //TODO here we need map object really, not WilmaHttpResponse
        WilmaHttpResponse wilmaHttpResponse = null;
        File file = new File(fileName);
        if (file.exists()) {
            //load the file
            String fileContent = loadFileToString(fileName);
            if (fileContent != null) { //TODO - this part can fail even if the string is not null
                JSONObject obj = new JSONObject(fileContent);
                String hashKey = obj.getString("Key");
                String responseCode = obj.getString("ResponseCode");
                String contentType = obj.getString("ContentType");
                String body = obj.getJSONObject("Body").getString("Body");
                JSONArray headers = obj.getJSONArray("Headers");
                wilmaHttpResponse.setContentType(contentType);
                wilmaHttpResponse.setBody(body);
                //how to set other fields???
            }
        }
        return wilmaHttpResponse;
    }

    private String loadFileToString(final String fileName) {
        String text;
        try {
            //CHECKSTYLE OFF - ignoring new String() error, as this is the most effective implementation
            text = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
            //CHECKSTYLE ON
        } catch (IOException e) {
            //cannot read a file
            text = null;
        }
        return text; //TODO, it is not implemented properly yet
    }

    private void saveMapObject(File file, String entryKey, WilmaHttpResponse wilmaHttpResponse) throws IOException {
        // if file does not exists, then create it
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        FileOutputStream fos = fileOutputStreamFactory.createFileOutputStream(file);
        fos.write(("{\n  \"Key\": \"" + entryKey + "\",\n").getBytes());
        fos.write(("  \"ResponseCode\": \"" + wilmaHttpResponse.getStatusCode() + "\",\n").getBytes());
        fos.write(("  \"ContentType\": \"" + wilmaHttpResponse.getContentType() + "\",\n").getBytes());
        Map<String, String> headers = wilmaHttpResponse.getHeaders();
        fos.write("  \"Headers\": [".getBytes());
        int j = 1;
        for (String key : headers.keySet()) {
            fos.write(("    { \"" + key + "\": \"" + headers.get(key) + "\" }").getBytes());
            if (j != headers.size()) {
                fos.write(",".getBytes());
            }
            fos.write("\n".getBytes());
            j++;
        }
        fos.write("  ],\n  \"Body\": ".getBytes());
        String myBody = new JSONObject().put("Body", wilmaHttpResponse.getBody()).toString();
        fos.write((myBody + "\n}").getBytes());
        fos.close();
    }

    /**
     * Method that is used to preserve the response when it arrives first time.
     * Note that only responses with 200 response code will be preserved.
     *
     * @param shortCircuitHashCode is the hash code used as key in the Short Circuit Map
     * @param wilmaHttpResponse    is the response object
     * @param parameterList        may contain the timeout value to be used for the entry
     */
    protected void preserveResponse(String shortCircuitHashCode, WilmaHttpResponse wilmaHttpResponse, ParameterList parameterList) {
        long timeout;
        if (shortCircuitMap.containsKey(shortCircuitHashCode)) { //only if this needs to be handled
            //do we have the response already, or we need to catch it right now?
            ShortCircuitResponseInformation shortCircuitResponseInformation = shortCircuitMap.get(shortCircuitHashCode);
            if (shortCircuitResponseInformation == null) {
                //we need to store the response now
                //but first check if response code is 200 - we store only those responses
                if (wilmaHttpResponse.getStatusCode() == HttpServletResponse.SC_OK) {
                    String timeoutParameterName = "timeout";
                    if (parameterList != null && parameterList.get(timeoutParameterName) != null) {
                        timeout = Long.valueOf(parameterList.get(timeoutParameterName))
                                + Calendar.getInstance().getTimeInMillis();
                    } else {
                        timeout = Long.MAX_VALUE; //forever
                    }
                    shortCircuitResponseInformation = new ShortCircuitResponseInformation(wilmaHttpResponse, timeout);
                    shortCircuitMap.put(shortCircuitHashCode, shortCircuitResponseInformation);
                    logger.info("ShortCircuit: Message captured for hashcode: " + shortCircuitHashCode);
                }
            } else { //we have response
                //take care about timeout
                timeout = Calendar.getInstance().getTimeInMillis();
                if (timeout > shortCircuitResponseInformation.getTimeout()) {
                    shortCircuitMap.remove(shortCircuitHashCode);
                    logger.debug("ShortCircuit: Timeout has happened for hashcode: " + shortCircuitHashCode);
                }
            }
        }
    }

    /**
     * List the actual status of the Short Circuit Map.
     *
     * @param httpServletResponse is the response object
     * @return with the response body (and with the updated httpServletResponse object
     */
    protected String getShortCircuitMap(HttpServletResponse httpServletResponse) {
        StringBuilder response = new StringBuilder("{\n  \"shortCircuitCache\": [\n");
        if (!shortCircuitMap.isEmpty()) {
            String[] keySet = shortCircuitMap.keySet().toArray(new String[shortCircuitMap.size()]);
            for (int i = 0; i < keySet.length; i++) {
                String entryKey = keySet[i];
                boolean cached = shortCircuitMap.get(entryKey) != null;
                //CHECKSTYLE OFF - we must use "new String" here
                String decodedEntryKey = new String(Base64.decodeBase64(entryKey)); //make it human readable
                //CHECKSTYLE ON
                response.append("    { \"id\": \"").append(i)
                        .append("\", \"hashCode\": \"").append(decodedEntryKey)
                        .append("\", \"cached\": ").append(cached)
                        .append(" }");
                if (i < keySet.length - 1) {
                    response.append(",");
                }
                response.append("\n");
            }
        }
        response.append("  ]\n}\n");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        return response.toString();
    }

}
