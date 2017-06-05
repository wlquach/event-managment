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
import io.wquach.service.CrudService;

/**
 * Created by wquach on 6/3/17.
 */
@RestController
@RequestMapping(path = "/v1/events")
public class EventController {
    @Autowired
    @Qualifier("jdbcEvent")
    QueryResultProcessorFactory queryResultProcessorFactory;

    @Autowired
    CrudService<Event> eventService;

    @Autowired
    JsonFactory jsonFactory;

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public Event addEvent(@Valid @RequestBody Event event) {
        //if event comes with ID throw
        int id = eventService.add(event);

        return EventBuilder.create()
                .id(id)
                .title(event.getTitle())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .build();
    }

    /**
     * Get all the events in the system. For scalability, this method will write the events to the writer as they are being
     * queried from persistence.
     *
     * @param responseWriter the response writer to write the events to
     * @param response       the HttpServletResponse used to set headers
     * @throws IOException if writing to the Writer fails
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public void getAllEvents(Writer responseWriter, HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        JsonGenerator jsonGen = jsonFactory.createGenerator(responseWriter);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jsonGen.setCodec(objectMapper);
        jsonGen.writeStartArray();

        Consumer queryResultProcessor = queryResultProcessorFactory.get(jsonGen);
        eventService.getAll(queryResultProcessor);

        jsonGen.writeEndArray();
        jsonGen.flush();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = "application/json")
    public Event getSingleEvent(@PathVariable int id) {
        return eventService.getSingle(id);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity deleteSingleEvent(@PathVariable int id) {
        eventService.deleteSingle(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity updateEvent(@Valid @RequestBody Event event) {
        eventService.update(event);
        return ResponseEntity.noContent().build();
    }
}
