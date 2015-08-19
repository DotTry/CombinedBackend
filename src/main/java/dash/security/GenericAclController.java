package dash.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("aclController")
public class GenericAclController<T> extends ApplicationObjectSupport {

	@Autowired
	private MutableAclService mutableAclService;

	@Autowired
	private MutableAclService mutableAclService2;
	
	/*
	 * Creates/Updates the ACL of object and sets the owner to recipient object:
	 * the object to create an ACL for (aka. registers the object) recipient:
	 * the Sid of the user you wish to give ownership of the object (use new
	 * PrincipleSid(String username) to generate this)
	 */
	public boolean createACL(T object, Sid recipient, int ds) {
		MutableAcl acl;
		ObjectIdentity oid;

		try{
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		
		switch(ds){
		case 1:
		{
			try {
				acl = (MutableAcl) mutableAclService.readAclById(oid);
			} catch (NotFoundException nfe) {
				acl = mutableAclService.createAcl(oid);
			}
			acl.setOwner(new PrincipalSid("Root"));
			mutableAclService.updateAcl(acl);
		}
		case 2:{
			try {
				acl = (MutableAcl) mutableAclService2.readAclById(oid);
			} catch (NotFoundException nfe) {
				acl = mutableAclService2.createAcl(oid);
			}
			acl.setOwner(new PrincipalSid("Root"));
			mutableAclService.updateAcl(acl);
		}
		}

		logger.debug("Added Acl for Sid " + recipient + " contact " + object);
		return true;
	}

	/*
	 * Creates new Acl with Root as owner
	 * 
	 * Params object- the object to generate an ACL for
	 */

	public boolean createACL(T object, int ds) {
		MutableAcl acl;
		ObjectIdentity oid;

		try {
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		
		switch(ds){
			case 1:
			{
				try {
					acl = (MutableAcl) mutableAclService.readAclById(oid);
				} catch (NotFoundException nfe) {
					acl = mutableAclService.createAcl(oid);
				}

				acl.setOwner(new PrincipalSid("Root"));
				mutableAclService.updateAcl(acl);
			}
			case 2:
			{
				try {
					acl = (MutableAcl) mutableAclService2.readAclById(oid);
				} catch (NotFoundException nfe) {
					acl = mutableAclService2.createAcl(oid);
				}

				acl.setOwner(new PrincipalSid("Root"));
				mutableAclService2.updateAcl(acl);
			}
		}

		logger.debug("Added Acl for Sid " + getUsername() + " object "
				+ object);
		return true;
	}
	
	/*
	 * create ace will generate new permission entries in the acl_entries table
	 * 
	 * This method's parameters are as follows: object: the object that you
	 * would like to create new permissions for, must implement IAclObject
	 * permission: the permission you would like to grant recipient: the Sid of
	 * the user that will be granted permissions
	 */
	public boolean createAce(T object, Permission permission, Sid recipient) {
		int ds = 1;
		MutableAcl acl;
		ObjectIdentity oid;

		try {
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			if(ds == 1){
				acl = (MutableAcl) mutableAclService.readAclById(oid);
			}
			else if(ds == 2){
				acl = (MutableAcl) mutableAclService2.readAclById(oid);
			}
			else
				acl = null;
		} catch (NotFoundException nfe) {
			nfe.printStackTrace();
			return false;
		}
		
		List<AccessControlEntry> entries = acl.getEntries();
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getSid().equals(recipient)
					&& entries.get(i).getPermission().equals(permission)) {
				acl.deleteAce(i);
			}
		}

		acl.insertAce(acl.getEntries().size(), permission, recipient,
				true);
		mutableAclService.updateAcl(acl);
		mutableAclService2.updateAcl(acl);
		return true;
	}

