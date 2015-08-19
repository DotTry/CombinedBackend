package dash.pojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import dash.errorhandling.AppException;
import dash.filters.AppConstants;
import dash.service.UserService;
import dash.tran.TaskSwitch;
import dash.tran.UserSwitch;

/**
 *
 * Service class that handles REST requests
 *
 * @author plindner
 *
 */
@Component("userResource")
@Path("/users")
public class UsersResource {

	private static final String userPicturePathCHW = AppConstants.APPLICATION_UPLOAD_LOCATION_FOLDER_CHW
			+ "/users";
	
	private static final String userPicturePathVMA = AppConstants.APPLICATION_UPLOAD_LOCATION_FOLDER_VMA
			+ "/users";

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserSwitch userTran;
	/*
	 * *********************************** CREATE
	 * ***********************************
	 */

	/**
	 * Adds a new resource (user) from the given json format (at least username
	 * and password elements are required at the DB level)
	 *
	 * @param user
	 * @return
	 * @throws AppException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response createUser(User user, @QueryParam(value = "ds") int ds) throws AppException {
		Long createUserId = userTran.createUser(user, ds);
		return Response
				.status(Response.Status.CREATED)
				// 201
				.entity("A new user has been created at index")
				.header("Location", String.valueOf(createUserId))
				.header("ObjectId", String.valueOf(createUserId)).build();
	}

	/**
	 * Adds a new user (resource) from "form" (at least title and feed elements
	 * are required at the DB level)
	 *
	 * @param username
	 * @param password
	 * @param firstName
	 * @param lastNameuser
	 * @return
	 * @throws AppException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
	@Produces({ MediaType.TEXT_HTML })
	@Transactional
	public Response createUserFromApplicationFormURLencoded(
			@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("fistName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("city") String city,
			@FormParam("homePhone") String homePhone,
			@FormParam("cellPhone") String cellPhone,
			@FormParam("email") String email,
			@FormParam("picturePath") String picturePath,
			@FormParam("profile_picture_filename") String profile_picture_filename)
			throws AppException {

		User user = new User(username, password, firstName, lastName, city,
				homePhone, cellPhone, email, picturePath,
				profile_picture_filename);

		Long createUserid = userService.createUser(user, 0);

		return Response
				.status(Response.Status.CREATED)
				// 201
				.entity("A new user/resource has been created at /services/users/"
						+ createUserid)
				.header("Location",
						"http://localhost:8888/services/users/"
								+ String.valueOf(createUserid)).build();
	}

	/**
	 * A list of resources (here users) provided in json format will be added to
	 * the database.
	 *
	 * @param users
	 * @return
	 * @throws AppException
	 */
	@POST
	@Path("list")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response createUsers(List<User> users, @QueryParam(value = "ds") int ds) throws AppException {
		userTran.createUsers(users, ds);
		return Response.status(Response.Status.CREATED) // 201
				.entity("List of users was successfully created").build();
	}

	/*
	 * *********************************** READ
	 * ***********************************
	 */
	/**
	 * Returns all users from the database
	 *
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 * @throws AppException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<User> getUsers(
			@QueryParam("orderByInsertionDate") String orderByInsertionDate,
			@QueryParam("numberDaysToLookBack") Integer numberDaysToLookBack)
			throws IOException, AppException {
		List<User> users = userService.getUsers(orderByInsertionDate,
				numberDaysToLookBack);
		return users;
	}

	@GET
	@Path("myUser")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<User> getMyUser(
			@QueryParam("orderByInsertionDate") String orderByInsertionDate,
			@QueryParam("numberDaysToLookBack") Integer numberDaysToLookBack)
			throws IOException, AppException {
		List<User> users = userService.getMyUser(orderByInsertionDate,
				numberDaysToLookBack);
		return users;
	}

	@GET
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getUserById(@PathParam("id") Long id,
			@QueryParam("detailed") boolean detailed) throws IOException,
			AppException {
		User userById = userService.getUserById(id);
		return Response.status(200).entity(new GenericEntity<User>(userById) {
		}).header("Access-Control-Allow-Headers", "X-extra-header")

				.allow("OPTIONS").build();
	}

	@GET
	@Path("myRole")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getMyRole() throws IOException, AppException {

		try {
			List<String> role = userService.getRole(userService.getMyUser(
					"ASC", null).get(0));
			return Response.status(Response.Status.OK).entity(role).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build();
		}

	}

	/*
	 * *********************************** UPDATE
	 * ***********************************
	 */

