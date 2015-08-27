package dash.pojo;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dash.errorhandling.AppException;
import dash.service.LocationService;
import dash.service.UserService;
import dash.tran.ClassSwitch;
import dash.tran.LocationSwitch;

@Component
@Path("/locations")
public class LocationResource {

	@Autowired
	private LocationService locationService;
	
	@Autowired 
	private UserService userService;
	
	@Autowired
	private LocationSwitch locationTran;
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response createLocation(Location location, @QueryParam("userName") String user_name, @QueryParam(value = "ds") int ds) throws AppException {
		Long createLocationId = locationTran.createLocation(location, user_name, ds);
		return Response.status(Response.Status.CREATED)
				// 201
				.entity("A new location has been created")
				.header("Location", String.valueOf(createLocationId))
		         .header("ObjectId", String.valueOf(createLocationId)).build();
	}
	
	@POST
	@Path("list")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response createLocations(List<Location> locations, @QueryParam("userName") String user_name, @QueryParam(value = "ds") int ds) throws AppException {
		locationService.createLocations(locations, user_name, ds);
		return Response.status(Response.Status.CREATED) 
				.entity("List of clocations was successfully created").build();
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<Location> getLocations(
			@QueryParam("orderByInsertionDate") String orderByInsertionDate,
			@QueryParam("numberDaysToLookBack") Integer numberDaysToLookBack)
					throws IOException,	AppException {
		List<Location> locations = locationService.getLocations(
				orderByInsertionDate, numberDaysToLookBack);
		return locations;
	}
	
	@GET
	@Path("byManager")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<Location> getLocationsByManager(
			@QueryParam("orderByInsertionDate") String orderByInsertionDate,
			@QueryParam("numberDaysToLookBack") Integer numberDaysToLookBack)
					throws IOException,	AppException {
		List<Location> locations = locationService.getLocationsByManager(
				orderByInsertionDate, numberDaysToLookBack);
		return locations;
	}

	@GET
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getLocationById(@PathParam("id") Long id)
					throws IOException,	AppException {
		Location locationById = locationService.getLocationById(id);
		return Response
				.status(Response.Status.OK)
				.entity(new GenericEntity<Location>(locationById) {
				})
						.header("Access-Control-Allow-Headers", "X-extra-header")
						.allow("OPTIONS").build();
	}
	
	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response putLocationById(@PathParam("id") Long id, Location location, @QueryParam("userName") String user_name)
			throws AppException {

		Location locationById = locationService.verifyLocationExistenceById(id);

		if (locationById == null) {
			// resource not existent yet, and should be created under the
			// specified URI
			Long createLocationId = locationService.createLocation(location, user_name, 00);
			return Response
					.status(Response.Status.CREATED)
					// 201
					.entity("A new location has been created AT THE LOCATION you specified")
					.header("Location",
							"../locations/"
									+ String.valueOf(createLocationId)).build();
		} else {
			// resource is existent and a full update should occur
			locationService.updateFullyLocation(location);
			return Response
					.status(Response.Status.OK)
					// 200
					.entity("The location you specified has been fully updated created AT THE LOCATION you specified")
					.header("Location",
							"../locations/"
									+ String.valueOf(id)).build();
		}
	}

	// PARTIAL update
	@POST
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response partialUpdateLocationp(@PathParam("id") Long id, Location location)
			throws AppException {
		location.setId(id);
		locationService.updatePartiallyLocation(location);
		return Response
				.status(Response.Status.OK)
				.entity("The location you specified has been successfully updated")
				.build();
	}

	/*
	 * *********************************** DELETE ***********************************
	 */
	@DELETE
	@Path("{id}")
	@Produces({ MediaType.TEXT_HTML })
	public Response deleteLocation(@PathParam("id") Long id)
			throws AppException {
		Location location= new Location();
		location.setId(id);
		locationService.deleteLocation(location);
		return Response.status(Response.Status.NO_CONTENT)// 204
				.entity("Location successfully removed from database").build();
	}
	
	@PUT
	@Path("{id}/MANAGER/{user}")
	@Produces({MediaType.TEXT_HTML})
	public Response resetManager(@PathParam("user") Long userId, @PathParam("id") Long id) throws AppException{
		User user= userService.getUserById(userId);
		Location location= new Location();
		location.setId(id);
		locationService.resetManager(user, location);
		return Response.status(Response.Status.OK).entity("MANAGER RESET: User "+user.getUsername()
				+" set as sole MANAGER for location "+ location.getId()).build();
	}
	
	@POST
	@Path("{id}/MANAGER/{user}")
	@Produces({MediaType.TEXT_HTML})
	public Response addManager(@PathParam("user") Long userId, @PathParam("id") Long id) throws AppException{
		User user= userService.getUserById(userId);
		Location location= new Location();
		location.setId(id);
		locationService.addManager(user, location);
		return Response.status(Response.Status.OK).entity("MANAGER ADDED: User "+user.getUsername()
				+" added as a MANAGER for location "+ location.getId()).build();
	}
	
	@DELETE
	@Path("{id}/MANAGER/{user}")
	@Produces({MediaType.TEXT_HTML})
	public Response deleteManager(@PathParam("user") Long userId, @PathParam("id") Long id) throws AppException{
		User user= userService.getUserById(userId);
		Location location= new Location();
		location.setId(id);
		locationService.deleteManager(user, location);
		return Response.status(Response.Status.OK).entity("MANAGER DELETED: User "+user.getUsername()
				+" removed as MANAGER for location "+ location.getId()).build();
	}
	
}
