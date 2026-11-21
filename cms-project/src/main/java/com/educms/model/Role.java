package com.educms.model;

/** User roles with increasing privilege. */
public enum Role {
    AUTHOR,   // can create/edit own content
    EDITOR,   // can edit/publish any content, manage media
    ADMIN     // full access including user management and reports
}
