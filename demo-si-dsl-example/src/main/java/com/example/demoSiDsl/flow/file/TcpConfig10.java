package com.example.demoSiDsl.flow.file;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;

import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
// import lombok.extern.slf4j.Slf4j;
 
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository; 
 
// @Slf4j
@Configuration
@Profile("TcpConfig10")
@EnableJpaRepositories(considerNestedRepositories = true)
public class TcpConfig10 {

  @Repository
  public interface PersonRepository extends JpaRepository<Person, Long> {

  }
  
  @Autowired
  PersonRepository personRepository;

  @Bean
  public IntegrationFlow fileInputFlow() {
    return IntegrationFlows.from(
        //Setting up the inbound adapter for the flow
        Files
            .inboundAdapter(new File("/home/farhad/apps/spring-integration-sample/demo-si-dsl-example"))
            .autoCreateDirectory(true)
            .patternFilter("*.txt"), p -> p.poller(Pollers.fixedDelay(10, TimeUnit.SECONDS)
            .errorChannel(MessageChannels.direct().get())))
        // Transform the file content to string
        .transform(Files.toStringTransformer())
        //Transform the file content to list of lines in the file
        .<String, List<String>>transform(wholeText -> Arrays.asList(wholeText.split(Pattern.quote("\n"))))
        //Split the list to a single person record line
        .split()
        //Transform each line in the file and map to a Person record
        .<String, Person>transform(eachPersonText -> {
          List<String> tokenizedString = Arrays.asList(eachPersonText.split(Pattern.quote("|")));
          try {
            return Person.builder()
                .personId(Long.parseLong(tokenizedString.get(0).trim()))
                .personName(tokenizedString.get(1).trim())
                .personPhoneNumber(tokenizedString.get(2).trim())
                .build();
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        // Save the record to the database.
        .handle((GenericHandler<Person>) (personRecordToSave, headers) -> personRepository
            .save(personRecordToSave))
        .log(Level.INFO)
        .get();
  }
  
  
  @Data
  @Entity
  @Builder
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Person {

    @Id
    private Long personId;
    private String personName;
    private String personPhoneNumber;

  }  
  
}
