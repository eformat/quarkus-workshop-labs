package org.acme.people.rest;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.acme.people.model.DataTable;
import org.acme.people.model.EyeColor;
import org.acme.people.model.Person;
import org.acme.people.model.StarWarsPerson;
import org.acme.people.service.StarWarsService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.vertx.axle.core.eventbus.EventBus;
import io.vertx.axle.core.eventbus.Message;

@Path("/person")
@ApplicationScoped
public class PersonResource {

    @Inject EventBus bus;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getAll() {
        return Person.listAll();
    }

    @GET
    @Path("/eyes/{color}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> findByColor(@PathParam(value = "color") EyeColor color) {
        return Person.findByColor(color);
    }

    @GET
    @Path("/birth/before/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Finds people born before a specific year",
            description = "Search the people database and return a list of people born before the specified year")
    @APIResponses(value = {
            @APIResponse(responseCode = "200",
                    description = "The list of people born before the specified year",
                    content = @Content(schema = @Schema(implementation = Person.class))),
            @APIResponse(responseCode = "500", description = "Something bad happened")})
    public List<Person> getBeforeYear(
            @Parameter(description = "Cutoff year for searching for people", required = true,
                    name = "year") @PathParam(value = "year") int year) {
        return Person.getBeforeYear(year);
    }

    @GET
    @Path("/datatable")
    @Produces(MediaType.APPLICATION_JSON)
    public DataTable datatable(@QueryParam(value = "draw") int draw,
            @QueryParam(value = "start") int start, @QueryParam(value = "length") int length,
            @QueryParam(value = "search[value]") String searchVal) {
        // TODO: Begin result
        DataTable result = new DataTable();
        result.setDraw(draw);

        // TODO: Filter based on search
        PanacheQuery<Person> filteredPeople;

        if (searchVal != null && !searchVal.isEmpty()) {
            filteredPeople = Person.<Person>find("name like :search",
                    Parameters.with("search", "%" + searchVal + "%"));
        } else {
            filteredPeople = Person.findAll();
        }
        // TODO: Page and return
        int page_number = start / length;
        filteredPeople.page(page_number, length);

        result.setRecordsFiltered(filteredPeople.count());
        result.setData(filteredPeople.list());
        result.setRecordsTotal(Person.count());

        return result;
    }

    @POST
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<String> addPerson(@PathParam("name") String name) {
        return bus.<String>send("add-person", name) 
          .thenApply(Message::body); 
    }

    @GET
    @Path("/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Person byName(@PathParam("name") String name) {
        return Person.find("name", name).firstResult();
    }

    @Inject
    @RestClient
    StarWarsService swService; 
    
    @GET
    @Path("/swpeople")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StarWarsPerson> getCharacters() {
        return IntStream.range(1, 6) 
            .mapToObj(swService::getPerson)  
            .collect(Collectors.toList());  
    }    

}
