package com.emotion.chat.service;

import com.emotion.chat.dto.response.AiParseResultResponse;

public interface AiExtractService {

    AiParseResultResponse extractFromText(String text);
}
