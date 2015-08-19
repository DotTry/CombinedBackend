package dash.filters;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
    Allow CORS requests.
 */
@Singleton
public class ResponseCorsFilter implements Filter {

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException { }

	@Override
	public void destroy() { }

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
		if (servletResponse instanceof HttpServletResponse) {
	        
			final HttpServletResponse alteredResponse = ((HttpServletResponse) servletResponse);
			
			// Get client's origin
			HttpServletRequest origRequest = (HttpServletRequest) servletRequest;
			
			//try to match origin with list
			try{
				String matchedOrigin = matchOrigin(origRequest.getHeader("origin"));
				addHeadersFor200Response(alteredResponse, matchedOrigin);
			}
			catch(NullPointerException e){
				addHeadersFor200Response(alteredResponse, "*");
			}
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}
	
	/**
	 * Trying to match the incoming request with a list of domain names. If the origin of the request is in the list it 
	 * will return the same origin, otherwise the return will be null.
	 * 
	 * @param origin The request origin.
	 * @return
	 */
	private String matchOrigin(String origin) {
		
		//list of origins to match
		if (!origin.matches("http://www.housuggest.org|http://housuggest.org|http://localhost:8100"))
			return null;
		
	  return origin;
	}
	
	private void addHeadersFor200Response(final HttpServletResponse response, String origin) {
		response.addHeader("Access-Control-Allow-Origin", origin);
		
		response.addHeader("Access-Control-Allow-Methods",
				"GET, POST, PUT, OPTIONS, DELETE");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader(
				"Access-Control-Allow-Headers",
				"Cache-Control, Pragma, Origin, Authorization, Content-Type, X-Requested-With, X-XSRF-TOKEN");
		
		//Exposes all headers so that Restangular client may retrieve data from it.
		//response.addHeader("Access-Control-Expose-Headers", "ObjectId");
	}
}
