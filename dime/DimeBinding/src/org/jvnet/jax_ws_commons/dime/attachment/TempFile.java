package org.jvnet.jax_ws_commons.dime.attachment;

import java.io.File;

/**
 * A file that deletes the data on disk during garbage collection or VM
 * shutdown.
 * 
 * @author Oliver Treichel
 */
class TempFile extends File {
    private static final long serialVersionUID = 1L;

    public TempFile(final File file) {
        super(file.getAbsolutePath());

        // just in case finalizer did not run until VM shutdown
        deleteOnExit();
    }

    @Override
    protected void finalize() throws Throwable {
        if (exists()) {
            delete();
        }

        super.finalize();
    }
}