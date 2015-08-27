package dash.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dash.dao.MessageDao;
import dash.errorhandling.AppException;
import dash.filters.AppConstants;
import dash.helpers.NullAwareBeanUtilsBean;
import dash.pojo.Message;
import dash.pojo.Task;
import dash.security.CustomPermission;
import dash.security.GenericAclController;

/*
 * Message Service DB Access Impl
 * @Author CarlSteven
 */
@Component("messageService")
public class MessageServiceDbAccessImpl extends ApplicationObjectSupport
		implements MessageService {

	@Autowired
	MessageDao messageDao;

	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private GenericAclController<Message> aclController;

	/********************* Create related methods implementation ***********************/
	@Override
	public Long createMessage(Message message, Task task, int ds) throws AppException {
		long messageId = messageDao.createMessage(message, ds);
		message.setId(messageId);
		aclController.createACL(message, ds);
		aclController.createAce(message, CustomPermission.READ, ds);
		aclController.createAce(message, CustomPermission.WRITE, ds);
		aclController.createAce(message, CustomPermission.DELETE, ds);
		return messageId;
	}

	// Inactive
	@Override
	@Transactional
	public void createMessages(List<Message> messages) throws AppException {

	}

	// ******************** Read related methods implementation
	// **********************
	@Override
	public List<Message> getMessagesByTask(int numberOfPosts, Long startIndex, Task task) throws AppException {

		//verify optional parameter numberDaysToLookBack first
		
		List<Message> messages = messageDao.getMessages(numberOfPosts, startIndex, task);
		return getMessagesFromEntities(messages);
	}

	@Override
	public Message getMessageById(Long id) throws AppException {
		Message messageById = messageDao.getMessageById(id);
		if (messageById == null) {
			throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
					404, "The post you requested with id " + id
							+ " was not found in the database",
					"Verify the existence of the post with the id " + id
							+ " in the database", AppConstants.DASH_POST_URL);
		}

		return messageDao.getMessageById(id);
	}

	private List<Message> getMessagesFromEntities(List<Message> groupPosts) {
		List<Message> response = new ArrayList<Message>();
		for (Message post : groupPosts) {
			response.add(post);
		}

		return response;
	}
	
	@Override
	public int getNumberOfMessages() {
		int totalNumber = messageDao.getNumberOfMessages();

		return totalNumber;

	}

	/********************* UPDATE-related methods implementation ***********************/
	@Override
	public void updateFullyMessage(Message message, int ds) throws AppException {
		// do a validation to verify FULL update with PUT
		if (isFullUpdate(message)) {
			throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
					400, "Please specify all properties for Full UPDATE",
					"required properties - name, description",
					AppConstants.DASH_POST_URL);
		}

		//verify whether message exists
		getMessageById(message.getId());
		messageDao.updateMessage(message, ds);
	}

	/**
	 * Verifies the "completeness" of post resource sent over the wire
	 *
	 * @param Post
	 * @return
	 */
	private boolean isFullUpdate(Message post) {
		return post.getId() == null || post.getContent() == null;
	}

	/********************* DELETE-related methods implementation ***********************/

	@Override
	public void deleteMessage(Message message, int ds) {

		messageDao.deleteMessageById(message, ds);
		aclController.deleteACL(message, ds);

	}

	@Override
	public void updatePartiallyMessage(Message message, Task task, int ds) throws AppException {
		//do a validation to verify existence of the resource
		Message verifyMessageExistenceById = getMessageById(message.getId());
		
		if(verifyMessageExistenceById.getSender_id() != message.getSender_id()) {

			throw new AppException(Response.Status.FORBIDDEN.getStatusCode(),
					404, "Not allowed to change sender_id in the database.", ""
							+ message.getId(), AppConstants.DASH_POST_URL);
		}
		copyPartialProperties(verifyMessageExistenceById, message);
		messageDao.updateMessage(verifyMessageExistenceById, ds);
	}

	private void copyPartialProperties(Message verifyPostExistenceById,
			Message post) {

		BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
		try {
			notNull.copyProperties(verifyPostExistenceById, post);
		} catch (IllegalAccessException e) {
			logger.debug("debugging info for exception: ", e); 
		} catch (InvocationTargetException e) {
			logger.debug("debugging info for exception: ", e); 
		}

	}

}
