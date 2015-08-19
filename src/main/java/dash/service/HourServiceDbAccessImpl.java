package dash.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dash.dao.HourDao;
import dash.errorhandling.AppException;
import dash.filters.AppConstants;
import dash.helpers.NullAwareBeanUtilsBean;
import dash.pojo.Group;
import dash.pojo.Hour;
import dash.pojo.Task;
import dash.security.CustomPermission;
import dash.security.GenericAclController;

@Component("hourService")
public class HourServiceDbAccessImpl extends ApplicationObjectSupport implements
		HourService {

	@Autowired
	HourDao hourDao;

	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private GenericAclController<Hour> aclController;

	/********************* Create related methods implementation ***********************/
	@Override
	public Long createHour(Hour hour, int ds) throws AppException {
		hour.setPending(true);
		long hourId = hourDao.createHour(hour, ds);
		hour.setId(hourId);
		aclController.createACL(hour, ds);
		aclController.createAce(hour, CustomPermission.READ);
		aclController.createAce(hour, CustomPermission.WRITE);
		aclController.createAce(hour, CustomPermission.DELETE);
		return hourId;
	}

	// ******************** Read related methods implementation **********************
	@Override
	public List<Hour> getHours(int numberOfHours, Long startIndex, boolean onlyPending) throws AppException{
		
		List<Hour> hours = hourDao.getHours(numberOfHours, startIndex, onlyPending, "ASC");

		return getHoursFromEntities(hours);
	}

	@Override
	public List<Hour> getHoursByGroup(int numberOfHours, Long startIndex,
			Group group, boolean onlyPending) throws AppException {

		// verify optional parameter numberDaysToLookBack first
		List<Task> tasksByGroup = taskService.getTasksByGroup(group);
		List<Hour> groupHours= new ArrayList<Hour>();
		for(Task task: tasksByGroup){
			groupHours.addAll( hourDao.getHours(numberOfHours, startIndex, task, onlyPending));
		}

		return getHoursFromEntities(groupHours);
	}

	@Override
	public List<Hour> getHoursByMyUser(int numberOfHours, Long startIndex, boolean onlyPending) throws AppException {
		
		
		List<Hour> hours = hourDao.getHours(numberOfHours, startIndex, onlyPending, "DESC");
		return getHoursFromEntities(hours);

	}

	@Override
	public Hour getHourById(Long id) throws AppException {
		Hour hourById = hourDao.getHourById(id);
		if (hourById == null) {
			throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
					404, "The hour you requested with id " + id
							+ " was not found in the database",
					"Verify the existence of the hour with the id " + id
							+ " in the database", AppConstants.DASH_POST_URL);
		}

		return hourDao.getHourById(id);
	}

	private List<Hour> getHoursFromEntities(List<Hour> hourEntities) {
		List<Hour> response = new ArrayList<Hour>();
		for (Hour hour : hourEntities) {
			response.add(hour);
		}

		return response;
	}



	@Override
	public int getNumberOfHours() {
		int totalNumber = hourDao.getNumberOfHours();

		return totalNumber;

	}

	/********************* UPDATE-related methods implementation ***********************/
	@Override
	@Transactional
	public void updateFullyHour(Hour hour) throws AppException {
		Hour verifyHourExistenceById = getHourById(hour
				.getId());
		
		copyAllProperties(verifyHourExistenceById, hour);
		verifyHourExistenceById.setPending(true);
		verifyHourExistenceById.setApproved(false);
		hourDao.updateHour(verifyHourExistenceById, 0);
	}

	@Override
	@Transactional
	public void updateFullyHour(Hour hour, Group group) throws AppException {
		updateFullyHour(hour);
	}

	private void copyAllProperties(Hour verifyHourExistenceById, Hour hour) {

		BeanUtilsBean withNull = new BeanUtilsBean();
		try {
			withNull.copyProperty(verifyHourExistenceById, "title",
					hour.getTitle());
			withNull.copyProperty(verifyHourExistenceById, "start_time",
					hour.getStart_time());
			withNull.copyProperty(verifyHourExistenceById, "end_time",
					hour.getEnd_time());
			withNull.copyProperty(verifyHourExistenceById, "duration",
					hour.getDuration());

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	/********************* DELETE-related methods implementation ***********************/

	@Override
	public void deleteHour(Hour hour, int ds) {

		hourDao.deleteHourById(hour, ds);
		aclController.deleteACL(hour);

	}

	@Override
	@Transactional
	public void updatePartiallyHour(Hour hour) throws AppException {
		//do a validation to verify existence of the resource
		Hour verifyHourExistenceById = getHourById(hour.getId());
		
		copyPartialProperties(verifyHourExistenceById, hour);
		verifyHourExistenceById.setApproved(false);
		verifyHourExistenceById.setPending(true);
		hourDao.updateHour(verifyHourExistenceById, 0);


	}

	@Override
	@Transactional
	public void updatePartiallyHour(Hour hour, Group group) throws AppException {
		updatePartiallyHour(hour);
	}

	private void copyPartialProperties(Hour verifyHourExistenceById, Hour hour) {

		BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
		try {
			notNull.copyProperties(verifyHourExistenceById, hour);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void approveHour(Hour hour, boolean approved, int ds) throws AppException {

		hour.setApproved(approved);
		hour.setPending(false);
		hourDao.updateHour(hour, ds);
		
		
	}

	@Override
	@Transactional
	public void approveHour(Hour hour, Group group, boolean approved)
			throws AppException {
		approveHour(hour, approved, 0);
	}
	
	/*----------------------------------------------------------
	 * The Following Methods are to manipulate uploaded files
	 * ------------------------------------------------------------
	 */

	public void uploadFile(InputStream uploadedInputStream,
			String uploadedFileLocation, Hour hour) throws AppException {

		try {
			File file = new File(uploadedFileLocation);
			file.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			throw new AppException(
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), 500,
					"Could not upload file due to IOException", "\n\n"
							+ e.getMessage(), AppConstants.DASH_POST_URL);
		}

	}

	@Override
	public void deleteUploadFile(String uploadedFileLocation, Hour hour)
			throws AppException {
		Path path = Paths.get(uploadedFileLocation);
		try {
			Files.delete(path);
		} catch (NoSuchFileException x) {
			x.printStackTrace();
			throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
					404, "NoSuchFileException thrown, Operation unsuccesful.",
					"Please ensure the file you are attempting to"
							+ " delete exists at " + path + ".",
					AppConstants.DASH_POST_URL);

		} catch (DirectoryNotEmptyException x) {
			x.printStackTrace();
			throw new AppException(
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					404,
					"DirectoryNotEmptyException thrown, operation unsuccesful.",
					"This method should not attempt to delete,"
							+ " This should be considered a very serious error. Occured at "
							+ path + ".", AppConstants.DASH_POST_URL);
		} catch (IOException x) {
			x.printStackTrace();
			throw new AppException(
					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					500,
					"IOException thrown and the designated file was not deleted.",
					" Permission problems occured at " + path + ".",
					AppConstants.DASH_POST_URL);
		}

	}

	@Override
	public List<String> getFileNames(Hour hour) {
		List<String> results = new ArrayList<String>();
		
		//TODO:Switch statement for datasources
		File[] files = new File(AppConstants.APPLICATION_UPLOAD_LOCATION_FOLDER_CHW
				+ "/" + hour.getPicturePath()).listFiles();
		// If this pathname does not denote a directory, then listFiles()
		// returns null.

		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					results.add(file.getName());
				}
			}
		}
		return results;
	}

}
