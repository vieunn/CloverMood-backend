package com.example.api.config;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Custom deserializer for Instant that handles ISO 8601 format
        javaTimeModule.addDeserializer(Instant.class, new StdDeserializer<Instant>(Instant.class) {
            @Override
            public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getValueAsString();
                if (value == null) {
                    return null;
                }
                try {
                    return Instant.parse(value);
                } catch (Exception e) {
                    throw new IOException("Unable to parse Instant: " + value, e);
                }
            }
        });
        
        // Custom serializer for Instant to serialize as ISO 8601 string
        javaTimeModule.addSerializer(Instant.class, new StdSerializer<Instant>(Instant.class) {
            @Override
            public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value.toString());
                }
            }
        });
        
        // Custom deserializer for LocalDateTime that handles ISO offset format
        javaTimeModule.addDeserializer(LocalDateTime.class, new StdDeserializer<LocalDateTime>(LocalDateTime.class) {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getValueAsString();
                if (value == null) {
                    return null;
                }
                try {
                    // Try to parse as ISO offset date-time and convert to LocalDateTime
                    return OffsetDateTime.parse(value).toLocalDateTime();
                } catch (Exception e1) {
                    try {
                        // Fallback to ISO zoned date-time
                        return ZonedDateTime.parse(value).toLocalDateTime();
                    } catch (Exception e2) {
                        try {
                            // Final fallback to ISO local date time
                            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        } catch (Exception e3) {
                            throw new IOException("Unable to parse LocalDateTime: " + value, e3);
                        }
                    }
                }
            }
        });
        
        // Custom serializer for LocalDateTime to serialize as ISO string
        javaTimeModule.addSerializer(LocalDateTime.class, new StdSerializer<LocalDateTime>(LocalDateTime.class) {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            }
        });
        
        mapper.registerModule(javaTimeModule);
        
        return mapper;
    }
}
