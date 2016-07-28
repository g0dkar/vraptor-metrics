package g0dkar.com.vraptor;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;

import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.controller.ControllerMethod;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.interceptor.Interceptor;

/**
 * Intercepts all requests and measure the request time and size, exceptions (if thrown), controller time, method execution time and in the future: response sizes.
 * @author Rafael Lins
 *
 */
@Intercepts
@RequestScoped
public class MetricsInterceptor implements Interceptor {
	private final Metrics metrics;
	private final HttpServletRequest request;
//	private final HttpServletResponse response;
	
	/** @deprecated CDI */ @Deprecated
	MetricsInterceptor() { this(null, null/*, null*/); }
	
	@Inject
	public MetricsInterceptor(final Metrics metrics, final HttpServletRequest request/*, final HttpServletResponse response*/) {
		this.metrics = metrics;
		this.request = request;
//		this.response = response;
	}
	
	public void intercept(final InterceptorStack stack, final ControllerMethod method, final Object controllerInstance) throws InterceptionException {
		final Context requestTime = metrics.markRequestStart();
		final Context controllerTime = controllerInstance != null ? metrics.metrics().timer(MetricRegistry.name(controllerInstance.getClass())).time() : null;
		final Context methodTime = method != null ? metrics.metrics().timer(MetricRegistry.name(controllerInstance.getClass(), method.getMethod().getName(), String.valueOf(method.getMethod().getParameterTypes().length))).time() : null;
		metrics.markRequestSize(request.getContentLengthLong());
		
		try {
			stack.next(method, controllerInstance);
//			metrics.markResponseSize(response.getOutputStream());
		} catch (final Throwable t) {
			metrics.markException(t);
//			metrics.markResponseSize(-1);
			throw t;
		} finally {
			requestTime.stop();
			if (controllerTime != null) { controllerTime.stop(); }
			if (methodTime != null) { methodTime.stop(); }
		}
	}
	
	public boolean accepts(final ControllerMethod method) {
		return true;
	}
}
