package gapi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;

public class TimeoutProxy implements InvocationHandler {

	private static final int MAX_ITERATION_LEVEL = 4;

	private Object obj;

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(T obj) {
		return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new TimeoutProxy(obj));
	}

	private TimeoutProxy(Object obj) {
		this.obj = obj;
	}

	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		int iterationCounter = 0;
		while (true) {
			Object result;
			try {
				result = m.invoke(obj, args);
			} catch (InvocationTargetException e) {
				Throwable actualException = e.getTargetException();
				if (actualException instanceof RuntimeException) {
					Throwable cause = ((RuntimeException) actualException).getCause();
					if (cause instanceof SocketTimeoutException) {
						assertDoesntExceedMax(iterationCounter, cause);
						iterationCounter++;
						continue;
					}
				}
				throw actualException;
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RuntimeException("Unexpected invocation exception", e);
			}
			return result;
		}
	}

	private void assertDoesntExceedMax(int iterationCounter, Throwable cause) {
		if (iterationCounter > MAX_ITERATION_LEVEL) {
			throw new RuntimeException("Max of " + MAX_ITERATION_LEVEL + " iterations exceeded, couldn't connect to Spreadsheets.", cause);
		}
	}
}
