package dash.tran;

import org.springframework.transaction.annotation.Transactional;

import dash.errorhandling.AppException;
import dash.pojo.Class;
import dash.pojo.Group;
import dash.pojo.Location;
import dash.pojo.User;
import dash.service.ClassService;
import dash.service.GroupService;

public class GroupTransactionImpl {
	@Transactional("transactionManagerCHW")
	public void deleteGroup1(Group group, int ds, GroupService groupService) throws AppException {
		groupService.deleteGroup(group, ds);
	}

	@Transactional("transactionManagerVMA")
	public void deleteGroup2(Group group, int ds, GroupService groupService) throws AppException {
		groupService.deleteGroup(group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public long createGroup1(Group group, int ds, GroupService groupService) throws AppException {
		return groupService.createGroup(group, ds);
	}

	@Transactional("transactionManagerVMA")
	public long createGroup2(Group group, int ds, GroupService groupService) throws AppException {
		return groupService.createGroup(group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void updatePartiallyGroup1(Group group, int ds, GroupService groupService) throws AppException {
		groupService.updatePartiallyGroup(group, ds);
	}

	@Transactional("transactionManagerVMA")
	public void updatePartiallyGroup2(Group group, int ds, GroupService groupService) throws AppException {
		groupService.updatePartiallyGroup(group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void addManager1(User user, Group group, int ds, GroupService groupService) throws AppException {
		groupService.addManager(user, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void addManager2(User user, Group group, int ds, GroupService groupService) throws AppException {
		groupService.addManager(user, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void resetManager1(User user, Group group, int ds, GroupService groupService) throws AppException{
		groupService.resetManager(user, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void resetManager2(User user, Group group, int ds, GroupService groupService) throws AppException{
		groupService.resetManager(user, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void deleteManager1(User user, Group group, int ds, GroupService groupService) throws AppException {
		groupService.deleteManager(user, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void deleteManager2(User user, Group group, int ds, GroupService groupService) throws AppException {
		groupService.deleteManager(user, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void addMember1(User user, Group group, int ds, GroupService groupService) throws AppException {
		groupService.addMember(user, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void addMember2(User user, Group group, int ds, GroupService groupService) throws AppException {
		groupService.addMember(user, group, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void deleteMember1(User user, Group group, int ds, GroupService groupService) throws AppException {
		groupService.deleteMember(user, group, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void deleteMember2(User user, Group group, int ds, GroupService groupService) throws AppException {
		groupService.deleteMember(user, group, ds);
	}
}
