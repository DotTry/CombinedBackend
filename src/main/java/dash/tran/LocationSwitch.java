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
import dash.pojo.Location;
import dash.pojo.User;
import dash.security.CustomPermission;
import dash.security.GenericAclController;
import dash.service.ClassServiceDbAccessImpl;
import dash.service.LocationService;
import dash.service.ClassService;

public class LocationSwitch extends ApplicationObjectSupport{

	@Autowired
	private LocationService locationService;

	@Autowired
	private LocationTransactionImpl transaction;
	/*********************
	 * Create related methods implementation
	 ***********************/

	public Long createLocation(Location location, String user_name, int ds) throws AppException {

		switch (ds){ 
		case 1:
			transaction.createLocation1(location, user_name, ds, locationService);
		case 2:
			transaction.createLocation2(location, user_name, ds, locationService);
		default:
			return (long)00;
		}
		
	}
	
	public void addManager(User user, Location location, int ds)throws AppException{
		switch (ds){ 
		case 1:
			transaction.addManager1(user, location, ds, locationService);
		case 2:
			transaction.addManager2(user, location, ds, locationService);
		default:
		}
	}
	
	public void resetManager(User user, Location location, int ds)throws AppException{
		switch (ds){ 
		case 1:
			transaction.resetManager1(user, location, ds, locationService);
		case 2:
			transaction.resetManager2(user, location, ds, locationService);
		default:
		}
	}
	
	public void deleteManager(User user, Location location, int ds)throws AppException{
		switch (ds){ 
		case 1:
			transaction.deleteManager1(user, location, ds, locationService);
		case 2:
			transaction.deleteManager2(user, location, ds, locationService);
		default:
		}
	}
	
}
