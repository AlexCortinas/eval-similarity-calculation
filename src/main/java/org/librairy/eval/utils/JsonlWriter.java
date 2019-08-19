package org.librairy.eval.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class JsonlWriter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonlWriter.class);
    private final File file;

    private BufferedWriter writer;
    private ObjectMapper jsonMapper = new ObjectMapper();

    public JsonlWriter(File jsonFile) throws IOException {
        if (jsonFile.exists()) jsonFile.delete();
        if (!jsonFile.getParentFile().exists()) jsonFile.getParentFile().mkdirs();
        this.file = jsonFile;
        this.writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(jsonFile))));
    }


    public void write(T data){

        String json = null;
        try {
            json = jsonMapper.writeValueAsString(data);
            writer.write(json+"\n");
        } catch (Exception e) {
            LOG.error("Unexpected error writing to file: " + file.getAbsolutePath(), e);
        }
    }

    public void close(){
        try {
            writer.close();
        } catch (IOException e) {
            LOG.warn("Error closing file: " + file.getAbsolutePath(), e);
        }
    }

}
