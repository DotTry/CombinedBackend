package dash.service;

import java.util.List;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import dash.errorhandling.AppException;
import dash.pojo.Location;
import dash.pojo.User;

/**
 * @Author plindner
 */
public interface LocationService {
	/*
	 * ******************** Create related methods **********************
	 *
	 *Create a new location. If user is set, it's set this user as owner and manager, otherwise Admin will be the owner.
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Long createLocation(Location location, String user_name, int ds) throws AppException;

	/*
	 * Create multiple locations as ADMIN.
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void createLocations(List<Location> locations, String user_name, int ds) throws AppException;
	
	/*
	 * ******************* Read related methods ********************
	 */
	/**
	 *
	 * @param orderByInsertionDate
	 *            - if set, it represents the order by criteria (ASC or DESC)
	 *            for displaying locations
	 * @param numberDaysToLookBack
	 *            - if set, it represents number of days to look back for locations,
	 *            null
	 * @return list with locations corresponding to search criteria
	 * @throws AppException
	 */
	
	public List<Location> getLocations(String orderByInsertionDate,
			Integer numberDaysToLookBack) throws AppException;

	@PostFilter("hasPermission(filterObject, 'Manager')")
	public List<Location> getLocationsByManager(String orderByInsertionDate,
			Integer numberDaysToLookBack) throws AppException;
	
	public Location getLocationById(Long id) throws AppException;
	
	public Location verifyLocationExistenceById(Long id);
	
	/*
	 * ******************** Update related methods **********************
	 */
	@PreAuthorize("hasPermission(#location, 'MANAGER') or hasRole('ROLE_MODERATOR')")
	public void updateFullyLocation(Location location) throws AppException;

	@PreAuthorize("hasPermission(#location, 'MANAGER') or hasRole('ROLE_MODERATOR')")
	public void updatePartiallyLocation(Location location) throws AppException;

	/*
	 * ******************** Delete related methods **********************
	 */
	@PreAuthorize("hasPermission(#location, 'MANAGER') or hasRole('ROLE_MODERATOR')")
	public void deleteLocation(Location location);
	
	/**
	 * ACL related methods
	 */
	// Adds an additional manager to the location
	@PreAuthorize("hasPermission(#location, 'MANAGER') or hasRole('ROLE_ADMIN')")
	public void addManager(User user, Location location, int ds)throws AppException;
	
	//Removes all managers and sets new manager to user
	@PreAuthorize("hasRole('ROLE_MODERATOR')")
	public void resetManager(User user, Location location, int ds)throws AppException;
	
	//Removes a single manager from a location
	@PreAuthorize("hasPermission(#user, 'WRITE') or hasRole('ROLE_MODERATOR')")
	public void deleteManager(User user, Location location, int ds)throws AppException;
	
}
