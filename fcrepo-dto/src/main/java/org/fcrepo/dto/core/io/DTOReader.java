package org.fcrepo.dto.core.io;

import java.io.IOException;
import java.io.InputStream;

import org.fcrepo.dto.core.FedoraObject;

/**
 * Interface for reading a {@link FedoraObject} from a stream.
 */
public interface DTOReader {
    /**
     * Gets a new instance configured like this one.
     *
     * @return a new instance.
     */
    DTOReader getInstance();

    /**
     * Deserializes the given stream into a <code>FedoraObject</code>.
     *
     * @param source the stream to read from. It will be closed by the time
     *        this method exits, regardless of success.
     * @return a new <code>FedoraObject</code> based on the content of the
     *         stream.
     * @throws IOException if the stream cannot be deserialized for any reason.
     */
    FedoraObject readObject(InputStream source) throws IOException;

    /**
     * Releases all resources associated with this reader.
     * This can be safely called multiple times.
     */
    void close();
    
}
