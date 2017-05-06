package com.voc.jira.plugins.jira.util;

import java.net.InetSocketAddress;

import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachePool {
	private static final Logger log = LoggerFactory.getLogger(CachePool.class);
	private static final String LOG_PREFIX = "+++++++++++++ Memcached ++++++";
	private static final int POOL_SIZE = 20;
	private static int ACTUAL_POOL_SIZE;
	private static MemcachedClient[] m = null;
	private static CachePool instance = null;
	
	private static void err(Exception e) {
		e.printStackTrace();
		log.error(LOG_PREFIX + e.getMessage());
	}
	
	private CachePool(final String baseUrl, final String mport) {
		final String host = Url.getHost(baseUrl);
		final String mhost = getHost(baseUrl, host);
		System.out.println("[MEMCACHED INFO] CachePool() baseUrl == " + baseUrl + ", mhost == " + mhost);
		System.out.println("[MEMCACHED INFO] CachePool() port == " + mport);
		
		m = new MemcachedClient[POOL_SIZE];
		for (int j = 0; j < POOL_SIZE; j++) {
			m[j] = null;
		}
		ACTUAL_POOL_SIZE = 0;
		for (int i = 0; i < POOL_SIZE; i++) {
			try {
				m[i] = new MemcachedClient(new InetSocketAddress(mhost, Integer.parseInt(mport))); //port default 11211
				ACTUAL_POOL_SIZE++;
			} catch (SecurityException e) {
				err(e);
				break;
			} catch (Exception e) {
				err(e);
				break;
			}
		}
	}

	private static String getHost(final String baseUrl, final String host) {
		if(baseUrl.contains("roving.com")){
			return "jira".equalsIgnoreCase(host)
				? "10.20.97.188" // production jira
				: "10.20.97.189"; // everyone else
		} else {
			return host;
		}
	}

	public static synchronized CachePool getInstance(final String baseUrl, final String mport) {
		try {
			if (instance == null) {
				instance = new CachePool(baseUrl,mport);
			}
			return instance;
		} catch (Exception e) {
			err(e);
		}
		return null;
	}

	int next = 0;
	public synchronized MemcachedClient getClient() {
		MemcachedClient c = m[next];
		next++;
		if (next >= ACTUAL_POOL_SIZE) {
			next = 0;
		}
		return c;
	}
}
