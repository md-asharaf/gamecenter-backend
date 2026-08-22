package com.adnibog.gamecenter.service.parser;

import java.io.InputStream;
import java.util.List;

import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.Question;

public interface QuestionParser {
  boolean supports(String filename);

  List<Question> parse(InputStream inputStream, Project project) throws Exception;
}
