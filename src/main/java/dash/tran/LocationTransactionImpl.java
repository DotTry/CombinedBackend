package dash.tran;

import org.springframework.transaction.annotation.Transactional;

import dash.errorhandling.AppException;
import dash.pojo.Location;
import dash.pojo.User;
import dash.service.LocationService;

public class LocationTransactionImpl {
	
	@Transactional("transactionManagerCHW")
	public Long createLocation1(Location location, String user_name, int ds, LocationService locationService) throws AppException {
		return locationService.createLocation(location, user_name, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public Long createLocation2(Location location, String user_name, int ds, LocationService locationService) throws AppException {
		return locationService.createLocation(location, user_name, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void addManager1(User user, Location location, int ds, LocationService locationService)throws AppException{
		locationService.addManager(user, location, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void addManager2(User user, Location location, int ds, LocationService locationService)throws AppException{
		locationService.addManager(user, location, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void resetManager1(User user, Location location, int ds, LocationService locationService)throws AppException{
		locationService.resetManager(user, location, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void resetManager2(User user, Location location, int ds, LocationService locationService)throws AppException{
		locationService.resetManager(user, location, ds);
	}
	
	@Transactional("transactionManagerCHW")
	public void deleteManager1(User user, Location location, int ds, LocationService locationService)throws AppException{
		locationService.deleteManager(user, location, ds);
	}
	
	@Transactional("transactionManagerVMA")
	public void deleteManager2(User user, Location location, int ds, LocationService locationService)throws AppException{
		locationService.deleteManager(user, location, ds);
	}
}
