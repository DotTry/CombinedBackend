package dash.service;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.Response;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAclService;
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

public class ClassServiceDbAccessImpl extends ApplicationObjectSupport
		implements ClassService {

	@Autowired
	ClassDao classDao;

	@Autowired
	LocationService locationService;;

	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private GenericAclController<Class> aclController;
	
	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private SimpleMailMessage templateMessage;
	
	/********************* Create related methods implementation ***********************/
	@Override
	public Long createClass(Class clas, Location loc, int ds) throws AppException {

		validateInputForCreation(clas);
		long classId = classDao.createClass(clas, ds);
		clas.setId(classId);
		aclController.createACL(clas, ds);
		aclController.createAce(clas, CustomPermission.MANAGER, 1);
		return classId;
	}

	@Override
	@Transactional
	public void createClasses(List<Class> classes, Location location)
			throws AppException {
		for (Class clas : classes) {
			createClass(clas, location, 1);
		}
	}

	private void validateInputForCreation(Class clas) throws AppException {
		if (clas.getName() == null) {
			throw new AppException(
					Response.Status.BAD_REQUEST.getStatusCode(),
					400,
					"Provided data not sufficient for insertion",
					"Please verify that the classname is properly generated/set",
					AppConstants.DASH_POST_URL);
		}
	}

	// ******************** Read related methods implementation
	// **********************
	@Override
	public List<Class> getClasses(String orderByInsertionDate,
			Integer numberDaysToLookBack) throws AppException {

		// verify optional parameter numberDaysToLookBack first
		if (numberDaysToLookBack != null) {
			List<Class> recentClasses = classDao
					.getRecentClasses(numberDaysToLookBack);
			return getClassesFromEntities(recentClasses);
		}

		if (isOrderByInsertionDateParameterValid(orderByInsertionDate)) {
			throw new AppException(
					Response.Status.BAD_REQUEST.getStatusCode(),
					400,
					"Please set either ASC or DESC for the orderByInsertionDate parameter",
					null, AppConstants.DASH_POST_URL);
		}
		List<Class> classes = classDao.getClasses(orderByInsertionDate);

		return getClassesFromEntities(classes);
	}

	@Override
	public List<Class> getClassesByLocation(Location location) {

		List<Class> classes = classDao.getClassesByLocation(location);
		return getClassesFromEntities(classes);

	}

	private boolean isOrderByInsertionDateParameterValid(
			String orderByInsertionDate) {
		return orderByInsertionDate != null
				&& !("ASC".equalsIgnoreCase(orderByInsertionDate) || "DESC"
						.equalsIgnoreCase(orderByInsertionDate));
	}

	@Override
	public Class getClassById(Long id) throws AppException {
		Class classById = classDao.getClassById(id);
		if (classById == null) {
			throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
					404, "The class you requested with id " + id
							+ " was not found in the database",
					"Verify the existence of the class with the id " + id
							+ " in the database", AppConstants.DASH_POST_URL);
		}

		return classDao.getClassById(id);
	}

	@Override
	public List<Class> getClassesByMembership(String orderByInsertionDate,
			Integer numberDaysToLookBack) throws AppException {

		return getClasses(orderByInsertionDate, numberDaysToLookBack);
	}

	private List<Class> getClassesFromEntities(List<Class> classEntities) {
		List<Class> response = new ArrayList<Class>();
		for (Class clas : classEntities) {
			clas
					.setFinished((clas.getTime() != null) ? (clas
							.getTime().before(new Date()) ? 1 : 0) : 0);
			response.add(clas);
		}

		return response;
	}

	public List<Class> getRecentClasses(int numberOfDaysToLookBack) {
		List<Class> recentClasses = classDao
				.getRecentClasses(numberOfDaysToLookBack);

		return getClassesFromEntities(recentClasses);
	}

	/********************* DELETE-related methods implementation ***********************/
	@Override
	public void deleteClass(Class clas, Location loc, int ds) throws AppException {

		classDao.deleteClass(clas, ds);
		aclController.deleteACL(clas, 1);

	}

	/****************** Update Related Methods ***********************/
	@Override
	public void updatePartiallyClass(Class clas, Location loc, int ds)
			throws AppException {
		try {
			// do a validation to verify existence of the resource
			Class verifyClassExistenceById = getClassById(clas.getId());
			copyPartialProperties(verifyClassExistenceById, clas);
			classDao.updateClass(verifyClassExistenceById, ds);
		} catch (AppException ex) {
			throw new AppException(
					Response.Status.NOT_FOUND.getStatusCode(),
					404,
					"The resource you are trying to update does not exist in the database",
					"Please verify existence of data in the database for the id - "
							+ clas.getId(), AppConstants.DASH_POST_URL);
		}
	}

	private void copyPartialProperties(Class verifyClassExistenceById,
			Class clas) {

		BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
		try {
			notNull.copyProperties(verifyClassExistenceById, clas);
		} catch (IllegalAccessException e) {
			logger.debug("debugging info for exception: ", e);
		} catch (InvocationTargetException e) {
			logger.debug("debugging info for exception: ", e);
		}
	}

	@Override
	@Transactional
	public void addMember(User user, Class clas) throws AppException {
		aclController.createAce(clas, CustomPermission.MEMBER,
				new PrincipalSid(user.getUsername()), 1);
	}

	@Override
	@Transactional
	public void deleteMember(User user, Class clas) throws AppException {
		aclController.deleteACE(clas, CustomPermission.MEMBER,
				new PrincipalSid(user.getUsername()), 1);
	}

	@Override
	public void sendAlertEmail(List<String> membersForClass, Class clas) {
		MimeMessage message = this.mailSender.createMimeMessage();

		MimeMessageHelper helper;
		try {
			helper = new MimeMessageHelper(message, true);
			helper.setFrom("NOREPLY@Housuggest.org");
			helper.setTo("NOREPLY@Housuggest.org");
			String [] memberArray = new String[membersForClass.size()];
			membersForClass.toArray(memberArray);
			helper.setBcc(memberArray);
			helper.setSubject("You have a class coming up soon.");
			String htmlText = generateHtml(clas);
			helper.setText(htmlText, true);
		} catch (MessagingException e) {
			logger.debug("debugging info for exception: ", e);
		}
		try {
			mailSender.send(message);
		} catch (MailException e) {
			// TODO: Do we need a front end error if the sending fails?
			logger.debug("debugging info for exception: ", e); 
		}
	}

	private String generateHtml(Class clas) {
		String html = "<html><body><p> Good Morning! </p><p> This is a friendly reminder that you have an upcoming class in 24 hours. </p>";
		html += "<p>Name: " + clas.getName() + " <br/>";
		if (clas.getTime() != null) {
			SimpleDateFormat formater = new SimpleDateFormat(
					"MMM dd yyyy 'at' hh:mm a");
			String formattedDate = formater.format(clas.getTime());
			html += "Time: " + formattedDate + " <br/>";
		}
		if (clas.getAddress() != null) {
			html += "Address: " + clas.getAddress() + " <br/>";
		}
		html += "</p>";
		Location trainingCenter = null;
		try {
			trainingCenter = locationService.getLocationById(clas
					.getLocation_id());
		} catch (AppException e) {
			logger.debug("debugging info for exception: ", e); 
		}
		html += "<p> Please contact " + trainingCenter.getName()
				+ " if you have any questions or concerns. </p>";
		html += "<p> We hope you enjoy this class! </p><p> CHWApp Support Team </p></body></html>";
		return html;
	}

	@Override
	public List<String> getMembersForClass(Class clas) {
		List<String> members = classDao
				.getMembersForClass(clas);
		return members;
	}

	@Override
	public List<Class> getTodaysClasses() {
		List<Class> classes = classDao.getTodaysClasses();
		return getClassesFromEntities(classes);
	}
}
