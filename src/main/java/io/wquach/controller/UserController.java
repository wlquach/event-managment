package io.wquach.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import io.wquach.dao.QueryResultProcessorFactory;
import io.wquach.domain.Event;
import io.wquach.domain.EventBuilder;
import io.wquach.domain.User;
import io.wquach.domain.UserBuilder;
import io.wquach.service.CrudService;

/**
 * Created by wquach on 6/4/17.
 */
@RestController
@RequestMapping(path = "/v1/users")
public class UserController {
    @Autowired
    @Qualifier("jdbcUser")
    QueryResultProcessorFactory queryResultProcessorFactory;

    @Autowired
    CrudService<User> userService;

    @Autowired
    JsonFactory jsonFactory;

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public User addUser(@Valid @RequestBody User user) {
        //if event comes with ID throw
        int id = userService.add(user);

        return UserBuilder.create()
                .id(id)
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    /**
     * Get all the users in the system. For scalability, this method will write the users to the writer as they are being
     * queried from persistence.
     *
     * @param responseWriter the response writer to write the users to
     * @param response       the HttpServletResponse used to set headers
     * @throws IOException if writing to the Writer fails
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public void getAllUsers(Writer responseWriter, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        JsonGenerator jsonGen = jsonFactory.createGenerator(responseWriter);
        ObjectMapper objectMapper = new ObjectMapper();
        jsonGen.setCodec(objectMapper);
        jsonGen.writeStartArray();

        Consumer queryResultProcessor = queryResultProcessorFactory.get(jsonGen);
        userService.getAll(queryResultProcessor);

        jsonGen.writeEndArray();
        jsonGen.flush();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = "application/json")
    public User getSingleUser(@PathVariable int id) {
        return userService.getSingle(id);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity deleteSingleUser(@PathVariable int id) {
        userService.deleteSingle(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity updateEvent(@Valid @RequestBody User user) {
        userService.update(user);
        return ResponseEntity.noContent().build();
    }
}
