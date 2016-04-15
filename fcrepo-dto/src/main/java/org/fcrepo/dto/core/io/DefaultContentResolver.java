
package org.fcrepo.dto.core.io;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * A {@link ContentResolver} that resolves <code>http[s]</code> and local <code>file</code> URIs.
 * <p>
 * <b>NOTE:</b> This resolver only works with relative URIs when a base URI is also provided.
 */
public class DefaultContentResolver implements ContentResolver {

    private static final int FILE = 1;

    private static final int HTTP = 2;

    private final CloseableHttpClient defaultHttpClient = HttpClientBuilder.create().build();

    private HttpClient httpClient;

    /**
     * Creates an instance that uses a new, single-threaded HTTP client.
     */
    public DefaultContentResolver() {
        httpClient = defaultHttpClient;
    }

    /**
     * Sets the HTTP client. If the new client is different from the default, and the default is still in use, it will
     * be automatically closed.
     *
     * @param httpClient the new value, never <code>null</code>.
     */
    public void setHttpClient(final HttpClient httpClient) {
        if (httpClient != defaultHttpClient && this.httpClient == defaultHttpClient) {
            try {
                defaultHttpClient.close();
            } catch (final IOException e) {
                throw new IOError(e);
            }
        }
        this.httpClient = httpClient;
    }

    @Override
    @PreDestroy
    public void close() {
        if (this.httpClient == defaultHttpClient) {
            try {
                defaultHttpClient.close();
            } catch (final IOException e) {
                throw new IOError(e);
            }
        }
    }

    @Override
    public InputStream resolveContent(final URI base, final URI ref) throws IOException {
        final URI absoluteRef = getAbsolute(base, ref);
        switch (getSchemeType(absoluteRef)) {
            case FILE:
                return new FileInputStream(absoluteRef.getSchemeSpecificPart());
            case HTTP:
                return httpClient.execute(new HttpGet(absoluteRef)).getEntity().getContent();
            default:
                return null;
        }
    }

    @Override
    public void resolveContent(final URI base, final URI ref, final OutputStream sink) throws IOException {
        try (InputStream content = resolveContent(base, ref)) {
            IOUtils.copy(content, sink);
        }
    }

    private static URI getAbsolute(final URI base, final URI ref) throws IOException {
        if (base != null) {
            if (!base.isAbsolute()) { throw new IllegalArgumentException("Base URI must be absolute"); }
            return base.resolve(ref);
        } else if (ref.isAbsolute()) {
            return ref;
        } else {
            throw new IOException("URI is not absolute and base " + "URI not specified -- cannot resolve " + ref);
        }
    }

    private static int getSchemeType(final URI ref) throws IOException {
        final String scheme = ref.getScheme();
        if (scheme.equals("file")) {
            return FILE;
        } else if (scheme.equals("http") || scheme.equals("https")) {
            return HTTP;
        } else {
            throw new IOException("Unsupported URI scheme: " + scheme + " -- " + "cannot resolve " + ref);
        }
    }

}
