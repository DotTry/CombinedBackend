package dash.tran;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import dash.dao.ClassDao;
import dash.errorhandling.AppException;
import dash.filters.AppConstants;
import dash.helpers.NullAwareBeanUtilsBean;
import dash.pojo.Class;
import dash.pojo.Group;
import dash.pojo.Location;
import dash.pojo.Task;
import dash.pojo.User;
import dash.security.CustomPermission;
import dash.security.GenericAclController;
import dash.service.ClassServiceDbAccessImpl;
import dash.service.LocationService;
import dash.service.TaskService;
import dash.service.ClassService;

public class TaskSwitch extends ApplicationObjectSupport{

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskTransactionImpl transaction;
	/*********************
	 * Create related methods implementation
	 * @throws AppException 
	 ***********************/
	
	public Long createTask(Task task, Group group, int ds) throws AppException {
		switch (ds){ 
		case 1:
			return transaction.createTask1(task, group, ds, taskService);
		case 2:
			return transaction.createTask2(task, group, ds, taskService);
		default:
			return (long) 0;
		}
	}
	
	public void deleteTask(Task task, Group group, int ds) throws AppException {
		switch(ds){
		case 1:
			transaction.deleteTask1(task, group, ds, taskService);
		case 2:
			transaction.deleteTask2(task, group, ds, taskService);
		default:
		}
	}
	
	public void updatePartiallyTask(Task task, Group group, int ds) throws AppException {
		switch(ds){
		case 1:
			transaction.updatePartiallyTask1(task, group, ds, taskService);
		case 2:
			transaction.updatePartiallyTask2(task, group, ds, taskService);
		default:
		}
	}
	
	public void addManager(User user, Task task, Group group, int ds)
			throws AppException {
		switch(ds){
		case 1:
			transaction.addManager1(user, task, group, ds, taskService);
		case 2:
			transaction.addManager2(user, task, group, ds, taskService);
		default:
		}
	}
	
	public void resetManager(User user, Task task, int ds) throws AppException {
		switch(ds){
		case 1:
			transaction.resetManager1(user, task, ds, taskService);
		case 2:
			transaction.resetManager2(user, task, ds, taskService);
		default:
		}
	}
	
	public void deleteManager(User user, Task task, Group group, int ds)
			throws AppException {
		switch(ds){
		case 1:
			transaction.deleteManager1(user, task, group, ds, taskService);
		case 2:
			transaction.deleteManager2(user, task, group, ds, taskService);
		default:
		}
	}
	
	public void addMember(User user, Task task, int ds) throws AppException {
		switch(ds){
		case 1:
			transaction.addMember1(user, task, ds, taskService);
		case 2:
			transaction.addMember2(user, task, ds, taskService);
		default:
		}
	}
	
	public void deleteMember(User user, Task task, Group group, int ds)
			throws AppException {
		switch(ds){
		case 1:
			transaction.deleteMember1(user, task, group, ds, taskService);
		case 2:
			transaction.deleteMember2(user, task, group, ds, taskService);
		default:
		}
	}
}
