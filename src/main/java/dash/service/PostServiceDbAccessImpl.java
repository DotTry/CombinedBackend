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

import dash.dao.PostDao;
import dash.errorhandling.AppException;
import dash.filters.AppConstants;
import dash.helpers.NullAwareBeanUtilsBean;
import dash.pojo.Group;
import dash.pojo.Post;
import dash.security.CustomPermission;
import dash.security.GenericAclController;

public class PostServiceDbAccessImpl extends ApplicationObjectSupport implements
		PostService {

	@Autowired
	PostDao postDao;

	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private GenericAclController<Post> aclController;

	private static final String SORT_ORDER = null;
	private static final Integer NUM_DAYS_LOOKBACK = null;

	/********************* Create related methods implementation ***********************/
	@Override
	public Long createPost(Post post, Group group, int ds) throws AppException {

		long postId = postDao.createPost(post, ds);
		post.setId(postId);
		aclController.createACL(post, ds);
		aclController.createAce(post, CustomPermission.READ);
		aclController.createAce(post, CustomPermission.WRITE);
		aclController.createAce(post, CustomPermission.DELETE);
		return postId;
	}

	// ******************** Read related methods implementation
	// **********************
	@Override
	public List<Post> getPosts(int numberOfPosts, Long startIndex)
			throws AppException {

		List<Post> posts = postDao.getPosts(numberOfPosts, startIndex);
		return getPostsFromEntities(posts);
	}

	@Override
	public List<Post> getPostsByGroup(int numberOfPosts, Long startIndex,
			Group group) throws AppException {

		// verify optional parameter numberDaysToLookBack first
		List<Post> groupPosts = postDao.getPosts(numberOfPosts, startIndex,
				group);
		return getPostsFromEntities(groupPosts);
	}

	// TODO: rework so that it can paginate.
	@Override
	public List<Post> getPostsByMyGroups(int numberOfPosts, Long startIndex)
			throws AppException {

		List<Group> myGroups = groupService.getGroupsByMembership(SORT_ORDER,
				NUM_DAYS_LOOKBACK);
		myGroups.addAll(groupService.getGroupsByManager(SORT_ORDER,
				NUM_DAYS_LOOKBACK));

		List<Post> postsByMyGroups = new ArrayList<Post>();
		for (int i = 0; i < myGroups.size(); i++) {
			postsByMyGroups.addAll(getPostsByGroup(numberOfPosts, startIndex,
					myGroups.get(i)));
		}

		return postsByMyGroups;
	}

	@Override
	public Post getPostById(Long id) throws AppException {
		Post postById = postDao.getPostById(id);
		if (postById == null) {
			throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
					404, "The post you requested with id " + id
							+ " was not found in the database",
					"Verify the existence of the post with the id " + id
							+ " in the database", AppConstants.DASH_POST_URL);
		}

		return postDao.getPostById(id);
	}

	private List<Post> getPostsFromEntities(List<Post> postEntities) {
		List<Post> response = new ArrayList<Post>();
		for (Post post : postEntities) {
			response.add(post);
		}

		return response;
	}

	@Override
	public int getNumberOfPosts() {
		int totalNumber = postDao.getNumberOfPosts();

		return totalNumber;

	}

	/********************* UPDATE-related methods implementation ***********************/

	@Override
	public void updateFullyPost(Post post, int ds) throws AppException {
		try {
			//verify whether post exists
			Post verifyPostExistenceById = getPostById(post.getId());

			copyAllProperties(verifyPostExistenceById, post);
			postDao.updatePost(verifyPostExistenceById, ds);
		} catch (AppException ex) {

			throw new AppException(
					Response.Status.NOT_FOUND.getStatusCode(),
					404,
					"The resource you are trying to update does not exist in the database",
					"Please verify existence of data in the database for the id - "
							+ post.getId(), AppConstants.DASH_POST_URL);
		}

	}

	private void copyAllProperties(Post verifyPostExistenceById, Post post) {

		BeanUtilsBean withNull = new BeanUtilsBean();
		try {
			withNull.copyProperty(verifyPostExistenceById, "content",
					post.getContent());
			withNull.copyProperty(verifyPostExistenceById, "image",
					post.getImage());
			withNull.copyProperty(verifyPostExistenceById, "task_link_id",
					post.getTask_link_id());

		} catch (IllegalAccessException e) {
			logger.debug("debugging info for exception: ", e);
		} catch (InvocationTargetException e) {
			logger.debug("debugging info for exception: ", e);
		}

	}

	/********************* DELETE-related methods implementation ***********************/

	@Override
	public void deletePost(Post post, int ds) {

		postDao.deletePostById(post, ds);
		aclController.deleteACL(post);

	}

	@Override
	@Transactional
	public void updatePartiallyPost(Post post) throws AppException {

			throw new AppException(
					Response.Status.NOT_FOUND.getStatusCode(),
					404,
					"The resource you are trying to update does not exist in the database",
					"Please verify existence of data in the database for the id - "
							+ post.getId(), AppConstants.DASH_POST_URL);
		}

	private void copyPartialProperties(Post verifyPostExistenceById, Post post) {

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
