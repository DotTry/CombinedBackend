package dash.service;

import java.util.List;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import dash.errorhandling.AppException;
import dash.pojo.Group;
import dash.pojo.Task;
import dash.pojo.User;

public interface TaskService {
	/*
	 * ******************** Create related methods **********************
	 */
	
	/**
	 * Create a new task and set the current user as owner and manager.
	 * @param task
	 * @param group
	 * @return
	 * @throws AppException
	 */
	@PreAuthorize("hasPermission(#group, 'member') or hasPermission(#group, 'MANAGER') or hasRole('ROLE_MODERATOR')")
	public Long createTask(Task task, Group group, int ds) throws AppException;

	/*
	 * ******************* Read related methods ********************
	 */
	/**
	 *
	 * @param orderByInsertionDate
	 *            - if set, it represents the order by criteria (ASC or DESC)
	 *            for displaying tasks
	 * @param numberDaysToLookBack
	 *            - if set, it represents number of days to look back for tasks,
	 *            null
	 * @param group
	 * 			- the group of by which to search for tasks
	 * @return list with tasks corresponding to search criteria
	 * @throws AppException
	 */
	
	public List<Task> getTasks(String orderByInsertionDate,
			Integer numberDaysToLookBack, boolean completedOnly) throws AppException;

	@PostFilter("hasPermission(filterObject, 'MEMBER')")
	public List<Task> getTasksByMembership(String orderByInsertionDate,
			Integer numberDaysToLookBack, boolean completedOnly) throws AppException;
	
	@PostFilter("hasPermission(filterObject, 'Manager')")
	public List<Task> getTasksByManager(String orderByInsertionDate,
			Integer numberDaysToLookBack, boolean completedOnly) throws AppException;
	
	public List<Task> getTasksByGroup( Group group) throws AppException;
	
	public Task getTaskById(Long id) throws AppException;

	/*
	 * ******************** Update related methods **********************
	 */
	@PreAuthorize("hasPermission(#task, 'MANAGER') or hasPermission(#group, 'manager') "
			+ "or hasRole('ROLE_MODERATOR')")
	public void updateFullyTask(Task task, Group group) throws AppException;

	@PreAuthorize("hasPermission(#task, 'MANAGER') or hasPermission(#group, 'manager') "
			+ "or hasRole('ROLE_MODERATOR')")
	public void updatePartiallyTask(Task task, Group group, int ds) throws AppException;

	/*
	 * ******************** Delete related methods **********************
	 */
	@PreAuthorize("hasPermission(#task, 'MANAGER') or hasPermission(#group, 'manager') "
			+ "or hasRole('ROLE_MODERATOR')")
	public void deleteTask(Task task, Group group, int ds) throws AppException;
	
	/**
	 * *******************  ACL related methods  ****************************
	 * TODO: Rework permissions for 
	 */
	// Adds an additional manager to the task
	@PreAuthorize("hasPermission(#task, 'MANAGER') or hasPermission(#group, 'manager') "
			+ "or hasRole('ROLE_ADMIN')")
	public void addManager(User user, Task task, Group group, int ds) throws AppException;
	
	//Removes all managers and sets new manager to user
	@PreAuthorize("hasRole('ROLE_MODERATOR')")
	public void resetManager(User user, Task task, int ds) throws AppException;
	
	//Removes a single manager from a task
	@PreAuthorize("hasPermission(#user, 'WRITE')  or hasPermission(#group, 'manager') "
			+ "or hasRole('ROLE_MODERATOR')")
	public void deleteManager(User user, Task task, Group group, int ds) throws AppException;
	
	//Adds a member to the task
	@PreAuthorize("hasRole('ROLE_USER')")
	public void addMember(User user, Task task, int ds) throws AppException;
	
	//Removes member
	@PreAuthorize("hasPermission(#user, 'WRITE') or hasPermission(#group, 'manager') "
			+ "or hasRole('ROLE_MODERATOR') or hasPermission(#task, 'MANAGER')")
	public void deleteMember(User user, Task task, Group group, int ds) throws AppException;	

	/*
	 * ******************** Helper methods **********************
	 */
	public int getNumberOfTasks();

}
