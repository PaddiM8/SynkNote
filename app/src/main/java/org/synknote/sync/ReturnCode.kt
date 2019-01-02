package org.synknote.sync

enum class ReturnCode {
    Success,
    EmailExists,
    IncorrectPassword,
    UserNotFound,
    InvalidToken,
    NoteNotFound,
    PermissionDenied,
    InvalidInput,
    ClientError
}