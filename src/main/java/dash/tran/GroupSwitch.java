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
import dash.pojo.User;
import dash.security.CustomPermission;
import dash.security.GenericAclController;
import dash.service.ClassServiceDbAccessImpl;
import dash.service.GroupService;
import dash.service.LocationService;
import dash.service.ClassService;

public class GroupSwitch extends ApplicationObjectSupport{

	@Autowired
	private GroupService groupService;

	@Autowired
	private GroupTransactionImpl transaction;
	
	public void deleteGroup(Group group, int ds) throws AppException {
		switch (ds){ 
		case 1:
			transaction.deleteGroup1(group, ds, groupService);
			break;
		case 2:
			transaction.deleteGroup2(group, ds, groupService);
			break;
		default:
		};

	}
	
	public Long createGroup(Group group, int ds) throws AppException {
		switch (ds){ 
		case 1:
			return transaction.createGroup1(group, ds, groupService);
		case 2:
			return transaction.createGroup2(group, ds, groupService);
		default:
			return (long)00;
		}
		
	}
	
	public void updatePartiallyGroup(Group group, int ds) throws AppException {
		switch (ds){ 
		case 1:
			transaction.updatePartiallyGroup1(group, ds, groupService);
			break;
		case 2:
			transaction.updatePartiallyGroup2(group, ds, groupService);
			break;
		default:
			
		}
	}
	
	public void addManager(User user, Group group, int ds) throws AppException {
		switch (ds){ 
		case 1:
			transaction.addManager1(user, group, ds, groupService);
			break;
		case 2:
			transaction.addManager2(user, group, ds, groupService);
			break;
		default:
		}
	}

	public void resetManager(User user, Group group, int ds)
			throws AppException {
		switch (ds){ 
		case 1:
			transaction.resetManager1(user, group, ds, groupService);
			break;
		case 2:
			transaction.resetManager2(user, group, ds, groupService);
			break;
		default:
		}
	}
	
	public void deleteManager(User user, Group group, int ds) throws AppException {
		switch (ds){ 
		case 1:
			transaction.deleteManager1(user, group, ds, groupService);
			break;
		case 2:
			transaction.deleteManager2(user, group, ds, groupService);
			break;
		default:
		}
	}
	
	public void addMember(User user, Group group, int ds) throws AppException {
		switch (ds){ 
		case 1:
			transaction.addMember1(user, group, ds, groupService);
			break;
		case 2:
			transaction.addMember2(user, group, ds, groupService);
			break;
		default:
		}
	}
	
	public void deleteMember(User user, Group group, int ds) throws AppException {
		switch (ds){ 
		case 1:
			transaction.deleteMember1(user, group, ds, groupService);
			break;
		case 2:
			transaction.deleteMember2(user, group, ds, groupService);
			break;
		default:
		}
	}
}
