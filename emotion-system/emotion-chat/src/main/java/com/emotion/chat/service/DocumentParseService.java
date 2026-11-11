package com.emotion.chat.service;

import com.emotion.chat.dto.response.AiParseResultResponse;

public interface DocumentParseService {

    String extractText(byte[] fileBytes, String fileName);

    Long saveParsedResult(AiParseResultResponse parsed);
}
