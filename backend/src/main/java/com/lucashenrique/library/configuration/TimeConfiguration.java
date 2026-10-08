package com.lucashenrique.library.configuration;
import java.time.*;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
@Configuration
public class TimeConfiguration {
 @Bean public Clock libraryClock(@Value("${library.time-zone:America/Sao_Paulo}") String zone){return Clock.system(ZoneId.of(zone));}
}