	// Same as method above but gives the current "logged in" user the
	// permission.
	public boolean createAce(T object, Permission permission) {
		int ds = 1;
		MutableAcl acl;
		ObjectIdentity oid;

		try {
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		try {
			if(ds == 1){
				acl = (MutableAcl) mutableAclService.readAclById(oid);
			}
			else if(ds == 2){
				acl = (MutableAcl) mutableAclService2.readAclById(oid);
			}
			else
				acl = null;
		} catch (NotFoundException nfe) {
			nfe.printStackTrace();
			return false;
		}

		acl.insertAce(acl.getEntries().size(), permission, new PrincipalSid(
				getUsername()),
				true);
		mutableAclService.updateAcl(acl);
		mutableAclService2.updateAcl(acl);
		return true;
	}
	
	public boolean setOwner(T object, Sid recipient){
		int ds = 1;
		MutableAcl acl;
		ObjectIdentity oid;

		try {
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		try {
			if(ds == 1){
				acl = (MutableAcl) mutableAclService.readAclById(oid);
			}
			else if(ds == 2){
				acl = (MutableAcl) mutableAclService2.readAclById(oid);
			}
			else
				acl = null;
			
		} catch (NotFoundException nfe) {
			nfe.printStackTrace();
			return false;
		}
		
		acl.setOwner(recipient);
		
		return true;
	}
	
	public boolean setOwner(T object){
		int ds = 1;
		MutableAcl acl;
		ObjectIdentity oid;

		try {
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		try {
			if(ds == 1){
				acl = (MutableAcl) mutableAclService.readAclById(oid);
			}
			else if(ds == 2){
				acl = (MutableAcl) mutableAclService2.readAclById(oid);
			}
			else
				acl = null;
			
		} catch (NotFoundException nfe) {
			nfe.printStackTrace();
			return false;
		}
		
		acl.setOwner(new PrincipalSid(getUsername()));
		
		return true;
	}

	public boolean deleteACL(T object) {
		int ds = 1;
		try {
			ObjectIdentity oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
			if(ds == 1){
				mutableAclService.deleteAcl(oid, false);
			}
			else if(ds == 2){
				mutableAclService2.deleteAcl(oid, false);
			}
			
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	/*
	 * Deletes an existing all matching entries in ACL_Entry effectively
	 * removing single permission from a user
	 * 
	 * Params object= the object to which the permission allows access
	 * permission= the permission to be removed recipient= the Sid of the user
	 * whose permission you are revoking
	 */
	public boolean deleteACE(T object, Permission permission, Sid recipient) {
		int ds = 1;
		MutableAcl acl;
		ObjectIdentity oid;

		try {
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		try {
			if(ds == 1){
				acl = (MutableAcl) mutableAclService.readAclById(oid);
			}
			else if(ds == 2){
				acl = (MutableAcl) mutableAclService2.readAclById(oid);
			}
			else
				acl = null;
			
		} catch (NotFoundException nfe) {
			nfe.printStackTrace();
			return false;
		}

		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getSid().equals(recipient)
					&& entries.get(i).getPermission().equals(permission)) {
				acl.deleteAce(i);
			}
		}

		mutableAclService.updateAcl(acl);
		mutableAclService2.updateAcl(acl);

		if (logger.isDebugEnabled()) {
			logger.debug("Deleted " + object.getClass()
					+ ((IAclObject) object).getId()
					+ " ACL permissions for recipient " + recipient);
		}
		
		return true;

	}
	
	//Deletes all aces of object for the permission given.
	public boolean clearPermission(T object, Permission permission) {
		int ds = 1;
		MutableAcl acl;
		ObjectIdentity oid;

		try {
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		try {
			if(ds == 1){
				acl = (MutableAcl) mutableAclService.readAclById(oid);
			}
			else if(ds == 1){
				acl = (MutableAcl) mutableAclService2.readAclById(oid);
			}
			else
				acl = null;
			
		} catch (NotFoundException nfe) {
			nfe.printStackTrace();
			return false;
		}

		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getPermission().equals(permission)) {
				acl.deleteAce(i);
			}
		}

		mutableAclService.updateAcl(acl);
		mutableAclService2.updateAcl(acl);
		if (logger.isDebugEnabled()) {
			logger.debug("Deleted " + object.getClass()
					+ ((IAclObject) object).getId()
					+ " ACL permissions for all instances of "+permission.toString());
		}

		return true;

	}
	
	public boolean hasPermission(T object, Permission permission, Sid recipient)
	{
		int ds = 1;
		MutableAcl acl;
		ObjectIdentity oid;

		try {
			oid = new ObjectIdentityImpl(object.getClass(),
					((IAclObject) object).getId());
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		try {
			if(ds == 1){
				acl = (MutableAcl) mutableAclService.readAclById(oid);
			}
			else if(ds == 1){
				acl = (MutableAcl) mutableAclService2.readAclById(oid);
			}
			else
				acl = null;
			
		} catch (NotFoundException nfe) {
			nfe.printStackTrace();
			return false;
		}
		try{
		acl.isGranted(Arrays.asList(permission) , Arrays.asList(recipient), false);
		}catch (Exception e){return false;}
		return true;

	}

	public MutableAcl getAcl(T object){
		int ds = 1;
		MutableAcl acl;
		ObjectIdentity oid;

	
		oid = new ObjectIdentityImpl(object.getClass(),
				((IAclObject) object).getId());
		if(ds == 1){
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		else if(ds == 2){
			acl = (MutableAcl) mutableAclService2.readAclById(oid);
		}
		else
			acl = null;
		
		
		return acl;
		
	}
	
	// Gets the username of the current "logged in" user
	protected String getUsername() {
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();

		if (auth.getPrincipal() instanceof UserDetails) {
			return ((UserDetails) auth.getPrincipal()).getUsername();
		} else {
			return auth.getPrincipal().toString();
		}
	}
	
	

}
