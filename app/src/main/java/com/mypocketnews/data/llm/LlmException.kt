package com.mypocketnews.data.llm

open class LlmException(message: String) : Exception(message)

class LlmAuthException(message: String) : LlmException(message)

class LlmRateLimitException(message: String) : LlmException(message)
