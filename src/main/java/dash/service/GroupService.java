package dash.service;

import java.util.List;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import dash.errorhandling.AppException;
import dash.pojo.Group;
import dash.pojo.User;

public interface GroupService {
	/*
	 * ******************** Create related methods **********************
	 * 
	 * Create a new group and set the current user as owner and manager.
	 */
	public Long createGroup(Group group, int ds) throws AppException;

	/*
	 * ******************* Read related methods ********************
	 */
	/**
	 *
	 * @param orderByInsertionDate
	 *            - if set, it represents the order by criteria (ASC or DESC)
	 *            for displaying groups
	 * @param numberDaysToLookBack
	 *            - if set, it represents number of days to look back for
	 *            groups, null
	 * @return list with groups corresponding to search criteria
	 * @throws AppException
	 */

	public List<Group> getGroups(String orderByInsertionDate,
			Integer numberDaysToLookBack) throws AppException;

	@PostFilter("hasPermission(filterObject, 'MEMBER')")
	public List<Group> getGroupsByMembership(String orderByInsertionDate,
			Integer numberDaysToLookBack) throws AppException;

	@PostFilter("hasPermission(filterObject, 'Manager')")
	public List<Group> getGroupsByManager(String orderByInsertionDate,
			Integer numberDaysToLookBack) throws AppException;

	public Group getGroupById(Long id) throws AppException;

	/*
	 * ******************** Update related methods **********************
	 */
	@PreAuthorize("hasPermission(#group, 'MANAGER') or hasRole('ROLE_MODERATOR')")
	public void updateFullyGroup(Group group) throws AppException;

	@PreAuthorize("hasPermission(#group, 'MANAGER') or hasRole('ROLE_MODERATOR')")
	public void updatePartiallyGroup(Group group, int ds) throws AppException;

	/*
	 * ******************** Delete related methods **********************
	 */
	@PreAuthorize("hasPermission(#group, 'MANAGER') or hasRole('ROLE_MODERATOR')")
	public void deleteGroup(Group group, int ds);
	/**
	 * ACL related methods
	 */
	// Adds an additional manager to the group
	@PreAuthorize("hasPermission(#group, 'MANAGER') or hasRole('ROLE_ADMIN')")
	public void addManager(User user, Group group, int ds) throws AppException;

	// Removes all managers and sets new manager to user
	@PreAuthorize("hasRole('ROLE_MODERATOR')")
	public void resetManager(User user, Group group, int ds) throws AppException;

	// Removes a single manager from a group
	@PreAuthorize("hasPermission(#user, 'WRITE') or hasRole('ROLE_MODERATOR')")
	public void deleteManager(User user, Group group, int ds) throws AppException;

	// Adds a member to the group
	@PreAuthorize("hasRole('ROLE_USER')")
	public void addMember(User user, Group group, int ds) throws AppException;

	
	//Removes member
	@PreAuthorize("hasPermission(#user, 'WRITE') or hasRole('ROLE_MODERATOR') or hasPermission(#group, 'MANAGER')")
	public void deleteMember(User user, Group group, int ds) throws AppException;

	/*
	 * ******************** Helper methods **********************
	 */
	public int getNumberOfGroups();

}
