package dash.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.transaction.annotation.Transactional;

import dash.dao.LocationDao;
import dash.errorhandling.AppException;
import dash.filters.AppConstants;
import dash.helpers.NullAwareBeanUtilsBean;
import dash.pojo.Location;
import dash.pojo.Task;
import dash.pojo.User;
import dash.security.CustomPermission;
import dash.security.GenericAclController;


public class LocationServiceDbAccessImpl extends ApplicationObjectSupport implements
LocationService {

	@Autowired
	LocationDao locationDao;

	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private GenericAclController<Location> aclController;
	
	@Autowired
	private GenericAclController<Task> taskAclController;
	
	@Autowired
	private TaskService taskService;
	
	/********************* Create related methods implementation ***********************/
	@Override
	public Long createLocation(Location location, String user_name, int ds) throws AppException {

		validateInputForCreation(location);

		//verify existence of resource in the db (feed must be unique)
		Location locationByName = locationDao.getLocationByName(location.getName());
		if (locationByName != null) {
			throw new AppException(
					Response.Status.CONFLICT.getStatusCode(),
					409,
					"Location with locationname already existing in the database with the id "
							+ locationByName.getId(),
							"Please verify that the location is properly generated",
							AppConstants.DASH_POST_URL);
		}

		long locationId = locationDao.createLocation(location, ds);
		location.setId(locationId);
		aclController.createACL(location, ds);
		if (user_name != null)
			aclController.createAce(location, CustomPermission.MANAGER, new PrincipalSid(user_name));
		else
			aclController.createAce(location, CustomPermission.MANAGER);
		return locationId;
	}
	
	@Override
	public void createLocations(List<Location> locations, String user_name, int ds) throws AppException {
		for (Location location : locations) {
			createLocation(location, user_name, ds);
		}
	}

	private void validateInputForCreation(Location location) throws AppException {
		//minimum requirement is location name and description
		if (location.getName() == null || location.getDescription() == null) {
			throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(), 400, "Provided data not sufficient for insertion",
					"Please verify that the locationname and description are properly generated/set",
					AppConstants.DASH_POST_URL);
		}		
	}

	// ******************** Read related methods implementation **********************
	@Override
	public List<Location> getLocations(String orderByInsertionDate,
			Integer numberDaysToLookBack) throws AppException {

		//verify optional parameter numberDaysToLookBack first
		if(numberDaysToLookBack!=null){
			List<Location> recentLocations = locationDao
					.getRecentLocations(numberDaysToLookBack);
			return getLocationsFromEntities(recentLocations);
		}

		if(isOrderByInsertionDateParameterValid(orderByInsertionDate)){
			throw new AppException(
					Response.Status.BAD_REQUEST.getStatusCode(),
					400,
					"Please set either ASC or DESC for the orderByInsertionDate parameter",
					null, AppConstants.DASH_POST_URL);
		}
		List<Location> locations = locationDao.getLocations(orderByInsertionDate);

		return getLocationsFromEntities(locations);
	}
	
	private boolean isOrderByInsertionDateParameterValid(
			String orderByInsertionDate) {
		return orderByInsertionDate!=null
				&& !("ASC".equalsIgnoreCase(orderByInsertionDate) || "DESC".equalsIgnoreCase(orderByInsertionDate));
	}

	private List<Location> getLocationsFromEntities(List<Location> locationEntities) {
		List<Location> response = new ArrayList<Location>();
		for (Location location : locationEntities) {
			response.add(location);
		}

		return response;
	}

	public List<Location> getRecentLocations(int numberOfDaysToLookBack) {
		List<Location> recentLocations = locationDao
				.getRecentLocations(numberOfDaysToLookBack);

		return getLocationsFromEntities(recentLocations);
	}
	
	@Override
	public List<Location> getLocationsByManager(String orderByInsertionDate,
	Integer numberDaysToLookBack) throws AppException {
	
		return getLocations(orderByInsertionDate, numberDaysToLookBack);
	}
	
	@Override
	public Location getLocationById(Long id) throws AppException {
		Location locationById = locationDao.getLocationById(id);
		if (locationById == null) {
			throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
					404,
					"The location you requested with id " + id
					+ " was not found in the database",
					"Verify the existence of the location with the id " + id
					+ " in the database", AppConstants.DASH_POST_URL);
		}

		return locationDao.getLocationById(id);
	}

	/********************* UPDATE-related methods implementation ***********************/
	@Override
	@Transactional
	public void updateFullyLocation(Location location) throws AppException {
				
		Location verifyLocationExistenceById = verifyLocationExistenceById(location
				.getId());
		if (verifyLocationExistenceById == null) {
			throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
					404,
					"The resource you are trying to update does not exist in the database",
					"Please verify existence of data in the database for the id - "
							+ location.getId(),
							AppConstants.DASH_POST_URL);
		}
		copyAllProperties(verifyLocationExistenceById, location);
		locationDao.updateLocation(location);
	}

	private void copyAllProperties(Location verifyLocationExistenceById, Location location) {

		BeanUtilsBean withNull=new BeanUtilsBean();
		try {
			withNull.copyProperty(verifyLocationExistenceById, "name", location.getName());
			withNull.copyProperty(verifyLocationExistenceById, "description", location.getDescription());
		} catch (IllegalAccessException e) {
			logger.debug("debugging info for exception: ", e); 
		} catch (InvocationTargetException e) {
			logger.debug("debugging info for exception: ", e); 
		}

	}

	/********************* DELETE-related methods implementation ***********************/

	@Override
	@Transactional
	public void deleteLocation(Location location) {

		locationDao.deleteLocation(location);
		aclController.deleteACL(location);

	}
	
	@Override
	public Location verifyLocationExistenceById(Long id) {
		Location locationById = locationDao.getLocationById(id);
		if (locationById == null) {
			return null;
		} else {
			return locationById;
		}
	}

	@Override
	@Transactional
	public void updatePartiallyLocation(Location location) throws AppException {
		//do a validation to verify existence of the resource
		Location verifyLocationExistenceById = verifyLocationExistenceById(location.getId());
		if (verifyLocationExistenceById == null) {
			throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
					404,
					"The resource you are trying to update does not exist in the database",
					"Please verify existence of data in the database for the id - "
							+ location.getId(), AppConstants.DASH_POST_URL);
		}
		copyPartialProperties(verifyLocationExistenceById, location);
		locationDao.updateLocation(verifyLocationExistenceById);

	}

	private void copyPartialProperties(Location verifyLocationExistenceById, Location location) {

		BeanUtilsBean notNull=new NullAwareBeanUtilsBean();
		try {
			notNull.copyProperties(verifyLocationExistenceById, location);
		} catch (IllegalAccessException e) {
			logger.debug("debugging info for exception: ", e); 
		} catch (InvocationTargetException e) {
			logger.debug("debugging info for exception: ", e); 
		}

	}

	/**
	 * ACL related methods
	 */
	@Override
	@Transactional
	public void addManager(User user, Location location)throws AppException{
		
		aclController.createAce(location, CustomPermission.MANAGER, new PrincipalSid(user.getUsername()));
		if(aclController.hasPermission(location, CustomPermission.MEMBER, new PrincipalSid(user.getUsername())))	
				aclController.deleteACE(location, CustomPermission.MEMBER, new PrincipalSid(user.getUsername()));
		
	}
	
	@Override
	@Transactional
	public void resetManager(User user, Location location)throws AppException{
		aclController.clearPermission(location, CustomPermission.MANAGER);
		aclController.createAce(location, CustomPermission.MANAGER, new PrincipalSid(user.getUsername()));
		if(aclController.hasPermission(location, CustomPermission.MEMBER, new PrincipalSid(user.getUsername())))	
			aclController.deleteACE(location, CustomPermission.MEMBER, new PrincipalSid(user.getUsername()));
	
	}
	
	//Removes a single manager from a location
	@Override
	@Transactional
	public void deleteManager(User user, Location location)throws AppException{
		aclController.deleteACE(location, CustomPermission.MANAGER, new PrincipalSid(user.getUsername()));
		aclController.createAce(location, CustomPermission.MEMBER, new PrincipalSid(user.getUsername()));
	}

}