	/**
	 * The method offers both Creation and Update resource functionality. If
	 * there is no resource yet at the specified location, then a user creation
	 * is executed and if there is then the resource will be full updated.
	 *
	 * @param id
	 * @param user
	 * @return
	 * @throws AppException
	 */
	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response putUserById(@PathParam("id") Long id, User user)
			throws AppException {

		try {
			userService.getUserById(id);
		} catch (AppException ex) {
			
			if (ex.getStatus() == 404) {
			// user not existent yet
			Long createUserId = userService.createUser(user, 0);
			return Response
					.status(Response.Status.CREATED)
					// 201
					.entity("A new user has been created AT THE LOCATION you specified")
					.header("Location", String.valueOf(createUserId)).build();
			}

		}

		// resource is existent and a full update should occur
		userService.updateFullyUser(user, 0);
		return Response
				.status(Response.Status.OK)
				// 200
				.entity("The user " + user.getUsername()
						+ " has been fully updated.").build();

	}

	// PARTIAL update
	@POST
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response partialUpdateUser(@PathParam("id") Long id, User user, @QueryParam(value = "ds") int ds)
			throws AppException {
		user.setId(id);
		userTran.updatePartiallyUser(user, ds);
		return Response.status(Response.Status.OK)
				// 200
				.entity("The user you specified has been successfully updated")
				.build();
	}

	// Changes this users Role
	// Expects role to = {ROLE_USER, ROLE_MODERATOR, ROLE_ADMIN}
	@POST
	@Path("{id}/role")
	public Response updateUserRole(@PathParam("id") Long id,
			@QueryParam("role") String role) throws AppException {

		User user = userService.getUserById(id);
		switch (userService.getRole(user).get(0)) {
		case "ROLE_ROOT":
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Cannot modify root user permissions").build();
		case "ROLE_ADMIN":
			if (userService.getRole(userService.getMyUser("ASC", null).get(0))
					.contains("ROLE_ADMIN")
					|| userService.getRole(
							userService.getMyUser("ASC", null).get(0))
							.contains("ROLE_ROOT")) {
				break;
			} else
				return Response
						.status(401)
						.entity("You do not have required permissions for this"
								+ ".  You must have admin priviliges to modify another admin's role.")
						.build();
		case "ROLE_VISITOR":
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Cannot modify visitor user permissions").build();
		}
		switch (role) {
		case "ROLE_USER":
			userService.setRoleUser(user);
			break;
		case "ROLE_MODERATOR":
			userService.setRoleModerator(user);
			break;
		case "ROLE_ADMIN":
			userService.setRoleAdmin(user);
			break;
		default:
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("The role you specified does not exist").build();
		}
		return Response
				.status(Response.Status.OK)
				.entity("The users role you specified has been successfully updated")
				.build();
	}

	@POST
	@Path("{id}/password")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response passwordReset(@PathParam("id") Long id, User user)
			throws AppException {
		user.setId(id);
		userService.resetPassword(user);
		return Response.status(Response.Status.OK)
				// 200
				.entity("The user you specified has been successfully updated")
				.build();
	}

	/**
	 * Delete a user
	 * 
	 * @param id
	 *            User ID
	 * @return
	 * @throws AppException
	 */
	@DELETE
	@Path("{id}")
	@Produces({ MediaType.TEXT_HTML })
	public Response deleteUser(@PathParam("id") Long id, @QueryParam(value = "ds") int ds) throws AppException {
		User user = new User();
		user.setId(id);
		userTran.deleteUser(user, ds);
		return Response.status(Response.Status.NO_CONTENT)// 204
				.entity("User successfully removed from database").build();
	}

