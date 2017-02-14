package com.commercetools.sunrise.cms;

/**
 * An unchecked exception signalling that there was a problem when obtaining the content from the CMS.
 * <p>
 * It could be caused e.g. by wrong credentials, non-unique content requested etc.
 */
public class CmsServiceException extends RuntimeException {

    public CmsServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CmsServiceException(final Throwable cause) {
        super(cause);
    }

    public CmsServiceException(final String message) {
        super(message);
    }
}
