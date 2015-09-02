package dash.tran;

import org.springframework.transaction.annotation.Transactional;

import dash.errorhandling.AppException;
import dash.pojo.Group;
import dash.pojo.Post;
import dash.pojo.Task;
import dash.pojo.User;
import dash.service.PostService;
import dash.service.TaskService;

public class TaskTransactionImpl {
	
	@Transactional("transactionManagerCHW")
	public Long createTask1(Task task, Group group, int ds, TaskService taskService) throws AppException {
		return taskService.createTask(task, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public Long createTask2(Task task, Group group, int ds, TaskService taskService) throws AppException {
		return taskService.createTask(task, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void deleteTask1(Task task, Group group, int ds, TaskService taskService) throws AppException {
		taskService.deleteTask(task, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void deleteTask2(Task task, Group group, int ds, TaskService taskService) throws AppException {
		taskService.deleteTask(task, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void updatePartiallyTask1(Task task, Group group, int ds, TaskService taskService) throws AppException {
		taskService.updatePartiallyTask(task, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void updatePartiallyTask2(Task task, Group group, int ds, TaskService taskService) throws AppException {
		taskService.updatePartiallyTask(task, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void addManager1(User user, Task task, Group group, int ds, TaskService taskService)
			throws AppException {
		taskService.addManager(user, task, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void addManager2(User user, Task task, Group group, int ds, TaskService taskService)
			throws AppException {
		taskService.addManager(user, task, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void resetManager1(User user, Task task, int ds, TaskService taskService) throws AppException {
		taskService.resetManager(user, task, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void resetManager2(User user, Task task, int ds, TaskService taskService) throws AppException {
		taskService.resetManager(user, task, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void deleteManager1(User user, Task task, Group group, int ds, TaskService taskService)
			throws AppException {
		taskService.deleteManager(user, task, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void deleteManager2(User user, Task task, Group group, int ds, TaskService taskService)
			throws AppException {
		taskService.deleteManager(user, task, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void addMember1(User user, Task task, int ds, TaskService taskService) throws AppException {
		taskService.addMember(user, task, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void addMember2(User user, Task task, int ds, TaskService taskService) throws AppException {
		taskService.addMember(user, task, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void deleteMember1(User user, Task task, Group group, int ds, TaskService taskService)
			throws AppException {
		taskService.deleteMember(user, task, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void deleteMember2(User user, Task task, Group group, int ds, TaskService taskService)
			throws AppException {
		taskService.deleteMember(user, task, group, ds);
	}
}
