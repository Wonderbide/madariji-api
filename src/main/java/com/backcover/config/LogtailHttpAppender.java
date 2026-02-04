package com.backcover.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import net.logstash.logback.encoder.LogstashEncoder;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Custom Logback appender to send logs directly to Logtail via HTTP
 */
public class LogtailHttpAppender extends AppenderBase<ILoggingEvent> {
    
    private String sourceToken;
    private String endpointUrl = "https://s1390626.eu-nbg-2.betterstackdata.com/";
    private LogstashEncoder encoder;
    
    @Override
    public void start() {
        if (sourceToken == null || sourceToken.isEmpty()) {
            addError("Source token is not set for LogtailHttpAppender");
            return;
        }
        
        encoder = new LogstashEncoder();
        encoder.setContext(getContext());
        encoder.start();
        
        super.start();
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        
        try {
            byte[] jsonBytes = encoder.encode(event);
            sendToLogtail(jsonBytes);
        } catch (Exception e) {
            addError("Failed to send log to Logtail", e);
        }
    }
    
    private void sendToLogtail(byte[] jsonBytes) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + sourceToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(jsonBytes.length));
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBytes);
                os.flush();
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200 && responseCode != 201 && responseCode != 204) {
                addError("Logtail returned response code: " + responseCode);
            }
        } finally {
            conn.disconnect();
        }
    }
    
    @Override
    public void stop() {
        if (encoder != null) {
            encoder.stop();
        }
        super.stop();
    }
    
    public void setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
    }
    
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
}