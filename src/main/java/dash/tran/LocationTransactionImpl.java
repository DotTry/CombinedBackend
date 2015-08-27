package dash.tran;

import org.springframework.transaction.annotation.Transactional;

import dash.errorhandling.AppException;
import dash.pojo.Location;
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
}
