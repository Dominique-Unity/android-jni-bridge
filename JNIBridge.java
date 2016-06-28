package bitter.jnibridge;

import java.lang.reflect.*;

public class JNIBridge
{
	static native Object invoke(long ptr, Class clazz, Method method, Object[] args);
	static native void   delete(long ptr);

	static Object newInterfaceProxy(final long ptr, final Class[] interfaces)
	{
		return Proxy.newProxyInstance(JNIBridge.class.getClassLoader(), interfaces, new InterfaceProxy(ptr));
	}

	static void disableInterfaceProxy(final Object proxy)
	{
		((InterfaceProxy) Proxy.getInvocationHandler(proxy)).disable();
	}

	private static class InterfaceProxy implements InvocationHandler
	{
		private Object m_InvocationLock = new Object[0];
		private long m_Ptr;

		public InterfaceProxy(final long ptr) { m_Ptr = ptr; }

		public Object invoke(Object proxy, Method method, Object[] args)
		{
			synchronized (m_InvocationLock)
			{
				if (m_Ptr == 0)
					return null;
				android.util.Log.i("JNIBridge", "ProxyInvoke " + method.getName());
				return JNIBridge.invoke(m_Ptr, method.getDeclaringClass(), method, args);
			}
		}

		public void finalize()
		{
			synchronized (m_InvocationLock)
			{
				if (m_Ptr == 0)
					return;
				JNIBridge.delete(m_Ptr);
			}
		}

		public void disable()
		{
			synchronized (m_InvocationLock)
			{
				m_Ptr = 0;
			}
		}
	}
}