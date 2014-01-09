package matrixstudio.ui;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.field.AbstractField;

public class StoreField extends AbstractField {

	private String url;

	private Browser browser;
	
	public StoreField() {
		super("Store", BasicsUI.NO_INFO);
	}

	@Override
	public boolean activate() {
		if ( browser == null ) return false;
		return browser.setFocus();
	}

	@Override
	public void setEnable(boolean enable) {
		super.setEnable(enable);
		if ( browser != null ) {
			browser.setEnabled(enable);
		}
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
		updateBrowserUrl();
	}
	
	@Override
	public void createWidget(Composite parent) {
		super.createWidget(parent);
		createBrowser(parent);
		createButtonBar(parent);
	}
	
	private void createBrowser(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, grabExcessVerticalSpace(), fieldHorizontalSpan(), 1));
		updateBrowserUrl();
	}

	@Override
	public boolean grabExcessVerticalSpace() {
		return true;
	}
	
	public void refresh() {
		if ( browser != null ) {
			browser.refresh();
		}
	}
	
	private void updateBrowserUrl() {
		if ( browser != null ) {
			if ( url != null ) {
				browser.setUrl(url);
			} else {
				browser.setText("<html><body><h1>No store selected.</h1></body></html>");
			}
		}
	}
	public static void trustAllHttpsCertificates() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
		        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
		        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
		    }
		};
	
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String arg0, SSLSession arg1) {return true; }
		});
	}

}
