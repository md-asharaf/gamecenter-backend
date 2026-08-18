package com.vocabkicker.serverless.service.parser;

import org.springframework.stereotype.Component;

@Component
public class QuestionParserFactory {

    private final CsvQuestionParser csvParser;
    private final DocxQuestionParser docxParser;

    public QuestionParserFactory(CsvQuestionParser csvParser, DocxQuestionParser docxParser) {
        this.csvParser = csvParser;
        this.docxParser = docxParser;
    }

    public QuestionParser getParser(String filename) {
        if (filename == null) {
            return null;
        }
        
        String lowerCaseFilename = filename.toLowerCase();
        if (lowerCaseFilename.endsWith(".csv")) {
            return csvParser;
        } else if (lowerCaseFilename.endsWith(".docx")) {
            return docxParser;
        }
        
        return null;
    }
}