	/*
	 * *********************************** DELETE
	 * ***********************************
	 * 
	 * Currently disabled
	 * 
	 * @DELETE
	 * 
	 * @Path("{id}")
	 * 
	 * @Produces({ MediaType.TEXT_HTML }) public Response
	 * deleteUser(@PathParam("id") Long id) throws AppException { User user= new
	 * User(); user.setId(id); userService.deleteUser(user); return
	 * Response.status(Response.Status.NO_CONTENT)// 204
	 * .entity("User successfully removed from database").build(); }
	 * 
	 * @DELETE
	 * 
	 * @Path("admin")
	 * 
	 * @Produces({ MediaType.TEXT_HTML }) public Response deleteUsers() {
	 * userService.deleteUsers(); return
	 * Response.status(Response.Status.NO_CONTENT)// 204
	 * .entity("All users have been successfully removed").build(); }
	 */

	@POST
	@Path("/upload")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	public Response uploadFile(@QueryParam("id") Long id,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@HeaderParam("Content-Length") final long fileSize)
			throws AppException {

		User user = userService.getUserById(id);

		// TODO: Generate directory if not set
		if (user.getPicture() == null) {
			String fileName = user.getId().toString();
			int hashcode = fileName.hashCode();
			int mask = 255;
			int firstDir = hashcode & mask;
			int secondDir = (hashcode >> 8) & mask;
			StringBuilder path = new StringBuilder(File.separator);
			path.append(String.format("%03d", firstDir));
			path.append(File.separator);
			path.append(String.format("%03d", secondDir));
			path.append(File.separator);
			path.append(fileName);
			user.setPicture(path.toString());
			partialUpdateUser(user.getId(), user, 0);
		}

		if (!userService.getFileNames(user).isEmpty()) {
			List<String> files = userService.getFileNames(user);
			for (String file : files) {
				deleteUpload(user.getId(), file);
			}
		}

		//TODO:Switch statement for datasources
		String uploadedFileLocation = userPicturePathCHW + "/" + user.getPicture()
				+ "/"
				+ fileDetail.getFileName().replaceAll("%20", "_").toLowerCase();
		;
		// save it
		userService.uploadFile(uploadedInputStream, uploadedFileLocation, user);

		String output = "File uploaded to : " + uploadedFileLocation;
		user.setProfile_picture_filename(fileDetail.getFileName());
		userService.updatePartiallyUser(user, 0);
		return Response.status(200).entity(output).build();

	}

	@GET
	@Path("/upload")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getFileNames(@QueryParam("userId") Long id)
			throws AppException {

		User user = userService.getUserById(id);
		JaxbList<String> fileNames = new JaxbList<String>(
				userService.getFileNames(user));
		return Response.status(200).entity(fileNames).build();
	}

	// //Gets a specific file and allows the user to download the pdf
	// @GET
	// @Path("/upload")
	// public Response getFile(@QueryParam("userId") Long id,
	// @QueryParam("fileName") String fileName) throws AppException {
	//
	// User user= userService.getUserById(id);
	//
	// if(user==null){
	// return Response.status(Response.Status.BAD_REQUEST)
	// .entity("Invalid userId, unable to locate user with id: "+id).build();
	// }
	//
	// String uploadedFileLocation =
	// AppConstants.APPLICATION_UPLOAD_LOCATION_FOLDER+user.getPicture()+"/" +
	// fileName;
	//
	//
	// return Response.ok(userService.getUploadFile(uploadedFileLocation, user))
	// .type("application/pdf").build();
	// }

	@DELETE
	@Path("/upload")
	public Response deleteUpload(@QueryParam("userId") Long id,
			@QueryParam("fileName") String fileName) throws AppException {

		User user = userService.getUserById(id);
//TODO:Switch statement for datasources
		String uploadedFileLocation = userPicturePathCHW + "/" + user.getPicture()
				+ "/" + fileName;
		// save it
		userService.deleteUploadFile(uploadedFileLocation, user);

		String output = "File removed from: " + uploadedFileLocation;
		user.setProfile_picture_filename("");
		userService.updatePartiallyUser(user, 0);
		return Response.status(200).entity(output).build();
	}

	public void setuserService(UserService userService) {
		this.userService = userService;
	}

	@XmlRootElement(name = "fileNames")
	public static class JaxbList<T> {
		protected List<T> list;

		public JaxbList() {
		}

		public JaxbList(List<T> list) {
			this.list = list;
		}

		@XmlElement(name = "fileName")
		public List<T> getList() {
			return list;
		}
	}

}
